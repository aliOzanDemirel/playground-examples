import {action, autorun, computed, extendObservable, runInAction} from 'mobx';
import * as mobxUtils from 'mobx-utils';
import {goodNews, notifyFullTop} from "../../../common/service/notifier-service";

export default class SapManagementStore {

    constructor(api) {
        this.api = api;

        extendObservable(this, {
            inputSapEnv: null,
            inputAccountId: null,
            currentEnv: null,
            availableEnvironments: null,
            observedSwitchEnvResp: {state: null},
            loading: computed(() => {
                return this.observedSwitchEnvResp.state === mobxUtils.PENDING;
            })
        });

        autorun('observedSwitchEnvRespAutorun', () => {
            if (this.observedSwitchEnvResp.state === mobxUtils.FULFILLED) {
                runInAction(() => this.currentEnv = this.observedSwitchEnvResp.value.currentEnv);
                goodNews(this.observedSwitchEnvResp.value.message);
            }
        });

        // reaction(() => {
        //     console.log('resp state: ', this.observedSwitchEnvResp.state);
        //     return this.loading ? false : this.observedSwitchEnvResp.value;
        // }, (respBody) => {
        //     console.log('respBody: ', respBody);
        //     if (respBody) {
        //         this.currentEnv = respBody.currentEnv;
        //         notifier.warn('success', 'Success', 'SAP environment is switched to ' + this.currentEnv);
        //     }
        // });

        // when(() => this.observedAppInfoResp && this.observedAppInfoResp.state !== mobxUtils.PENDING,
        //     () => observedAppInfoRespHandler(this.observedAppInfoResp.value));
    };

    // 'switchEnv: false' => only checks connection availability
    switchOrCheckSapEnv = action('switchOrCheckSapEnv', switchEnv => {

        // do not request server if input is already the switch target
        if (this.currentEnv !== this.inputSapEnv || !switchEnv) {
            this.observedSwitchEnvResp = mobxUtils.fromPromise(this.api.postSapSwitchOrCheck({
                sapEnvRequested: this.inputSapEnv,
                sapSwitchFlag: switchEnv
            }));
        }
    });

    getSapEnvInfo = action('getSapEnvInfo', () => {
        this.api.getSapEnvironments().then(action.bound(body => {
            this.availableEnvironments = body.availableEnvironments;
            this.inputSapEnv = body.currentEnv;
            this.currentEnv = body.currentEnv;
        }));
    });

    validateSapAccount = () => {
        this.api.getValidateSapAccount(this.inputAccountId).then(resp => {
            notifyFullTop(resp.message, 'Account ' + this.inputAccountId);
        });
    };

}