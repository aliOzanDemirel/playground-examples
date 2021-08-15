import {action, extendObservable} from 'mobx';
import {goodNews} from "../../../common/service/notifier-service";

export default class LoginManagementStore {

    constructor(api) {
        this.api = api;

        // this is for login report panel, no need to have observable values
        this.inputAuditStartDate = null;
        this.inputAuditEndDate = null;

        extendObservable(this, {
            initialIsSecureConnection: null,
            initialEncryptionKey: null,
            initialTimeDifference: null,
            initialTimeDifferenceDelta: null
        });
    };

    onAuditDatesChange = (moments, dateStrings) => {
        this.inputAuditStartDate = dateStrings[0];
        this.inputAuditEndDate = dateStrings[1];
    };

    downloadLoginAudits = () => {
        this.api.getDownloadLoginAudits({
            startDate: this.inputAuditStartDate,
            endDate: this.inputAuditEndDate
        })
    };

    getRememberMeDetails = action('getRememberMeDetails', () => {
        this.api.getRememberMeOptions().then(action.bound(resp => {
                this.initialIsSecureConnection = resp.isSecureConnection;
                this.initialEncryptionKey = resp.encryptionKey;
                this.initialTimeDifference = resp.timeDifference;
                this.initialTimeDifferenceDelta = resp.timeDifferenceDelta;
            })
        );
    });

    updateRememberMeDetails = action('updateRememberMeDetails', formValues => {
        console.log(formValues);
        this.api.postRememberMeOptions(formValues).then(respBodies => {
            respBodies.forEach(it => goodNews('Updated remember me details in ' + it.serverIP))
        });
    });

    onPanelChange = action('onPanelChange', activePanelKey => {
        if (activePanelKey === 'rememberMeUpdate') {
            this.getRememberMeDetails();
        }
    });

}