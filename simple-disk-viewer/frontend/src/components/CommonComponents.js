import React from 'react';
import {Icon, Modal, Row, Spin} from "antd";
import {Link} from "react-router-dom";
import {pages} from "../common/commons";

export function LoadingSpin(props) {
    return <Spin indicator={
        <Icon type="loading" style={{fontSize: 30}} spin delay={500}/>
    } {...props}/>;
}

export function NotFound(props) {
    return <div id='not-found'>
        <h2>Page is not found</h2>
        <Link to={pages.directoryMainPage}>Go to table page</Link>
    </div>;
}

export function DateAndTimeCell(props) {
    return <div>
        <Row className='no-padding-no-margin'>{props.dateAndTime.date}</Row>
        <Row className='no-padding-no-margin'>{props.dateAndTime.time}</Row>
    </div>
}

// runs callback function on confirmation
export function getConfirmation(callback, title, desc) {
    Modal.confirm({
        title: title ? title : 'Are you sure?',
        content: desc ? desc : '',
        okText: 'Yes',
        okType: 'danger',
        cancelText: 'No',
        onOk() {
            callback();
        }
    });
}
