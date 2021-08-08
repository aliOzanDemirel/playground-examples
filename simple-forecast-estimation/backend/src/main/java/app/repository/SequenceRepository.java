package app.repository;

import app.domain.Sequence;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SequenceRepository extends MongoRepository<Sequence, String>, SequenceRepositoryBase {
}
