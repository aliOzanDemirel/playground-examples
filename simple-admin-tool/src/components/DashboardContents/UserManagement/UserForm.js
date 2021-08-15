import React from 'react';
import {inject, observer} from "mobx-react";
import {Checkbox, Col, Form, Input, Row} from "antd";

const Item = Form.Item;
const userFormItemLayout = {
    wrapperCol: {
        xs: {span: 30},
        sm: {span: 20},
    }
};
// let syncFormValues;

let UserForm = inject('userStore')(observer(class UserForm extends React.Component {

    // componentWillMount = () => {
    //     syncFormValues = this.props.userStore.syncFormValuesWithStore;
    // };

    // checking if passwords match
    handleConfirmPasswordBlur = (e) => {
        this.props.userStore.markPasswordDirty(this.props.userStore.formPasswordDirty || !!e.target.value);
    };
    checkIfPasswordsMatch = (rule, value, callback) => {
        if (value && value !== this.props.form.getFieldValue('password')) {
            callback('Passwords do not match!');
        } else {
            callback();
        }
    };
    checkConfirmPassword = (rule, value, callback) => {
        const {form, userStore} = this.props;
        if (value && userStore.formPasswordDirty) {
            form.validateFields(['confirm'], {force: true});
        }
        callback();
    };

    render() {
        let {getFieldDecorator} = this.props.form;
        this.props.userStore.validateFields = this.props.form.validateFields;
        let {username, email, authorities} = this.props.userStore.formValues;
        return (
            <Form layout="vertical">
                <Row>
                    <Col span={12}>
                        <Item {...userFormItemLayout} >
                            {getFieldDecorator('username', {
                                initialValue: username,
                                rules: [{
                                    required: true, message: 'Enter username!',
                                }]
                            })(<Input placeholder="Type username" disabled={username !== null}/>)}
                        </Item>
                        <Item {...userFormItemLayout} >
                            {getFieldDecorator('email', {
                                initialValue: email,
                                rules: [{
                                    type: 'email', message: 'Input is not valid e-mail',
                                }, {
                                    required: true, message: 'Enter e-mail',
                                }]
                            })(<Input placeholder="Type email"/>)}
                        </Item>
                        <Item {...userFormItemLayout}>
                            {getFieldDecorator('password', {
                                rules: [{
                                    required: true, message: 'Enter password',
                                }, {
                                    validator: this.checkConfirmPassword,
                                }]
                            })(<Input placeholder="Type password" type="password"/>)}
                        </Item>
                        <Item {...userFormItemLayout}>
                            {getFieldDecorator('confirm', {
                                rules: [{
                                    required: true, message: 'Confirm password',
                                }, {
                                    validator: this.checkIfPasswordsMatch,
                                }]
                            })(<Input placeholder="Confirm password" type="password"
                                      onBlur={this.handleConfirmPasswordBlur}/>
                            )}
                        </Item>
                    </Col>
                    <Col span={12}>
                        <Item label="Authorities" {...userFormItemLayout}>
                            {getFieldDecorator('authorities', {
                                initialValue: authorities && authorities.slice(0)
                            })(<Checkbox.Group options={this.props.userStore.userRoles.slice(0)}/>
                            )}
                        </Item>
                    </Col>
                </Row>
            </Form>
        );
    }
}));

export default Form.create()(UserForm);
// {
//     onValuesChange(unused, changedValue) {
//         syncFormValues(changedValue);
//     }
// }