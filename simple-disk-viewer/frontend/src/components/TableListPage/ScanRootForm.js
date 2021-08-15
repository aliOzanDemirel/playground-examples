import React from 'react';
import {inject, observer} from 'mobx-react';
import {Button, Col, Form, Row, Select} from "antd";

let ScanRootForm = inject('tableListStore')(observer(class ScanRootForm extends React.Component {

    handleSubmit = (e) => {

        // prevent form submission
        e.preventDefault();
        this.props.form.validateFields((err, values) => {
            if (!err) {
                this.props.tableListStore.loadFilesAndFolders(values.root);
            }
        });
    };

    render() {

        let {getFieldDecorator} = this.props.form;
        let {fileSystemRootValues, initialRootValue} = this.props.tableListStore;
        return (
            fileSystemRootValues
                ?
                <Form role='form' layout='vertical' onSubmit={this.handleSubmit}>
                    <Row gutter={10}>
                        <Col xs={24} sm={6}>
                            <Form.Item>
                                {getFieldDecorator('root', {
                                    initialValue: initialRootValue,
                                    rules: [{
                                        required: true, message: 'Root should be selected!',
                                    }]
                                })(
                                    <Select>{
                                        fileSystemRootValues.map((it, index) => {
                                            return <Select.Option key={index} value={it}>
                                                {it}
                                            </Select.Option>
                                        })
                                    }</Select>
                                )}
                            </Form.Item>
                        </Col>
                        <Col xs={24} sm={6}>
                            <Button htmlType='submit' type='primary' icon='search'>
                                Scan selected root directory
                            </Button>
                        </Col>
                    </Row>
                </Form>
                :
                null
        );
    }
}));

export default Form.create()(ScanRootForm);
