package dev.nixoly.fireworkblockinteractfix;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateAnnouncerTest {

    private static UpdateChecker.LatestRelease release(String tag) {
        return new UpdateChecker.LatestRelease(tag, "https://example.com/release");
    }

    @Test
    void shouldNotNotifyWhenCurrentMatchesLatest() {
        assertFalse(UpdateAnnouncer.shouldNotify("1.0.6", release("1.0.6")));
    }

    @Test
    void shouldNotNotifyWhenVersionsDifferOnlyByLeadingV() {
        assertFalse(UpdateAnnouncer.shouldNotify("v1.0.6", release("1.0.6")));
        assertFalse(UpdateAnnouncer.shouldNotify("1.0.6", release("V1.0.6")));
    }

    @Test
    void shouldNotNotifyWhenVersionsDifferOnlyByWhitespace() {
        assertFalse(UpdateAnnouncer.shouldNotify(" 1.0.6 ", release("1.0.6")));
        assertFalse(UpdateAnnouncer.shouldNotify("1.0.6", release(" 1.0.6 ")));
    }

    @Test
    void shouldNotifyWhenCurrentIsOlder() {
        assertTrue(UpdateAnnouncer.shouldNotify("1.0.5", release("1.0.6")));
    }

    @Test
    void shouldNotNotifyWhenCurrentIsNewer() {
        assertFalse(UpdateAnnouncer.shouldNotify("1.0.7", release("1.0.6")));
    }

    @Test
    void shouldNotNotifyWhenCurrentPreReleaseHasNewerCoreVersion() {
        assertFalse(UpdateAnnouncer.shouldNotify("1.0.7-beta.1", release("1.0.6")));
    }

    @Test
    void shouldNotifyWhenStableReleaseReplacesSameVersionBeta() {
        assertTrue(UpdateAnnouncer.shouldNotify("1.0.7-beta.1", release("1.0.7")));
    }

    @Test
    void shouldCompareMultiDigitVersionPartsNumerically() {
        assertTrue(UpdateAnnouncer.shouldNotify("1.0.9", release("1.0.10")));
        assertFalse(UpdateAnnouncer.shouldNotify("1.0.10", release("1.0.9")));
    }

    @Test
    void shouldNotNotifyWhenVersionCannotBeComparedSafely() {
        assertFalse(UpdateAnnouncer.shouldNotify("beta-build", release("1.0.7")));
        assertFalse(UpdateAnnouncer.shouldNotify("1.0.7", release("latest")));
    }

    @Test
    void shouldNotNotifyForNullOrBlankCurrentVersion() {
        assertFalse(UpdateAnnouncer.shouldNotify(null, release("1.0.6")));
        assertFalse(UpdateAnnouncer.shouldNotify("", release("1.0.6")));
        assertFalse(UpdateAnnouncer.shouldNotify("   ", release("1.0.6")));
    }

    @Test
    void shouldNotNotifyForNullReleaseOrTag() {
        assertFalse(UpdateAnnouncer.shouldNotify("1.0.6", null));
        assertFalse(UpdateAnnouncer.shouldNotify("1.0.6", release(null)));
    }

    @Test
    void shouldNotNotifyForDevBuilds() {
        assertFalse(UpdateAnnouncer.shouldNotify("1.0.6-dev", release("1.0.6")));
        assertFalse(UpdateAnnouncer.shouldNotify("1.0.6-DEV-SNAPSHOT", release("1.0.6")));
    }

    @Test
    void shouldNotNotifyForUnexpandedPluginYmlPlaceholder() {
        assertFalse(UpdateAnnouncer.shouldNotify("${version}", release("1.0.6")));
    }

    @ParameterizedTest
    @CsvSource({
            "1.0.6, 1.0.6",
            "' v1.0.6 ', 1.0.6",
            "V1.0.6, 1.0.6"
    })
    void normalizeVersionStripsWhitespaceAndLeadingV(String raw, String expected) {
        assertEquals(expected, UpdateAnnouncer.normalizeVersion(raw));
    }

    @Test
    void github106ReleaseJarScenarioExplainsFalsePositive() {
        assertTrue(UpdateAnnouncer.shouldNotify("1.0.5", release("1.0.6")));
    }
}
