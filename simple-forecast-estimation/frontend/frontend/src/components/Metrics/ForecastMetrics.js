import React from 'react';
import {inject, observer} from 'mobx-react';
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import {MetricsCellTable} from "../CommonComponents";

export default inject('forecastStore')(observer(class ForecastMetrics extends React.Component {

    componentDidMount = () => {
        this.props.forecastStore.initializeForecastMetrics()
    };

    render() {
        let store = this.props.forecastStore;
        return (
            <div>
                <Row className='justify-content-center'>
                    <Col xs sm={7}>
                        <h4 style={{textAlign: 'center'}}>Forecast Metrics</h4>
                    </Col>
                    <Col xs sm={5}/>
                </Row>
                {
                    Object.keys(store.forecastMetrics)
                        .map((objKey, index) => {
                            return <Row key={index} className='metrics-forecast-row'>
                                <Col xs sm={1} className='metrics-table-day-column'>
                                    {objKey}:
                                </Col>
                                <Col xs sm={6}>
                                    <MetricsCellTable forecastData={store.forecastMetrics[objKey]}/>
                                </Col>
                                <Col xs sm={5}/>
                            </Row>
                        })
                }
            </div>
        );
    }
}));
