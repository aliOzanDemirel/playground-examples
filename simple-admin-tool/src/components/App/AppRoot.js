import React from 'react';
import {useStrict} from 'mobx';
import {Provider} from 'mobx-react';
import {Router} from 'react-router-dom';
import {RouterStore, syncHistoryWithStore} from 'mobx-react-router';
import createBrowserHistory from 'history/createBrowserHistory';
import {setLoginStore} from "../../common/service/request-service";
import App from "./App";
import LoginStore from "../LoginPage/LoginStore";
import UserStore from "../DashboardContents/UserManagement/UserStore";
import SapStore from "../DashboardContents/SapManagement/SapStore";
import ContentStore from "../DashboardContents/ContentManagement/ContentStore";
import ErrorSearchStore from "../DashboardContents/ErrorManagement/ErrorSearchStore";
import CronJobsStore from "../DashboardContents/CronJobsManagement/CronJobsStore";
import LoginManagementStore from "../DashboardContents/LoginManagement/LoginManagementStore";
import SwitchStatusStore from "../DashboardContents/SwitchStatusManagement/SwitchStatusStore";
import IndexingStore from "../DashboardContents/IndexingManagement/IndexingStore";
import ReportMailerStore from "../DashboardContents/MailAndDownloadReport/ReportMailerStore";
import CodeEditorStore from "../DashboardContents/CodeEditor/CodeEditorStore";
import ConfigFileStore from "../DashboardContents/ConfigFileManagement/ConfigFileStore";
import CacheStore from "../DashboardContents/CacheManagement/CacheStore";
import SiteStore from "../DashboardContents/SiteManagement/SiteStore";
import MockApi from "../../common/api/mock-api";
import AppApi from "../../common/api/app-api";

// enabled by default, should be parameterized for later
const clusterModeEnabled = true;

// MockApi can be used for development of frontend without running backend
const api = process.env.NODE_ENV === 'development' ? new MockApi(clusterModeEnabled) : new AppApi(clusterModeEnabled);

const browserHistory = createBrowserHistory();
const routingStore = new RouterStore();

// send loginStore to request-service to handle logouts
let loginStore = new LoginStore(api);
setLoginStore(loginStore);

const stores = {
    appRouter: routingStore,
    loginStore: loginStore,
    sapStore: new SapStore(api),
    userStore: new UserStore(api),
    contentStore: new ContentStore(api),
    errSearchStore: new ErrorSearchStore(api),
    switchStatusStore: new SwitchStatusStore(api),
    cronJobsStore: new CronJobsStore(api),
    manageLoginStore: new LoginManagementStore(api),
    indexingStore: new IndexingStore(api),
    reportMailerStore: new ReportMailerStore(api),
    codeEditorStore: new CodeEditorStore(api),
    configFileStore: new ConfigFileStore(api),
    cacheStore: new CacheStore(api),
    siteStore: new SiteStore(api)
};

const history = syncHistoryWithStore(browserHistory, routingStore);
// history.subscribe((location, action) => console.info(location.pathname));

// make sure that nothing modifies state other than action methods defined in observed stores.
useStrict(true);

export default class AppRoot extends React.Component {
    render() {
        return (
            <Provider {...stores}>
                <Router history={history}>
                    <App/>
                </Router>
            </Provider>
        );
    }
}