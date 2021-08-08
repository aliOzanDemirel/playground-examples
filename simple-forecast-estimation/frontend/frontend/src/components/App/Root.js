import React from 'react';
import {useStrict} from 'mobx';
import {Provider} from 'mobx-react';
import {Router} from 'react-router-dom';
import {RouterStore, syncHistoryWithStore} from 'mobx-react-router';
import createBrowserHistory from 'history/createBrowserHistory';
import MockApi from "../../common/api/mock-api";
import AppApi from "../../common/api/app-api";
import EstimationStore from "../Estimation/EstimationStore";
import ForecastStore from "../Forecast/ForecastStore";
import SignUpStore from "../SignUp/SignUpStore";
import LoginStore from "../Login/LoginStore";
import ForecastEstimationApp from "./ForecastEstimationApp";
import {setLoginStoreInRequestService} from "../../common/request-service";

// MockApi can be used for development of frontend without running backend
const api = process.env.REACT_APP_USE_MOCK === true ? new MockApi() : new AppApi();

const browserHistory = createBrowserHistory();
const routingStore = new RouterStore();
const loginStore = new LoginStore(api);

setLoginStoreInRequestService(loginStore);

const stores = {
    appRouter: routingStore,
    loginStore: loginStore,
    estimationStore: new EstimationStore(api),
    forecastStore: new ForecastStore(api),
    signUpStore: new SignUpStore(api)
};

const history = syncHistoryWithStore(browserHistory, routingStore);

// make sure that nothing modifies state other than action methods defined in observed stores.
useStrict(true);

export default class Root extends React.Component {
    render() {
        return (
            <Provider {...stores}>
                <Router history={history}>
                    <ForecastEstimationApp/>
                </Router>
            </Provider>
        );
    }
}