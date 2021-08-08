package app.repository;

import app.domain.HighScore;
import app.domain.User;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Update.update;

@Repository
public class UserRepositoryImpl implements UserRepositoryBase {

    @Value("${app.score.limit}")
    private int scoreLimit;

    private final MongoTemplate mongoTemplate;

    @Autowired
    private SequenceRepositoryImpl sequenceRepository;

    public UserRepositoryImpl(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean changePassword(final String oldPassword,
                                  final String newPassword,
                                  final String username) {
        final Query searchUserQuery = new Query(where("username").is(username).andOperator(where("password").is(oldPassword)));
        final UpdateResult updateResult = mongoTemplate.updateFirst(searchUserQuery, update("password", newPassword), User.class);
        return updateResult.wasAcknowledged();
    }

    public List<HighScore> findAllByOrderByHighScoreDesc() {
        Query query = new Query();
        query.limit(scoreLimit);
        query.with(new Sort(Sort.Direction.DESC, "highScore"));
        query.addCriteria(Criteria.where("username"));
        List<User> users = mongoTemplate.find(query, User.class);
        return users
                .stream()
                .map(u -> new HighScore(u.getSequence(), u.getHighScore()))
                .collect(Collectors.toList());
    }

    @Override
    public <S extends User> S insert(S s) {

        s.setSequence(sequenceRepository.getNextSequenceId(s.getClass().getName()));
        return mongoTemplate.save(s);
    }
}
