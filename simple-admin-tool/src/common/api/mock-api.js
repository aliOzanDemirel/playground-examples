import {extendObservable} from "mobx";

export default class MockApi {

    constructor(defaultClusterMode) {

        // this boolean value is observed from HeaderAppInfo
        extendObservable(this, {
            cluster: defaultClusterMode
        });
        this.firstIP = '10.11.12.13';
        this.secondIP = '1.2.3.4';
    }

    responseBody = objToReturn => {
        return new Promise((resolve, reject) => {
            resolve(objToReturn);
        }).catch((err) => {
            console.error('error in mock service: ', err);
        });
    };

    defaultMultiResp = () => this.responseBody([{
        serverIP: this.firstIP
    }, {
        serverIP: this.secondIP
    }]);

    fullResponse = body => this.responseBody({
        statusCode: 200,
        body: JSON.stringify(body)
    });

    // LoginStore ****************************************
    getAppInfo = () => this.responseBody({
        serverIP: this.firstIP,
        version: '3.0-SNAPSHOT',
        startupDate: 'July 17, 2018, 06:30'
    });
    postLogin = (loginFormBody) => this.getAppInfo();
    postLogout = () => this.responseBody({});

    // SapStore ****************************************
    getSapEnvironments = () => this.responseBody({
        currentEnv: 'SYRIZA',
        availableEnvironments: ['GOP', 'SYRIZA', 'SPD']
    });
    // returns requested env even for checking connection, this would not be issue for a real api
    postSapSwitchOrCheck = body => this.responseBody({
        currentEnv: body.sapEnvRequested,
        message: body.sapSwitchFlag ? 'SAP env is switched.' : 'SAP connection is successful.'
    });
    getValidateSapAccount = accountId => this.responseBody({
        message: 'Account is valid.'
    });

    // UserStore ****************************************
    getUsers = () => {
        const tableData = [{
            username: 'Admin Ozan',
            email: 'user@mail.com',
            authorities: ['ROLE_LEVEL_0', 'ROLE_LEVEL_1', 'ROLE_LEVEL_2', 'ROLE_LEVEL_3']
        }];
        for (let i = 1; i < 35; i++) {
            tableData.push({
                username: i + '. Ozan',
                email: 'ali@ozan.com',
                authorities: ['ROLE_LEVEL_' + i % 5]
            });
        }
        return this.responseBody(tableData);
    };
    getUserRoles = () => this.responseBody(
        ['ROLE_LEVEL_0', 'ROLE_LEVEL_1', 'ROLE_LEVEL_2', 'ROLE_LEVEL_3', 'ROLE_LEVEL_4']);
    postSaveUser = (values) => this.responseBody({
        user: {...values}
    });
    postRemoveUser = username => this.responseBody({});

    // ErrorSearchStore ****************************************
    getError = (errorId) => this.responseBody({
        sysErrorMsg: 'Error with id ' + errorId + ' is bad. Too bad...',
        sysErrorDate: new Date().toDateString()
    });
    getLastErrors = () => {
        const tableData = [];
        for (let i = 0; i < 11; i++) {
            tableData.push({
                id: i + 10,
                message: 'mocking error',
                errorDate: new Date().getTime(),
                request: 'requested/' + i
            });
        }
        return this.responseBody(tableData);
    };

    // ContentStore ****************************************
    getContentRepos = () => this.responseBody({
        availableContentRepos: ['amazonContent', 'googleContent'],
        currentContentRepo: 'amazonContent'
    });
    postSwitchContentRepo = newRepo => this.responseBody({
        message: 'Content repo is switched.'
    });

    // SwitchStatusStore ****************************************
    getFlagsStates = () => this.responseBody({
        searchEnabled: true,
        orderLockingEnabled: true,
        paymentEnabled: false,
        pdfEnabled: true
    });
    postSwitchSearchFlag = (checkedFlag) => this.defaultMultiResp();
    postSwitchOrderLockingFlag = (checkedFlag) => this.defaultMultiResp();
    postSwitchPaymentFlag = (checkedFlag) => this.defaultMultiResp();
    postTogglePdfEnabled = () => this.defaultMultiResp();

    // CronJobsStore ****************************************
    postCronJobTriggerStatuses = () => this.defaultMultiResp();
    getCronJobTriggers = () => this.responseBody({
        refreshConfiguration: false,
        refreshCache: true,
        sendWeeklyMails: true,
        indexProducts: false,
        indexCategories: true,
        cleanSessions: false,
    });

    // LoginManagementStore ****************************************
    getDownloadLoginAudits = (dates) => this.responseBody({});
    getRememberMeOptions = () => this.responseBody({
        isSecureConnection: false,
        encryptionKey: 'mock-key',
        timeDifference: 25,
        timeDifferenceDelta: 10
    });
    postRememberMeOptions = (formValues) => this.defaultMultiResp();

    // IndexingStore ****************************************
    postRunIndexer = indexerName => this.defaultMultiResp();
    getIndexerStats = indexerName => this.responseBody({
        lastIndexingTime: 'July 17, 2018, 07:30',
        lastIndexedCount: 3761
    });

    // ReportMailerStore ****************************************
    getDownloadSubscriptionReport = subscriptionDetails => this.responseBody({});
    postMailTriggerOrderReport = () => this.responseBody({
        message: 'Triggered sending mail for report subscriptions'
    });
    postMailSalesRep = salesRepInfo => this.responseBody({
        message: 'Order Reports are sent via mail'
    });

    // CodeEditorStore ****************************************
    postCodeFileContent = (codeContentInfo) => this.defaultMultiResp();
    getCodeFileContent = (codePathJson) => this.responseBody({
        loadedContent: 'Code loaded from mock service'
    });

    // ConfigFileStore ****************************************
    postUploadConfig = (multipartFormData, successCallback) => successCallback({respBody: {serverIP: '93.94.155.112'}});
    getConfigTypes = () => this.responseBody([
        'sap', 'sml', 'test', 'mock', 'config'
    ]);
    postInitializeBean = beanName => this.defaultMultiResp();
    getInitializableBeanNames = () => this.responseBody(['LocaleManager', 'ConfigurationManager', 'ResourceManager']);

    // CacheStore ****************************************
    getAllCaches = () => {
        const firstServer = [];
        const secondServer = [];
        for (let i = 1; i < 15; i++) {
            firstServer.push({
                cacheName: i + '. cache',
                serverIP: this.firstIP,
                status: 'ACTIVE',
                cacheSize: i,
                cacheMemoryStoreHitCount: 337,
                cacheDiskStoreHitCount: 164,
                cacheMissCountNotFound: 23,
                cacheMissCountExpired: 9
            });
            secondServer.push({
                cacheName: i + '. cache',
                serverIP: this.secondIP,
                status: 'PASSIVE',
                cacheSize: i + 10,
                cacheMemoryStoreHitCount: 1223,
                cacheDiskStoreHitCount: 3453,
                cacheMissCountNotFound: 234,
                cacheMissCountExpired: 124
            });
        }
        return this.responseBody([{
            serverIP: this.firstIP,
            cacheRegions: firstServer
        }, {
            serverIP: this.secondIP,
            cacheRegions: secondServer
        }]);
    };
    getCacheDetails = (cacheName) => {
        const firstServer = [];
        const secondServer = [];
        for (let i = 1; i < 8; i++) {
            firstServer.push({
                key: cacheName + ' - ' + i,
                serverIP: this.firstIP,
                createTime: i + 10,
                expiresInSeconds: i + 20
            });
            secondServer.push({
                key: cacheName + ' - ' + i,
                serverIP: this.secondIP,
                createTime: i + 100,
                expiresInSeconds: i + 500
            });
        }
        let first = {
            serverIP: this.firstIP
        };
        first[cacheName] = firstServer;
        let second = {
            serverIP: this.secondIP
        };
        second[cacheName] = secondServer;
        return this.responseBody([first, second]);
    };
    postRemoveCaches = reqBody => this.defaultMultiResp();

    // SiteStore ****************************************
    getAllSites = () => {
        const firstServer = [];
        const secondServer = [];
        for (let i = 1; i < 15; i++) {
            firstServer.push({
                siteName: 'site_data_' + i,
                status: 'INITIALIZED',
                lastInitDate: 'July 17, 2018, 07:00'
            });
            secondServer.push({
                siteName: 'site_data_' + i,
                status: 'IN_PROGRESS',
                lastInitDate: 'July 17, 2018, 07:00'
            });
        }
        return this.responseBody([{
            serverIP: this.firstIP,
            sites: firstServer
        }, {
            serverIP: this.secondIP,
            sites: secondServer
        }]);
    };
    postRunIndexerForSite = siteName => this.defaultMultiResp();
    postInitializeSite = siteName => this.defaultMultiResp();

}