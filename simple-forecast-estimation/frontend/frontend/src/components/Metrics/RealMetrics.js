import React from 'react';
import {inject, observer} from 'mobx-react';
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import {actualDateRepresentation, presentActualDay} from "../../common/commons";

export default inject('forecastStore')(observer(class RealMetrics extends React.Component {
    render() {
        let realValues = this.props.forecastStore.realMetrics;
        return (
            <div>
                <Row className='justify-content-center'>
                    <Col xs className='centered-text'>
                        <h4 className="metrics-header-text">Real Metrics</h4>
                    </Col>
                </Row>
                <Row className='metrics-real-values-row'>
                    <Col xs sm={1}/>
                    <Col xs sm={3} className='align-content-to-center'>
                        <span>Sun Coverage (%)</span>
                    </Col>
                    <Col xs sm={3} className='align-content-to-center'>
                        <span>Wind Speed (RPM)</span>
                    </Col>
                    <Col xs sm={3} className='align-content-to-center'>
                        <span>Power (mW)</span>
                    </Col>
                    <Col xs sm={2} className='align-content-to-center'>
                        <span>Output (mW/min)</span>
                    </Col>
                </Row>
                <Row className='metrics-real-values-row'>
                    <Col xs sm={1} className='align-content-to-center metrics-table-day-column'>
                        <span>{realValues ? presentActualDay(realValues, true) : actualDateRepresentation}:</span>
                    </Col>
                    <Col xs sm={3} className='align-content-to-center'>
                        <i className="far fa-sun metrics-icon-size"/><span>&nbsp; / {realValues && realValues.sunCoverage}</span>
                    </Col>
                    <Col xs sm={3} className='align-content-to-center'>
                        <i className="fas fa-wind metrics-icon-size"/><span>&nbsp; / {realValues && realValues.windSpeed}</span>
                    </Col>
                    <Col xs sm={3} className='align-content-to-center'>
                        <i className="fas fa-bolt metrics-icon-size"/><span>&nbsp; / {realValues && realValues.power}</span>
                    </Col>
                    <Col xs sm={2} className='align-content-to-center'>
                        <i className="fas fa-globe metrics-icon-size"/><span>&nbsp; / {realValues &&
                    (realValues.total && Math.round(realValues.total))}</span>
                    </Col>
                </Row>
            </div>
        );
    }
}));
