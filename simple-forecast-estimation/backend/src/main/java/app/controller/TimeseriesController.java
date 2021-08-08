package app.controller;

import app.domain.Timeseries;
import app.service.TimeseriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping ("/timeseries")
public class TimeseriesController {

    @Value ("${app.history.limit}")
    private int historyCount;

    @Autowired
    private TimeseriesService timeseriesService;

    @GetMapping
    public List<Timeseries> getTimeseriesHistory(@RequestParam(value = "forecast", required = false) boolean forecast) {
        if (forecast) {
            return timeseriesService.getTimeseriesForecast();
        }
        return timeseriesService.getTimeseriesHistory(historyCount);
    }
}
