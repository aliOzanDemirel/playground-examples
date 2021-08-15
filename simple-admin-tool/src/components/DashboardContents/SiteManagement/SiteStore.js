import {action, autorun, computed, extendObservable} from 'mobx';
import * as mobxUtils from 'mobx-utils';
import {checkSelection, sortTable} from "../../../common/commons";
import {getConfirmation} from "../../Common/CommonComponents";
import {goodNews} from "../../../common/service/notifier-service";

export default class SiteStore {

    constructor(api) {
        this.api = api;

        this.serverSize = 1;
        extendObservable(this, {
            table: {
                data: [],
                selRows: []
            },
            observedSiteTableResp: {state: null},
            loading: computed(() => {
                return this.observedSiteTableResp.state === mobxUtils.PENDING;
            })
        });

        autorun('observedSiteTableRespAutorun', () => {
            if (this.observedSiteTableResp.state === mobxUtils.FULFILLED) {
                this.handleSiteTableResponse(this.observedSiteTableResp.value)
            }
        });
    };

    loadTableData = action('loadTableData', () => {
        this.observedSiteTableResp = mobxUtils.fromPromise(this.api.getAllSites());
    });

    handleSiteTableResponse = action('handleSiteTableResponse', respBodies => {
        this.serverSize = respBodies.length;
        this.table.data = respBodies.reduce((allInOneArray, body) => {
            return allInOneArray.concat(body.sites.map(siteInfo => this.getRowData(siteInfo, body.serverIP)))
        }, []).sort((first, second) => sortTable(first, second, 'siteName'))
    });

    getRowData = (data, serverIP) => {
        return {
            ...data,
            key: data.siteName + serverIP,
            serverIP: serverIP
        }
    };

    initializeSite = siteName => {
        if (checkSelection(this.table.selRows, true,
            'No site is selected.', 'Only one site should be selected.', 'siteName')) {

            if (siteName !== 'all') {
                siteName = this.table.selRows[0].siteName;
            }
            getConfirmation(() => this.api.postRunIndexerForSite(siteName)
                    .then(bodies => bodies.forEach(it => goodNews('Started initializing ' + siteName + ' in ' + it.serverIP))),
                'Are you sure to initialize ' + siteName + '?');
        }
    };

    runIndexerForSite = siteName => {
        if (checkSelection(this.table.selRows, true,
            'No site is selected.', 'Only one site should be selected.', 'siteName')) {

            if (siteName !== 'all') {
                siteName = this.table.selRows[0].siteName;
            }
            getConfirmation(() => this.api.postInitializeSite(siteName)
                    .then(bodies => bodies.forEach(it => goodNews('Indexing for ' + siteName + ' in ' + it.serverIP))),
                'Are you sure to run indexer for ' + siteName + '?');
        }
    };

    goToWebsite = () => {
        if (checkSelection(this.table.selRows, true,
            'No site is selected.', 'Only one site should be selected.', 'siteName')) {
            window.open('/site/' + this.table.selRows[0].siteName, '_blank').focus();
        }
    };

}