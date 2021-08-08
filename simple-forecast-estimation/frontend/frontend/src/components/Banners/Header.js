import React from 'react';
import {inject, observer} from 'mobx-react';
import {withRouter} from 'react-router-dom';
import Navbar from "react-bootstrap/Navbar";
import Container from "react-bootstrap/Container";
import Logo from "./Logo";
import {pages} from "../../common/commons";
import UserDetails from "./UserDetails";


export default inject('loginStore')(withRouter(observer(class Header extends React.Component {

    componentDidUpdate(prevProps, prevState, snapshot) {
        this.props.loginStore.updateIfForecastPage(this.props.location.pathname === pages.forecast)
    }

    render() {
        let {tokenExists, isForecastPage} = this.props.loginStore;
        return (
            <Navbar id="header" expand="xl" role='banner'>
                {!tokenExists || isForecastPage
                    ?
                    <Container fluid className='justify-content-center'>
                        <Logo/>
                    </Container>
                    :
                    <Container fluid className='logged-in-header mr-2'>
                        <Logo className='align-center justify-content-start'/>
                        <UserDetails className='justify-content-end'/>
                    </Container>
                }
            </Navbar>
        );
    }
})));
