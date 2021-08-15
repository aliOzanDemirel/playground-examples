import React from 'react';
import {inject} from 'mobx-react';
import {Route, Switch} from 'react-router-dom';
import {Layout} from "antd";
import LoginPage from "../LoginPage/LoginPage";
import Dashboard from "../Dashboard/Dashboard";
import NotFound from "../Common/NotFoundPage";
import Header from "../Dashboard/Header";
import AuthRoute from "../Common/AuthRoute";

export default inject('appRouter', 'loginStore')(class App extends React.Component {

    componentDidMount = () => {
        this.checkLoginStatus(this.props);
    };

    checkLoginStatus = ({loginStore, appRouter}) => {
        loginStore.loadAppInfo(appRouter);
    };

    render() {
        return (
            <Layout>
                <Layout.Header style={{width: '100%'}}>
                    <Header/>
                </Layout.Header>
                <Switch>
                    <Route exact path='/admin/login' component={LoginPage}/>

                    <AuthRoute path='/admin/dashboard' component={Dashboard}/>

                    <Route component={NotFound}/>
                </Switch>
                <footer className='admin-footer'>Best Admin This Admin Everyday Sunny ©2018</footer>
            </Layout>
        );
    }
});