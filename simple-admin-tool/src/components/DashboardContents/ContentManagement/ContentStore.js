import {action, computed, extendObservable} from 'mobx';
import {goodNews} from "../../../common/service/notifier-service";

export default class ContentStore {

    constructor(api) {
        this.api = api;

        extendObservable(this, {
            inputContentRepo: null,
            currentContentRepo: null,
            availableContentRepos: null,
            shouldLoadRepos: computed(function () {
                return this.currentContentRepo === null || this.availableContentRepos === null;
            })
        });

    };

    getAvailableRepos = action('getAvailableRepos', () => {
        if (this.shouldLoadRepos) {
            this.api.getContentRepos().then(action.bound((respBody) => {
                    this.availableContentRepos = respBody.availableContentRepos;
                    this.inputContentRepo = respBody.currentContentRepo;
                    this.currentContentRepo = respBody.currentContentRepo;
                })
            );
        }
    });

    switchContentRepo = action('switchContentRepo', () => {

        // do not request server if input is already the switch target
        if (this.inputContentRepo !== this.currentContentRepo) {
            this.api.postSwitchContentRepo(this.inputContentRepo).then(action.bound(resp => {
                this.currentContentRepo = this.inputContentRepo;
                goodNews('Active repository is switched to ' + this.inputContentRepo);
            }));
        }
    });

};