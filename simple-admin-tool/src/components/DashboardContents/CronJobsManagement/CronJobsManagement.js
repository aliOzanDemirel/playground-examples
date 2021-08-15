import React from 'react';
import {inject, observer} from "mobx-react";
import {Button, Checkbox, Col, Divider, Row} from "antd";

export default inject('cronJobsStore')(observer(class CronJobsManagement extends React.Component {

    componentDidMount = () => {
        this.props.cronJobsStore.setCronJobTriggers();
    };

    render() {
        let {onCheckboxChange, inputCronJobTriggers, resetCheckboxes, saveCronJobStatus} = this.props.cronJobsStore;

        // below variables are for aligning 2 columns of checkboxes
        let columnSize = 2;
        let start = -2;
        let end = 0;
        let triggerKeys = inputCronJobTriggers ? Object.keys(inputCronJobTriggers) : [];

        return (
            <div>
                {
                    inputCronJobTriggers &&
                    [...new Array(Math.ceil(triggerKeys.length / columnSize))].map(() => {

                        start += columnSize;
                        end += columnSize;

                        return <Row gutter={6} key={start + 200}> {

                            triggerKeys.slice(start, end).map((key, index) =>
                                <Col key={index} span={12}>
                                    <Checkbox checked={inputCronJobTriggers[key]} style={{margin: '0 0 8px 0'}}
                                              onChange={(e) => onCheckboxChange(key, e.target.checked)}
                                              value={inputCronJobTriggers[key]}>
                                        {key}
                                    </Checkbox>
                                </Col>
                            )
                        }
                        </Row>
                    })
                }
                <Divider/>
                <Row gutter={12}>
                    <Col span={4}>
                        <Button onClick={saveCronJobStatus}>
                            Save Activation Status
                        </Button>
                    </Col>
                    <Col span={3}>
                        <Button onClick={resetCheckboxes}>
                            Reset
                        </Button>
                    </Col>
                </Row>
            </div>

        );
    }
}));