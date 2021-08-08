import React from 'react';
import {inject, observer} from 'mobx-react';
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";

export default inject('loginStore')(observer(class UserDetails extends React.Component {
    render() {
        let {userDetails} = this.props.loginStore;
        return (
            userDetails && <Row>
                <Col xs={12} className='d-flex justify-content-end'>
                    ID: {userDetails.userIdShown}
                </Col>
                <Col xs={12} className='d-flex justify-content-end'>
                    <span>{userDetails.email}</span>
                </Col>
                <Col xs={12} className='d-flex justify-content-end'>
                    Score: {userDetails.highScore}
                </Col>
            </Row>
        );
    }
}));
