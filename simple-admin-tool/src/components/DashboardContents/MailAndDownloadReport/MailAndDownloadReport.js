import React from 'react';
import {inject, observer} from "mobx-react";
import {Button, Col, Collapse, DatePicker, Radio, Row} from "antd";
import {defaultBackendDateFormat, onFieldChange, panelStyle} from "../../../common/commons";
import SalesRepForm from "./SalesRepForm";

export default inject('reportMailerStore')(observer(class MailAndDownloadReport extends React.Component {

    componentWillUnmount = () => {
        this.props.reportMailerStore.resetFields();
    };

    render() {
        let {
            inputOrderReportSendStatus, onDateChange, downloadSubscriptionReport,
            sendMailSubscriptionDetails, sendOrderReportMailToSalesRep
        } = this.props.reportMailerStore;

        return (
            <Collapse bordered={false} activeKey={['subscriptions', 'representatives']}>
                <Collapse.Panel key='subscriptions' style={panelStyle} showArrow={false}
                                header='Report Subscription Details'>
                    <Row className="row-margin row-padding">
                        <Col span={5}>
                            <Radio.Group value={inputOrderReportSendStatus} onChange={(e) =>
                                onFieldChange(this.props.reportMailerStore, 'inputOrderReportSendStatus', e)}>
                                <Radio.Button key='1' value='WBS'>Will be sent</Radio.Button>
                                <Radio.Button key='2' value='AS'>Already sent</Radio.Button>
                            </Radio.Group>
                        </Col>
                        <Col span={5}>
                            <DatePicker format={defaultBackendDateFormat} placeholder='Date' onChange={onDateChange}/>
                        </Col>
                        <Col span={5}>
                            <Button icon='download' onClick={downloadSubscriptionReport}>Download Subscriptions</Button>
                        </Col>
                        <Col span={5}>
                            <Button icon='enter' onClick={sendMailSubscriptionDetails}>Send Mails</Button>
                        </Col>
                    </Row>
                </Collapse.Panel>
                <Collapse.Panel key='representatives' style={panelStyle} showArrow={false}
                                header='Order Report to Representatives'>
                    <SalesRepForm sendMailAction={sendOrderReportMailToSalesRep}/>
                </Collapse.Panel>
            </Collapse>
        );
    }
}));