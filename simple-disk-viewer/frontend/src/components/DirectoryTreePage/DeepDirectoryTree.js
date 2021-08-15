import React from 'react';
import {inject, observer} from 'mobx-react';
import {Tree} from "antd";

export default inject('directoryTreeStore')(observer(class DeepDirectoryTree extends React.Component {

    onSelect = (selectedKeys, info) => {

        this.props.directoryTreeStore.updateSelectedNode(info.node.props)
    };

    onLoadData = treeNode => {

        return new Promise(resolve => {

            if (treeNode.props.children) {
                resolve();
                return;
            }

            const promise = this.props.directoryTreeStore.expandFileTree(treeNode);
            if (promise) {
                // return promise.then(() => resolve());
                promise.then(() => resolve());
            } else {
                resolve();
            }
        });
    };

    renderTreeNodes = children => {

        return children && children.map(item => {

            if (item.children) {
                return (
                    <Tree.TreeNode title={item.title} key={item.key} dataRef={item}>
                        {this.renderTreeNodes(item.children)}
                    </Tree.TreeNode>
                );
            }
            return <Tree.TreeNode key={item.key} {...item} dataRef={item}/>;
        });
    };

    render() {

        let store = this.props.directoryTreeStore;
        return (
            store.treeData
                ?
                <Tree.DirectoryTree
                    expandAction='doubleClick'
                    onSelect={this.onSelect}
                    loadData={this.onLoadData}>
                    {this.renderTreeNodes(store.treeData)}
                </Tree.DirectoryTree>
                :
                null
        );
    }
}));
