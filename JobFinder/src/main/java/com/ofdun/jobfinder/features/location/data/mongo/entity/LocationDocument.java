package com.ofdun.jobfinder.features.location.data.mongo.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "locations")
@AllArgsConstructor
@NoArgsConstructor
public class LocationDocument {
    @Id
    private Long id;

    @NotNull
    @Size(min = 1, max = 55)
    private String city;

    @NotNull
    @Size(min = 1, max = 55)
    private String country;
}

