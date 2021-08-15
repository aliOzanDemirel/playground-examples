import {action, runInAction} from 'mobx';
import {warn} from "./service/notifier-service";

// generic function to update input values in controlled components
export function onFieldChange(store, field, event) {
    action('onFieldChange', () => {
        store[field] = event.target.value;
    })();
}

// table object should have property 'selRows'
export function updateSelectedRows(table, selectedRows) {
    runInAction('updateSelectedRows', () => {
        if (typeof table.selRows !== 'undefined') {
            table.selRows = selectedRows;
        }
    });
}

export function isValidNumeric(num) {
    return num && !isNaN(num) && num.length !== 0;
}

export function getFormDataFromJson(formAsJson) {
    let formData = new FormData();
    Object.keys(formAsJson).forEach(key => formData.append(key, formAsJson[key]));
    return formData;
}

export function checkSelection(selRows, multipleNotAllowed, noSelectionMsg, multipleSelectionMsg, equalityKey) {
    if (selRows.length === 0) {
        warn(noSelectionMsg ? noSelectionMsg : 'You did not select any row');
    } else if (selRows.length > 1 && multipleNotAllowed) {

        if (equalityKey) {
            // selection is not multiple if objects are considered equal
            let allShouldBeSame = selRows[0][equalityKey];
            if (selRows.every(it => it[equalityKey] === allShouldBeSame)) {
                return true;
            }
        }
        warn(multipleSelectionMsg ? multipleSelectionMsg : 'You should select only one row');
    } else {

        // means selected rows count is valid for action
        return true;
    }
    return false;
}

// sorts by first the given key then the serverIP
export function sortTable(first, second, key) {
    if (first[key] < second[key]) {
        return -1
    } else if (first[key] > second[key]) {
        return 1
    }
    if (first.serverIP < second.serverIP) {
        return -1
    } else if (first.serverIP > second.serverIP) {
        return 1
    }
    return 0
}

export const panelStyle = {
    background: '#f7f7f7',
    borderRadius: 6,
    marginBottom: 18,
    border: 0,
    overflow: 'hidden'
};

export const defaultBackendDateFormat = 'YYYY-MM-DD';

export const defaultPage = {
    url: '/admin/dashboard/config-uploader',
    menuKey: '8'
};