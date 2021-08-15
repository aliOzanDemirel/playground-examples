import React from 'react'
import {Link} from 'react-router-dom'

export default class NotFound extends React.Component {
    render() {
        return (
            <div className="center-div">
                <h2>Page not found.</h2>
                <Link to="/admin/login">Go to login</Link>
                <br/>
                <Link to="/admin/dashboard">Go to dashboard</Link>
            </div>
        )
    }
};