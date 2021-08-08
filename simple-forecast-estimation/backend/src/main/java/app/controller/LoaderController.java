package app.controller;

import app.service.CsvLoaderService;
import app.service.EstimationService;
import app.service.TimeseriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ScheduledFuture;

@RestController
@RequestMapping("/tasks")
public class LoaderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Value("${app.schedule.rate}")
    private int rate;

    @Autowired
    private TimeseriesService timeseriesService;
    @Autowired
    private EstimationService estimationService;
    @Autowired
    private CsvLoaderService loaderService;
    @Autowired
    private TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledFuture;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity schedule() {

        if (scheduledFuture != null) {
            LOGGER.info("Stopping scheduler");

            if (scheduledFuture.cancel(true)) {
                LOGGER.info("Scheduler is successfully stopped.");
            } else {
                LOGGER.warn("Scheduler could not be stopped.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        LOGGER.info("Cleaning estimations timeseries.");
        timeseriesService.cleanTimeseries();
        estimationService.cleanEstimations();

        LOGGER.info("Resetting loader service's average timeseries and timeseries list.");
        loaderService.resetServiceData();

        LOGGER.info("Scheduling task with fixed rate: {}", rate);
        scheduledFuture = taskScheduler.scheduleAtFixedRate(loaderService, rate);

        return ResponseEntity.ok().build();
    }

}
