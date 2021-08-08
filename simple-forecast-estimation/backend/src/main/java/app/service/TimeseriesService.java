package app.service;

import app.domain.Timeseries;
import app.domain.TimeseriesStatus;
import app.repository.TimeseriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimeseriesService {

    @Autowired
    private TimeseriesRepository timeseriesRepository;

    public List<Timeseries> getTimeseriesHistory(int count) {
        return timeseriesRepository.findAllOrderByStartDateLimitBy(TimeseriesStatus.HISTORY, count);
    }

    public List<Timeseries> getTimeseriesForecast() {
        return timeseriesRepository.findAllByStatus(TimeseriesStatus.FORECAST);
    }

    public Timeseries changeStatusFromRealToHistory(int sunCoverage, int windSpeed) {
        Timeseries timeseries = timeseriesRepository.findAllByStatusLimitBy(TimeseriesStatus.REAL, 1).get(0);
        timeseries.setStatus(TimeseriesStatus.HISTORY);
        timeseries.setSunCoverage(sunCoverage);
        timeseries.setWindSpeed(windSpeed);
        return timeseriesRepository.save(timeseries);
    }

    public void cleanTimeseries() {
        timeseriesRepository.deleteAll();
    }

}
