import React from 'react';
import {inject, observer} from "mobx-react";
import {Button, Col, Modal, Row, Table} from "antd";
import {LoadingSpin} from "../../Common/CommonComponents";
import UserForm from "./UserForm";
import {updateSelectedRows} from "../../../common/commons";

const userListingColumns = [{
    title: "Username",
    dataIndex: "username",
    width: '10%'
}, {
    title: "E-Mail",
    dataIndex: "email",
    width: '25%'
}, {
    title: 'Authorities',
    dataIndex: 'authorities'
}];

export default inject('userStore')(observer(class UserManagement extends React.Component {

    componentDidMount = () => {
        this.props.userStore.loadUsers();
        this.props.userStore.loadUserRoles();
    };

    userManagementActionButtons = () => {
        let {removeUser, editUser, addUser} = this.props.userStore;
        return <Row>
            <Col span={3}>
                <Button onClick={addUser}>Add New User</Button>
            </Col>
            <Col span={4}>
                <Button onClick={editUser}>Edit Selected User</Button>
            </Col>
            <Col span={4}>
                <Button onClick={removeUser}>Remove Selected User</Button>
            </Col>
        </Row>
    };

    render() {
        let store = this.props.userStore;
        let selRowKeys = store.table.selRows.map(it => it.key);
        let rowSelection = {
            selRowKeys,
            onChange: (selectedRowKeys, selectedRows) => {
                updateSelectedRows(store.table, selectedRows);
            }
        };
        return (
            <div>
                <LoadingSpin spinning={store.loadingUsers}>
                    <Table title={this.userManagementActionButtons} rowSelection={rowSelection}
                           columns={userListingColumns} dataSource={store.table.data.slice(0)}/>
                </LoadingSpin>
                <Modal title="Add User" okText="Save" wrapClassName="vertical-center-modal"
                       visible={store.formVisible} destroyOnClose={true}
                       onCancel={store.hideForm} onOk={store.saveUser}>
                    <UserForm/>
                </Modal>
            </div>
        );
    }
}));