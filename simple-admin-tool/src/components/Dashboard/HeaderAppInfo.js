import React from 'react';
import {Button, Col, Row, Switch} from "antd";
import {inject, observer} from "mobx-react";

export default inject('loginStore', 'appRouter')(observer(class HeaderAppInfo extends React.Component {

    handleLogout = (e) => {
        let {appRouter, loginStore} = this.props;
        if (loginStore.isLoggedIn) {
            loginStore.logout().then(() => {
                appRouter.replace('/admin/login');
            });
        }
    };

    render() {
        const {loginStore} = this.props;
        return (
            loginStore.isLoggedIn &&
            <Row>
                <Col span={5}>
                    <h5 className="header-text">Version: {loginStore.appInfo && loginStore.appInfo.version}</h5>
                </Col>
                <Col span={6}>
                    <h5 className="header-text">Startup: {loginStore.appInfo && loginStore.appInfo.startupDate}</h5>
                </Col>
                <Col span={3}>
                    <h5 className="header-text">IP: {loginStore.appInfo && loginStore.appInfo.serverIP}</h5>
                </Col>
                <Col span={2}>
                    <h5 className="header-text">Cluster Mode:</h5>
                </Col>
                <Col span={2}>
                    <Switch onChange={loginStore.switchClusterMode} checked={loginStore.api.cluster}/>
                </Col>
                <Col span={4}>
                    <h4 className="header-text">Hello {loginStore.authenticatedUser}</h4>
                </Col>
                <Col span={1}>
                    <Button style={{align: 'center'}} onClick={this.handleLogout} icon="logout" size="small">
                        Logout
                    </Button>
                </Col>
            </Row>
        );
    }
}));