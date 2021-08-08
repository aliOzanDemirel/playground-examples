import {action, autorun, computed, extendObservable, runInAction} from 'mobx';
import * as mobxUtils from "mobx-utils";
import React from 'react';
import {ForecastCell} from "../CommonComponents";
import {actualDateRepresentation, presentActualDay, presentExpoNum, runIfParamIsArray} from "../../common/commons";

export default class EstimationStore {

    // there is now too much unnecessary logic here which can be refactored greatly
    // also loginStore can be a component of this store
    constructor(api) {
        this.api = api;
        this.initialForecastNotLoaded = true;
        this.initialRealDataNotLoaded = true;
        this.realDataIndex = 4;
        this.totalForecastCount = 4;

        extendObservable(this, {
            loggedInUserId: null,
            estimationTableData: [],
            observedForecastTimeseries: {state: null},
            observedHistoryTimeseries: {state: null},
            observedHistoryEstimationAndScore: {state: null},
            loadingHistory: computed(() => {
                return this.observedForecastTimeseries.state !== mobxUtils.FULFILLED ||
                    this.observedHistoryTimeseries.state !== mobxUtils.FULFILLED ||
                    this.observedHistoryEstimationAndScore.state !== mobxUtils.FULFILLED;
            })
        });

        // observe the api call returning to observedHistory fields and fill the table with history data
        autorun('observeTimeseriesAndEstimations', () => {
            if (this.observedHistoryTimeseries.state === mobxUtils.FULFILLED &&
                this.observedHistoryEstimationAndScore.state === mobxUtils.FULFILLED &&
                this.observedForecastTimeseries.state === mobxUtils.FULFILLED) {

                let forecastTimeseries = this.observedForecastTimeseries.value;
                let historyTimeseries = this.observedHistoryTimeseries.value;
                let estimationAndScores = this.observedHistoryEstimationAndScore.value;

                let historyTimeseriesWithEstimations = historyTimeseries.map((timeserie, index) => {

                    let estimation = estimationAndScores.find((el, i, arr) =>
                        el.timeseriesUUID === timeserie.timeseriesUUID);
                    let est = '';
                    let score = 0;

                    if (estimation) {
                        est = estimation.estimation;
                        score = estimation.score;
                    } else {
                        console.log('no estimation is matched with: ' + timeserie.timeseriesUUID);
                    }
                    return this.buildRowData(timeserie.timeseriesUUID,
                        est, score, timeserie, index + 1)
                });

                runInAction('observeTimeseriesAndEstimations', () => {
                    this.estimationTableData = [
                        ...this.generateDummyForecastAndRealWeather(),
                        ...historyTimeseriesWithEstimations
                    ];

                    this.updateForecastData(forecastTimeseries, this.loggedInUserId);

                    this.initialForecastNotLoaded = false;
                })
            }
        })
    }

    initTable = action('initTable', (userDetails) => {

        this.loggedInUserId = userDetails.userId;

        // observedHistory objects will have values 'state' and 'value' when the promise (api call) is resolved
        this.observedHistoryTimeseries = mobxUtils.fromPromise(this.api.getTimeseries(false));
        this.observedHistoryEstimationAndScore = mobxUtils.fromPromise(this.api.getEstimationsOfUser(userDetails.userId));
        this.observedForecastTimeseries = mobxUtils.fromPromise(this.api.getTimeseries(true));
    });

    receiveForecastMessage = (forecastMessage, userDetailsInHeader) => {
        // console.log('receiveForecastMessage: ', forecastMessage);

        runIfParamIsArray(forecastMessage, () => {
            this.updateForecastData(forecastMessage, userDetailsInHeader.userId)
        });
    };

    updateForecastData = action('updateForecastData', (forecastMessage, userId) => {
        // console.log('updateForecastData: ', forecastMessage);

        for (let i = 0; i < forecastMessage.length; i++) {
            let oldIndex = this.totalForecastCount - i - 1;
            let newForecast = forecastMessage[i];
            if (newForecast.status === 'FORECAST') {
                this.estimationTableData[oldIndex] = this.buildRowData(newForecast.timeseriesUUID,
                    '', '', newForecast, 'D+' + (i + 1));

                this.setEstimationAndScoreOfTimeseries(oldIndex, userId, newForecast, 'D+' + (i + 1));
            }
        }
    });

    updateRealData = action('updateRealData', (updatedRealData) => {
        let existingRealData = this.estimationTableData[this.realDataIndex];

        this.estimationTableData[this.realDataIndex] = this.buildRowData(
            updatedRealData.timeseriesUUID,
            existingRealData.estimation,
            existingRealData.score,
            this.getTimeseriesForecast(updatedRealData),
            presentActualDay(updatedRealData, false));

        if (existingRealData.estimation === '') {
            this.setEstimationAndScoreOfTimeseries(this.realDataIndex,
                this.loggedInUserId,
                this.getTimeseriesForecast(updatedRealData),
                presentActualDay(updatedRealData, false));
        }
    });

    handleRealData = action('handleRealData', (liveData, userDetailsInHeader) => {
        // console.log('handleRealData: ', liveData);

        // in final app, it can only be 4 and -1
        let existingDataIndex = this.estimationTableData.findIndex((el, i, arr) =>
            el.uid === liveData.timeseriesUUID);

        // update dummy real data row if it is the first incoming message from live feed
        // or update real data if the message from live feed is already the current real data
        if (this.initialRealDataNotLoaded || existingDataIndex === this.realDataIndex) {

            // in case if forecast message is received before real data message when page is just initialized,
            // prevent updating dummy D with real one while the D+1 forecast already points to incoming real data
            if (this.initialRealDataNotLoaded && existingDataIndex === 3) {
                let forecastToBeRealData = this.estimationTableData[existingDataIndex];
                // console.log('THIS BLOCK SHOULD NEVER EXECUTE');

                this.estimationTableData = [
                    ...this.getUpdatedForecastsUpToIncomingRealData(false),
                    this.buildRowData(liveData.timeseriesUUID,
                        forecastToBeRealData.estimation,
                        forecastToBeRealData.score,
                        this.getTimeseriesForecast(liveData),
                        presentActualDay(liveData, false)),
                    ...this.estimationTableData.slice(this.realDataIndex + 1)
                ];
            } else {
                this.updateRealData(liveData);

                if (this.initialRealDataNotLoaded) {
                    this.initialRealDataNotLoaded = false
                }
            }
        } else {
            let toBeRealData = null;

            // this condition means that the incoming real data is already in the table as forecast D+1 data
            // and now the current real data will be history data while the forecast D+1 switches to be real data
            // this will never happen in latest version of app
            if (existingDataIndex === 3) {
                toBeRealData = this.estimationTableData[existingDataIndex];
            }
            // the incoming real data do not exist so it will replace the current real data
            // this means day is changing
            else if (existingDataIndex === -1) {
                toBeRealData = this.estimationTableData[this.realDataIndex];
            }

            if (toBeRealData) {
                // console.log('toBeRealData: ', toBeRealData);

                // get the first four elements of table which are forecast data
                let forecasts = this.getUpdatedForecastsUpToIncomingRealData(
                    existingDataIndex === -1);

                let historyEntries = this.estimationTableData.slice(this.realDataIndex);
                this.estimationTableData = [
                    ...forecasts,
                    this.buildRowData(liveData.timeseriesUUID,
                        '', '',
                        this.getTimeseriesForecast(liveData),
                        presentActualDay(liveData, false)),
                    ...historyEntries
                ];

                // generate new date values for history data since day has just progressed
                this.updateDateOfHistoryTimeseries();

                let oldRealDataIndex = this.realDataIndex + 1;
                let oldRealData = this.estimationTableData[oldRealDataIndex];
                // console.log('oldRealData: ', oldRealData);

                // update old day's (now history timeseries) score and estimation
                this.setEstimationAndScoreOfTimeseries(oldRealDataIndex,
                    userDetailsInHeader.userId,
                    this.getForecastAndOutputFromRowData(oldRealData),
                    oldRealData.date);

                // update new current day's score and estimation
                this.setEstimationAndScoreOfTimeseries(this.realDataIndex,
                    userDetailsInHeader.userId,
                    this.getTimeseriesForecast(liveData),
                    presentActualDay(liveData, false));

                // update highscore of user
                this.api.getLoggedInUserDetails().then(action.bound(
                    resp => userDetailsInHeader.highScore = presentExpoNum(resp.highScore)))
            }
        }
    });

    setEstimationAndScoreOfTimeseries = (indexToUpdate, userId, forecastObj, date) => {
        // console.log('Updating estimation for ' + forecastObj.timeseriesUUID);

        this.api.getEstimationsOfUser(userId, forecastObj.timeseriesUUID)
            .then(action.bound(resp => {
                let estResp = resp[0];
                if (estResp) {
                    // console.log('Estimation returned: ', estResp);
                    // let oldForecast = this.estimationTableData[oldIndex];
                    // if (oldForecast.uid === resp.timeseriesUUID) {
                    //     oldForecast.estimation = resp.estimation;
                    //     oldForecast.score = resp.score;
                    // }
                    this.estimationTableData[indexToUpdate] = this.buildRowData(estResp.timeseriesUUID,
                        estResp.estimation, estResp.score, forecastObj, date)
                }
            }));
    };

    getUpdatedForecastsUpToIncomingRealData = (isIncomingRealDataNewInTable) => {

        // incoming real data is not from forecast data so the existing forecasts will stay as is
        if (isIncomingRealDataNewInTable) {
            return this.estimationTableData.slice(0, 4)
        }
        // forecast D+1 is being updated, so a new dummy D+4 will be added while the days are shifted towards D
        else {

            this.estimationTableData[0].date = 'D+3';
            this.estimationTableData[1].date = 'D+2';
            this.estimationTableData[2].date = 'D+1';
            if (this.initialForecastNotLoaded) {
                this.estimationTableData[0].uid = 1;
                this.estimationTableData[1].uid = 2;
                this.estimationTableData[2].uid = 3
            }

            return [this.getDummyRowData(0), this.estimationTableData[0],
                this.estimationTableData[1], this.estimationTableData[2]]
        }
    };

    getForecastAndOutputFromRowData = (rowData) => {
        let forecastCellData = rowData.forecast.props.timeseries;
        return this.getTimeseriesForecast(forecastCellData);
    };

    getTimeseriesForecast = (timeseriesData) => {
        return {
            timeseriesUUID: timeseriesData.timeseriesUUID,
            sunCoverage: timeseriesData.sunCoverage,
            windSpeed: timeseriesData.windSpeed,
            power: timeseriesData.power,
            total: timeseriesData.total
        }
    };

    buildRowData = (uid, estimationVal, scoreVal, timeseries, dateOldness) => {
        return {
            uid: uid,
            date: typeof dateOldness === 'string' ? dateOldness : 'D-' + Math.abs(dateOldness),
            estimation: estimationVal,
            score: presentExpoNum(scoreVal),
            output: timeseries.total && Math.round(timeseries.total),
            forecast: <ForecastCell timeseries={timeseries}/>
        }
    };

    updateDateOfHistoryTimeseries = action('updateDateOfHistoryTimeseries', () => {
        this.estimationTableData = this.estimationTableData.map((rowData, index) => {
            let dateOldness = rowData.date;
            if (index > this.realDataIndex) {
                dateOldness = index - this.realDataIndex
            }

            return this.buildRowData(rowData.uid, rowData.estimation, rowData.score,
                this.getForecastAndOutputFromRowData(rowData), dateOldness)
        })
    });

    generateDummyForecastAndRealWeather = () => {
        let dummyDatas = [];
        for (let index = 0; index < this.totalForecastCount + 1; index++) {
            dummyDatas.push(this.getDummyRowData(index))
        }
        return dummyDatas;
    };

    getDummyRowData = (index) => {
        return {
            uid: index,
            date: index === 4 ? actualDateRepresentation : 'D+' + Math.abs(index - this.totalForecastCount),
            estimation: '',
            score: 0,
            output: 0,
            forecast: <ForecastCell
                timeseries={{timeseriesUUID: index, windSpeed: 0, sunCoverage: 0, power: 0, total: 0}}/>
        }
    };

    saveEstimation = action('saveEstimation', (userId, timeseriesId, newVal) => {
        return this.api.saveEstimation({
            estimation: newVal,
            userUUID: userId,
            timeseriesUUID: timeseriesId
        }).then(respEstimationId => {
            // console.log('Saved estimation:', respEstimationId);

            if (respEstimationId) {
                let timeserieIndex = this.estimationTableData.findIndex((el, i, arr) => el.uid === timeseriesId);
                this.estimationTableData[timeserieIndex].estimation = newVal;
                return true;
            } else {
                return false;
            }
        })
    });

}