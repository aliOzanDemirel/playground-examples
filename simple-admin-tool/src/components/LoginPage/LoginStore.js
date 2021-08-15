import {action, computed, extendObservable, runInAction} from 'mobx';
import {defaultPage} from "../../common/commons";

const AUTH_USERNAME_KEY = 'AuthUser';
const appStorage = window.localStorage;

/**
 * this store is more like an app store since it has the appInfo and delegate method for cluster config.
 */
export default class LoginStore {

    constructor(api) {
        this.api = api;

        extendObservable(this, {
            appInfo: null,
            authenticatedUser: appStorage.getItem(AUTH_USERNAME_KEY),
            isLoggedIn: computed(() => {
                return this.authenticatedUser !== undefined &&
                    this.authenticatedUser !== null && this.authenticatedUser !== "null";
            })
        });
    }

    // only the cluster in api is observable, not api itself nor that it has to
    switchClusterMode = action('switchClusterMode', isCluster => {
        this.api.cluster = isCluster;
    });

    login = (formValues, appRouter) => {
        this.api.postLogin(formValues).then(action('login', respBody => {
            this.updateAuthUser(formValues.username);
            this.afterLogin(respBody, appRouter);
        }));
    };

    // sets app information if user is logged in
    afterLogin = action('handleLogin', (respBody, appRouter) => {
        this.appInfo = {
            serverIP: respBody.serverIP,
            version: respBody.version,
            startupDate: respBody.startupDate
        };
        appRouter.push(defaultPage.url);
        // appRouter.replace('/admin/login');
    });

    // observer header and authorized route components will react to this by redirecting to login
    logout = () => {
        return this.api.postLogout().then(() => this.updateAuthUser(null));
    };

    // request for app info if page is refreshed when user is logged in
    loadAppInfo = (appRouter) => {
        if (this.isLoggedIn) {
            this.api.getAppInfo().then(respBody => this.afterLogin(respBody, appRouter));
        }
    };

    // remove cached user in browser if username is null
    updateAuthUser = (username) => {
        if (username) {
            appStorage.setItem(AUTH_USERNAME_KEY, username);
        } else {
            appStorage.removeItem(AUTH_USERNAME_KEY);
        }
        runInAction('updateAuthUser', () => this.authenticatedUser = username)
    };

}