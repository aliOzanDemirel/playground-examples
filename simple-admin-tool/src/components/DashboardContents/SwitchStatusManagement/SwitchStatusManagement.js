import React from 'react';
import {inject, observer} from "mobx-react";
import {Col, Divider, Row, Switch} from "antd";
import {getTextual} from "../../Common/CommonComponents";

export default inject('switchStatusStore')(observer(class SwitchStatusManagement extends React.Component {

    componentDidMount() {
        this.props.switchStatusStore.initFlagsStates();
    }

    render() {
        let {
            switchSearchFlag, searchEnabled, switchOrderLocking, orderLockingEnabled,
            switchPayment, paymentEnabled, togglePdfEnabled, isPdfEnabled,
        } = this.props.switchStatusStore;

        return (
            <div style={{padding: '30px 0 0 0'}}>
                <Row>
                    <Col span={8} offset={1}>
                        <h4>
                            Search functionality is: {getTextual(searchEnabled, true)}
                        </h4>
                    </Col>
                    <Col span={3}>
                        <Switch onChange={switchSearchFlag} checked={searchEnabled}/>
                    </Col>
                </Row>
                <Divider/>
                <Row>
                    <Col span={8} offset={1}>
                        <h4>
                            Locking order functionality is: {getTextual(orderLockingEnabled, true)}
                        </h4>
                    </Col>
                    <Col span={3}>
                        <Switch onChange={switchOrderLocking} checked={orderLockingEnabled}/>
                    </Col>
                </Row>
                <Divider/>
                <Row>
                    <Col span={8} offset={1}>
                        <h4>
                            Payment functionality is: {getTextual(paymentEnabled, true)}
                        </h4>
                    </Col>
                    <Col span={3}>
                        <Switch onChange={switchPayment} checked={paymentEnabled}/>
                    </Col>
                </Row>
                <Divider/>
                <Row>
                    <Col span={8} offset={1}>
                        <h4>
                            PDF is: {getTextual(isPdfEnabled, true)} in application
                        </h4>
                    </Col>
                    <Col span={3}>
                        <Switch onChange={togglePdfEnabled} checked={isPdfEnabled}/>
                    </Col>
                </Row>
            </div>
        );
    }
}));