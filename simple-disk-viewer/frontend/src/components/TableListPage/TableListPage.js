import React from 'react';
import {inject, observer} from 'mobx-react';
import {Button, Col, Row} from "antd";
import ScanPathForm from "./ScanPathForm";
import ScanRootForm from "./ScanRootForm";
import FileAndFolderTable from "./FileAndFolderTable";

export default inject('tableListStore')(observer(class TableListPage extends React.Component {

    componentDidMount = () => {
        this.props.tableListStore.loadSystemRoots().then(initialRootValue => {

            this.props.tableListStore.loadFilesAndFolders(initialRootValue)
        });
    };

    componentWillUnmount = () => {
        this.props.tableListStore.closeEventSource()
    };

    render() {
        return (
            <Row className='bottom-padding-8'>
                <Col span={24}>
                    <Row className='no-padding-no-margin'>
                        <Col span={24}>
                            <Row className='no-padding-no-margin'>
                                <Col span={18}>
                                    <ScanRootForm/>
                                </Col>
                                <Col span={6}>
                                    <Button htmlType='button' type='primary' icon='rollback'
                                            onClick={() => this.props.tableListStore.loadLastScanned()}>
                                        Scan the last
                                    </Button>
                                </Col>
                            </Row>
                            <ScanPathForm/>
                        </Col>
                    </Row>
                    <Row className='no-padding-no-margin'>
                        <Col>
                            <FileAndFolderTable/>
                        </Col>
                    </Row>
                </Col>
            </Row>
        );
    }
}));
