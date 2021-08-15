import * as rp from 'request-promise';
import {badNews} from "./notifier-service";

const BACKEND_URL = process.env.NODE_ENV === 'development' ? 'https://localhost:443/admin' : '';

// though this should not have any effect on same-origin requests
const withCredentials = process.env.NODE_ENV === 'development';

// this store should be the same loginStore used in login component
let loginStore = null;

export function setLoginStore(loginStoreToPass) {
    loginStore = loginStoreToPass;
}

// generate a random number and set to cookie that expires after a year
// this generated token will also be sent via header in every request for server to check both exist and match
let generatedCookie = Math.floor(Math.random() * 10000000);
document.cookie = 'csrf-protected=' + generatedCookie + '; max-age=' + 60 * 60 * 24 * 365;

// resolveWithFullResponse: to avoid getting just the response body to promise chain
// simple: to avoid the behaviour of catch block in request-promise library (catches <200 && >=300 by default)
const request = rp.defaults({
    baseUrl: BACKEND_URL,
    withCredentials: withCredentials,
    resolveWithFullResponse: true,
    simple: false,
    headers: {
        'csrf-token': generatedCookie,
        'accept': 'application/json, application/vnd.ms-excel, text/csv'
    }
});

// add 'json: true' as well as 'body: object' to options if you want to submit json, request will parse 'body' as json.
// this method will post form if no json property is found. if the 'bodyObject' is json, form body will be parsed and
// sent as urlencoded instead of multipart data. also request will add appropriate headers while posting form and json.
function doPost(options, bodyObject) {
    let opt;
    if (options.json) {
        opt = {
            body: bodyObject,
        }
    } else {
        opt = {
            form: bodyObject
        }
    }
    return doRequest({
        ...options,
        ...opt,
        method: 'POST'
    });
}

function doGet(options) {
    return doRequest({
        ...options,
        method: 'GET'
    });
}

function doGetWithJsonBody(options, bodyObject) {
    return doRequest({
        ...options,
        body: bodyObject,
        json: true,
        method: 'GET'
    });
}

// TODO: this can return a promise to be resolved immediately
function postFormDataWithVanillaAjax(url, formData, successCallback) {
    let xhr = new XMLHttpRequest();
    xhr.open('POST', BACKEND_URL + url, true);
    xhr.withCredentials = true;
    xhr.setRequestHeader('csrf-token', generatedCookie);

    xhr.onerror = () => {
        handleError(xhr.status, xhr.response.url, 'Failed while posting file!');
    };
    xhr.onloadend = (e) => {
        if (xhr.status >= 200 && xhr.status < 300) {
            try {
                let json = JSON.parse(xhr.responseText);
                successCallback(json);
            } catch (ex) {
                console.error("Response is not JSON convertible!")
            }
        } else {
            handleError(xhr.status, xhr.response.url, 'Failed while posting file!');
        }
    };

    xhr.send(formData);
}

export {doGet, doPost, doGetWithJsonBody, postFormDataWithVanillaAjax};

const doRequest = (options) => {
    return request(options).catch(caughtError).then(checkStatus);
};

const checkStatus = (response) => {

    let body = response.body ? (typeof response.body === 'string' ? JSON.parse(response.body) : response.body) : true;

    // there can be multiple servers in backend, if all servers failed to execute request,
    // promise will be rejected after a notification of requested endpoint
    let successfulResponses = [];
    if (response.statusCode === 207) {
        successfulResponses = handleMultipleResponses(body);
    } else if (response.statusCode >= 200 && response.statusCode < 300) {
        return body;
    }

    if (successfulResponses.length === 0) {
        return handleError(response.statusCode, response.url, body.message);
    } else {
        return successfulResponses;
    }
};

// takes multiple AdminResponses and returns response bodies of servers that executed request successfully
const handleMultipleResponses = (responses) => {
    return responses.reduce((successfuls, resp) => {
        if (resp.status < 200 || resp.status >= 300) {
            badNews(resp.respBody.message, resp.status + ' returned from ' + resp.respBody.serverIP, 0);
        } else {
            successfuls.push(resp.respBody)
        }
        return successfuls;
    }, []);
};

const caughtError = (error) => {
    console.error("Caught error!", error);
    return handleError(400, error.options.url, error.message);
};

const handleError = (status, url, message) => {
    badNews(message, (url ? url : 'Unknown action') + ' returned ' + status);

    if (status === 401) {
        if (loginStore.isLoggedIn) {
            loginStore.logout();
        }
    }

    // we don't have to check if there is a valid response in rest of the promise chain if we just reject it
    return Promise.reject(message);
};