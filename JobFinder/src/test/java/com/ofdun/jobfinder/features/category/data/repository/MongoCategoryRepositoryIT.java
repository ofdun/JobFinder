package com.ofdun.jobfinder.features.category.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.common.test.MongoRepositoryITBase;
import com.ofdun.jobfinder.features.category.data.mongo.entity.CategoryDocument;
import com.ofdun.jobfinder.features.category.data.mongo.repository.MongoCategoryCRUDRepository;
import com.ofdun.jobfinder.features.category.data.mongo.repository.MongoCategoryRepository;
import com.ofdun.jobfinder.features.category.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag("mongo")
@ActiveProfiles("test-mongo")
@Import(MongoCategoryRepository.class)
class MongoCategoryRepositoryIT extends MongoRepositoryITBase {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private MongoCategoryCRUDRepository mongoCategoryCRUDRepository;

    @Test
    void getAllCategories_returnsSortedByName() {
        mongoCategoryCRUDRepository.save(new CategoryDocument(1L, "Zeta"));
        mongoCategoryCRUDRepository.save(new CategoryDocument(2L, "Alpha"));

        var all = categoryRepository.getAllCategories();

        assertEquals(2, all.size());
        assertEquals("Alpha", all.get(0).getName());
        assertEquals("Zeta", all.get(1).getName());
    }
}
