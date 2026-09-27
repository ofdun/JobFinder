package com.ofdun.jobfinder.support;

import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class TestTagsExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {
        String tags = context.getTags().stream().sorted().collect(Collectors.joining(", "));
        context.publishReportEntry("tags", tags.isEmpty() ? "untagged" : tags);
    }
}
