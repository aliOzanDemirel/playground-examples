import React from 'react';
import {Link} from 'react-router-dom';
import Navbar from "react-bootstrap/Navbar";
import logo from '../../logo/covid19.png';
import {pages} from "../../common/commons";

export default class Logo extends React.Component {
    render() {
        return (
            <Navbar.Brand>
                <Link to={pages.login}>
                    <img src={logo}
                         alt="logo"
                         width="69"
                         height="30"
                         className="d-inline-block align-top"/>
                </Link>
            </Navbar.Brand>
        );
    }
}
