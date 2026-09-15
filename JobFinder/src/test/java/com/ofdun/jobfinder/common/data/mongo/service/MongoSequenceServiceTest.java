package com.ofdun.jobfinder.common.data.mongo.service;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.common.data.mongo.entity.DatabaseSequence;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;

@ExtendWith(MockitoExtension.class)
class MongoSequenceServiceTest {
    @Mock private MongoOperations storage;
    @InjectMocks private MongoSequenceService service;

    @Test
    @Tag("equivalence")
    void generateSequence_whenAvailable_incrementsAtomically() {
        when(storage.findAndModify(
                        any(Query.class),
                        any(Update.class),
                        any(FindAndModifyOptions.class),
                        eq(DatabaseSequence.class)))
                .thenReturn(new DatabaseSequence("applicant", 42L));

        var result = service.generateSequence("applicant");

        assertEquals(42L, result);
        verify(storage)
                .findAndModify(
                        argThat(
                                query ->
                                        "applicant"
                                                .equals(query.getQueryObject().getString("_id"))),
                        argThat(
                                update ->
                                        update.getUpdateObject()
                                                        .get("$inc", org.bson.Document.class)
                                                        .getInteger("seq")
                                                == 1),
                        argThat(options -> options.isUpsert() && options.isReturnNew()),
                        eq(DatabaseSequence.class));
    }

    @Test
    @Tag("equivalence")
    void generateSequence_whenNoCounter_throwsFailure() {
        when(storage.findAndModify(
                        any(Query.class),
                        any(Update.class),
                        any(FindAndModifyOptions.class),
                        eq(DatabaseSequence.class)))
                .thenReturn(null);

        var error =
                assertThrows(
                        IllegalStateException.class, () -> service.generateSequence("applicant"));

        assertTrue(error.getMessage().contains("applicant"));
    }
}
