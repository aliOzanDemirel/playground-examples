import React from 'react';
import {Button, Icon, Modal, Spin} from "antd";

export function LoadingSpin(props) {
    return <Spin indicator={
        <Icon type="loading" style={{fontSize: 30}} spin delay={500}/>
    } {...props}/>;
}

export function InitializerButtons(props) {
    return <Button.Group>
        {
            props.initializerBeanNames &&
            props.initializerBeanNames.map((it, index) =>
                <Button key={index} htmlType='button' type='dashed' size='large'
                        onClick={() => props.initialize(it)}>Initialize {it}</Button>)
        }
    </Button.Group>;
}

// runs callback function on confirmation
export function getConfirmation(callback, title) {
    Modal.confirm({
        title: title ? title : 'Are you sure?',
        description: '',
        okText: 'Yes',
        okType: 'danger',
        cancelText: 'No',
        onOk() {
            callback();
        }
    });
}

export function getTextual(check, isItalic) {
    if (isItalic) {
        return check ? <em>enabled</em> : <em>disabled</em>
    }
    return check ? 'enabled' : 'disabled'
}