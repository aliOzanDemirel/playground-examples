package app.service;

import app.domain.Timeseries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SocketService {

    private static final Logger log = LoggerFactory.getLogger(SocketService.class);

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private UserService userService;

    // @SendTo ("/topic/timeseries/real")
    public void liveFeedRealWeather(Timeseries timeseries) {
        log.info("Feeding real: {}", timeseries);
        template.convertAndSend("/topic/timeseries/real", timeseries);
    }

    // @SendTo ("/topic/timeseries/forecast")
    public void liveFeedWeatherForecast(List<Timeseries> timeseries) {
        log.info("Feeding forecast:\n{}", timeseries);
        template.convertAndSend("/topic/timeseries/forecast", timeseries);
    }

    // @SendTo ("/topic/users/scores")
    public void liveFeedUserScores() {
        log.info("Feeding user scores");
        template.convertAndSend("/topic/users/scores", userService.getHighScores());
    }

}
