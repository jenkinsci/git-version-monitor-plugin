package io.jenkins.plugins.gitversionmonitor;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.node_monitors.AbstractAsyncNodeMonitorDescriptor;
import hudson.node_monitors.NodeMonitor;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.util.StreamTaskListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import jenkins.MasterToSlaveFileCallable;
import org.jenkinsci.Symbol;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
@SuppressWarnings("unused")
public class GitVersionNodeMonitor extends NodeMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(GitVersionNodeMonitor.class);

    @DataBoundConstructor
    @SuppressWarnings("unused") // Used by Stapler
    public GitVersionNodeMonitor() {
        // Default constructor for Stapler
    }

    @SuppressWarnings("unused") // Used by Stapler
    public String toHtml(@Nullable String version) {
        GitVersion gitVersion = GitVersion.parse(version);
        if (gitVersion == null) {
            return "N/A";
        }
        return gitVersion.toString();
    }

    @Extension
    @Symbol("gitVersion")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends AbstractAsyncNodeMonitorDescriptor<String> {
        @Override
        protected @Nullable Callable<String, IOException> createCallable(Computer computer) {
            Node node = computer.getNode();
            if (node == null) {
                LOG.warn("Node is null for computer: {}", computer.getName());
                return null;
            }
            FilePath filePath = node.getRootPath();
            if (filePath == null) {
                LOG.warn("Root path is null for node: {}", node.getNodeName());
                return null;
            }
            return filePath.asCallableWith(new GetGitVersion());
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Git Version";
        }
    }

    /**
     * Obtains the string that represents the architecture.
     */
    private static class GetGitVersion extends MasterToSlaveFileCallable<String> {

        private static final Logger LOG = LoggerFactory.getLogger(GetGitVersion.class);

        @Override
        public @Nullable String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamTaskListener listener = new StreamTaskListener(out, StandardCharsets.UTF_8);
            Launcher launcher = new Launcher.LocalLauncher(listener);
            Launcher.ProcStarter procStarter = launcher.launch()
                    .cmds("git", "--version")
                    .stdout(out)
                    .stderr(null)
                    .quiet(true)
                    .pwd(f);
            Proc proc = procStarter.start();
            int exitCode = proc.join();

            String output = out.toString(StandardCharsets.UTF_8).trim();
            if (exitCode != 0) {
                LOG.warn(
                        "Failed to get Git version from {}: exit code {}, output: {}",
                        f.getAbsolutePath(),
                        exitCode,
                        output);
                return null;
            }
            LOG.debug("Git version output: {}", output);
            GitVersion gitVersion = GitVersion.parse(output);
            if (gitVersion == null) {
                LOG.warn("Failed to parse Git version from output: {}", output);
                return null;
            }
            LOG.debug("Parsed Git version: {}", gitVersion);
            return gitVersion.toString();
        }
    }
}
