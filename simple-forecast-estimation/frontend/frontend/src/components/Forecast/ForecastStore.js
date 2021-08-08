import {action, extendObservable, observable} from 'mobx';
import {presentExpoNum, runIfParamIsArray, totalTimeOfContestInMillis} from "../../common/commons";

export default class ForecastStore {

    constructor(api) {
        this.api = api;
        this.startTime = null;
        this.endTime = null;

        extendObservable(this, {
            progressVal: 0,
            highScoreTableData: [],
            forecastMetrics: {},
            realMetrics: {}
        });
    }

    initializeForecastMetrics = () => {
        this.api.getTimeseries(true).then(forecasts => {
            this.updateForecastMetrics(forecasts.slice(0, forecasts.length > 4 ? 4 : forecasts.length))
        })
    };

    initializeHighScoreList = () => {
        this.api.getHighScores().then(this.updateHighScores)
    };

    updateRealValues = action('updateRealValues', (realValueMessage) => {
        // console.log('updateRealValues: ', realValueMessage);

        if (this.realMetrics.timeseriesUUID !== realValueMessage.timeseriesUUID) {
            if (realValueMessage.startDate) {
                this.resetTimer(realValueMessage.startDate);
            } else {
                this.resetTimer(new Date().getTime());
            }
        }
        this.realMetrics = realValueMessage;
    });

    updateForecastMetrics = action('updateForecastMetrics', (forecastMessage) => {
        // console.log('updateForecastMetrics: ', forecastMessage);

        runIfParamIsArray(forecastMessage, () => {
            this.forecastMetrics = forecastMessage.reduce(function (objToRender, forecast, index) {
                objToRender['D+' + (index + 1)] = forecast;
                return objToRender;
            }, {});
        });
    });

    updateHighScores = action('updateHighScores', (scoresMessage) => {
        // console.log('updateHighScores: ', scoresMessage);

        runIfParamIsArray(scoresMessage, () => {
            scoresMessage.forEach(it => it.highscore = presentExpoNum(it.highscore));
            this.highScoreTableData = observable.array(scoresMessage);
        });
    });

    resetTimer = action('resetTimer', (startTimeInMillis) => {
        this.startTime = startTimeInMillis;
        this.endTime = this.startTime + totalTimeOfContestInMillis;
        this.progressVal = 0;
    });

    updateProgress = action('updateProgress', () => {
        if (this.startTime && this.endTime) {
            let passed = Date.now() - this.startTime;
            this.progressVal = Math.floor((passed / totalTimeOfContestInMillis) * 100);
            if (this.progressVal > 100) {
                this.resetTimer();
            }
        }
    });

}