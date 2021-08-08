import React from 'react';
import {inject, observer} from 'mobx-react';
import BootstrapTable from 'react-bootstrap-table-next';
import cellEditFactory from 'react-bootstrap-table2-editor';
import {TinyButton as ScrollUpButton} from "react-scroll-up-button";
import {isEstimationRowEditable, isValidNumeric} from "../../common/commons";
import {LoadingSpin} from "../CommonComponents";
import WeatherSubscription from "./WeatherSubscription";

const estimationTableColumns = [{
    dataField: "uid",
    editable: false,
    hidden: true
}, {
    text: "Date",
    dataField: "date",
    editable: false,
    headerStyle: (colum, colIndex) => {
        return {width: '15%', textAlign: 'center'};
    },
    style: function callback(cell, row, rowIndex, colIndex) {
        return {width: '15%', textAlign: 'center'};
    }
}, {
    text: 'Forecast',
    dataField: 'forecast',
    editable: false,
    headerStyle: (colum, colIndex) => {
        return {width: '27%', textAlign: 'center'};
    },
    style: function callback(cell, row, rowIndex, colIndex) {
        return {width: '27%', textAlign: 'center'};
    }
}, {
    text: 'Output',
    dataField: 'output',
    editable: false,
    headerStyle: (colum, colIndex) => {
        return {width: '15%', textAlign: 'center'};
    },
    style: function callback(cell, row, rowIndex, colIndex) {
        return {width: '15%', textAlign: 'center'};
    }
}, {
    text: "Estimation",
    dataField: "estimation",
    editable: (cell, row, rowIndex, colIndex) => {
        return isEstimationRowEditable(row)
    },
    validator: (newValue, row, column) => {
        if (isValidNumeric(newValue) && newValue >= 0) {
            return true;
        } else if (newValue === '') {
            return {
                valid: false
            }
        }
        return {
            valid: false,
            message: newValue < 0 ? 'only positive' : 'invalid number'
        }
    },
    headerStyle: (colum, colIndex) => {
        return {width: '23%', textAlign: 'center'};
    },
    style: function callback(cell, row, rowIndex, colIndex) {
        let baseStyle = {width: '23%', textAlign: 'center'};
        if (isEstimationRowEditable(row)) {
            baseStyle = {...baseStyle, backgroundColor: '#FFFFF0'}
        }
        return baseStyle;
    }
}, {
    text: "Score",
    dataField: "score",
    editable: false,
    headerStyle: (colum, colIndex) => {
        return {width: '20%', textAlign: 'center'};
    },
    style: function callback(cell, row, rowIndex, colIndex) {
        return {width: '20%', textAlign: 'center'};
    }
}];

export default inject('estimationStore', 'loginStore')(observer(class EstimationPage extends React.Component {

    componentDidMount = () => {
        setTimeout(
            () => this.props.estimationStore.initTable(this.props.loginStore.userDetails),
            2500)
    };

    render() {
        let estStore = this.props.estimationStore;
        let logStore = this.props.loginStore;
        return (
            <div>
                <LoadingSpin spinning={estStore.loadingHistory}>
                    <BootstrapTable bootstrap4 bordered={false} keyField='uid' id='estimationTableId'
                                    data={estStore.estimationTableData.slice(0)} columns={estimationTableColumns}
                                    cellEdit={cellEditFactory({
                                        mode: 'click',
                                        blurToSave: true,
                                        timeToCloseMessage: 1000,
                                        beforeSaveCell(oldValue, newValue, row, column, done) {
                                            let userId = logStore.userDetails.userId;
                                            setTimeout(() => {
                                                if (newValue !== oldValue) {
                                                    estStore.saveEstimation(userId, row.uid, newValue)
                                                        .then(respFromService => done(respFromService))
                                                        .catch(reason => done(false));
                                                } else {
                                                    done(false);
                                                }
                                            }, 0)
                                        }
                                    })}/>
                </LoadingSpin>

                <ScrollUpButton/>

                <WeatherSubscription subscribe={!this.props.estimationStore.loadingHistory}
                                     handleRealWeather={(data) => estStore.handleRealData(data, this.props.loginStore.userDetails)}
                                     handleForecastWeather={(data) => estStore.receiveForecastMessage(data, this.props.loginStore.userDetails)}
                />
            </div>
        );
    }
}));


