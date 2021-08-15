import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import 'antd/dist/antd.css'
import registerServiceWorker from './registerServiceWorker';
import AppRoot from "./components/App/AppRoot";
// import App from "./components/App/App";

ReactDOM.render(<AppRoot/>, document.getElementById('root'));
registerServiceWorker();
