import React from 'react';
import {observer} from "mobx-react";
import {Button, Col, Row} from "antd";

export default observer(class IndexingPanelArea extends React.Component {
    render() {
        return (
            <Row className="row-margin row-padding">
                <Col span={4}>
                    <Button className='row-margin' size='small' onClick={this.props.runHandler}>
                        Run Indexer
                    </Button>
                    <Button className='row-margin' size='small' onClick={this.props.statsHandler}>
                        Check Stats
                    </Button>
                </Col>
                <Col span={7} offset={1}>
                    <h4>
                        Last Ran: {this.props.lastRan}
                    </h4>
                    <h4>
                        Total Counted: {this.props.totalCounted}
                    </h4>
                </Col>
            </Row>
        );
    }
});