package app.repository;

import app.domain.Timeseries;
import app.domain.TimeseriesStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class TimeseriesRepositoryImpl implements TimeSeriesRepositoryBase {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * first timeseries of returned list will be the closest to actual day because
     * its start date is most recent compared to other history timeseries
     */
    @Override
    public List<Timeseries> findAllOrderByStartDateLimitBy(TimeseriesStatus status, int limit) {
        final Query query = new Query()
                .addCriteria(Criteria.where("status").is(status))
                .limit(limit)
                .with(new Sort(Sort.Direction.DESC, "startDate"));
        return mongoTemplate.find(query, Timeseries.class);
    }

    @Override
    public List<Timeseries> findAllByStatusLimitBy(TimeseriesStatus status, int limit) {
        final Query query = new Query()
                .addCriteria(Criteria.where("status").is(status))
                .limit(limit)
                .with(new Sort(Sort.Direction.DESC, "timeseriesUUID"));
        return mongoTemplate.find(query, Timeseries.class);
    }
}
