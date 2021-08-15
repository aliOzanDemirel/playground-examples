import React from 'react';
import {inject, observer} from 'mobx-react';
import {Col, Collapse, Row} from "antd";
import DeepDirectoryTree from "./DeepDirectoryTree";
import FileDetails from "./FileDetails";
import TreeExpansionTimeline from "./TreeExpansionTimeline";
import {getPathParameter} from "../../common/commons";
import {LoadingSpin} from "../CommonComponents";

const panelStyle = {
    background: 'whitesmoke',
    borderRadius: 6,
    marginBottom: 6,
    border: 0,
    overflow: 'hidden'
};

export default inject('directoryTreeStore')(observer(class DirectoryTreePage extends React.Component {

    componentDidMount() {

        const path = getPathParameter(this.props.location.search);
        if (path) {
            this.props.directoryTreeStore.loadDetailedFileTree(path);
        }
    }

    render() {

        let store = this.props.directoryTreeStore;
        return (
            <Row className='bottom-padding-8'>
                <Col>
                    <LoadingSpin spinning={store.loading}>
                        <Collapse bordered={false} accordion={false}
                                  defaultActiveKey={['fileAttributes', 'directoryTree', 'timelineLogs']}>

                            <Collapse.Panel key="fileAttributes" style={panelStyle} showArrow={true}
                                            header="File attributes">
                                {
                                    store.selectedFileResponse
                                        ?
                                        <FileDetails data={store.selectedFileResponse}/>
                                        :
                                        null
                                }
                            </Collapse.Panel>
                            {
                                store.treeData
                                    ?
                                    <Collapse.Panel key="directoryTree" style={panelStyle} showArrow={true}
                                                    header="Directory tree">

                                        <DeepDirectoryTree/>
                                    </Collapse.Panel>
                                    :
                                    null
                            }
                            {
                                store.timeline
                                    ?
                                    <Collapse.Panel key="timelineLogs" style={panelStyle} showArrow={true}
                                                    header="Logs of expansion"
                                                    className='bottom-margin-5'>

                                        <TreeExpansionTimeline timeline={store.timeline}/>
                                    </Collapse.Panel>
                                    :
                                    null
                            }
                        </Collapse>
                    </LoadingSpin>
                </Col>
            </Row>
        );
    }
}));
