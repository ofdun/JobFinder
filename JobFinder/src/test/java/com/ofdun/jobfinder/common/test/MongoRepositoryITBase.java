package com.ofdun.jobfinder.common.test;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataMongoTest
@ActiveProfiles("test-mongo")
@Testcontainers
public abstract class MongoRepositoryITBase {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void registerMongoProperties(DynamicPropertyRegistry registry) {
        String uri = String.format(
                "mongodb://%s:%d/test?directConnection=true",
                mongo.getHost(), mongo.getMappedPort(27017)
        );

        registry.add("spring.data.mongodb.uri", () -> uri);
        registry.add("spring.mongodb.uri", () -> uri);
        registry.add("app.persistence.type", () -> "mongo");
    }

    @Autowired
    protected MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getDb().drop();
    }
}
