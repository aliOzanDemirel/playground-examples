import React from 'react';
import {inject, observer} from "mobx-react";
import {Button, Col, Divider, Input, Row} from "antd";
import {onFieldChange} from "../../../common/commons";
import ErrorSearchMessageBox from "./ErrorSearchMessageBox";
import ErrorSearchTable from "./ErrorSearchTable";

export default inject('errSearchStore')(observer(class ErrorSearchManagement extends React.Component {

    componentDidMount = () => {
        this.props.errSearchStore.listLastErrors();
    };

    render() {
        let {getError, sysErrorMsg, inputErrorId} = this.props.errSearchStore;

        let showIfErrorExists = sysErrorMsg === null ? "hidden" : "";

        return (
            <div>
                <Row className="row-margin-10">
                    <Col span={5} className="col-margin">
                        <Input placeholder="Type error ID" onPressEnter={getError} value={inputErrorId}
                               onChange={(e) => onFieldChange(this.props.errSearchStore, 'inputErrorId', e)}/>
                    </Col>
                    <Col span={4} className="col-margin">
                        <Button onClick={getError}>
                            Search Error
                        </Button>
                    </Col>
                </Row>
                <Row className={"row-margin " + showIfErrorExists}>
                    <ErrorSearchMessageBox type="info" observedVal={this.props.errSearchStore}/>
                </Row>
                <Divider/>
                <ErrorSearchTable/>
            </div>
        );
    }
}));