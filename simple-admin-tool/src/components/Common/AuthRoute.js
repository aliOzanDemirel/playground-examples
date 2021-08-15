import React from 'react';
import {inject, observer} from "mobx-react";
import {Redirect, Route} from "react-router-dom";

export default inject('loginStore')(observer(class AuthRoute extends React.Component {
    render() {
        if (this.props.loginStore.isLoggedIn) {
            return <Route {...this.props} />
        } else {
            return <Redirect to='/admin/login'/>
        }
    }
}));