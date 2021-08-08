import React from 'react';
import {inject, observer} from 'mobx-react';
import ProgressWithTimer from "./ProgressWithTimer";
import RealMetrics from "./RealMetrics";
import ForecastMetrics from "./ForecastMetrics";
import WeatherSubscription from "../Estimation/WeatherSubscription";

export default inject('forecastStore')(observer(class Metrics extends React.Component {
    render() {
        let forStore = this.props.forecastStore;
        return (
            <div>
                <ProgressWithTimer/>

                <RealMetrics/>
                <hr/>
                <ForecastMetrics/>

                <WeatherSubscription subscribe={true}
                                     handleRealWeather={forStore.updateRealValues}
                                     handleForecastWeather={forStore.updateForecastMetrics}
                />
            </div>
        );
    }
}));
