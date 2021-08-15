import {action, extendObservable, runInAction} from 'mobx';
import {warn} from "../../../common/service/notifier-service";
import {isValidNumeric} from "../../../common/commons";

export default class ErrorSearchStore {

    constructor(api) {
        this.api = api;

        extendObservable(this, {
            inputErrorId: null,
            sysErrorMsg: null,
            sysErrorDate: null,
            searchLoading: false,
            table: {
                loading: false,
                data: []
            }
        });
    };

    listLastErrors = () => {
        runInAction('startLoadingTable', () => this.table.loading = true);

        this.api.getLastErrors().then(action('listLastErrorsHandler',
            body => this.table.data = body.map(data => {
                return {
                    key: data.id,
                    id: data.id,
                    request: data.request,
                    message: data.message ? data.message.trim() : 'NO MESSAGE RECORDED',
                    errorDate: new Date(data.errorDate).toLocaleString('tr-TR')
                }
            })
        )).finally(action('finishLoadingTable', () => {
            this.table.loading = false
        }));
    };

    getError = () => {
        if (isValidNumeric(this.inputErrorId)) {

            runInAction('startLoadingTable', () => this.searchLoading = true);

            this.api.getError(this.inputErrorId).then(action('getError', body => {
                this.sysErrorMsg = body.sysErrorMsg;
                this.sysErrorDate = body.sysErrorDate;
            })).finally(action('finishLoadingError', () => {
                this.searchLoading = false
            }));
        } else {
            warn('Input is not a valid number');
        }
    };

};