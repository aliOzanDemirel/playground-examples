package app.repository;

import app.domain.Timeseries;
import app.domain.TimeseriesStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TimeseriesRepository extends MongoRepository<Timeseries, String>, TimeSeriesRepositoryBase {

    List<Timeseries> findAllByStatus(TimeseriesStatus status);

    Optional<Timeseries> findByStatus(TimeseriesStatus status);

    Page<Timeseries> findByStatus(TimeseriesStatus status, Pageable pageable);

}
