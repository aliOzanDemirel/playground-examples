package app.service;

import app.domain.Estimation;
import app.domain.Timeseries;
import app.domain.TimeseriesStatus;
import app.repository.EstimationRepository;
import app.repository.TimeseriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EstimationService {

    @Value("#{T(Double).parseDouble('${app.price.mwhczk}')}")
    private Double priceMwhCzk;

    @Autowired
    private EstimationRepository estimationRepository;

    @Autowired
    private TimeseriesRepository timeseriesRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SocketService socketService;

    public List<Estimation> getEstimationsByUserId(String id) {
        return estimationRepository.findByUserUUID(id).orElse(new ArrayList<>());
    }

    public Estimation getEstimationByUserAndTimeseriesId(String userUUID, String timeseriesUUID) {
        return estimationRepository.findByUserUUIDAndTimeseriesUUID(userUUID, timeseriesUUID).orElse(null);
    }

    public Estimation createEstimation(Estimation estimation) {
        timeseriesRepository
                .findById(estimation.getTimeseriesUUID())
                .filter(ts -> ts.getStatus().equals(TimeseriesStatus.FORECAST))
                .orElseThrow(() -> new IllegalArgumentException("Timeseries " + estimation.getTimeseriesUUID() + " is not forecast."));

        Optional<Estimation> currentEstimation = estimationRepository.findByUserUUIDAndTimeseriesUUID(estimation.getUserUUID(), estimation.getTimeseriesUUID());
        String id = null;
        if (currentEstimation.isPresent()) {
            id = currentEstimation.get().getEstimationUUID();
        }
        estimation.setEstimationUUID(id);
        return estimationRepository.save(estimation);
    }

    public void calculatePoints(String id) {

        List<Estimation> estimations = estimationRepository.findByTimeseriesUUID(id).orElse(new ArrayList<>());
        Timeseries timeseries = timeseriesRepository.findById(id).get();
        double total = timeseries.getTotal();

        for (Estimation est : estimations) {
            double diff = Math.abs(est.getEstimation() - total);
            double percent = Math.max(0, 1 - diff / total);

            // from MWh/Czk to mW/min/Czk
            double wminCzk = priceMwhCzk / 1000 / 1000 / 1000 / 60;
            double score = (total * percent) * wminCzk;
            est.setScore(score);

            userService.newHighScore(est.getUserUUID(), score);

            estimationRepository.save(est);
        }

        socketService.liveFeedUserScores();
    }

    public void cleanEstimations() {
        estimationRepository.deleteAll();
    }

}
