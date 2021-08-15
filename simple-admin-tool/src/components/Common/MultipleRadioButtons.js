import React from 'react';
import {Radio} from "antd";
import {observer} from "mobx-react";

export default observer(class MultipleRadioButtons extends React.Component {
    render() {
        return (
            <Radio.Group defaultValue={this.props.currentVal} value={this.props.currentVal}
                         onChange={this.props.onChange}>
                {
                    this.props.values &&
                    this.props.values.map(it => <Radio.Button key={it} value={it}>{it}</Radio.Button>)
                }
            </Radio.Group>
        )
    }
});