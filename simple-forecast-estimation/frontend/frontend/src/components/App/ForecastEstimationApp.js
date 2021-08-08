import React from 'react';
import {inject} from 'mobx-react';
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import {Route, Switch} from "react-router-dom";
import AuthenticatedRoute from "../AuthenticatedRoute";
import EstimationPage from "../Estimation/EstimationPage";
import ForecastPage from "../Forecast/ForecastPage";
import SignUpPage from "../SignUp/SignUpPage";
import LoginPage from "../Login/LoginPage";
import Header from "../Banners/Header";
import {ToastContainer} from 'react-toastify';
import {pages} from "../../common/commons";
import {NotFound} from "../CommonComponents";
import Footer from "../Banners/Footer";

export default inject('appRouter', 'loginStore')(class ForecastEstimationApp extends React.Component {

    componentWillMount = () => {
        this.checkLoginStatus(this.props);
    };

    checkLoginStatus = ({loginStore, appRouter}) => {
        loginStore.checkIfUserIsLoggedIn(appRouter);
    };

    render() {
        return (
            <div className='hide-overflow-x'>

                <Header/>

                <Row id='main-content' role='main'>
                    <Col xs sm md lg xl className='no-padding-no-margin'>
                        <Switch>
                            <Route exact path={pages.login} component={LoginPage}/>
                            <Route exact path={pages.signUp} component={SignUpPage}/>
                            <AuthenticatedRoute path={pages.estimation} component={EstimationPage}/>
                            <Route exact path={pages.forecast} component={ForecastPage}/>
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
