import React from 'react';
import {inject, observer} from "mobx-react";
import {Button, Col, Input, Row} from "antd";
import {onFieldChange} from "../../../common/commons";

export default inject('codeEditorStore')(observer(class CodeEditor extends React.Component {
    render() {
        let {
            inputPath, inputTextArea, loadFile, saveFile
        } = this.props.codeEditorStore;

        return (
            <Row className="row-margin row-padding">
                <Row className="row-margin row-padding">
                    <Col span={16}>
                        <Input placeholder='Enter valid JSP path' value={inputPath} onPressEnter={loadFile}
                               onChange={(e) => onFieldChange(this.props.codeEditorStore, 'inputJspPath', e)}/>
                    </Col>
                    <Col span={3} offset={2}>
                        <Button icon='reload' onClick={loadFile}>
                            Load JSP
                        </Button>
                    </Col>
                    <Col span={3}>
                        <Button icon='save' onClick={saveFile}>
                            Save JSP
                        </Button>
                    </Col>
                </Row>
                <Row className="row-margin row-padding">
                    <Col span={24}>
                        <Input.TextArea value={inputTextArea} rows={40}/>
                    </Col>
                </Row>
            </Row>
        );
    }
}));