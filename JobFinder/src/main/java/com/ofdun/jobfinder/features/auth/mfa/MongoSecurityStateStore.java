package com.ofdun.jobfinder.features.auth.mfa;

import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoSecurityStateStore implements SecurityStateStore {
    private final MongoTemplate mongo;

    public MongoSecurityStateStore(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public Snapshot read(String key) {
        var document = mongo.findById(key, Document.class, "account_security");
        return document == null
                ? new Snapshot(-1, new SecurityState())
                : new Snapshot(
                        document.getLong("version"),
                        SecurityStateStore.decode(document.getString("payload")));
    }

    public boolean save(String key, long version, SecurityState state) {
        if (version == -1) {
            try {
                mongo.insert(
                        new Document("_id", key)
                                .append("version", 0L)
                                .append("payload", SecurityStateStore.encode(state)),
                        "account_security");
                return true;
            } catch (DuplicateKeyException ex) {
                return false;
            }
        }
        return mongo.updateFirst(
                                Query.query(
                                        Criteria.where("_id").is(key).and("version").is(version)),
                                new Update()
                                        .inc("version", 1L)
                                        .set("payload", SecurityStateStore.encode(state)),
                                "account_security")
                        .getModifiedCount()
                == 1;
    }

    public void delete(String key) {
        mongo.remove(Query.query(Criteria.where("_id").is(key)), "account_security");
    }
}
