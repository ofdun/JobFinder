package com.ofdun.jobfinder.support;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class TestProcessExtension implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) {
        System.out.printf(
                "TEST_PROCESS pid=%d worker=%s suite=%s%n",
                ProcessHandle.current().pid(),
                System.getProperty("org.gradle.test.worker"),
                context.getRequiredTestClass().getName());
    }
}
