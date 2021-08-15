import {action, autorun, computed, extendObservable, runInAction} from 'mobx';
import * as mobxUtils from 'mobx-utils';
import {goodNews} from "../../../common/service/notifier-service";
import {checkSelection, sortTable} from "../../../common/commons";
import {getConfirmation} from "../../Common/CommonComponents";

// this is not a cache store for frontend but the store of CacheManagement component.
export default class CacheStore {

    constructor(api) {
        this.api = api;

        this.serverSize = 1;

        extendObservable(this, {
            table: {
                data: [],
                selRows: []
            },
            detailTable: {
                data: []
            },
            detailTableVisible: false,
            observedCachesResp: {state: null},
            loading: computed(() => {
                return this.observedCachesResp.state === mobxUtils.PENDING
            })
        });

        autorun('observedCachesRespAutorun', () => {
            if (this.observedCachesResp.state === mobxUtils.FULFILLED) {
                this.handleCacheInfoResponse(this.observedCachesResp.value)
            }
        });
    };

    getCacheInfos = action('getCacheInfos', () => {
        this.observedCachesResp = mobxUtils.fromPromise(this.api.getAllCaches())
    });

    handleCacheInfoResponse = action('handleCacheInfoResponse', respBodies => {
        this.serverSize = respBodies.length;
        this.table.data = respBodies.map(serverResp => serverResp.cacheRegions)
            .reduce((allRegions, serverRegions) => {
                return allRegions.concat(serverRegions.map(this.getCacheInfoRowData))
            }, []).sort((first, second) => sortTable(first, second, 'cacheName'))
    });

    getCacheInfoRowData = data => {
        return {
            ...data,
            key: data.cacheName + data.serverIP
        }
    };

    openDetails = cacheName => {
        runInAction('showDetailTable', () => this.detailTableVisible = true);

        this.api.getCacheDetails(cacheName).then(action('openDetails', respBodies => {

            this.detailTable.data = respBodies.map(serverResp => serverResp[cacheName])
                .reduce((allDetails, cacheDetail) => {
                    return allDetails.concat(cacheDetail.map(this.getCacheDetailRowData))
                }, []).sort((first, second) => sortTable(first, second, 'itemKey'))
        }));
    };

    getCacheDetailRowData = data => {
        return {
            ...data,
            key: data.key + data.serverIP,
            itemKey: data.key
        }
    };

    removeAllCaches = () => {
        getConfirmation(() => this.api.postRemoveCaches('all').then(action('removeAllCaches', bodies => {
            this.table.data = [];
            bodies.forEach(it => goodNews('Removed all caches in ' + it.serverIP))
        })), 'Are you sure to remove all caches?')
    };

    removeCache = () => {
        if (checkSelection(this.table.selRows, false, 'No cache is selected.')) {

            let cacheName = this.table.selRows[0].cacheName;

            getConfirmation(() => {
                this.api.postRemoveCaches(cacheName).then(action('removeCache', bodies => {
                    this.table.data = this.table.data.filter(it => it.cacheName !== cacheName);
                    bodies.forEach(it => goodNews('Removed ' + cacheName + ' in ' + it.serverIP))
                }))
            }, 'Are you sure to remove cache ' + cacheName + '?');
        }
    };

    hideDetails = action('hideDetails', () => {
        this.detailTableVisible = false;
    });

}