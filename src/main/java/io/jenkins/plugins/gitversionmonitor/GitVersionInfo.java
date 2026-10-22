package io.jenkins.plugins.gitversionmonitor;

import java.io.Serializable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The Git version of a node, parsed once on the node itself.
 *
 * @param version the parsed version, shown in the column
 * @param full the version as Git reports it, e.g. {@code 2.50.0.windows.1}, shown as a tooltip
 */
@NullMarked
public record GitVersionInfo(GitVersion version, String full) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Parse the output of {@code git --version}.
     * @return the version, or null if the output is not recognized
     */
    public static @Nullable GitVersionInfo parse(@Nullable String output) {
        GitVersion version = GitVersion.parse(output);
        if (version == null || output == null) {
            return null;
        }
        return new GitVersionInfo(version, output.replace("git version", "").trim());
    }
}
