import React from 'react';
import {observer} from "mobx-react";
import {Button, Col, Row} from "antd";
import * as PropTypes from "prop-types";

let DeleteFileButton = observer(class DeleteFileButton extends React.Component {

    render() {
        return (
                <Row>
                    <Col className='row-margin' span={4}>
                        <Button size='small' type='danger' onClick={this.props.onClick}>
                            Delete
                        </Button>
                    </Col>
                </Row>
        );
    }
});

DeleteFileButton.propTypes = {
    onClick: PropTypes.func.isRequired
};

export default DeleteFileButton;
