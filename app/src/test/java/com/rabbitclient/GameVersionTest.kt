package com.rabbitclient

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameVersionTest {
    @Test fun parsesRequestedMinimum() {
        assertEquals(GameVersion(listOf(1, 21, 90)), GameVersion.parse("1.21.90"))
    }

    @Test fun comparesPatchVersionsNumerically() {
        assertTrue(GameVersion.parse("1.21.100")!! > GameVersion.parse("1.21.90")!!)
    }

    @Test fun acceptsYearBasedVersionSyntaxButDoesNotClaimVerifiedSupport() {
        assertEquals("Detected — runtime not verified", GameVersion.targetRangeStatus("26.0"))
    }

    @Test fun unknownAndMalformedVersionsDoNotClaimSupport() {
        assertNull(GameVersion.parse("not-a-version"))
        assertEquals("Unknown version", GameVersion.targetRangeStatus("unknown"))
    }

    @Test fun rejectsVersionsBelowTarget() {
        assertEquals("Below target version", GameVersion.targetRangeStatus("1.21.80"))
    }
}
