import {action, extendObservable} from 'mobx';
import {goodNews} from "../../../common/service/notifier-service";

export default class CronJobsStore {

    constructor(api) {
        this.api = api;

        extendObservable(this, {

            // this object's state won't change since it will be used for resetting
            cronJobTriggers: null,

            // this object's state may change since it takes input
            inputCronJobTriggers: null
        });
    };

    setCronJobTriggers = action('setCronJobTriggers', () => {

        this.api.getCronJobTriggers().then(action.bound(respBody => {
            this.inputCronJobTriggers = respBody;
            this.cronJobTriggers = respBody;
        }));
    });

    saveCronJobStatus = action('saveCronJobStatus', (e) => {
        this.api.postCronJobTriggerStatuses(this.inputCronJobTriggers).then(action.bound(resp => {
            this.cronJobTriggers = this.inputCronJobTriggers;
            goodNews('Cron jobs\' statuses are updated.');
        }));
    });

    resetCheckboxes = action('resetCheckboxes', (e) => {
        this.inputCronJobTriggers = {...this.cronJobTriggers};
    });

    onCheckboxChange = action('onCheckboxChange', (key, value) => {
        this.inputCronJobTriggers[key] = value;
    });

}