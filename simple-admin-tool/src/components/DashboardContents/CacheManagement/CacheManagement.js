import React from 'react';
import {inject, observer} from "mobx-react";
import {Button, Col, Modal, Row, Table} from "antd";
import CacheDetailsTable from "./CacheDetailsTable";
import {updateSelectedRows} from "../../../common/commons";

export default inject('cacheStore')(observer(class CacheManagement extends React.Component {

    constructor(props) {
        super(props);

        this.cacheTableColumns = [{
            title: 'Cache Name',
            dataIndex: 'cacheName',
            sorter: (first, second) => {
                if (first.cacheName < second.cacheName) {
                    return -1
                } else if (first.cacheName > second.cacheName) {
                    return 1
                }
                return 0
            },
            render: (text, row, index) => {
                const obj = {
                    children: <Button onClick={() => props.cacheStore.openDetails(text)}>{text}</Button>,
                    props: {}
                };
                if (index % props.cacheStore.serverSize === 0) {
                    obj.props.rowSpan = props.cacheStore.serverSize
                } else {
                    obj.props.rowSpan = 0
                }
                return obj
            }
        }, {
            title: 'Server',
            dataIndex: 'serverIP',
            sorter: (first, second) => {
                if (first.serverIP < second.serverIP) {
                    return -1
                } else if (first.serverIP > second.serverIP) {
                    return 1
                }
                return 0
            }
        }, {
            title: 'Status',
            dataIndex: 'status'
        }, {
            title: 'Items',
            dataIndex: 'cacheSize'
        }, {
            title: 'Memory Hits',
            dataIndex: 'cacheMemoryStoreHitCount'
        }, {
            title: 'Disk Hits',
            dataIndex: 'cacheDiskStoreHitCount'
        }, {
            title: 'Not Found Misses',
            dataIndex: 'cacheMissCountNotFound'
        }, {
            title: 'Expired Misses',
            dataIndex: 'cacheMissCountExpired'
        }];
    }

    // get cache stats when component is loaded and continue requesting stats in every 30 seconds
    componentDidMount = () => {
        this.props.cacheStore.getCacheInfos();
        this.cacheInfoTimer = setInterval(this.props.cacheStore.getCacheInfos, 30000);
    };

    componentWillUnmount = () => {
        clearInterval(this.cacheInfoTimer);
    };

    cacheActionButtons = () => {
        let {removeAllCaches, removeCache} = this.props.cacheStore;
        return <Row>
            <Col span={4}>
                <Button onClick={removeAllCaches}>Delete All Caches</Button>
            </Col>
            <Col span={4}>
                <Button onClick={removeCache}>Delete Selected Cache</Button>
            </Col>
        </Row>
    };

    render() {
        let store = this.props.cacheStore;
        let selRowKeys = store.table.selRows.map(it => it.key);
        let rowSelection = {
            selRowKeys,
            onChange: (selectedRowKeys, selectedRows) => {
                updateSelectedRows(store.table, selectedRows);
            }
        };
        return (
            <div>
                <Table title={this.cacheActionButtons}
                       rowSelection={rowSelection}
                       columns={this.cacheTableColumns}
                       dataSource={store.table.data.slice(0)}
                       loading={store.loading}
                       pagination={{pageSize: 50}}
                       size='middle'/>

                <Modal title="Cache Details" wrapClassName="vertical-center-modal"
                       visible={store.detailTableVisible} destroyOnClose={true}
                       onCancel={store.hideDetails} onOk={store.hideDetails}>
                    <CacheDetailsTable/>
                </Modal>
            </div>
        );
    }
}));