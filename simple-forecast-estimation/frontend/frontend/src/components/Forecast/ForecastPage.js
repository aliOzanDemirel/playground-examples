import React from 'react';
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import HighScoreList from "./HighScoreList";
import Metrics from "../Metrics/Metrics";
import {inject} from "mobx-react";

export default inject('loginStore')(class ForecastPage extends React.Component {

    componentDidMount() {
        this.props.loginStore.updateIfForecastPage(true)
    }

    render() {
        return (
            <Row>
                <Col xs sm={8} lg={9} className='pr-0'>
                    <Metrics/>
                </Col>
                <Col xs sm={4} lg={3}>
                    <HighScoreList/>
                </Col>
            </Row>
        );
    }
});
