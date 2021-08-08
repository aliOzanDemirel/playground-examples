package app.repository;

import app.domain.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class SequenceRepositoryImpl implements SequenceRepositoryBase {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public long getNextSequenceId(String key) {
        Query query = new Query(Criteria.where("_id").is(key));

        Update update = new Update();
        update.inc("seq", 1);

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);

        Sequence sequence = mongoTemplate.findAndModify(query, update, options, Sequence.class);

        if (sequence == null) {
            mongoTemplate.save(new Sequence(key, 0));
            return 0;
        }

        return sequence.getSeq();
    }
}
