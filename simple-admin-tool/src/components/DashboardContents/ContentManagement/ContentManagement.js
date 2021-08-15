import React from 'react';
import {inject, observer} from "mobx-react";
import {Button, Col, Divider, Row} from "antd";
import MultipleRadioButtons from "../../Common/MultipleRadioButtons";
import {onFieldChange} from "../../../common/commons";

export default inject('contentStore')(observer(class ContentManagement extends React.Component {

    componentDidMount() {
        this.props.contentStore.getAvailableRepos();
    }

    render() {
        let {
            currentContentRepo, availableContentRepos, inputContentRepo, switchContentRepo
        } = this.props.contentStore;

        return (
            <div>
                <Row>
                    <h3>
                        Current content repository is: {currentContentRepo}
                    </h3>
                </Row>
                <Divider/>
                <Row type="flex" justify="start">
                    <Col span={10}>
                        <MultipleRadioButtons currentVal={inputContentRepo} values={availableContentRepos}
                                              onChange={(e) => onFieldChange(this.props.contentStore, 'inputContentRepo', e)}/>
                    </Col>
                    <Col span={4}>
                        <Button onClick={switchContentRepo}>
                            Switch Repository
                        </Button>
                    </Col>
                </Row>
            </div>
        );
    }
}));