package io.jenkins.plugins.gitversionmonitor;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public record GitVersion(Integer major, Integer minor, Integer patch, Integer build) {

    private static final Logger LOG = LoggerFactory.getLogger(GitVersion.class);

    public static GitVersion of(Integer major, Integer minor, Integer patch, Integer build) {
        return new GitVersion(major, minor, patch, build);
    }

    /**
     * Parse a raw version string into a GitVersion object.
     * @param version the version string from the "git --version" command
     * @return a GitVersion object containing the parsed version information
     */
    public static @Nullable GitVersion parse(@Nullable String version) {
        if (version == null || version.isEmpty()) {
            return null;
        }

        try {
            // Remove useless parts of the version string
            String[] parts = version.replace("git version", "")
                    .replace(".windows", "")
                    .replaceAll("\\(.*\\)", "")
                    .replace(" ", "")
                    .trim()
                    .split("\\.");
            String major = parts.length > 0 ? parts[0] : "0";
            String minor = parts.length > 1 ? parts[1] : "0";
            String patch = parts.length > 2 ? parts[2] : "0";
            String build = parts.length > 3 ? parts[3] : "0";

            // Ensure all parts are numeric
            if (!major.matches("\\d+") || !minor.matches("\\d+") || !patch.matches("\\d+") || !build.matches("\\d+")) {
                LOG.warn("Invalid version format: {}", version);
                return null;
            }

            return new GitVersion(
                    Integer.parseInt(major), Integer.parseInt(minor), Integer.parseInt(patch), Integer.parseInt(build));
        } catch (Exception e) {
            LOG.error("Failed to parse Git version: {}", version, e);
            return null;
        }
    }

    @Override
    public String toString() {
        if (build != 0) {
            return "%d.%d.%d.%d".formatted(major, minor, patch, build);
        }
        return "%d.%d.%d".formatted(major, minor, patch);
    }
}
