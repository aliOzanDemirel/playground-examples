import React from 'react';
import {inject, observer} from "mobx-react";
import {Table} from "antd";

export default inject('cacheStore')(observer(class CacheDetailsTable extends React.Component {

    constructor(props) {
        super(props);

        this.cacheDetailsTableColumns = [{
            title: 'Item Key',
            dataIndex: 'itemKey',
            render: (text, row, index) => {
                let conf = {};
                if (index % props.cacheStore.serverSize === 0) {
                    conf.rowSpan = props.cacheStore.serverSize
                } else {
                    conf.rowSpan = 0
                }
                return {
                    children: text,
                    props: conf
                }
            }
        }, {
            title: 'Server',
            dataIndex: 'serverIP'
        }, {
            title: 'Created',
            dataIndex: 'createTime'
        }, {
            title: 'Expires (s)',
            dataIndex: 'expiresInSeconds',
        }];
    }

    render() {
        return (
            <Table dataSource={this.props.cacheStore.detailTable.data.slice(0)}
                   columns={this.cacheDetailsTableColumns}
                   size='small'/>
        );
    }
}));