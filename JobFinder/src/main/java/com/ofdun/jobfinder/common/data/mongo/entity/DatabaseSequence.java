package com.ofdun.jobfinder.common.data.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "database_sequences")
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseSequence {
    @Id private String id;

    private long seq;
}

