import React from 'react';
import {Route, Switch} from "react-router-dom";
import {observer} from "mobx-react";
import {Layout} from 'antd';
import SideMenu from "./SideMenu";
import NotFound from "../Common/NotFoundPage";
import {dashboardItemRoutes} from "../../common/dashboard-components";

export default observer(class Dashboard extends React.Component {
    render() {
        return (
            <Layout style={{minHeight: 600}}>
                <Layout.Sider breakpoint='lg' collapsedWidth='0'>
                    <SideMenu style={{background: '#ffffff', width: 240, lineHeight: '20px', height: '100%'}}/>
                </Layout.Sider>
                {/*pulling content area to right as much pixel as SideMenu's width flows over 200*/}
                <Layout.Content style={{background: '#ffffff', padding: '32px 34px 32px 74px', minHeight: 600}}>
                    <Switch>
                        {
                            dashboardItemRoutes &&
                            dashboardItemRoutes.map((it, index) =>
                                <Route key={index} exact path={it.path} component={it.component}/>
                            )
                        }
                        <Route component={NotFound}/>
                    </Switch>
                </Layout.Content>
            </Layout>
        );
    }
});