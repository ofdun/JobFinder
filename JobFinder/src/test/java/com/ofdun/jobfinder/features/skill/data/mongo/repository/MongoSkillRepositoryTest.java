package com.ofdun.jobfinder.features.skill.data.mongo.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.skill.data.mongo.mapper.SkillMongoMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoSkillRepositoryTest {
    @Mock private MongoSkillCRUDRepository storage;
    @InjectMocks private MongoSkillRepository repository;

    @Test
    @Tag("equivalence")
    void getSkillById_whenPresent_mapsFields() {
        var expected = skill();
        when(storage.findById(1L)).thenReturn(Optional.of(SkillMongoMapper.toEntity(expected)));

        var result = repository.getSkillById(1L);

        assertEquals(Optional.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getSkillById_whenMissing_returnsEmpty() {
        when(storage.findById(404L)).thenReturn(Optional.empty());

        var result = repository.getSkillById(404L);

        assertTrue(result.isEmpty());
    }

    @Test
    @Tag("equivalence")
    void getAllSkills_whenPresent_preservesFields() {
        var expected = skill();
        when(storage.findAllByOrderByNameAsc())
                .thenReturn(List.of(SkillMongoMapper.toEntity(expected)));

        var result = repository.getAllSkills();

        assertEquals(List.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getAllSkills_whenStorageFails_propagatesFailure() {
        var failure = new IllegalStateException("storage unavailable");
        when(storage.findAllByOrderByNameAsc()).thenThrow(failure);

        var error = assertThrows(IllegalStateException.class, repository::getAllSkills);

        assertSame(failure, error);
    }
}
