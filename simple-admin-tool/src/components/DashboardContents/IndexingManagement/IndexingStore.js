import {action, extendObservable} from 'mobx';
import {goodNews} from "../../../common/service/notifier-service";

export default class IndexingStore {

    constructor(api) {
        this.api = api;

        extendObservable(this, {
            indexers: {
                PRODUCT: {
                    lastRan: null,
                    totalCounted: null
                },
                CATEGORY: {
                    lastRan: null,
                    totalCounted: null
                },
                CONTENT_SUGGESTION: {
                    lastRan: null,
                    totalCounted: null
                }
            }
        });
    };

    runIndexer = action('runIndexer', indexer => {
        this.api.postRunIndexer(indexer).then(bodies =>
            bodies.forEach(it => goodNews('Started indexing \'' + indexer + '\' in ' + it.serverIP))
        );
    });

    getIndexerStats = action('getIndexerStats', indexer => {
        this.api.getIndexerStats(indexer).then(action.bound(resp => {
            this.indexers[indexer].lastRan = resp.lastIndexingTime;
            this.indexers[indexer].totalCounted = resp.lastIndexedCount;
        }))
    });

}