package com.ofdun.jobfinder.features.auth.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/authentication.feature")
@ConfigurationParameter(key = "cucumber.glue", value = "com.ofdun.jobfinder.features.auth.bdd")
@ConfigurationParameter(
        key = "cucumber.plugin",
        value =
                "pretty,html:build/reports/cucumber/authentication.html,junit:build/test-results/bddTest/TEST-authentication.xml")
@ConfigurationParameter(key = "cucumber.publish.quiet", value = "true")
@ConfigurationParameter(key = "cucumber.junit-platform.naming-strategy", value = "long")
@ConfigurationParameter(key = "cucumber.execution.parallel.enabled", value = "false")
public class AuthenticationBddE2E {}
