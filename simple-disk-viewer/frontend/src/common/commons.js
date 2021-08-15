import {toast} from 'react-toastify';

// react env variable is overridden if it is empty and baseUrl of request.js library does not work with only context
export const backendUrl = process.env.REACT_APP_BACKEND_URL;
export const EventType = Object.freeze({CREATED: 'Created', MODIFIED: 'Modified', DELETED: 'Deleted'});

export const pages = {
    directoryMainPage: '/',
    directoryDetailsPage: '/tree'
};

export function goodNews(desc, duration) {
    toast(desc ? desc : 'Success', {
        type: toast.TYPE.SUCCESS,
        position: toast.POSITION.TOP_RIGHT,
        autoClose: duration ? duration : 2000,
        closeOnClick: true,
        hideProgressBar: true
    })
}

export function badNews(desc, duration) {
    toast(desc ? desc : 'Unknown error happened.', {
        type: toast.TYPE.ERROR,
        position: toast.POSITION.TOP_RIGHT,
        autoClose: duration ? duration : 2000,
        closeOnClick: true,
        hideProgressBar: true
    })
}

export function parseToDateAndTime(epochMillis) {

    const resp = {
        date: '',
        time: ''
    };
    if (epochMillis) {
        const date = new Date(Number(epochMillis));
        resp.date = date.toLocaleDateString();
        resp.time = date.toLocaleTimeString()
    }
    return resp;
}

export function booleanToYesNo(bool) {
    if (bool === true) {
        return 'yes'
    } else if (bool === false) {
        return 'no'
    } else {
        return 'invalid'
    }
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

export function comparatorSorter(first, second, objectKey) {
    if (first[objectKey] < second[objectKey]) {
        return -1
    } else if (first[objectKey] > second[objectKey]) {
        return 1
    }
    return 0
}

export function getEventSource(path) {

    let eventSource = new EventSource(backendUrl + "/observe?path=" + path);

    eventSource.addEventListener('open', function (e) {
        goodNews(`subscribed to file events for ${path}`)
    }, false);

    eventSource.addEventListener('message', function (e) {
        console.warn('unexpected event:', e);
    }, false);

    eventSource.addEventListener('error', function (e) {
        if (e.readyState === EventSource.CLOSED) {
            console.info('closed', e);
        } else {
            console.error('Error occurred, event source will be closed!', e);
            eventSource.close()
        }
    }, false);

    return eventSource
}

// hacky way to get parameter 'path' from old tab, react-router-dom does not pass state to new tab
export function getPathParameter(queryString) {
    let toBeExtracted = '?path=';
    let index = queryString.indexOf(toBeExtracted);
    if (index !== -1) {
        const decodedQs = queryString.substring(index + toBeExtracted.length);
        return decodeURIComponent(decodedQs.replace(/\+/g, '%20'));
    }
    return null
}
