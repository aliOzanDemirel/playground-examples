import React from 'react';
import {inject, observer} from "mobx-react";
import {Button, Col, Collapse, DatePicker, Row} from "antd";
import {defaultBackendDateFormat, panelStyle} from "../../../common/commons";
import RememberMeForm from "./RememberMeForm";

export default inject('manageLoginStore')(observer(class LoginManagement extends React.Component {
    render() {
        let {downloadLoginAudits, onAuditDatesChange, onPanelChange} = this.props.manageLoginStore;
        return (
            <Collapse bordered={false} accordion defaultActiveKey='loginReport' onChange={onPanelChange}>
                <Collapse.Panel key="loginReport" style={panelStyle} showArrow={false}
                                header="Download Login Audits">
                    <Row className="row-margin row-padding">
                        <Col span={8}>
                            <DatePicker.RangePicker onChange={onAuditDatesChange} format={defaultBackendDateFormat}/>
                            {/*defaultValue={[moment('2018-01-01', rangeDateFormat),*/}
                            {/*moment('2018-02-24', rangeDateFormat)]}*/}
                        </Col>
                        <Col span={4}>
                            <Button onClick={downloadLoginAudits}>
                                Download
                            </Button>
                        </Col>
                    </Row>
                </Collapse.Panel>
                <Collapse.Panel key="rememberMeUpdate" style={panelStyle} showArrow={false}
                                header="Update Remember Me Options">
                    <Row className="row-margin row-padding">
                        <RememberMeForm/>
                    </Row>
                </Collapse.Panel>
            </Collapse>
        );
    }
}));