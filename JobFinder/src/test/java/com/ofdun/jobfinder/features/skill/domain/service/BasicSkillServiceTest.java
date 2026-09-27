package com.ofdun.jobfinder.features.skill.domain.service;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.skill.domain.repository.SkillRepository;
import com.ofdun.jobfinder.features.skill.exception.SkillNotFoundException;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicSkillServiceTest {
    @Mock private SkillRepository repository;
    @InjectMocks private BasicSkillService service;

    @Test
    @Tag("equivalence")
    void getSkillById_whenPresent_returnsModel() {
        var model = skill();
        when(repository.getSkillById(1L)).thenReturn(Optional.of(model));

        var result = service.getSkillById(1L);

        assertEquals(model, result);
    }

    @Test
    @Tag("equivalence")
    void getSkillById_whenMissing_throwsNotFound() {
        when(repository.getSkillById(404L)).thenReturn(Optional.empty());

        var error = assertThrows(SkillNotFoundException.class, () -> service.getSkillById(404L));

        assertNotNull(error);
    }

    @Test
    @Tag("equivalence")
    void getAllSkills_whenPresent_returnsModels() {
        var expected = List.of(skill());
        when(repository.getAllSkills()).thenReturn(expected);

        var result = service.getAllSkills();

        assertEquals(expected, result);
    }

    @Test
    @Tag("equivalence")
    void getAllSkills_whenStorageFails_propagatesFailure() {
        var failure = new IllegalStateException("storage unavailable");
        when(repository.getAllSkills()).thenThrow(failure);

        var error = assertThrows(IllegalStateException.class, service::getAllSkills);

        assertSame(failure, error);
    }
}
