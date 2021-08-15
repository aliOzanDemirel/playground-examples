import React from 'react';
import {inject, observer} from 'mobx-react';
import {Button, Col, Form, Input, Row} from "antd";

let ScanPathForm = inject('tableListStore')(observer(class ScanPathForm extends React.Component {

    handleSubmit = (e) => {

        // prevent form submission
        e.preventDefault();
        this.props.form.validateFields((err, values) => {
            if (!err) {
                this.props.tableListStore.loadFilesAndFolders(values.path);
            }
        });
    };

    render() {

        let {getFieldDecorator} = this.props.form;
        return (
            <Form role='form' layout='vertical' onSubmit={this.handleSubmit}>
                <Row gutter={10}>
                    <Col xs={24} sm={18}>
                        <Form.Item>
                            {getFieldDecorator('path', {})(<Input
                                placeholder="Enter path manually to scan, relative to root"/>)}
                        </Form.Item>
                    </Col>
                    <Col xs={24} sm={6}>
                        <Button htmlType='submit' type='primary' icon='folder'>
                            Scan inputted path
                        </Button>
                    </Col>
                </Row>
            </Form>
        );
    }
}));

export default Form.create()(ScanPathForm);
