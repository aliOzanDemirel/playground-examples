package app.service;

import app.domain.Timeseries;
import app.domain.TimeseriesStatus;
import app.repository.TimeseriesRepository;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CsvLoaderService implements ApplicationListener<ApplicationReadyEvent>, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvLoaderService.class);

    @Value("${app.schedule.rate}")
    private double rate;

    private LinkedList<List<Timeseries>> timeseriesList = new LinkedList<>();
    private Timeseries averageTimeseries = new Timeseries();

    @Autowired
    private TimeseriesRepository timeseriesRepository;
    @Autowired
    private TimeseriesService timeseriesService;
    @Autowired
    private EstimationService estimationService;
    @Autowired
    private SocketService socketService;

    /**
     * it is needed when the scheduler is started, every new runner for scheduler corresponds to new day changing world.
     */
    public void resetServiceData() {
        timeseriesList = new LinkedList<>();
        averageTimeseries = new Timeseries();
        loadResources();
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        loadResources();
    }

    private void loadResources() {
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
            Resource[] resources = resolver.getResources("classpath*:/timeseries/*.csv");

            for (Resource resource : resources) {
                LOGGER.info("Loading CSV file: {}", resource.getFilename());

                // getFile throws exception when file is read from inside of a jar file
                List<Timeseries> fileTimeseries = loadCsvTimeseries(resource.getInputStream());

                List<List<Timeseries>> timeseries = new ArrayList<>(fileTimeseries
                        .stream()
                        .collect(Collectors.groupingBy(el -> fileTimeseries.indexOf(el) / 60))
                        .values()
                );
                timeseriesList.addAll(timeseries);
            }
            createAverage();
            prepareForecast();
        } catch (IOException e) {
            LOGGER.error("Error occurred while loading resources ", e);
        }
    }

    private List<Timeseries> loadCsvTimeseries(InputStream inputStream) {
        try {
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            CsvMapper mapper = new CsvMapper();

            MappingIterator<Timeseries> readValues = mapper.readerFor(Timeseries.class).with(schema).readValues(inputStream);

            return readValues.readAll();
        } catch (Exception e) {
            LOGGER.error("Error occurred while loading object list from inputStream", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void run() {
        List<Timeseries> timeseries = timeseriesList.getFirst();
        if (timeseries.isEmpty()) {
            timeseriesList.removeFirst();
            Timeseries yesterdayTimeseries = timeseriesService.changeStatusFromRealToHistory(averageTimeseries.getSunCoverage(), averageTimeseries.getWindSpeed());
            LOGGER.info("Sending average timeseries {}", yesterdayTimeseries);
            socketService.liveFeedRealWeather(yesterdayTimeseries);
            estimationService.calculatePoints(yesterdayTimeseries.getTimeseriesUUID());
            if (timeseriesList.size() < 6) {
                loadResources();
            } else {
                createAverage();
                prepareForecast();
            }
            timeseries = timeseriesList.getFirst();
            Instant now = Instant.now();
            timeseries.forEach(t -> {
                t.setStartDate(now);
                t.setStatus(TimeseriesStatus.REAL);
            });
        }

        Timeseries real = timeseries.get(0);
        if (real.getTimeseriesUUID() == null) {
            String id = timeseriesRepository.save(real).getTimeseriesUUID();
            Instant now = Instant.now();
            timeseries.forEach(t -> {
                t.setTimeseriesUUID(id);
                t.setStartDate(now);
                t.setStatus(TimeseriesStatus.REAL);
            });
        }

        double total = timeseriesRepository.findById(real.getTimeseriesUUID()).get().getTotal();

        // multiply by 5 seconds to get W/seconds and divide by 60 seconds to get W/min
        double totalIncrement = ((double) real.getPower() * (rate / 1000)) / 60;
        real.setTotal(total + totalIncrement);

        socketService.liveFeedRealWeather(real);
        timeseriesRepository.save(real);
        timeseries.remove(0);
    }

    private void createAverage() {
        List<Timeseries> timeseries = timeseriesList.getFirst();

        Double sunCoverage = timeseries.stream().mapToInt(Timeseries::getSunCoverage).average().getAsDouble();
        Double windSpeed = timeseries.stream().mapToInt(Timeseries::getWindSpeed).average().getAsDouble();

        averageTimeseries.setSunCoverage(sunCoverage.intValue());
        averageTimeseries.setWindSpeed(windSpeed.intValue());
    }

    private void prepareForecast() {
        List<Timeseries> forecasts = IntStream
                .range(1, 5)
                .mapToObj(this::createForcast)
                .collect(Collectors.toList());
        socketService.liveFeedWeatherForecast(forecasts);
    }

    private Timeseries createForcast(int index) {
        List<Timeseries> timeseries = timeseriesList.get(index);

        Timeseries forecast = new Timeseries();
        forecast.setTimeseriesUUID(timeseries.get(0).getTimeseriesUUID());
        forecast.setSequence(timeseries.get(0).getSequence());
        forecast.setStatus(TimeseriesStatus.FORECAST);

        Double sunCoverage = timeseries.stream()
                .mapToInt(Timeseries::getSunCoverage)
                .average().getAsDouble();
        forecast.setSunCoverage(calculateSpread(sunCoverage, index));

        Double windSpeed = timeseries.stream()
                .mapToInt(Timeseries::getWindSpeed)
                .average().getAsDouble();
        forecast.setWindSpeed(calculateSpread(windSpeed, index));

        Timeseries ts = timeseriesRepository.save(forecast);
        timeseries.stream().forEach(t -> {
            t.setTimeseriesUUID(ts.getTimeseriesUUID());
            t.setSequence(ts.getSequence());
            t.setStatus(TimeseriesStatus.FORECAST);
        });

        return forecast;
    }

    private int calculateSpread(Double input, int index) {
        int spread = (int) Math.round(input);
        return ThreadLocalRandom
                .current()
                .nextInt(spread - (spread / 10) * index, (spread + (spread / 10) * index) + 1);
    }

}
