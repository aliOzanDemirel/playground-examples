package app.repository;

import app.domain.Estimation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EstimationRepository extends MongoRepository<Estimation, String> {

    Optional<List<Estimation>> findByUserUUID(String userUUID);

    Optional<List<Estimation>> findByTimeseriesUUID(String timeseriesUUID);

    Optional<Estimation> findByUserUUIDAndTimeseriesUUID(String userUUID, String timeseriesUUID);
}
