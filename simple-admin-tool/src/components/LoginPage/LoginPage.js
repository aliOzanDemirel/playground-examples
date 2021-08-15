import React from 'react';
import {inject, observer} from "mobx-react";
import Form from "antd/es/form/Form";
import Input from "antd/es/input/Input";
import Button from "antd/es/button/button";
import Icon from "antd/es/icon/index";

let Login = inject('appRouter', 'loginStore')(observer(class LoginPage extends React.Component {

    handleSubmit = (e) => {

        // prevent form submission
        e.preventDefault();

        this.props.form.validateFields((err, values) => {
            if (!err) {
                this.props.loginStore.login(values, this.props.appRouter);
            }
        });
    };

    render() {
        let {getFieldDecorator} = this.props.form;
        return (
            <div className="block">
                <div className="centered">
                    <Form onSubmit={this.handleSubmit}>
                        <Form.Item>
                            {getFieldDecorator('username', {
                                rules: [{required: true, message: 'Enter username'}]
                            })(
                                <Input prefix={<Icon type="user" style={{color: 'rgba(0,0,0,.25)'}}/>}
                                       placeholder="Username"/>
                            )}
                        </Form.Item>
                        <Form.Item>
                            {getFieldDecorator('password', {
                                rules: [{required: true, message: 'Enter password'}]
                            })(
                                <Input prefix={<Icon type="lock" style={{color: 'rgba(0,0,0,.25)'}}/>}
                                       type="password" placeholder="Password"/>
                            )}
                        </Form.Item>
                        <Form.Item>
                            <Button htmlType="submit" className="login-form-button" icon="login">Login</Button>
                        </Form.Item>
                    </Form>
                </div>
            </div>
        );
    }
}));

export default Form.create()(Login);