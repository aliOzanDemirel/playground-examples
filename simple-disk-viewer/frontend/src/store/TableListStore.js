import * as mobxUtils from "mobx-utils";
import {action, autorun, computed, decorate, extendObservable, runInAction, toJS} from 'mobx';
import {EventType, getEventSource, goodNews, parseJsonOrGetString} from "../common/commons";
import {getConfirmation} from "../components/CommonComponents";

export default class TableListStore {

    constructor(api) {
        this.api = api;
        this.eventSource = null;
        this.lastScannedPath = null;

        extendObservable(this, {
            observedTableData: {
                state: null
            },
            // current folder that is loaded in table
            currentDirectory: null,
            fileSystemRootValues: []
        });

        autorun(() => {

            if (this.observedTableData.state === mobxUtils.FULFILLED) {

                runInAction(() => {

                    let rootFileResponse = this.observedTableData.value;
                    this.mutateFileResponseForView(rootFileResponse);
                    rootFileResponse.children && rootFileResponse.children.forEach(
                        child => this.mutateFileResponseForView(child)
                    );

                    this.currentDirectory = rootFileResponse;
                });
            }
        })
    }

    // mutate file responses: add unique keys for react to use as key prop
    // and add new object that corresponds to buttons column
    mutateFileResponseForView = (fileRow) => {
        fileRow.key = fileRow.path;
        fileRow.pathAndFolderFlag = {
            path: fileRow.path,
            isFolder: fileRow.isFolder
        };
    };

    get loadingTableData() {
        return this.observedTableData.state === mobxUtils.PENDING
    }

    // select first element as default value
    get initialRootValue() {
        return this.fileSystemRootValues[0]
    }

    loadFilesAndFolders = action((absolutePath) => {

        let oldPromise = null;
        if (mobxUtils.isPromiseBasedObservable(this.observedTableData)) {
            oldPromise = this.observedTableData;

            if (this.loadingTableData) {
                mobxUtils.fromPromise.reject(this.observedTableData);
            }
        }

        // appoint current one as the last scanned
        this.lastScannedPath = this.currentDirectory && this.currentDirectory.path;

        // observedTableData will have values 'state' and 'value' when the promise (api call) is resolved
        this.observedTableData = mobxUtils.fromPromise(
            this.api.fetchFilesAndFolders(absolutePath, 1).then(response => {
                setTimeout(() => this.handleFileEvents(absolutePath), 0);
                return response
            }),
            oldPromise
        );
    });

    loadSystemRoots = () => {

        return this.api.fetchFileSystemRoots().then(responseBody => {

            action(() => this.fileSystemRootValues = responseBody.content)();
            return this.initialRootValue
        });
    };

    loadLastScanned = () => {

        if (this.lastScannedPath) {
            this.loadFilesAndFolders(this.lastScannedPath)
        }
    };

    deleteFile = (path) => {

        getConfirmation(() => this.api.deleteFile(path).then(() => {
                goodNews(`${path} is removed successfully.`)
            }),
            'Are you sure to delete?', path);
    };

    closeEventSource = () => {
        if (this.eventSource) {
            this.eventSource.close();
            this.eventSource = null
        }
    };

    handleFileEvents = (path) => {

        this.closeEventSource();
        this.eventSource = getEventSource(path);
        console.info(`created new event source for ${path}`);

        // ignore 'Initialized' events
        this.eventSource.addEventListener(EventType.CREATED, this.handleCreatedEvent);
        this.eventSource.addEventListener(EventType.MODIFIED, this.handleModifiedEvent);
        this.eventSource.addEventListener(EventType.DELETED, this.handleDeletedEvent);
    };

    handleCreatedEvent = e => {

        const createdFile = parseJsonOrGetString(e.data);
        // console.log(`Event ${EventType.CREATED}, data: `, createdFile);

        runInAction(() => {

            if (this.currentDirectory) {

                let current = toJS(this.currentDirectory);

                this.mutateFileResponseForView(createdFile);
                current.children = [...current.children, createdFile];
                this.currentDirectory = current;
            }
        })
    };

    handleModifiedEvent = e => {

        const modifiedFile = parseJsonOrGetString(e.data);
        // console.log(`Event ${EventType.MODIFIED}, data: `, modifiedFile);

        runInAction(() => {

            let current = toJS(this.currentDirectory);

            if (current.path === modifiedFile.path) {

                this.mutateFileResponseForView(modifiedFile);
                this.currentDirectory = modifiedFile
            } else {

                current.children = current.children.map(childProxy => {

                    const child = toJS(childProxy);
                    if (child.path === modifiedFile.path) {
                        this.mutateFileResponseForView(modifiedFile);
                        return modifiedFile
                    }
                    return child
                });

                this.currentDirectory = current;
            }
        })
    };

    handleDeletedEvent = e => {

        // event data contains only the string that holds removed file's path
        const deletedFilePath = e.data;
        // console.log(`Event ${EventType.DELETED}, data: `, deletedFilePath);

        runInAction(() => {

            if (this.currentDirectory) {

                let current = toJS(this.currentDirectory);
                current.children = current.children.filter(it => toJS(it).path !== deletedFilePath);
                this.currentDirectory = current;
            }
        });
    }

}

decorate(TableListStore, {
    loadingTableData: computed
});
