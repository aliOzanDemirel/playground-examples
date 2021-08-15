import React from 'react';
import {inject, observer} from "mobx-react";
import {Button, Col, Row, Table} from "antd";
import {updateSelectedRows} from "../../../common/commons";

export default inject('siteStore')(observer(class SiteManagement extends React.Component {

    constructor(props) {
        super(props);

        this.siteTableColumns = [{
            width: '20%',
            title: 'Site Name',
            dataIndex: 'siteName',
            sorter: (first, second) => {
                if (first.siteName < second.siteName) {
                    return -1
                } else if (first.siteName > second.siteName) {
                    return 1
                }
                return 0
            },
            render: (text, row, index) => {
                const obj = {
                    children: <em style={{paddingLeft: 20, fontSize: 18}}>{text}</em>,
                    props: {}
                };
                if (index % props.siteStore.serverSize === 0) {
                    obj.props.rowSpan = props.siteStore.serverSize
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
            title: 'Last Initialized',
            dataIndex: 'lastInitDate'
        }];
    }

    componentDidMount = () => {
        this.props.siteStore.loadTableData();
    };

    siteActionButtons = () => {
        let {goToWebsite, runIndexerForSite, initializeSite, loadTableData} = this.props.siteStore;
        return <Row type="flex" justify="start">
            <Col span={4}>
                <Button onClick={goToWebsite}>Open Selected Site</Button>
            </Col>
            <Col span={3}>
                <Button onClick={loadTableData}>Refresh Table</Button>
            </Col>
            <Col span={3} offset={1}>
                <Button onClick={() => runIndexerForSite('all')}>Index All Sites</Button>
            </Col>
            <Col span={3}>
                <Button onClick={runIndexerForSite}>Index Selected</Button>
            </Col>
            <Col span={3} offset={1} className='col-margin-6'>
                <Button onClick={() => initializeSite('all')}>Initialize All Sites</Button>
            </Col>
            <Col span={3} className='col-margin-6'>
                <Button onClick={initializeSite}>Initialize Selected</Button>
            </Col>
        </Row>
    };

    render() {
        let store = this.props.siteStore;
        let selRowKeys = store.table.selRows.map(it => it.key);
        let rowSelection = {
            selRowKeys,
            onChange: (selectedRowKeys, selectedRows) => {
                updateSelectedRows(store.table, selectedRows);
            }
        };
        return (
            <div>
                <Table title={this.siteActionButtons}
                       rowSelection={rowSelection}
                       columns={this.siteTableColumns}
                       dataSource={store.table.data.slice(0)}
                       loading={store.loading}
                       pagination={{pageSize: 50}}
                       size='middle'/>
            </div>
        );
    }
}));