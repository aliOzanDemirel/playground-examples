import React from 'react';
import {observer} from 'mobx-react';
import {Timeline} from "antd";

let TreeExpansionTimeline = observer(class TreeExpansionTimeline extends React.Component {

    render() {

        let timeline = this.props.timeline;
        return (
            <Timeline>
                {
                    timeline && timeline.slice(0)
                        .map((logDesc, index) =>
                            <Timeline.Item key={index}>
                                <p className='row-margin'>
                                    {logDesc}
                                </p>
                            </Timeline.Item>
                        )
                }
            </Timeline>
        );
    }
});

export default TreeExpansionTimeline;
