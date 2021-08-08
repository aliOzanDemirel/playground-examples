import * as rp from 'request-promise';
import {
    appStorage,
    backendUrl,
    JWT_HEADER_KEY,
    loginActionEndpoint,
    notifyError,
    parseJsonOrGetString
} from "./commons";

// resolveWithFullResponse: to avoid getting just the response body to promise chain
// simple: to avoid the behaviour of catch block in request-promise library (catches <200 && >=300 by default)
const request = rp.defaults({
    baseUrl: backendUrl,
    withCredentials: process.env.NODE_ENV === 'development',
    resolveWithFullResponse: true,
    simple: false,
    headers: {
        'accept': 'application/json, text/css'
    },
    jar: true
});

let loginStore;

export function setLoginStoreInRequestService(actualLoginStore) {
    loginStore = actualLoginStore;
}

// add 'json: true' as well as 'body: object' to options if you want to submit json, request will parse 'body' as json.
// this method will post form if no json property is found. if the 'bodyObject' is json, form body will be parsed and
// sent as urlencoded instead of multipart data. also request will add appropriate headers while posting form and json.
function doPost(options, bodyObject, disableAuth = false) {
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
    }, disableAuth);
}

function doGet(options, urlQueryObject, disableAuth = false) {
    let opt = {
        ...options,
        method: 'GET'
    };
    if (urlQueryObject && typeof urlQueryObject === "object") {
        opt = {
            ...opt,
            qs: urlQueryObject
        }
    }
    return doRequest(opt, disableAuth);
}

export {doGet, doPost};

const doRequest = (options, disableAuth) => {
    if (!disableAuth) {
        let jwt = appStorage.getItem(JWT_HEADER_KEY);
        options.auth = {
            bearer: jwt && jwt.slice(7)
        }
    }
    return request(options).catch(caughtError).then(checkStatus);
};

const checkStatus = (response) => {

    // console.log('checkStatus response: ', response);
    let body = response.body
        ? (typeof response.body === 'string' ? parseJsonOrGetString(response.body) : response.body)
        : null;
    // console.log('checkStatus body: ', body);

    if (response.statusCode >= 200 && response.statusCode < 300) {

        // save JWT when logged in
        if (response.request.path === loginActionEndpoint) {
            loginStore.updateUserToken(response.caseless.get(JWT_HEADER_KEY))
        }

        return body;
    }
    return handleError(response.statusCode, response.url, body ? body.message : '');
};

const caughtError = (error) => {
    console.error("Error in request!", error);
    return handleError(400, error.options.url, error.message);
};

const handleError = (status, url, message) => {
    notifyError(message ? message : (url ? url : 'Unknown action') + ' returned ' + status);

    // if any request returns 401, remove the existing token which also redirects to login
    if (status === 401 || status === 403) {
        loginStore.logout();
    }

    // we don't have to check if there is a valid response in rest of the promise chain if we just reject it
    return Promise.reject(message);
};