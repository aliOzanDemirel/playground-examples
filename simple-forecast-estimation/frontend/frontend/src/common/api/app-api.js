import {doGet, doPost} from "../request-service";
import {loginActionEndpoint} from "../commons";

export default class AppApi {

    // returns {
    //     "userUUID": "490f",
    //     "sequence": 1
    //     "name": "Ondřej Lapáček",
    //     "username": "lapacek.ondrej@gmail.com",
    //     "highScore": 125
    // }
    getLoggedInUserDetails = () => {
        return doGet({
            url: '/users/self'
        });
    };

    // formValuesJson {
    //     "username": "lapacek.ondrej@gmail.com",
    //     "password": "************"
    // }
    // returns logged in user's details and JWT in a response header 'Authorization'
    login = (formValuesJson) => {
        return doPost({
            url: loginActionEndpoint,
            json: true
        }, formValuesJson, true)
    };

    // returns true or false, no object
    queryEmail = (emailVal) => {
        return doGet({
            url: '/users'
        }, {username: emailVal}, true);
    };

    // formValuesJson {
    //     "name": "Ondřej Lapáček",
    //     "username": "lapacek.ondrej@gmail.com",
    //     "password": "*****************"
    // }
    saveUser = (formValuesJson) => {
        return doPost({
            url: '/users/sign-up',
            json: true
        }, formValuesJson, true)
    };

    // jsonBody {
    //     timeseriesUUID: 'some uuid',
    //     userUUID: 'some uuid'
    //     estimation: 10
    // }
    saveEstimation = (jsonBody) => {
        return doPost({
            url: '/users/' + jsonBody.userUUID + '/estimations',
            json: true
        }, jsonBody)
    };

    // returns [{
    //     "timeseriesUUID": "a76ac268-be63-490f-8fdd-29bab6c29f60",
    //     "sunCoverage": 5,
    //     "windSpeed": 15,
    //     "power": 30,
    //     "total": 50
    // }]
    // onlyForecast: true for only forecast, false for only history
    getTimeseries = (onlyForecast) => {
        return doGet({
            url: '/timeseries'
        }, {forecast: onlyForecast}, true);
    };

    // returns one or more estimations: {
    //     "estimationUUID": "a76ac268-be63-490f-8fdd-29bab6c29f61",
    //     "timeseriesUUID": "a76ac268-be63-490f-8fdd-29bab6c29z61",
    //     "userUUID": "a76ac268-be63-490f-8fdd-29bab6c29z61",
    //     "estimation": 222,
    //     "score": 333
    // }
    getEstimationsOfUser = (userId, timeseriesId) => {
        let queryParams = {};
        if (timeseriesId) {
            queryParams = {timeseriesId: timeseriesId}
        }
        return doGet({
            url: '/users/' + userId + '/estimations'
        }, queryParams);
    };

    // returns [{
    //     "sequence": "10",
    //     "highscore": 120
    // }]
    getHighScores = () => {
        return doGet({
            url: '/users/highscores'
        }, null, true);
    };

}
