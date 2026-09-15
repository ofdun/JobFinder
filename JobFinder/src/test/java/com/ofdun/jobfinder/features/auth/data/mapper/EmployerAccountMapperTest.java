package com.ofdun.jobfinder.features.auth.data.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class EmployerAccountMapperTest {

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAccount() {
        var model = employer();

        var result = EmployerAccountMapper.toModel(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getEmail(), result.getEmail());
        assertEquals(model.getPasswordHash(), result.getPasswordHash());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = EmployerAccountMapper.toModel(null);

        assertNull(result);
    }
}
