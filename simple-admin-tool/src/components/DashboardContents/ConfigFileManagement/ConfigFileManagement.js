import React from 'react';
import {inject, observer} from "mobx-react";
import {Collapse} from "antd";
import {InitializerButtons} from "../../Common/CommonComponents";
import ConfigFileUploadForm from "./ConfigFileUploadForm";
import {panelStyle} from "../../../common/commons";

export default inject('configFileStore')(observer(class ConfigFileManagement extends React.Component {

    componentDidMount = () => {
        this.props.configFileStore.getInitializerBeans();
    };

    render() {
        let {initializerBeanNames, initializeBean} = this.props.configFileStore;
        return (
            <Collapse bordered={false} activeKey={['uploadFilePanel', 'initializerPanel']}>
                <Collapse.Panel key='uploadFilePanel' header="Config File Upload" style={panelStyle} showArrow={false}>
                    <ConfigFileUploadForm/>
                </Collapse.Panel>
                <Collapse.Panel key='initializerPanel' header="Bean Initializer" style={panelStyle} showArrow={false}>
                    <InitializerButtons initializerBeanNames={initializerBeanNames} initialize={initializeBean}/>
                </Collapse.Panel>
            </Collapse>
        );
    }
}));