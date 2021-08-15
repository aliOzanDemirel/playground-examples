import React from 'react';
import {observer} from 'mobx-react';
import {Route, Switch} from "react-router-dom";
import {Col, Row} from "antd";
import {ToastContainer} from 'react-toastify';
import Header from "./Banners/Header";
import Footer from "./Banners/Footer";
import {pages} from "../common/commons";
import {NotFound} from "./CommonComponents";
import FileAndFolderPage from "./TableListPage/TableListPage";
import DirectoryTreePage from "./DirectoryTreePage/DirectoryTreePage";

export default observer(class MainWindow extends React.Component {

    render() {
        return (
            <div className='hide-overflow-x'>

                <Header/>

                <Row id='main-content' role='main'>
                    <Col className='no-padding-no-margin'>
                        <Switch>
                            <Route exact path={pages.directoryMainPage} component={FileAndFolderPage}/>
                            <Route exact path={pages.directoryDetailsPage} component={DirectoryTreePage}/>
                            <Route component={NotFound}/>
                        </Switch>
                    </Col>
                </Row>

                <ToastContainer/>

                <Footer/>

            </div>
        );
    }
});
