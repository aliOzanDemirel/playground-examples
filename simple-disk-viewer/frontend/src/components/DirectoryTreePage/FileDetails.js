import React from 'react';
import {observer} from 'mobx-react';
import {Col, Row} from "antd";
import {booleanToYesNo} from "../../common/commons";
import PropTypes from "prop-types";

let FileDetails = observer(class FileDetails extends React.Component {

    render() {

        let fileResponse = this.props.data;
        return (
            <Row gutter={10}>
                <Col span={18}>
                    <Row className='row-margin'>
                        <Col>
                            Path: {fileResponse.path}
                        </Col>
                    </Row>
                    <Row className='row-margin'>
                        <Col>
                            Created: {new Date(Number(fileResponse.created)).toDateString()}
                        </Col>
                    </Row>
                    <Row className='row-margin'>
                        <Col>
                            Modified: {new Date(Number(fileResponse.modified)).toDateString()}
                        </Col>
                    </Row>
                </Col>
                <Col span={6}>
                    <Row className='row-margin'>
                        <Col>
                            Size (bytes): {fileResponse.sizeInBytes}
                        </Col>
                    </Row>
                    <Row className='row-margin'>
                        <Col>
                            Is Folder: {booleanToYesNo(fileResponse.isFolder)}
                        </Col>
                    </Row>
                    <Row className='row-margin'>
                        <Col>
                            Is Hidden: {booleanToYesNo(fileResponse.isHidden)}
                        </Col>
                    </Row>
                </Col>
            </Row>
        );
    }
});

FileDetails.propTypes = {
    data: PropTypes.object.isRequired
};

export default FileDetails;
