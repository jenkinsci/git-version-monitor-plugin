package io.jenkins.plugins.gitversionmonitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GitVersionTest {

    @Test
    void shouldParseVersions() {
        final String windows = "git version 2.50.0.windows.1";
        final String linux = "git version 2.50.0";
        final String mac = "git version 2.50.0 (Apple Git-137.1)";
        assertEquals(GitVersion.parse(windows), GitVersion.of(2, 50, 0, 1));
        assertEquals(GitVersion.parse(linux), GitVersion.of(2, 50, 0, 0));
        assertEquals(GitVersion.parse(mac), GitVersion.of(2, 50, 0, 0));
        assertEquals(GitVersion.parse("2.50.0.1"), GitVersion.of(2, 50, 0, 1));
        assertEquals(GitVersion.parse("2.50.0"), GitVersion.of(2, 50, 0, 0));
        assertEquals(GitVersion.parse("2.50"), GitVersion.of(2, 50, 0, 0));
    }

    @Test
    void shouldDisplayVersion() {
        final String windows = "git version 2.50.0.windows.1";
        final String linux = "git version 2.50.0";
        final String mac = "git version 2.50.0 (Apple Git-137.1)";
        assertEquals("2.50.0.1", GitVersion.parse(windows).toString());
        assertEquals("2.50.0", GitVersion.parse(linux).toString());
        assertEquals("2.50.0", GitVersion.parse(mac).toString());
    }
}
