package com.ofdun.jobfinder.features.auth.data.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoApplicantAccountRepositoryTest {
    @Mock private ApplicantRepository storage;
    @InjectMocks private MongoApplicantAccountRepository repository;

    @Test
    @Tag("equivalence")
    void findByEmail_whenPresent_mapsAccount() {
        var model = applicant();
        when(storage.getApplicantByEmail(model.getEmail())).thenReturn(Optional.of(model));

        var result = repository.findByEmail(model.getEmail()).orElseThrow();

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getPasswordHash(), result.getPasswordHash());
    }

    @Test
    @Tag("equivalence")
    void findByEmail_whenMissing_returnsAbsence() {
        when(storage.getApplicantByEmail("missing@example.test")).thenReturn(Optional.empty());

        var result = repository.findByEmail("missing@example.test");

        assertTrue(result.isEmpty());
    }
}
