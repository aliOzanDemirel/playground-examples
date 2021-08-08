import {toast} from 'react-toastify';

export const COOKIE_NOTICE_PAGE = 'https://read-cookie-notice';
export const GDPR_NOTICE_PAGE = 'http://read-gdpr-notice';

export const JWT_COOKIE_NAME = 'JWT';
export const JWT_HEADER_KEY = 'Authorization';
export const appStorage = window.localStorage;

// react env variable is overriden if it is empty and baseUrl of request.js library does not work with only context /
export const backendUrl = process.env.REACT_APP_BACKEND_URL;
export const websocketUrl = backendUrl + '/ws';
export const totalTimeOfContestInMillis = process.env.REACT_APP_CONTEST_DURATION * 60 * 1000;
export const actualDateRepresentation = 'D';
export const loginActionEndpoint = "/users/sign-in";

export const pages = {
    login: '/',
    signUp: '/sign-up',
    estimation: '/estimation',
    forecast: '/forecast'
};

// returns timeseries: {
//     "timeseriesUUID": "a76ac268-be63-490f-8fdd-29bab6c29f60",
//     "sunCoverage": 5,
//     "windSpeed": 15,
//     "power": 30
// }
export const liveFeedRealData = '/topic/timeseries/real';

// returns list of 4 different timeseries
export const liveFeedForecast = '/topic/timeseries/forecast';

// returns [{
//     "sequence": "10",
//     "highscore": 120
// }]
export const liveFeedHighScores = '/topic/users/scores';

export function notifyError(desc, duration) {
    toast(desc ? desc : 'Unknown error happened.', {
        type: toast.TYPE.ERROR,
        // position: toast.POSITION.BOTTOM_CENTER,
        position: toast.POSITION.TOP_CENTER,
        autoClose: duration ? duration : 1500,
        closeOnClick: true,
        hideProgressBar: true
    })
}

export function getRandomInt(min, max) {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function isValidNumeric(num) {
    return num && !isNaN(num) && num.length !== 0;
}

export function isMinusZero(value) {
    return 1 / value === -Infinity;
}

// only forecast data should be editable, dummy forecast will not be editable hence the uid check
export function isEstimationRowEditable(row) {
    return !row.score && (row.date && row.date.includes('+')) &&
        (row.uid !== 0 && row.uid !== 1 && row.uid !== 2 && row.uid !== 3);
}

export function parseJsonOrGetString(jsonString) {
    try {
        const jsonObj = JSON.parse(jsonString);
        if (jsonObj && typeof jsonObj === "object") {
            return jsonObj;
        }
    } catch (e) {
    }
    return jsonString;
}

export function removeCookieByName(name) {
    document.cookie = name + "=; path:/ expires=Thu, 01 Jan 1970 00:00:00 GMT";
}

export function runIfParamIsArray(param, callback) {
    if (Array.isArray(param)) {
        callback();
    } else {
        console.error('Received response is not an array!');
        notifyError('An error occured!')
    }
}

export function presentActualDay(realDayData, withDaySequence) {
    let daySequence = withDaySequence ? (realDayData.sequence ? '(' + realDayData.sequence + ')' : '') : '';
    return actualDateRepresentation + daySequence;
}

export function presentExpoNum(num, digits) {
    return num ? Number.parseFloat(num).toExponential(digits ? digits : 2) : 0
}