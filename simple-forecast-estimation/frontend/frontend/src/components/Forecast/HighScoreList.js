import React from 'react';
import {inject, observer} from 'mobx-react';
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import BootstrapTable from 'react-bootstrap-table-next';
import SockJsClient from 'react-stomp';
import {liveFeedHighScores, websocketUrl} from "../../common/commons";

const highScoreTableColumns = [{
    text: "User ID",
    dataField: "sequence",
    editable: false,
    headerStyle: (colum, colIndex) => {
        return {width: '30%', textAlign: 'center'};
    },
    style: function callback(cell, row, rowIndex, colIndex) {
        let style = {width: '30%', textAlign: 'center'};
        if (rowIndex < 3) {
            style = {
                ...style,
                fontWeight: 'bold'
            }
        }
        return style;
    }
}, {
    text: "High Score (CZK)",
    dataField: "highscore",
    headerStyle: (colum, colIndex) => {
        return {width: '70%', textAlign: 'center'};
    },
    style: function callback(cell, row, rowIndex, colIndex) {
        let style = {width: '70%', textAlign: 'center'};
        if (rowIndex < 3) {
            style.fontWeight = 'bold';
            if (rowIndex === 0) {
                style.fontSize = '19px';
            } else if (rowIndex === 1) {
                style.fontSize = '18px';
            } else if (rowIndex === 2) {
                style.fontSize = '17px';
            }
        }
        return style;
    }
}];

export default inject('forecastStore')(observer(class HighScoreList extends React.Component {

    componentDidMount() {
        this.props.forecastStore.initializeHighScoreList()
    }

    render() {
        let store = this.props.forecastStore;
        return (
            <div>
                <Row>
                    <Col xs={12} className='centered-text'>
                        <h4 className="high-score-header-text">High Scores</h4>
                    </Col>
                </Row>
                <Row>
                    <Col xs>
                        <BootstrapTable bootstrap4 bordered={false} keyField='sequence' id='highScoreTableId'
                                        data={store.highScoreTableData.slice(0)} columns={highScoreTableColumns}/>
                    </Col>
                </Row>

                <SockJsClient url={websocketUrl} topics={[liveFeedHighScores]}
                              onConnect={() => console.log("Subscribed to " + websocketUrl + liveFeedHighScores)}
                              onMessage={store.updateHighScores}/>
            </div>
        );
    }
}));
