package io.jenkins.plugins.gitversionmonitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Reads the monitor data through the REST API. Jenkins exports the data of every monitor, so data
 * without {@code @ExportedBean} breaks the API of the whole node list.
 */
@WithJenkins
class RemoteApiTest {

    /** Git may not be installed where the tests run, in which case there is no data to export. */
    @Test
    void nodeListApiExportsGitVersion(JenkinsRule j) throws Exception {
        ColumnTestSupport.enable(new GitVersionNodeMonitor());
        GitVersionInfo info =
                ColumnTestSupport.awaitData(GitVersionNodeMonitor.DescriptorImpl.class, j.jenkins.toComputer());
        assumeTrue(info != null, "Git is not installed");

        JSONObject data = j.getJSON("computer/api/json?depth=1")
                .getJSONObject()
                .getJSONArray("computer")
                .getJSONObject(0)
                .getJSONObject("monitorData")
                .getJSONObject(GitVersionNodeMonitor.class.getName());

        assertEquals(info.version().toString(), data.getString("version"));
        assertEquals(info.full(), data.getString("full"));
    }
}
