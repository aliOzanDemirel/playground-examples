import {action, computed, extendObservable} from 'mobx';
import {goodNews} from "../../../common/service/notifier-service";
import {getTextual} from "../../Common/CommonComponents";

export default class SwitchStatusStore {

    constructor(api) {
        this.api = api;

        extendObservable(this, {
            isPdfEnabled: null,
            searchEnabled: null,
            orderLockingEnabled: null,
            paymentEnabled: null,
            shouldLoadFlags: computed(function () {
                return this.searchEnabled === null || this.orderLockingEnabled === null
                    || this.paymentEnabled === null || this.isPdfEnabled === null;
            })
        });
    };

    initFlagsStates = () => {
        if (this.shouldLoadFlags) {
            this.api.getFlagsStates().then(action('initFlagsStates', respBody => {
                    this.searchEnabled = respBody.searchEnabled;
                    this.orderLockingEnabled = respBody.orderLockingEnabled;
                    this.paymentEnabled = respBody.paymentEnabled;
                    this.isPdfEnabled = respBody.isPdfEnabled;
                })
            );
        }
    };

    switchSearchFlag = action('switchSearchFlag', (checkedFlag) => {

        this.api.postSwitchSearchFlag(checkedFlag).then(action.bound(responses => {
            this.searchEnabled = checkedFlag;
            responses.forEach(it => goodNews('Search is ' + getTextual(this.searchEnabled) + ' in ' + it.serverIP));
        }));
    });

    switchOrderLocking = action('switchOrderLocking', (checkedFlag) => {

        this.api.postSwitchOrderLockingFlag(checkedFlag).then(action.bound(responses => {
            this.orderLockingEnabled = checkedFlag;
            responses.forEach(it => goodNews('Order locking is '
                + getTextual(this.orderLockingEnabled) + ' in ' + it.serverIP));
        }));
    });

    switchPayment = action('switchPayment', (checkedFlag) => {

        this.api.postSwitchPaymentFlag(checkedFlag).then(action.bound(responses => {
            this.paymentEnabled = checkedFlag;
            responses.forEach(it => goodNews('Payment is ' + getTextual(this.paymentEnabled) + ' in ' + it.serverIP));
        }));
    });

    togglePdfEnabled = action('togglePdfEnabled', (checked) => {

        this.api.postTogglePdfEnabled().then(action.bound(responses => {
            this.isPdfEnabled = checked;
            responses.forEach(it => goodNews('Pdf is ' + getTextual(this.paymentEnabled) + ' in ' + it.serverIP));
        }));
    });

}