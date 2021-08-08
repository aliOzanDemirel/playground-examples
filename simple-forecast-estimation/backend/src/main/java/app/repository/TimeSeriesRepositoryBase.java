package app.repository;

import app.domain.Timeseries;
import app.domain.TimeseriesStatus;

import java.util.List;

public interface TimeSeriesRepositoryBase {

    List<Timeseries> findAllOrderByStartDateLimitBy(TimeseriesStatus status, int limit);

    List<Timeseries> findAllByStatusLimitBy(TimeseriesStatus status, int limit);

}
