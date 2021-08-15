import {action, extendObservable} from 'mobx';
import {goodNews, warn} from "../../../common/service/notifier-service";

export default class ReportMailerStore {

    constructor(api) {
        this.api = api;

        extendObservable(this, {
            inputExecutionDate: null,
            inputOrderReportSendStatus: null
        });
    };

    resetFields = action('resetFields', () => {
        this.inputExecutionDate = null;
        this.inputOrderReportSendStatus = null;
    });

    onDateChange = action('onDateChange', (momentDate, dateString) => {
        this.inputExecutionDate = dateString;
    });

    sendMailSubscriptionDetails = () => {
        this.api.postMailTriggerOrderReport().then(body => goodNews(body.message));
    };

    downloadSubscriptionReport = () => {
        if (this.inputExecutionDate === null) {
            warn('Select a date');
        } else if (this.inputOrderReportSendStatus === null) {
            warn('Select send status');
        } else {
            this.api.getDownloadSubscriptionReport({
                executionDate: this.inputExecutionDate,
                status: this.inputOrderReportSendStatus
            });
        }
    };

    sendOrderReportMailToSalesRep = (formValues) => {
        this.api.postMailSalesRep(formValues).then(body => goodNews(body.message));
    };

}