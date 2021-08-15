import React from 'react';
import logo from './logo.png';
import {Link} from "react-router-dom";
import {pages} from "../../../common/commons";

export default class Logo extends React.Component {
    render() {
        return (
            <div>
                <Link to={pages.directoryMainPage}>
                    <img src={logo}
                         alt="logo"
                         width="120"
                         height="67"
                         className="logo-img"/>
                </Link>
            </div>
        );
    }
}
