import React from 'react';
import {inject, observer} from "mobx-react";
import {Redirect, Route} from "react-router-dom";
import {pages} from "../common/commons";

export default inject('loginStore')(observer(class AuthenticatedRoute extends React.Component {
    render() {
        if (this.props.loginStore.tokenExists) {
            return <Route {...this.props} />
        } else {
            return <Redirect to={pages.login}/>
        }
    }
}));