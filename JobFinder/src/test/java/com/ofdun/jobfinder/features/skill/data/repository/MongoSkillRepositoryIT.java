package com.ofdun.jobfinder.features.skill.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.common.test.MongoRepositoryITBase;
import com.ofdun.jobfinder.features.skill.data.mongo.entity.SkillDocument;
import com.ofdun.jobfinder.features.skill.data.mongo.repository.MongoSkillCRUDRepository;
import com.ofdun.jobfinder.features.skill.data.mongo.repository.MongoSkillRepository;
import com.ofdun.jobfinder.features.skill.domain.repository.SkillRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag("mongo")
@ActiveProfiles("test-mongo")
@Import(MongoSkillRepository.class)
class MongoSkillRepositoryIT extends MongoRepositoryITBase {

    @Autowired private SkillRepository skillRepository;
    @Autowired private MongoSkillCRUDRepository mongoSkillCRUDRepository;

    @Test
    void getAllSkills_returnsSortedByName() {
        mongoSkillCRUDRepository.save(new SkillDocument(1L, "Spring"));
        mongoSkillCRUDRepository.save(new SkillDocument(2L, "AWS"));

        var all = skillRepository.getAllSkills();

        assertEquals(2, all.size());
        assertEquals("AWS", all.get(0).getName());
    }
}
