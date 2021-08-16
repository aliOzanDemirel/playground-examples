var headersObjForCSRF = function () {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    var headers = {};
    headers[header] = token;
    return headers;
};

var postJSON = function (url, jsonObj, successCallback, failCallback) {
    return $.post({
        url: url,
        data: JSON.stringify(jsonObj),
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        headers: headersObjForCSRF(),
        success: successCallback,
        error: failCallback
    })
};

var postFormData = function (url, dataThatWillBeEncoded, successCallback, failCallback) {
    $.post({
        url: url,
        data: dataThatWillBeEncoded,
        contentType: "application/x-www-form-urlencoded; charset=UTF-8",
        headers: headersObjForCSRF(),
        success: successCallback,
        error: failCallback
    })
};

var loadAlertFromServer = function (alertType, alertMessage) {
    $('#responseBlock').load('/alert/type/' + alertType + '/message/' + encodeURIComponent(alertMessage))
};

var formatDateForSpring = function (date) {
    return date.toISOString().substr(0, 10)
};