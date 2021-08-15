import React from 'react';
import {inject, observer} from "mobx-react";
import {Button, Col, Collapse, Divider, Input, Row} from "antd";
import MultipleRadioButtons from "../../Common/MultipleRadioButtons";
import {onFieldChange, panelStyle} from "../../../common/commons";

export default inject('sapStore')(observer(class SapManagement extends React.Component {

    componentDidMount = () => {
        this.props.sapStore.getSapEnvInfo();
    };

    render() {
        let {
            currentEnv, availableEnvironments, inputSapEnv, inputAccountId, switchOrCheckSapEnv, validateSapAccount
        } = this.props.sapStore;

        return (
            <div>
                <Row>
                    <h3>
                        Current SAP environment is: {currentEnv}
                    </h3>
                </Row>
                <Divider/>
                <Row>
                    <Collapse bordered={false} accordion defaultActiveKey={['envSwitch', 'accountValidation']}>
                        <Collapse.Panel key="envSwitch" header="SAP Environment Switch"
                                        style={panelStyle} showArrow={false}>
                            <Row className="row-margin row-padding">
                                <Col span={6}>
                                    <MultipleRadioButtons currentVal={inputSapEnv} values={availableEnvironments}
                                                          onChange={(e) => onFieldChange(this.props.sapStore, 'inputSapEnv', e)}/>
                                </Col>
                                <Col span={4}>
                                    <Button onClick={() => switchOrCheckSapEnv(false)}>
                                        Check Connection</Button>
                                </Col>
                                <Col span={4}>
                                    <Button onClick={() => switchOrCheckSapEnv(true)}>
                                        Switch Environment</Button>
                                </Col>
                            </Row>
                        </Collapse.Panel>
                        <Collapse.Panel key="accountValidation" header="Account Validation"
                                        style={panelStyle} showArrow={false}>
                            <Row className="row-margin row-padding">
                                <Col span={6} className="col-margin">
                                    <Input placeholder="Enter SAP ID" value={inputAccountId}
                                           onPressEnter={validateSapAccount}
                                           onChange={(e) => onFieldChange(this.props.sapStore, 'inputAccountId', e)}/>
                                </Col>
                                <Col span={4} className="col-margin">
                                    <Button onClick={validateSapAccount}>
                                        Check Account
                                    </Button>
                                </Col>
                            </Row>
                        </Collapse.Panel>
                    </Collapse>
                </Row>
            </div>
        );
    }
}));