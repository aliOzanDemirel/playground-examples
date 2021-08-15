import React from 'react';
import DevTools from 'mobx-react-devtools';
import {Col, Row} from "antd";
import {observer} from "mobx-react";
import HeaderAppInfo from "./HeaderAppInfo";
import {defaultPage} from "../../common/commons";
import {Link} from "react-router-dom";

export default observer(class Header extends React.Component {
    render() {
        return (
            <Row>
                <Col span={6}>
                    <Link to={defaultPage.url}><h1 className="header-text">Some Admin Tool</h1></Link>
                </Col>
                <Col span={18}>
                    <HeaderAppInfo/>
                </Col>
                {process.env.NODE_ENV === 'development' ? <DevTools/> : null}
            </Row>
        );
    }
});