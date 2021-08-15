import React from 'react';
import {inject, observer} from "mobx-react";
import {Table} from "antd";
import {LoadingSpin} from "../../Common/CommonComponents";

const errorListingColumns = [{
    title: 'Id',
    dataIndex: 'id',
    width: '15%'
}, {
    title: 'Message',
    dataIndex: 'message',
    width: '45%'
}, {
    title: 'Date',
    dataIndex: 'errorDate',
    width: '20%'
}, {
    title: 'Request',
    dataIndex: 'request',
    width: '20%'
}];

export default inject('errSearchStore')(observer(class ErrorSearchTable extends React.Component {
    render() {
        let {loading, data} = this.props.errSearchStore.table;
        return (
            <LoadingSpin spinning={loading}>
                <Table title={() => 'Last Recorded Errors'}
                       columns={errorListingColumns}
                       dataSource={data.slice(0, data.length)}
                       pagination={{pageSize: 20}}/>
            </LoadingSpin>
        );
    }
}));