import React from 'react';
import Logo from "./Logo/Logo";
import {Col, Row} from "antd";

export default class Header extends React.Component {
    render() {
        return (
            <header id='header' role='banner'>
                <Row gutter={40} type='flex' justify='space-between' align='middle' className='header-row'>
                    <Col span={6} push={1}>
                        <Logo/>
                    </Col>
                    <Col span={10}>
                        <div className='align-content-to-center'>
                            <h3 className='banner-text'>Simple Disk Viewer</h3>
                        </div>
                    </Col>
                    <Col span={7}/>
                </Row>
            </header>
        );
    }
}
