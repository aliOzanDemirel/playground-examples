import {doGet, doGetWithJsonBody, doPost, postFormDataWithVanillaAjax} from "../service/request-service";
import {extendObservable} from "mobx";

export default class AppApi {

    constructor(defaultClusterMode) {

        // cluster mode parameter (observed from HeaderAppInfo) is not specific to business logic,
        // it can be applied on every state changing action, thus it is kept in api layer.
        extendObservable(this, {
            cluster: defaultClusterMode
        });
    }

    get clusterForBody() {
        return {
            clusterEnabled: this.cluster
        }
    }

    // LoginStore ****************************************
    postLogin = (loginFormBody) => {
        return doPost({url: '/login'}, loginFormBody);
    };
    postLogout = () => {
        return doPost({url: '/logout'});
    };
    getAppInfo = () => {
        return doGet({url: '/app/info'});
    };

    // SapStore ****************************************
    getSapEnvironments = () => {
        return doGet({url: '/sap/environments'});
    };
    postSapSwitchOrCheck = body => {
        return doPost({
            url: '/state/sap/switch/environment',
            json: true
        }, {
            ...this.clusterForBody,
            body
        });
    };
    getValidateSapAccount = (accountId) => {
        return doGet({
            url: '/sap/validate/account/' + accountId
        });
    };

    // UserStore ****************************************
    getUsers = () => {
        return doGet({url: '/user/list'});
    };
    getUserRoles = () => {
        return doGet({url: '/user/roles'});
    };
    postSaveUser = (values) => {
        return doPost({
            url: '/user/save',
            json: true
        }, values);
    };
    postRemoveUser = (username) => {
        return doPost({url: '/user/remove/' + username});
    };

    // ErrorSearchStore ****************************************
    getError = (errorId) => {
        return doGet({url: '/get/error/' + errorId});
    };
    getLastErrors = () => {
        return doGet({url: '/get/errors/last/20'});
    };

    // ContentStore ****************************************
    getContentRepos = () => {
        return doGet({url: '/content/repos'});
    };
    postSwitchContentRepo = (newRepo) => {
        return doPost({url: '/content/repo/switch/' + newRepo});
    };

    // SwitchStatusStore ****************************************
    getFlagsStates = () => {
        return doGet({url: '/switchable/flags/status'});
    };
    postSwitchSearchFlag = (checkedFlag) => {
        return doPost({
            url: '/state/switch/product-search-cache/enabled/' + checkedFlag
        });
    };
    postSwitchOrderLockingFlag = (checkedFlag) => {
        return doPost({
            url: '/state/switch/blocked-order/locked/' + checkedFlag
        });
    };
    postSwitchPaymentFlag = (checkedFlag) => {
        return doPost({
            url: '/state/switch/payment-term-description/flag/' + checkedFlag
        });
    };
    postTogglePdfEnabled = () => {
        return doPost({
            url: '/state/toggle/pdf/enabled'
        });
    };
    // postSwitchMultipleFlags = (checkedVideoFlag, checked3dFlag, checkedTourFlag) => {
    //     let url = '/state/switch/change/video/{videoFlag}/3d-view/{viewFlag}/product-tour/{tourFlag}'
    //         .split('{videoFlag}').join(checkedVideoFlag)
    //         .split('{viewFlag}').join(checked3dFlag)
    //         .split('{tourFlag}').join(checkedTourFlag);
    //     return doPost({
    //         url: url
    //     })
    // };

    // CronJobsStore ****************************************
    postCronJobTriggerStatuses = (jobStatusMap) => {
        return doPost({
            url: '/state/cron/job/triggers/status/update',
            json: true
        }, jobStatusMap);
    };
    getCronJobTriggers = () => {
        return doGet({
            url: '/cron/job/triggers/status'
        });
    };

    // LoginManagementStore ****************************************
    getDownloadLoginAudits = (dates) => {
        return doGetWithJsonBody({
            url: '/download/login/audits'
        }, dates);
    };
    getRememberMeOptions = () => {
        return doGet({
            url: '/remember-me/details'
        });
    };
    postRememberMeOptions = (rememberMeForm) => {
        return doPost({
            url: '/state/login/remember-me/update', json: true
        }, rememberMeForm);
    };

    // IndexingStore ****************************************
    postRunIndexer = (indexerName) => {
        return doPost({
            url: '/state/indexer/run/' + indexerName
        });
    };
    getIndexerStats = (indexerName) => {
        return doGet({
            url: '/indexer/stats/' + indexerName
        });
    };

    // ReportMailerStore ****************************************
    getDownloadSubscriptionReport = (subscriptionDetails) => {
        return doGetWithJsonBody({
            url: '/download/order/report/subscription/details'
        }, subscriptionDetails);
    };
    postMailTriggerOrderReport = () => doPost({
        url: '/email/trigger/order/report/service'
    });
    postMailSalesRep = salesRepInfo => {
        return doPost({
            url: '/email/order/reports/sales/representative'
        }, salesRepInfo);
    };

    // CodeEditorStore ****************************************
    postCodeFileContent = (codeContentInfo) => {
        return doPost({
            url: '/state/code/file/content/save',
            json: true
        }, codeContentInfo);
    };
    getCodeFileContent = (codePathJson) => {
        return doGetWithJsonBody({
            url: '/code/file/content/load'
        }, codePathJson);
    };

    // ConfigFileStore ****************************************
    postUploadConfig = (multipartFormData, successCallback) => {
        postFormDataWithVanillaAjax('/state/upload/config/file', multipartFormData, successCallback);
    };
    getConfigTypes = () => {
        return doGet({
            url: '/uploadable/config/types'
        });
    };
    postInitializeBean = (beanName) => {
        return doPost({
            url: '/state/initialize/bean/' + beanName
        });
    };
    getInitializableBeanNames = () => {
        return doGet({
            url: '/initializable/beans'
        });
    };

    // CacheStore ****************************************
    getAllCaches = () => {
        return doGet({
            url: '/state/cache/all'
        });
    };
    getCacheDetails = cacheName => {
        return doGet({
            url: '/state/cache/details/' + cacheName
        });
    };
    postRemoveCaches = cacheName => {
        return doPost({
            url: '/state/cluster/' + this.cluster + '/cache/remove/' + cacheName
        });
    };

    // SiteStore ****************************************
    getAllSites = () => {
        return doGet({
            url: '/state/site/all'
        });
    };
    postRunIndexerForSite = siteName => {
        return doPost({
            url: '/state/cluster/' + this.cluster + '/site/initialize/' + siteName
        });
    };
    postInitializeSite = siteName => {
        return doPost({
            url: '/state/cluster/' + this.cluster + '/site/index/' + siteName
        });
    };

}