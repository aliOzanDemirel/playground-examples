import React from 'react';
import {inject, observer} from 'mobx-react';
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import ProgressBar from "react-bootstrap/ProgressBar";

export default inject('forecastStore')(observer(class ProgressWithTimer extends React.Component {

    componentDidMount() {
        this.progressTimerId = setInterval(this.props.forecastStore.updateProgress, 1000);
    };

    componentWillUnmount() {
        clearInterval(this.progressTimerId);
    }

    render() {
        let store = this.props.forecastStore;
        return (
            <Row>
                <Col xs sm={2} className='align-content-to-center no-padding-no-margin'>
                    <h5>
                        {store.startTime && new Date(store.startTime).toLocaleTimeString('cs-CZ')}
                    </h5>
                </Col>
                <Col xs sm={8} className='no-padding-no-margin'>
                    <ProgressBar className='progress-bar-align-to-end' variant="info" now={store.progressVal}/>
                </Col>
                <Col xs sm={2} className='align-content-to-center no-padding-no-margin'>
                    <h5>
                        {/*{store.endTime && new Date(store.endTime).toISOString().substr(11, 8)}*/}
                        {store.endTime && new Date(store.endTime).toLocaleTimeString('cs-CZ')}
                    </h5>
                </Col>
            </Row>
        );
    }
}));
