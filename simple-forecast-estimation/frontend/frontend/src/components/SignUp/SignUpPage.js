import React from 'react';
import {inject, observer} from 'mobx-react';
import {Button, Checkbox, Form, Icon, Input} from "antd";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import {COOKIE_NOTICE_PAGE, GDPR_NOTICE_PAGE} from "../../common/commons";

const formLayout = 'horizontal';
const buttonItemLayout = formLayout === 'horizontal' ? {
    wrapperCol: {
        // xs: {span: 24},
        xs: {span: 3, offset: 9},
        sm: {span: 3, offset: 9},
        md: {span: 3, offset: 11},
        lg: {span: 3, offset: 11},
        xl: {span: 3, offset: 11}
    }
} : null;

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

let handleEmailChange, disableButton;
let SignUpPage = inject('appRouter', 'signUpStore')(observer(class SignUpPage extends React.Component {

    componentWillMount = () => {
        handleEmailChange = this.props.signUpStore.handleEmailChange;
        disableButton = this.props.signUpStore.disableSignUp;
    };

    handleSubmit = (e) => {

        // prevent form submission
        e.preventDefault();
        this.props.form.validateFields((err, values) => {
            if (!err) {
                this.props.signUpStore.saveUser(values, this.props.appRouter);
            }
        });
    };

    handleConfirmPasswordBlur = (value, store) => {
        store.markPasswordDirty(store.formPasswordDirty || !!value);
    };

    checkIfPasswordsMatch = (form, value, callback) => {
        if (value && value !== form.getFieldValue('password')) {
            callback('Passwords do not match!');
            disableButton(true)
        } else {
            form.validateFields(['username', 'gdprConfirmed', 'cookieConfirmed'], {force: true}, (errors, values) => {
                if (!errors) {
                    disableButton(false);
                }
            });
            callback();
        }
    };

    checkConfirmPassword = (form, store, value, callback) => {
        if (value && store.formPasswordDirty) {
            form.validateFields(['confirm'], {force: true});
        }
        callback();
    };

    render() {
        let store = this.props.signUpStore;
        let {getFieldDecorator} = this.props.form;
        return (
            <Form id='signUpForm' role='form' onSubmit={this.handleSubmit} layout={formLayout}>
                <Row>
                    <div className="form-centered">
                        <Col xs sm md lg xl>

                            <Form.Item {...registerFormLayout} >
                                {getFieldDecorator('name', {
                                    rules: [{
                                        required: false
                                    }]
                                })(<Input prefix={<Icon type="user" style={{color: 'rgba(0,0,0,.25)'}}/>}
                                          placeholder="Name (Optional)"/>)}
                            </Form.Item>

                            <Form.Item {...registerFormLayout} >
                                {getFieldDecorator('username', {
                                    rules: [{
                                        type: 'email', message: 'Not a valid e-mail'
                                    }, {
                                        required: true, message: 'Enter e-mail'
                                    }]
                                })(<Input prefix={<Icon type="mail" style={{color: 'rgba(0,0,0,.25)'}}/>}
                                          placeholder="Email for username"/>)}
                            </Form.Item>

                            <Form.Item {...registerFormLayout}>
                                {getFieldDecorator('password', {
                                    rules: [{
                                        required: true, message: 'Enter password'
                                    }, {
                                        validator: (rule, value, callback) =>
                                            this.checkConfirmPassword(this.props.form, store, value, callback)
                                    }]
                                })(<Input prefix={<Icon type="lock" style={{color: 'rgba(0,0,0,.25)'}}/>}
                                          type="password" placeholder="Password"/>)}
                            </Form.Item>

                            <Form.Item {...registerFormLayout}>
                                {getFieldDecorator('confirm', {
                                    rules: [{
                                        required: true, message: 'Confirm password'
                                    }, {
                                        validator: (rule, value, callback) => this.checkIfPasswordsMatch(
                                            this.props.form, value, callback)
                                    }]
                                })(<Input prefix={<Icon type="lock" style={{color: 'rgba(0,0,0,.25)'}}/>}
                                          type="password" placeholder="Confirm password"
                                          onBlur={(e) => this.handleConfirmPasswordBlur(e.target.value, store)}/>)}
                            </Form.Item>

                            <Form.Item {...registerFormLayout}>
                                {getFieldDecorator('gdprConfirmed', {
                                    valuePropName: 'checked',
                                    initialValue: false,
                                    rules: [{
                                        required: true,
                                        message: 'You have to consent',
                                        transform: value => (value || undefined),
                                        type: 'boolean'
                                    }]
                                })(<Checkbox>
                                    <span id='gdprConsentText'>
                                        GDPR ok? <a target="_blank" href={GDPR_NOTICE_PAGE}>notice</a>
                                    </span>
                                </Checkbox>)}
                            </Form.Item>

                            <Form.Item {...registerFormLayout}>
                                {getFieldDecorator('cookieConfirmed', {
                                    valuePropName: 'checked',
                                    initialValue: false,
                                    rules: [{
                                        required: true,
                                        message: 'You have to consent',
                                        transform: value => (value || undefined),
                                        type: 'boolean'
                                    }]
                                })(<Checkbox>
                                    <span id='gdprConsentText'>
                                        Cookie ok? <a target="_blank" href={COOKIE_NOTICE_PAGE}>notice</a>
                                    </span>
                                </Checkbox>)}
                            </Form.Item>

                            <Form.Item {...buttonItemLayout} >
                                <Button htmlType="submit" type="primary" className='mx-auto'
                                        disabled={store.signUpDisabled}>Sign up</Button>
                            </Form.Item>
                        </Col>
                    </div>
                </Row>
            </Form>
        );
    }
}));

export default Form.create({
    onValuesChange: (props, changedValues, allValues) => {

        props.form.validateFields((err, values) => {

            if (allValues.password === values.password &&
                allValues.username === values.username &&
                allValues.confirm === values.confirm &&
                allValues.gdprConfirmed === values.gdprConfirmed &&
                allValues.cookieConfirmed === values.cookieConfirmed) {

                if (err) {
                    disableButton(true);
                } else {
                    handleEmailChange(props, changedValues.username);
                }
            } else {

                props.form.setFieldsValue({
                    name: allValues.name,
                    username: allValues.username,
                    password: allValues.password,
                    confirm: allValues.confirm,
                    gdprConfirmed: allValues.gdprConfirmed,
                    cookieConfirmed: allValues.cookieConfirmed,
                });
            }
        })
    }
}, {force: true})(SignUpPage);
