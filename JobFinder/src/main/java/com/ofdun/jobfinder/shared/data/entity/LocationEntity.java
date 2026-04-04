package com.ofdun.jobfinder.shared.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationEntity {
    @Id
    private Long id;

    @NotNull
    @Size(min = 1, max = 55)
    private String city;

    @NotNull
    @Size(min = 1, max = 55)
    private String country;
}
