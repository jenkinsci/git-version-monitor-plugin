package io.jenkins.plugins.gitversionmonitor;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.Computer;
import hudson.node_monitors.AbstractAsyncNodeMonitorDescriptor;
import hudson.node_monitors.NodeMonitor;
import hudson.util.StreamTaskListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import jenkins.security.MasterToSlaveCallable;
import org.jenkinsci.Symbol;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node monitor that reports the version of Git installed on each node, with the full version
 * as a tooltip. Nodes with an older Git than the controller are marked with a warning.
 */
@NullMarked
public class GitVersionNodeMonitor extends NodeMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(GitVersionNodeMonitor.class);

    private static final long CONTROLLER_CACHE_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private static volatile @Nullable GitVersionInfo cachedControllerVersion;
    private static volatile long cachedAt;

    /** Replaces the controller's version, so that tests can make a node look outdated. */
    private transient @Nullable GitVersionInfo controllerVersionOverride;

    @DataBoundConstructor
    public GitVersionNodeMonitor() {
        // Default constructor for Stapler
    }

    void setControllerVersionOverride(@Nullable GitVersionInfo version) {
        this.controllerVersionOverride = version;
    }

    /**
     * The version of Git on the controller. The result is cached for a few minutes, because
     * this is called for every node each time the node list is rendered.
     */
    private @Nullable GitVersionInfo controllerVersion() {
        if (controllerVersionOverride != null) {
            return controllerVersionOverride;
        }
        GitVersionInfo cached = cachedControllerVersion;
        if (cached == null || System.currentTimeMillis() - cachedAt > CONTROLLER_CACHE_MILLIS) {
            cached = readGitVersion();
            cachedControllerVersion = cached;
            cachedAt = System.currentTimeMillis();
        }
        return cached;
    }

    /** The version shown in the column, or {@code N/A} if it is unknown. */
    public String toHtml(@Nullable GitVersionInfo info) {
        return info == null ? "N/A" : info.version().toString();
    }

    /**
     * Whether the node has an older Git than the controller. A node with a newer Git, or with
     * a version that is unknown on either side, is not outdated.
     */
    public boolean isOutdated(@Nullable GitVersionInfo info) {
        GitVersionInfo controller = controllerVersion();
        return info != null && controller != null && info.version().compareTo(controller.version()) < 0;
    }

    /**
     * The full version, with the controller's version added if the node is outdated. Empty if
     * the version is unknown.
     */
    public String getTooltip(@Nullable GitVersionInfo info) {
        if (info == null) {
            return "";
        }
        if (isOutdated(info)) {
            return info.full() + " (older than the controller: " + toHtml(controllerVersion()) + ")";
        }
        return info.full();
    }

    /**
     * Runs {@code git --version} and returns the parsed output, or null if Git is missing or the
     * output is not recognized.
     */
    private static @Nullable GitVersionInfo readGitVersion() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamTaskListener listener = new StreamTaskListener(out, StandardCharsets.UTF_8);
            Launcher launcher = new Launcher.LocalLauncher(listener);
            Proc proc = launcher.launch()
                    .cmds("git", "--version")
                    .stdout(out)
                    .stderr(null)
                    .quiet(true)
                    .start();
            int exitCode = proc.join();

            String output = out.toString(StandardCharsets.UTF_8).trim();
            if (exitCode != 0) {
                LOG.warn("Failed to get Git version: exit code {}, output: {}", exitCode, output);
                return null;
            }
            GitVersionInfo info = GitVersionInfo.parse(output);
            if (info == null) {
                LOG.warn("Failed to parse Git version from output: {}", output);
            }
            return info;
        } catch (IOException e) {
            LOG.debug("Could not run git", e);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Extension
    @Symbol("gitVersion")
    public static class DescriptorImpl extends AbstractAsyncNodeMonitorDescriptor<GitVersionInfo> {
        @Override
        protected MasterToSlaveCallable<GitVersionInfo, IOException> createCallable(Computer computer) {
            return new GetGitVersion();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.gitVersionMonitor_displayName();
        }

        /**
         * This monitor only reports information, so there is no threshold to take a
         * node offline.
         */
        @Override
        public boolean canTakeOffline() {
            return false;
        }
    }

    /** Runs on the node itself, so it reports the Git found on the node's path. */
    private static final class GetGitVersion extends MasterToSlaveCallable<GitVersionInfo, IOException> {
        private static final long serialVersionUID = 1L;

        @Override
        public @Nullable GitVersionInfo call() {
            return readGitVersion();
        }
    }
}
