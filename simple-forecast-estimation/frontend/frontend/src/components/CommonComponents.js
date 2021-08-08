import React from 'react';
import {Icon, Spin} from "antd";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Table from "react-bootstrap/Table";
import {Link} from "react-router-dom";
import {pages} from "../common/commons";

export function LoadingSpin(props) {
    return <Spin indicator={
        <Icon type="loading" style={{fontSize: 30}} spin delay={500}/>
    } {...props}/>;
}

export function ForecastCell(props) {
    return <Row>
        <Col xs className='no-padding-no-margin'>
            <span className='nested-table-font d-flex justify-content-center'>
                <i className="far fa-sun"/>
                <span>&nbsp;/ {props.timeseries.sunCoverage}</span>
            </span>
        </Col>
        <Col xs className='no-padding-no-margin'>
            <span className='nested-table-font d-flex justify-content-center'>
                <i className="fas fa-wind"/>
                <span>&nbsp;/ {props.timeseries.windSpeed}</span>
            </span>
        </Col>
    </Row>;
}

export function MetricsCellTable(props) {
    return <Table className='no-margin-in-nested-table metrics-cell-table'
                  responsive="sm" size='sm' borderless='true'>
        <thead>
        <tr>
            <th>Sun (%)</th>
            <th>Wind (RPM)</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>{props.forecastData.sunCoverage}</td>
            <td>{props.forecastData.windSpeed}</td>
        </tr>
        </tbody>
    </Table>;
}

export function NotFound(props) {
    return <div id='not-found'>
        <h2>Page not found.</h2>
        <Link to={pages.estimation}>Go to estimation page</Link>
    </div>;
}