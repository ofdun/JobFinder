package com.ofdun.jobfinder.features.category.data.postgres.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.category.data.postgres.mapper.CategoryMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgreSQLCategoryRepositoryTest {
    @Mock private CategoryJpaRepository storage;
    @InjectMocks private PostgreSQLCategoryRepository repository;

    @Test
    @Tag("equivalence")
    void getCategoryById_whenPresent_mapsFields() {
        var expected = category();
        when(storage.findById(1L)).thenReturn(Optional.of(CategoryMapper.toEntity(expected)));

        var result = repository.getCategoryById(1L);

        assertEquals(Optional.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getCategoryById_whenMissing_returnsEmpty() {
        when(storage.findById(404L)).thenReturn(Optional.empty());

        var result = repository.getCategoryById(404L);

        assertTrue(result.isEmpty());
    }

    @Test
    @Tag("equivalence")
    void getAllCategories_whenPresent_preservesFields() {
        var expected = category();
        when(storage.findAllByOrderByNameAsc())
                .thenReturn(List.of(CategoryMapper.toEntity(expected)));

        var result = repository.getAllCategories();

        assertEquals(List.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getAllCategories_whenStorageFails_propagatesFailure() {
        var failure = new IllegalStateException("storage unavailable");
        when(storage.findAllByOrderByNameAsc()).thenThrow(failure);

        var error = assertThrows(IllegalStateException.class, repository::getAllCategories);

        assertSame(failure, error);
    }
}
