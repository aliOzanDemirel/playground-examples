import React from 'react';
import {observer} from 'mobx-react';
import SockJsClient from 'react-stomp';
import {liveFeedForecast, liveFeedRealData, websocketUrl} from "../../common/commons";

export default observer(class WeatherSubscription extends React.Component {
    render() {
        return (
            this.props.subscribe
                ?
                <div>
                    {/*debug={true} subscribeHeaders={logStore.getAuthHeader() options={{transports: ['websocket']}}}*/}
                    <SockJsClient url={websocketUrl} topics={[liveFeedRealData]}
                                  onConnect={() => console.log("Subscribed to " + websocketUrl + liveFeedRealData)}
                                  onMessage={this.props.handleRealWeather}/>

                    <SockJsClient url={websocketUrl} topics={[liveFeedForecast]}
                                  onConnect={() => console.log("Subscribed to " + websocketUrl + liveFeedForecast)}
                                  onMessage={this.props.handleForecastWeather}/>
                </div>
                :
                null
        );
    }
});


