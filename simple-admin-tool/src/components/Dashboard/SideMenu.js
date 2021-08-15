import React from 'react';
import {Icon, Menu} from 'antd';
import {observer} from "mobx-react";
import {Link} from "react-router-dom";
import {configTools, supportTools} from "../../common/side-menu-items";
import {defaultPage} from "../../common/commons";

export default observer(class SideMenu extends React.Component {
    render() {
        // Menu or SubMenu cannot have custom component embedded to themselves, hence the below
        return (
            <Menu mode="inline" style={this.props.style} inlineIndent={10}
                  defaultSelectedKeys={[defaultPage.menuKey]} defaultOpenKeys={['configTools']}>
                <Menu.SubMenu key='supportTools' title='Support Tools'>
                    {
                        supportTools &&
                        supportTools.map(it =>
                            <Menu.Item key={it.key}>
                                <Link to={it.whereToLocation}>
                                    <Icon type={it.iconType}/>{it.desc}
                                </Link>
                            </Menu.Item>)
                    }
                </Menu.SubMenu>

                <Menu.SubMenu key='configTools' title='Config Tools'>
                    {
                        configTools &&
                        configTools.map(it =>
                            <Menu.Item key={it.key}>
                                <Link to={it.whereToLocation}>
                                    <Icon type={it.iconType}/>{it.desc}
                                </Link>
                            </Menu.Item>
                        )
                    }
                </Menu.SubMenu>
            </Menu>
        );
    }
});