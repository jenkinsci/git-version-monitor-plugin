package io.jenkins.plugins.gitversionmonitor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.casc.misc.junit.jupiter.WithJenkinsConfiguredWithCode;
import org.junit.jupiter.api.Test;

@WithJenkinsConfiguredWithCode
class ConfigurationAsCodeTest {

    @Test
    @ConfiguredWithCode("configuration-as-code.yml")
    void testConfigurationAsCode(JenkinsConfiguredWithCodeRule jenkinsRule) {
        GitVersionNodeMonitor.DescriptorImpl descriptor = (GitVersionNodeMonitor.DescriptorImpl)
                jenkinsRule.jenkins.getDescriptorOrDie(GitVersionNodeMonitor.class);
        assertNotNull(descriptor, "Descriptor should not be null");
    }
}
