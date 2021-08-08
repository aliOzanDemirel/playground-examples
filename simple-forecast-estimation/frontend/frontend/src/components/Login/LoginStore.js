import {action, computed, extendObservable, runInAction} from 'mobx';
import {
    appStorage,
    JWT_COOKIE_NAME,
    JWT_HEADER_KEY,
    pages,
    presentExpoNum,
    removeCookieByName
} from "../../common/commons";

export default class LoginStore {

    constructor(api) {
        this.api = api;

        extendObservable(this, {
            isForecastPage: false,
            userDetails: {
                email: null,
                userId: null,
                userIdShown: null,
                highScore: null
            },
            // decides if user is logged in by checking only local storage, not cookie
            userToken: appStorage.getItem(JWT_HEADER_KEY),
            tokenExists: computed(() => {
                return this.userToken !== undefined && this.userToken !== null && this.userToken !== "null";
            })
        });
    }

    login = (formValues, appRouter) => {
        this.api.login(formValues).then(respBody => {
            this.setUserDetailsAndRedirect(respBody, appRouter)
        });
    };

    // check token's validation and redirect if it is valid
    checkIfUserIsLoggedIn = (appRouter) => {
        if (this.tokenExists) {
            this.api.getLoggedInUserDetails().then(respBody => {
                this.setUserDetailsAndRedirect(respBody, appRouter)
            });
        }
    };

    setUserDetailsAndRedirect = (respBody, appRouter) => {
        this.updateUserDetails(respBody);
        this.redirectToEstimationPage(appRouter);
    };

    // set header information if user is logged in and token is validated
    updateUserDetails = action('updateUserDetails', respBody => {
        // console.log("updateUserDetails ", respBody)
        this.userDetails = {
            email: respBody.username,
            userId: respBody.userUUID,
            userIdShown: respBody.sequence,
            highScore: presentExpoNum(respBody.highScore)
        };
    });

    redirectToEstimationPage = (appRouter) => {
        // forecast page is not authorized
        if (appRouter.location.pathname !== pages.forecast) {
            appRouter.replace(pages.estimation);
        }
    };

    updateUserToken = (tokenReturned) => {
        if (tokenReturned) {
            appStorage.setItem(JWT_HEADER_KEY, tokenReturned);
        } else {
            appStorage.removeItem(JWT_HEADER_KEY);
            removeCookieByName(JWT_COOKIE_NAME)
        }
        runInAction('updateUserToken', () => this.userToken = tokenReturned)
    };

    updateIfForecastPage = action('updateIfForecastPage', (isForecastPage) => {
        this.isForecastPage = isForecastPage;
    });

    logout = () => {
        if (this.tokenExists) {
            this.updateUserToken(null);
        }
    };
}