import {action, extendObservable} from 'mobx';
import {getConfirmation} from "../../Common/CommonComponents";
import {goodNews} from "../../../common/service/notifier-service";

export default class ConfigFileStore {

    constructor(api) {
        this.api = api;

        extendObservable(this, {
            initializerBeanNames: null,
            configOptions: [],
            upload: {
                disabled: true
            }
        });
    };

    initializeBean = beanName => {
        getConfirmation(() => this.api.postInitializeBean(beanName).then(respBodies =>
                respBodies.forEach(it => goodNews('Initialized ' + beanName + ' in ' + it.serverIP))),
            'Are you sure to start initialization?')
    };

    getInitializerBeans = () => {
        this.api.getInitializableBeanNames().then(action('getInitializerBeans',
            respBeanNames => this.initializerBeanNames = respBeanNames))
    };

    unlockUploader = action('unlockUploader', () => {
        this.upload.disabled = false
    });

    loadConfigTypes = action('loadConfigTypes', () => {
        this.api.getConfigTypes().then(action.bound(resp => this.configOptions = resp))
    });

    uploadFile = values => {
        let data = new FormData();
        data.append('configFileType', values.configFileType);
        data.append('configFile', values.configFile[0]);

        getConfirmation(() => this.api.postUploadConfig(data, respBody =>
                goodNews('File is uploaded in ' + respBody.serverIP)),
            'Are you sure to upload this config file?');
    };

}