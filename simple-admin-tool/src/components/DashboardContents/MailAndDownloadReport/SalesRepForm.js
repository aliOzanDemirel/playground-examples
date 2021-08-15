import React from 'react';
import {Button, Form, Input} from "antd";
import {getConfirmation} from "../../Common/CommonComponents";

let SalesRepForm = class SalesRepForm extends React.Component {

    submitForm = () => {
        this.props.form.validateFields((err, values) => {
            if (!err) {
                this.props.sendMailAction(values);
            }
        });
    };

    handleClick = () => {
        getConfirmation(this.submitForm, 'Are you sure to send order reports to sales representatives?')
    };

    render() {
        let {getFieldDecorator} = this.props.form;

        return (
            <Form layout='inline'>
                <Form.Item>
                    {getFieldDecorator('salesOrganization', {
                        rules: [{required: true, message: 'Enter sales organization'}]
                    })(
                        <Input placeholder='Sales Organization'/>
                    )}
                </Form.Item>
                <Form.Item>
                    {getFieldDecorator('salesRepEmail', {
                        rules: [{
                            type: 'email', message: 'Input is not valid e-mail',
                        }, {
                            required: true, message: 'Enter sales representative e-mail',
                        }]
                    })(
                        <Input placeholder='Sales Rep E-mail'/>
                    )}
                </Form.Item>
                <Form.Item>
                    <Button htmlType="button" icon="enter" onClick={this.handleClick}>Send Mails</Button>
                </Form.Item>
            </Form>
        );
    }
};

export default Form.create()(SalesRepForm);