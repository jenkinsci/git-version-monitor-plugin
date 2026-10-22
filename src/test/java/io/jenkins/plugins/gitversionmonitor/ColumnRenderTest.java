package io.jenkins.plugins.gitversionmonitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import hudson.slaves.DumbSlave;
import java.util.List;
import org.htmlunit.html.HtmlElement;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Renders the node list to check that the column template can reach the monitor and shows the
 * full Git version as a tooltip. The unit tests of the monitor cannot catch a broken template.
 */
@WithJenkins
class ColumnRenderTest {

    @Test
    void tooltipIsEmptyForUnknownVersion() {
        GitVersionNodeMonitor monitor = new GitVersionNodeMonitor();

        assertEquals("", monitor.getTooltip(null));
    }

    @Test
    void tooltipIsTheFullVersion() {
        GitVersionNodeMonitor monitor = new GitVersionNodeMonitor();
        monitor.setControllerVersionOverride(GitVersionInfo.parse("git version 2.0.0"));

        assertEquals("2.50.0.windows.1", monitor.getTooltip(GitVersionInfo.parse("git version 2.50.0.windows.1")));
        assertEquals(
                "2.50.0 (Apple Git-137.1)",
                monitor.getTooltip(GitVersionInfo.parse("git version 2.50.0 (Apple Git-137.1)")));
    }

    /** Git may not be installed where the tests run, in which case there is no tooltip to show. */
    @Test
    void builtInNodeShowsTooltip(JenkinsRule j) throws Exception {
        GitVersionInfo version = awaitControllerVersion(j);

        ColumnTestSupport.enable(new GitVersionNodeMonitor());
        ColumnTestSupport.awaitData(GitVersionNodeMonitor.DescriptorImpl.class, j.jenkins.toComputer());
        String html = ColumnTestSupport.renderNodeList(j).getWebResponse().getContentAsString();

        assertTrue(
                html.contains("tooltip=\"" + new GitVersionNodeMonitor().getTooltip(version) + "\""),
                "the node list did not show the tooltip:\n" + html);
    }

    /** The monitor runs on a real agent, which sends its result back over remoting. */
    @Test
    void agentShowsTooltip(JenkinsRule j) throws Exception {
        awaitControllerVersion(j);
        DumbSlave agent = j.createOnlineSlave();
        ColumnTestSupport.enable(new GitVersionNodeMonitor());
        GitVersionInfo version =
                ColumnTestSupport.awaitData(GitVersionNodeMonitor.DescriptorImpl.class, agent.toComputer());
        assertNotNull(version, "the agent reported no Git version");

        String expected = new GitVersionNodeMonitor().getTooltip(version);
        List<HtmlElement> cells =
                ColumnTestSupport.cellsInRow(ColumnTestSupport.renderNodeList(j), agent.getNodeName(), expected);

        assertEquals(1, cells.size(), "expected the agent's row to show the tooltip " + expected);
    }

    @Test
    void olderThanControllerIsOutdated() {
        GitVersionNodeMonitor monitor = new GitVersionNodeMonitor();
        monitor.setControllerVersionOverride(GitVersionInfo.parse("git version 2.50.0"));

        assertTrue(monitor.isOutdated(GitVersionInfo.parse("git version 2.49.1")));
        assertFalse(monitor.isOutdated(GitVersionInfo.parse("git version 2.50.0")));
        assertFalse(monitor.isOutdated(GitVersionInfo.parse("git version 2.51.0")));
        assertFalse(monitor.isOutdated(null));
        assertEquals(
                "2.49.1 (older than the controller: 2.50.0)",
                monitor.getTooltip(GitVersionInfo.parse("git version 2.49.1")));
        assertEquals("2.51.0", monitor.getTooltip(GitVersionInfo.parse("git version 2.51.0")));
    }

    @Test
    void builtInNodeWarnsWhenControllerIsNewer(JenkinsRule j) throws Exception {
        GitVersionInfo version = awaitControllerVersion(j);
        GitVersionNodeMonitor monitor = new GitVersionNodeMonitor();
        monitor.setControllerVersionOverride(GitVersionInfo.parse("git version 99.0.0"));
        ColumnTestSupport.enable(monitor);

        String expected = monitor.getTooltip(version);
        List<HtmlElement> cells = ColumnTestSupport.renderNodeList(j).getByXPath("//td[@tooltip='" + expected + "']");

        assertEquals(1, cells.size());
        assertTrue(cells.get(0).getTextContent().contains("⚠"));
        assertTrue(expected.endsWith("(older than the controller: 99.0.0)"));
    }

    @Test
    void builtInNodeDoesNotWarnWhenNewerThanController(JenkinsRule j) throws Exception {
        GitVersionInfo version = awaitControllerVersion(j);
        GitVersionNodeMonitor monitor = new GitVersionNodeMonitor();
        monitor.setControllerVersionOverride(GitVersionInfo.parse("git version 0.0.1"));
        ColumnTestSupport.enable(monitor);

        String html = ColumnTestSupport.renderNodeList(j).getWebResponse().getContentAsString();

        assertTrue(html.contains("tooltip=\"" + monitor.getTooltip(version) + "\""));
        assertFalse(html.contains("older than the controller"));
    }

    /** Skips the test if Git is not installed, and returns what Git reported on the controller. */
    private static GitVersionInfo awaitControllerVersion(JenkinsRule j) throws Exception {
        ColumnTestSupport.enable(new GitVersionNodeMonitor());
        GitVersionInfo version =
                ColumnTestSupport.awaitData(GitVersionNodeMonitor.DescriptorImpl.class, j.jenkins.toComputer());
        assumeTrue(version != null, "Git not found");
        return version;
    }
}
