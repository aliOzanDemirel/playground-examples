import React from 'react';
import {inject, observer} from 'mobx-react';
import {Table} from "antd";
import {booleanToYesNo, comparatorSorter, parseToDateAndTime} from "../../common/commons";
import {DateAndTimeCell, LoadingSpin} from "../CommonComponents";
import ActionButtonsCell from "./ActionButtonsCell";
import FileDetails from "../DirectoryTreePage/FileDetails";

const columns = [{
    title: 'Path',
    dataIndex: 'path',
    key: 'path',
    sorter: (first, second) => comparatorSorter(first, second, 'path')
}, {
    title: 'Size (bytes)',
    dataIndex: 'sizeInBytes',
    key: 'sizeInBytes',
    width: '14%',
    sorter: (first, second) => comparatorSorter(first, second, 'sizeInBytes'),
    render: text => {
        const size = Number(text);
        if (size > 1000000000) {
            return Math.floor(size / 1000000000) + ' gb'

        } else if (size > 1000000) {
            return Math.floor(size / 1000000) + ' mb'

        } else {
            return size
        }
    }
}, {
    title: 'Created',
    dataIndex: 'created',
    key: 'created',
    width: '12%',
    sorter: (first, second) => comparatorSorter(first, second, 'created'),
    render: text => {
        return <DateAndTimeCell dateAndTime={parseToDateAndTime(text)}/>
    }
}, {
    title: 'Modified',
    dataIndex: 'modified',
    key: 'modified',
    width: '12%',
    sorter: (first, second) => comparatorSorter(first, second, 'modified'),
    render: text => {
        return <DateAndTimeCell dateAndTime={parseToDateAndTime(text)}/>
    }
}, {
    title: 'Folder',
    dataIndex: 'isFolder',
    key: 'isFolder',
    width: '4%',
    sorter: (first, second) => comparatorSorter(first, second, 'isFolder'),
    render: text => booleanToYesNo(text)
}, {
    title: 'Hidden',
    dataIndex: 'isHidden',
    key: 'isHidden',
    width: '4%',
    sorter: (first, second) => comparatorSorter(first, second, 'isHidden'),
    render: text => booleanToYesNo(text)
}, {
    title: 'Actions',
    dataIndex: 'pathAndFolderFlag',
    key: 'pathAndFolderFlag',
    width: '8%',
    render: data => <ActionButtonsCell pathAndFolderFlag={data}/>
}];

export default inject('tableListStore')(observer(class FileAndFolderTable extends React.Component {

    render() {

        let store = this.props.tableListStore;
        return (
            <LoadingSpin spinning={store.loadingTableData}>

                {store.currentDirectory && <FileDetails data={store.currentDirectory}/>}

                <Table style={{marginTop: 10}}
                       columns={columns}
                       pagination={{
                           hideOnSinglePage: true,
                           pageSize: 10
                       }}
                       dataSource={store.currentDirectory && store.currentDirectory.children
                           ? store.currentDirectory.children : []}
                       size='middle'
                />
            </LoadingSpin>
        );
    }
}));
