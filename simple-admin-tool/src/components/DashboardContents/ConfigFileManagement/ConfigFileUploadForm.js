import React from 'react';
import {inject, observer} from "mobx-react";
import {Button, Col, Form, Icon, Row, Select, Upload} from "antd";

let ConfigFileUploadForm = inject('configFileStore')(observer(class ConfigFileUploadForm extends React.Component {

    componentDidMount = () => {
        this.props.configFileStore.loadConfigTypes();
    };

    handleUpload = () => {
        this.props.form.validateFields((err, values) => {
            if (!err) {
                this.props.configFileStore.uploadFile(values);
            }
        });
    };

    resetUploader = (val, option) => {
        this.props.form.resetFields();
        this.props.configFileStore.unlockUploader()
    };

    // cancel antd upload component's automatic action
    beforeUpload = (file) => {
        return false;
    };

    // remove old file that was pushed to fileList while returning fileList to form's values
    getFileList = (antFileWrapper) => {
        if (Array.isArray(antFileWrapper)) {
            antFileWrapper = antFileWrapper.splice(0, 1);
            return antFileWrapper;
        }
        antFileWrapper.fileList = antFileWrapper.fileList.splice(0, 1);
        return antFileWrapper && antFileWrapper.fileList;
    };

    render() {
        let {upload, configOptions} = this.props.configFileStore;
        let {getFieldDecorator} = this.props.form;
        return (
            <Form>
                <Row gutter={20}>
                    <Col span={8}>
                        <Form.Item hasFeedback>
                            {getFieldDecorator('configFileType', {
                                rules: [{required: true, message: 'You must select a configuration'}],
                            })(
                                <Select placeholder='Select a configuration' onSelect={this.resetUploader}>
                                    {/*<Select.Option value="" selected>U.S.A</Select.Option>*/}
                                    {
                                        configOptions.map((it, index) =>
                                            <Select.Option key={index} value={it}>{it}</Select.Option>)
                                    }
                                </Select>
                            )}
                        </Form.Item>
                    </Col>
                    <Col span={4}>
                        <Form.Item>
                            <Button icon="upload" htmlType="button" onClick={this.handleUpload}>
                                Upload File
                            </Button>
                        </Form.Item>
                    </Col>
                </Row>
                <Form.Item>
                    <div className="dropbox">
                        {getFieldDecorator('configFile', {
                            valuePropName: 'fileList',
                            getValueFromEvent: this.getFileList,
                            rules: [{required: true, message: 'You must select a file'}]
                        })(<Upload.Dragger name='configFile' disabled={upload.disabled}
                                           beforeUpload={this.beforeUpload}>
                            <p className="ant-upload-drag-icon">
                                <Icon type="inbox"/>
                            </p>
                            <p className="ant-upload-text">Click or drag file to this area to upload</p>
                        </Upload.Dragger>)}
                    </div>
                </Form.Item>
            </Form>
        );
    }
}));

export default Form.create()(ConfigFileUploadForm);