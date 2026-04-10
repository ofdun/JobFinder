package com.ofdun.jobfinder.features.education.api.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;

/**
 * Deserializes {@link Year} from JSON.
 *
 * <p>Supports both formats:
 * <ul>
 *   <li>"YYYY" (e.g. "2002")</li>
 *   <li>"YYYY-MM-DD" (e.g. "2002-12-03") - only year part is used</li>
 * </ul>
 */
public class YearFromStringDeserializer extends JsonDeserializer<Year> {
    @Override
    public Year deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String raw = p.getValueAsString();
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return null;
        }

        // Accept a plain year first
        if (value.length() == 4) {
            return Year.parse(value);
        }

        // Accept ISO local date, extract year
        try {
            return Year.of(LocalDate.parse(value).getYear());
        } catch (Exception ignored) {
            // fall through
        }

        // Last attempt: take the first 4 digits if present
        if (value.length() >= 4) {
            String first4 = value.substring(0, 4);
            try {
                return Year.parse(first4);
            } catch (Exception ignored) {
                // fall through
            }
        }

        // Let Jackson build a proper error message
        return (Year) ctxt.handleWeirdStringValue(Year.class, value,
                "Expected 'YYYY' or ISO date 'YYYY-MM-DD'");
    }
}

