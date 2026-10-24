import http from "node:http";

const PORT = Number(process.env.PORT || 8787);
const API_KEY = (process.env.CURSEFORGE_API_KEY || "").trim();
const API_ROOT = "https://api.curseforge.com/v1";
const MAX_REQUESTS_PER_MINUTE = 90;
const requestCounts = new Map();
const categoryCache = new Map();

class ApiError extends Error {
  constructor(statusCode, message) {
    super(message);
    this.statusCode = statusCode;
  }
}

function sendJson(response, statusCode, body) {
  response.writeHead(statusCode, {
    "content-type": "application/json; charset=utf-8",
    "cache-control": "no-store",
    "x-content-type-options": "nosniff"
  });
  response.end(JSON.stringify(body));
}

function isRateLimited(request) {
  const forwarded = request.headers["x-forwarded-for"];
  const address = String(forwarded || request.socket.remoteAddress || "unknown").split(",")[0].trim();
  const now = Date.now();
  const state = requestCounts.get(address);
  if (!state || now >= state.resetAt) {
    requestCounts.set(address, { count: 1, resetAt: now + 60_000 });
    return false;
  }
  state.count += 1;
  return state.count > MAX_REQUESTS_PER_MINUTE;
}

async function curseForgeGet(path) {
  if (!API_KEY) {
    throw new ApiError(503, "CurseForge API is not configured. Set CURSEFORGE_API_KEY in the backend host's secret settings.");
  }
  let response;
  try {
    response = await fetch(API_ROOT + path, {
      method: "GET",
      headers: {
        accept: "application/json",
        "x-api-key": API_KEY,
        "user-agent": "RabbitClient-CurseForge-Proxy/0.1"
      },
      signal: AbortSignal.timeout(20_000)
    });
  } catch {
    throw new ApiError(502, "Could not reach the official CurseForge API.");
  }
  const raw = await response.text();
  let parsed;
  try {
    parsed = JSON.parse(raw);
  } catch {
    parsed = {};
  }
  if (!response.ok) {
    const message = response.status === 403
      ? "CurseForge rejected the API key. Verify that it is approved for this application."
      : response.status === 429
        ? "CurseForge is rate-limiting requests. Please retry shortly."
        : "Official CurseForge API returned HTTP " + response.status + ".";
    throw new ApiError(response.status === 429 ? 429 : 502, message);
  }
  return parsed;
}

function normalizeCategory(value) {
  const name = String(value || "Add-ons").trim().toLowerCase();
  if (name === "maps") return "maps";
  if (name === "texture packs") return "texturepacks";
  if (name === "scripts") return "scripts";
  if (name === "skins") return "skins";
  return "addons";
}

function categoryMatches(item, category) {
  const value = (String(item.name || "") + " " + String(item.slug || "")).toLowerCase().replace(/[^a-z0-9]/g, "");
  const wanted = normalizeCategory(category);
  if (wanted === "addons") return value.includes("addon") || value.includes("behaviorpack");
  if (wanted === "maps") return value.includes("map") || value.includes("world");
  if (wanted === "texturepacks") return value.includes("texturepack") || value.includes("resourcepack");
  if (wanted === "scripts") return value.includes("script");
  if (wanted === "skins") return value.includes("skin");
  return false;
}

async function resolveBedrockClass(category) {
  const cacheKey = normalizeCategory(category);
  const cached = categoryCache.get(cacheKey);
  if (cached && Date.now() < cached.expiresAt) return cached.value;

  const gameResponse = await curseForgeGet("/games?searchFilter=minecraft&index=0&pageSize=50");
  const games = Array.isArray(gameResponse.data) ? gameResponse.data : [];
  const bedrockGame = games.find((game) =>
    /bedrock/i.test(String(game.name || "") + " " + String(game.slug || ""))
  );
  const gameId = bedrockGame ? Number(bedrockGame.id) : 432;
  const categoriesResponse = await curseForgeGet("/categories?gameId=" + gameId);
  const categories = Array.isArray(categoriesResponse.data) ? categoriesResponse.data : [];
  const classes = categories.filter((item) => item.isClass === true);

  const bedrockClass = classes.find((item) =>
    /bedrock/i.test(String(item.name || "") + " " + String(item.slug || ""))
  );

  let result = null;
  if (bedrockClass) {
    const children = categories.filter((item) =>
      Number(item.parentCategoryId) === Number(bedrockClass.id) ||
      Number(item.rootGameCategoryId) === Number(bedrockClass.id)
    );
    const selectedChild = children.find((item) => categoryMatches(item, category));
    result = {
      gameId,
      classId: Number(bedrockClass.id),
      categoryId: selectedChild ? Number(selectedChild.id) : undefined
    };
  } else if (bedrockGame) {
    const selectedClass = classes.find((item) => categoryMatches(item, category));
    if (selectedClass) result = { gameId, classId: Number(selectedClass.id) };
  }

  if (!result) {
    throw new ApiError(
      502,
      "The official API did not expose a verifiable Bedrock category for this query. Rabbit Client will not silently show Java-only content."
    );
  }

  categoryCache.set(cacheKey, { value: result, expiresAt: Date.now() + 10 * 60_000 });
  return result;
}

async function searchProjects(url) {
  const query = (url.searchParams.get("query") || "").trim().slice(0, 100);
  const category = url.searchParams.get("category") || "Add-ons";
  const sort = url.searchParams.get("sort") === "Latest" ? "3" : "2";
  const target = await resolveBedrockClass(category);
  const params = new URLSearchParams({
    gameId: String(target.gameId),
    classId: String(target.classId),
    sortField: sort,
    sortOrder: "desc",
    index: "0",
    pageSize: "30"
  });
  if (target.categoryId) params.set("categoryId", String(target.categoryId));
  if (query) params.set("searchFilter", query);

  const result = await curseForgeGet("/mods/search?" + params.toString());
  const mods = Array.isArray(result.data) ? result.data : [];
  const route = normalizeCategory(category) === "maps" ? "maps"
    : normalizeCategory(category) === "texturepacks" ? "texture-packs"
      : normalizeCategory(category) === "scripts" ? "scripts"
        : normalizeCategory(category) === "skins" ? "skins" : "addons";

  return mods.map((mod) => {
    const officialUrl = String(mod.links?.websiteUrl || "");
    const safeUrl = officialUrl.startsWith("https://www.curseforge.com/")
      ? officialUrl
      : "https://www.curseforge.com/minecraft-bedrock/" + route + "/" + String(mod.slug || mod.id);
    return {
      id: Number(mod.id),
      name: String(mod.name || "Unnamed project"),
      slug: String(mod.slug || mod.id),
      author: String(mod.authors?.[0]?.name || "CurseForge creator"),
      summary: String(mod.summary || "").slice(0, 500),
      downloads: Number(mod.downloadCount || 0),
      imageUrl: String(mod.logo?.url || ""),
      projectUrl: safeUrl,
      fileName: "",
      downloadUrl: null
    };
  }).filter((project) => Number.isInteger(project.id) && project.id > 0 && project.name);
}

function isSupportedPackFile(fileName) {
  return /\.(mcpack|mcaddon|mcworld|zip)$/i.test(String(fileName || ""));
}

async function resolveDownload(projectId) {
  if (!Number.isInteger(projectId) || projectId <= 0) {
    throw new ApiError(400, "Invalid project ID.");
  }
  const fileResponse = await curseForgeGet("/mods/" + projectId + "/files?index=0&pageSize=50");
  const files = Array.isArray(fileResponse.data) ? fileResponse.data : [];
  const candidates = files
    .filter((file) => isSupportedPackFile(file.fileName))
    .sort((left, right) => Date.parse(right.fileDate || 0) - Date.parse(left.fileDate || 0));

  const file = candidates[0];
  if (!file) {
    throw new ApiError(404, "No downloadable .mcpack, .mcaddon, .mcworld, or .zip file was found for this project.");
  }

  const linkResponse = await curseForgeGet("/mods/" + projectId + "/files/" + Number(file.id) + "/download-url");
  const raw = linkResponse.data;
  const downloadUrl = typeof raw === "string" ? raw : String(raw?.url || "");
  if (!downloadUrl.startsWith("https://")) {
    throw new ApiError(404, "CurseForge does not provide a direct download URL for this file. Open the creator's project page instead.");
  }
  return {
    url: downloadUrl,
    fileName: String(file.fileName || "rabbitclient-pack.mcpack")
  };
}

const server = http.createServer(async (request, response) => {
  if (request.method === "OPTIONS") {
    response.writeHead(204, { "access-control-allow-methods": "GET, OPTIONS", "access-control-allow-headers": "content-type" });
    response.end();
    return;
  }
  if (request.method !== "GET") {
    sendJson(response, 405, { error: "Only GET requests are supported." });
    return;
  }
  if (isRateLimited(request)) {
    sendJson(response, 429, { error: "Too many requests. Wait a minute and retry." });
    return;
  }
  const url = new URL(request.url || "/", "http://localhost");
  try {
    if (url.pathname === "/health") {
      sendJson(response, 200, { ok: true, service: "rabbitclient-curseforge-api" });
      return;
    }
    if (url.pathname === "/api/curseforge/search") {
      sendJson(response, 200, { projects: await searchProjects(url) });
      return;
    }
    const downloadMatch = url.pathname.match(/^\/api\/curseforge\/download\/([0-9]+)$/);
    if (downloadMatch) {
      sendJson(response, 200, await resolveDownload(Number(downloadMatch[1])));
      return;
    }
    sendJson(response, 404, { error: "Route not found." });
  } catch (error) {
    const statusCode = error instanceof ApiError ? error.statusCode : 502;
    sendJson(response, statusCode, {
      error: error instanceof Error ? error.message : "Unexpected CurseForge proxy error."
    });
  }
});

server.listen(PORT, "0.0.0.0", () => {
  console.log("Rabbit Client CurseForge API listening on port " + PORT);
});
