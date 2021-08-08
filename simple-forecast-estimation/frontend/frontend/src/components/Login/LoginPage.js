import React from 'react';
import {inject, observer} from 'mobx-react';
import {Button, Form, Icon, Input} from "antd";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import {pages} from "../../common/commons";

const registerFormLayout = {
    labelCol: {
        xs: {span: 24},
        sm: {span: 24},
        md: {span: 0},
        lg: {span: 0},
        xl: {span: 0}
    },
    wrapperCol: {
        xs: {span: 24},
        sm: {span: 24},
        md: {span: 11, offset: 7},
        lg: {span: 11, offset: 7},
        xl: {span: 11, offset: 7}
    }
};

let LoginPage = inject('appRouter', 'loginStore')(observer(class LoginPage extends React.Component {

    onClickSignUpButton = (e) => {
        this.props.appRouter.push(pages.signUp);
    };

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
            <Form onSubmit={this.handleSubmit} layout='horizontal' role='form'>
                <Row>
                    <div className="form-centered">
                        <Col xs sm md lg xl>

                            <Form.Item {...registerFormLayout} >
                                {getFieldDecorator('username', {
                                    rules: [{
                                        type: 'email', message: 'Not a valid e-mail',
                                    }, {
                                        required: true, message: 'Enter e-mail',
                                    }]
                                })(<Input prefix={<Icon type="mail" style={{color: 'rgba(0,0,0,.25)'}}/>}
                                          placeholder="Username (email)"/>)}
                            </Form.Item>

                            <Form.Item {...registerFormLayout}>
                                {getFieldDecorator('password', {
                                    rules: [{
                                        required: true, message: 'Enter password',
                                    }]
                                })(<Input prefix={<Icon type="lock" style={{color: 'rgba(0,0,0,.25)'}}/>}
                                          type="password" placeholder="Password"/>)}
                            </Form.Item>

                            <div className='d-flex justify-content-center'>
                                <Button htmlType="submit" type="primary" className='mr-2 ml-auto'>Login</Button>
                                <Button onClick={this.onClickSignUpButton} htmlType="button" type="primary"
                                        className='ml-2 mr-auto'>Sign up</Button>
                            </div>
                        </Col>
                    </div>
                </Row>
            </Form>
        );
    }
}));

export default Form.create({})(LoginPage);
