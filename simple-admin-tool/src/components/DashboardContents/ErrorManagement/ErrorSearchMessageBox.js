import React from 'react';
import {observer} from "mobx-react";
import {LoadingSpin} from "../../Common/CommonComponents";
import {Alert} from "antd";

export default observer(class ErrorSearchMessageBox extends React.Component {
    render() {
        let obs = this.props.observedVal;
        return (
            <LoadingSpin spinning={obs.searchLoading}>
                <Alert type={this.props.type} closable
                       message={obs.sysErrorDate}
                       description={obs.sysErrorMsg && obs.sysErrorMsg.trim()}/>
            </LoadingSpin>
        );
    }
});