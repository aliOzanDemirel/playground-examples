import {getRandomInt} from "../commons";

export default class MockApi {

    responseBody = objToReturn => {
        return new Promise((resolve, reject) => {
            resolve(objToReturn);
        }).catch((err) => {
            console.error('error in mock service: ', err);
        });
    };

    fullResponse = body => this.responseBody({
        statusCode: 200,
        body: JSON.stringify(body)
    });

    getLoggedInUserDetails = () => this.responseBody({
        token: 'sth123sth456',
        usernameUUID: getRandomInt(1, 100),
        sequence: getRandomInt(1, 20),
        name: 'mock-user',
        username: 'mock@mail.com',
        highScore: getRandomInt(1, 300)
    });

    login = formValues => this.responseBody({
        token: 'sth123sth456',
        usernameUUID: getRandomInt(1, 100),
        sequence: getRandomInt(1, 20),
        name: 'mock-user',
        username: 'mock@mail.com',
        highScore: getRandomInt(1, 300)
    });

    queryEmail = emailVal => this.responseBody({
        exists: false
    });

    saveUser = jsonBody => this.responseBody({});
    saveEstimation = jsonBody => this.responseBody({
        estimationId: getRandomInt(1, 300)
    });

    getTimeseries = (onlyForecast) => {
        const arr = [];
        for (let i = 0; i < 11; i++) {
            arr.push({
                timeseriesUUID: 'UID' + i,
                sun: getRandomInt(i, 100),
                wind: getRandomInt(i, 100),
                power: getRandomInt(i, 200),
                total: getRandomInt(i, 200)
            });
        }
        return this.responseBody(arr);
    };

    getEstimationsOfUser = userId => {
        const arr = [];
        for (let i = 0; i < 11; i++) {
            arr.push({
                userUUID: 'UID' + i,
                estimationUUID: 'EST' + i,
                timeseriesUUID: 'TIM' + i,
                estimation: getRandomInt(i, 200),
                score: getRandomInt(i, 200)
            });
        }
        return this.responseBody(arr);
    };

    getHighScores = () => this.responseBody({
        sequence: getRandomInt(1, 30),
        highscore: getRandomInt(1, 200)
    });

}