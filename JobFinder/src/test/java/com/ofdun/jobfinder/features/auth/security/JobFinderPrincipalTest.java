package com.ofdun.jobfinder.features.auth.security;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.auth.enums.AccountType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Tag("equivalence")
class JobFinderPrincipalTest {
    @ParameterizedTest
    @EnumSource(AccountType.class)
    void constructor_whenFieldsPresent_preservesIdentity(AccountType role) {
        var principal = new JobFinderPrincipal(42L, role);

        assertEquals(42L, principal.id());
        assertEquals(role, principal.accountType());
    }

    @Test
    void constructor_whenFieldsMissing_preservesNulls() {
        var principal = new JobFinderPrincipal(null, null);

        assertNull(principal.id());
        assertNull(principal.accountType());
    }

    @Test
    void equals_whenIdentityMatches_returnsTrue() {
        var principal = new JobFinderPrincipal(42L, AccountType.APPLICANT);
        var other = new JobFinderPrincipal(42L, AccountType.APPLICANT);

        var result = principal.equals(other);

        assertTrue(result);
        assertEquals(principal.hashCode(), other.hashCode());
    }

    @Test
    void equals_whenIdDiffers_returnsFalse() {
        var principal = new JobFinderPrincipal(42L, AccountType.APPLICANT);
        var other = new JobFinderPrincipal(43L, AccountType.APPLICANT);

        var result = principal.equals(other);

        assertFalse(result);
    }

    @Test
    void equals_whenRoleDiffers_returnsFalse() {
        var principal = new JobFinderPrincipal(42L, AccountType.APPLICANT);
        var other = new JobFinderPrincipal(42L, AccountType.EMPLOYER);

        var result = principal.equals(other);

        assertFalse(result);
    }
}
