import React from 'react';
import {inject, observer} from "mobx-react";
import {Collapse} from "antd";
import {panelStyle} from "../../../common/commons";
import IndexingPanelArea from "./IndexingPanelArea";

export default inject('indexingStore')(observer(class IndexingManagement extends React.Component {
    render() {
        let {runIndexer, getIndexerStats, indexers} = this.props.indexingStore;
        let {PRODUCT, CATEGORY, CONTENT_SUGGESTION} = indexers;
        let keys = Object.keys(indexers);
        return (
            <Collapse bordered={false} activeKey={keys}>
                <Collapse.Panel key={keys[0]} style={panelStyle} showArrow={false}
                                header="Product Indexer">
                    <IndexingPanelArea lastRan={PRODUCT.lastRan} totalCounted={PRODUCT.totalCounted}
                                       runHandler={() => runIndexer(keys[0])}
                                       statsHandler={() => getIndexerStats(keys[0])}/>
                </Collapse.Panel>
                <Collapse.Panel key={keys[1]} style={panelStyle} showArrow={false}
                                header="Category Indexer">
                    <IndexingPanelArea lastRan={CATEGORY.lastRan}
                                       totalCounted={CATEGORY.totalCounted}
                                       runHandler={() => runIndexer(keys[1])}
                                       statsHandler={() => getIndexerStats(keys[1])}/>
                </Collapse.Panel>
                <Collapse.Panel key={keys[2]} style={panelStyle} showArrow={false}
                                header="Content Suggestion Indexer">
                    <IndexingPanelArea lastRan={CONTENT_SUGGESTION.lastRan}
                                       totalCounted={CONTENT_SUGGESTION.totalCounted}
                                       runHandler={() => runIndexer(keys[2])}
                                       statsHandler={() => getIndexerStats(keys[2])}/>
                </Collapse.Panel>
            </Collapse>
        );
    }
}));
