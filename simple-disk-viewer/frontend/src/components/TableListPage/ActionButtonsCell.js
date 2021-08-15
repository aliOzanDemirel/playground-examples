import React from 'react';
import {inject, observer} from "mobx-react";
import {Button, Col, Row} from "antd";
import * as PropTypes from "prop-types";
import {pages} from "../../common/commons";
import {Link} from "react-router-dom";
import DeleteFileButton from "./DeleteFileButton";

let ActionButtonsCell = inject('tableListStore', 'directoryTreeStore')(observer(class ActionButtonsCell extends React.Component {

    render() {

        let {path, isFolder} = this.props.pathAndFolderFlag;
        return (
            isFolder
                ?
                <div>
                    <Row>
                        <Col className='row-margin' span={4}>
                            <Button size='small'
                                    onClick={() => this.props.tableListStore.loadFilesAndFolders(path)}>
                                Scan as root
                            </Button>
                        </Col>
                    </Row>

                    <Row>
                        <Col className='row-margin' span={4}>
                            {/*TODO: cannot pass state to new tab*/}
                            <Link target='_blank' to={{
                                pathname: pages.directoryDetailsPage,
                                search: '?path=' + path
                            }}>
                                <Button size='small'>
                                    Directory tree
                                </Button>
                            </Link>
                        </Col>
                    </Row>

                    <DeleteFileButton onClick={() => this.props.tableListStore.deleteFile(path)}/>

                    {/*<Row>*/}
                    {/*    <Col className='row-margin' span={4}>*/}
                    {/*        <Button size='small' type='danger'*/}
                    {/*                onClick={(() => this.props.tableListStore.deleteFile(path))}>*/}
                    {/*            Delete*/}
                    {/*        </Button>*/}
                    {/*    </Col>*/}
                    {/*</Row>*/}
                </div>
                :
                <DeleteFileButton onClick={() => this.props.tableListStore.deleteFile(path)}/>
        );
    }
}));

ActionButtonsCell.propTypes = {
    pathAndFolderFlag: PropTypes.object.isRequired
};

export default ActionButtonsCell;
