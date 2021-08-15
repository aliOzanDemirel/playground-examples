import React from 'react';
import {inject, observer} from "mobx-react";
import {Button, Col, Form, Icon, Input, InputNumber, Row, Switch} from "antd";
import {badNews} from "../../../common/service/notifier-service";
import {getConfirmation} from "../../Common/CommonComponents";

const rememberMeFormItemStyle = {
    labelCol: {span: 5},
    wrapperCol: {span: 5}
};

let RememberMeForm = inject('manageLoginStore')(observer(class RememberMeForm extends React.Component {

    submitForm = () => {
        let {getFieldValue, validateFields} = this.props.form;
        if (getFieldValue('timeDifference') < 0 || getFieldValue('timeDifferenceDelta') < 0) {
            badNews('Number should be greater than 0', 'Error', 2)
        } else {
            validateFields((err, values) => {
                if (!err) {
                    this.props.manageLoginStore.updateRememberMeDetails(values);
                }
            });
        }
    };

    handleClick = () => {
        getConfirmation(this.submitForm, 'Are you sure to submit form?')
    };

    handleReset = () => {
        this.props.form.resetFields();
    };

    render() {
        let {getFieldDecorator} = this.props.form;
        let {
            initialIsSecureConnection, initialEncryptionKey,
            initialTimeDifference, initialTimeDifferenceDelta
        } = this.props.manageLoginStore;

        return (
            <Form layout="vertical">
                <Form.Item label="Time Difference" {...rememberMeFormItemStyle} >
                    {getFieldDecorator('timeDifference', {
                        initialValue: initialTimeDifference,
                        rules: [{
                            required: true, message: 'Reset form to avoid inputting this field'
                        }, {
                            type: 'number',
                            message: 'You need to enter number'
                        }]
                    })(<InputNumber min={-1}/>)}
                </Form.Item>
                <Form.Item label="Time Difference Delta" {...rememberMeFormItemStyle} >
                    {getFieldDecorator('timeDifferenceDelta', {
                        initialValue: initialTimeDifferenceDelta,
                        rules: [{
                            required: true, message: 'Reset form to avoid inputting this field'
                        }, {
                            type: 'number',
                            message: 'You need to enter number'
                        }]
                    })(<InputNumber min={-1}/>)}
                </Form.Item>
                <Form.Item label="Secure Connection (https)" {...rememberMeFormItemStyle}>
                    {getFieldDecorator('isSecureConnection', {
                        initialValue: initialIsSecureConnection
                    })(<Switch defaultChecked={initialIsSecureConnection}/>)}
                </Form.Item>
                <Form.Item label="Encryption Key" {...rememberMeFormItemStyle}>
                    {getFieldDecorator('encryptionKey', {
                        initialValue: initialEncryptionKey,
                        rules: [{required: true, message: 'Reset form to avoid inputting this field'}]
                    })(<Input prefix={<Icon type="lock" style={{color: 'rgba(0,0,0,.25)'}}/>}
                              placeholder="Encryption Key"/>)}
                </Form.Item>
                <Row>
                    <Col span={3}>
                        <Form.Item>
                            <Button htmlType="button" icon="enter" onClick={this.handleClick}>Submit</Button>
                        </Form.Item>
                    </Col>
                    <Col span={3}>
                        <Form.Item>
                            <Button htmlType="button" icon="rollback" onClick={this.handleReset}>Reset</Button>
                        </Form.Item>
                    </Col>
                </Row>
            </Form>
        );
    }
}));

export default Form.create()(RememberMeForm);