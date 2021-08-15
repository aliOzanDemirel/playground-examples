import * as rp from 'request-promise';
import {backendUrl, badNews, parseJsonOrGetString} from "./commons";

// resolveWithFullResponse: to avoid getting just the response body to promise chain
// simple: to avoid the behaviour of catch block in request-promise library (catches <200 && >=300 by default)
const request = rp.defaults({
    baseUrl: backendUrl,
    resolveWithFullResponse: true,
    simple: false
    // headers: {
    //     'accept': 'application/json, text/css'
    // },
});

function doGet(options, urlQueryObject) {
    return doRequest({
        ...options,
        method: 'GET'
    }, urlQueryObject);
}

function doDelete(options, urlQueryObject) {
    return doRequest({
        ...options,
        method: 'DELETE'
    }, urlQueryObject);
}

export {doGet, doDelete};

const doRequest = (options, urlQueryObject) => {

    if (urlQueryObject && typeof urlQueryObject === "object") {
        options = {
            ...options,
            qs: urlQueryObject
        }
    }
    return request(options).catch(caughtError).then(checkStatus);
};

const checkStatus = (response) => {

    let body = response.body
        ? (typeof response.body === 'string' ? parseJsonOrGetString(response.body) : response.body)
        : null;

    if (response.statusCode >= 200 && response.statusCode < 300) {

        return body;
    }
    return handleError(response.statusCode, response.url, body ? body.message : '');
};

const caughtError = (error) => {
    console.error("Error in request!", error);
    return handleError(400, error.options.url, error.message);
};

const handleError = (status, url, message) => {
    badNews(message ? message : (url ? url : 'Unknown action') + ' returned ' + status);

    // we don't have to check if there is a valid response in rest of the promise chain if we just reject it
    return Promise.reject(message);
};
