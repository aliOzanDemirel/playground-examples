import React from 'react';
import {configure} from 'mobx';
import {Provider} from 'mobx-react';
import {Router} from 'react-router-dom';
import {RouterStore, syncHistoryWithStore} from 'mobx-react-router';
import MainWindow from "./MainWindow";
import TableListStore from "../store/TableListStore";
import AppApi from "../common/api/app-api";
import MockApi from "../common/api/mock-api";
import DirectoryTreeStore from "../store/DirectoryTreeStore";

// MockApi can be used for development of frontend without running backend
const api = process.env.REACT_APP_USE_MOCK === "true" ? new MockApi() : new AppApi();

const routingStore = new RouterStore();
const stores = {
    appRouter: routingStore,
    tableListStore: new TableListStore(api),
    directoryTreeStore: new DirectoryTreeStore(api)
};

const browserHistory = require("history").createBrowserHistory();
const history = syncHistoryWithStore(browserHistory, routingStore);

// make sure that nothing modifies state other than action methods defined in observed stores.
configure({
    enforceActions: "observed"
});

export default class Root extends React.Component {
    render() {
        return (
            <Provider {...stores}>
                <Router history={history}>
                    <MainWindow/>
                </Router>
            </Provider>
        );
    }
}
