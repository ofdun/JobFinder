package com.ofdun.jobfinder.features.category.domain.service;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.category.domain.repository.CategoryRepository;
import com.ofdun.jobfinder.features.category.exception.CategoryNotFoundException;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicCategoryServiceTest {
    @Mock private CategoryRepository repository;
    @InjectMocks private BasicCategoryService service;

    @Test
    @Tag("equivalence")
    void getCategoryById_whenPresent_returnsModel() {
        var model = category();
        when(repository.getCategoryById(1L)).thenReturn(Optional.of(model));

        var result = service.getCategoryById(1L);

        assertEquals(model, result);
    }

    @Test
    @Tag("equivalence")
    void getCategoryById_whenMissing_throwsNotFound() {
        when(repository.getCategoryById(404L)).thenReturn(Optional.empty());

        var error =
                assertThrows(CategoryNotFoundException.class, () -> service.getCategoryById(404L));

        assertNotNull(error);
    }

    @Test
    @Tag("equivalence")
    void getAllCategories_whenPresent_returnsModels() {
        var expected = List.of(category());
        when(repository.getAllCategories()).thenReturn(expected);

        var result = service.getAllCategories();

        assertEquals(expected, result);
    }

    @Test
    @Tag("equivalence")
    void getAllCategories_whenStorageFails_propagatesFailure() {
        var failure = new IllegalStateException("storage unavailable");
        when(repository.getAllCategories()).thenThrow(failure);

        var error = assertThrows(IllegalStateException.class, service::getAllCategories);

        assertSame(failure, error);
    }
}
