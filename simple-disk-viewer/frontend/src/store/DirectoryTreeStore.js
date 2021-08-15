import * as mobxUtils from "mobx-utils";
import {action, autorun, extendObservable, observable, runInAction} from 'mobx';

export default class DirectoryTreeStore {

    constructor(api) {
        this.api = api;

        // depth to use when querying file system of server,
        // when the directory is expanded in every 3 levels, the new children will be loaded from server
        this.defaultDepthLevel = 3;

        // TODO: this is too heavy but tree nodes does not keep extra data as props,
        //  only title and some others, can be changed with extra call to get the details
        this.fileResponsesByKey = new Map();

        extendObservable(this, {
            observedFileResponse: {
                state: null
            },
            selectedFileResponse: null,
            treeData: [],
            timeline: []
        });

        autorun(() => {

            if (this.observedFileResponse.state === mobxUtils.FULFILLED) {

                const rootFileResponse = this.fileResponseForView(this.observedFileResponse.value, 0, 0);

                runInAction(() => {
                    this.treeData = observable([rootFileResponse]);
                    this.updateTimeline('Initialized folder structure with root: ' + rootFileResponse.title);
                    this.observedFileResponse.value = null
                });
            }
        })
    }

    get loading() {
        return this.observedFileResponse.state === mobxUtils.PENDING
    }

    fileResponseForView = (file, treeKey, childKey) => {

        const resp = {
            key: treeKey,
            title: file.path
        };

        resp.children = file.children && file.children.map(
            it => {
                const treeKeyForLevel = this.getChildKey(treeKey, childKey);
                return this.fileResponseForView(it, treeKeyForLevel, childKey++)
            }
        );

        // if leaf is set when creating tree, onLoad is not triggered by directory tree
        // even if the item is a folder, so these will be set as user clicks
        // resp.isLeaf = !Array.isArray(resp.children) || resp.children.length === 0;

        // so JS wouldn't convert '0' to number 0
        this.fileResponsesByKey.set(resp.key.toString(), file);

        return resp
    };

    getChildKey = (parentKey, childSpecificKey) => parentKey + '-' + childSpecificKey;

    updateSelectedNode = action(selectedNodeProps => {

        this.selectedFileResponse = this.fileResponsesByKey.get(selectedNodeProps.eventKey);
    });

    loadDetailedFileTree = action((absolutePath) => {

        this.observedFileResponse = mobxUtils.fromPromise(
            this.api.fetchFilesAndFolders(absolutePath, this.defaultDepthLevel)
        );
    });

    expandFileTree(treeNode) {

        const nodePath = treeNode.props.title;
        const nodeKey = treeNode.props.eventKey;

        const fileForNode = this.fileResponsesByKey.get(nodeKey);
        if (fileForNode && !fileForNode.isFolder) {
            runInAction(() => treeNode.props.dataRef.isLeaf = true);
            return null;
        }

        this.updateTimeline(`Fetching tree of ${nodePath}`);

        return this.api.fetchFilesAndFolders(nodePath, this.defaultDepthLevel)
            .then(currentAsRoot => {

                runInAction(() => {

                    if (currentAsRoot.children) {

                        treeNode.props.dataRef.children = currentAsRoot.children.map(
                            (child, index) => this.fileResponseForView(
                                child, this.getChildKey(nodeKey, index), 0
                            )
                        );
                        this.updateTimeline(`Loaded ${currentAsRoot.path} with its children`);

                    } else {

                        treeNode.props.dataRef.isLeaf = true;
                        this.updateTimeline(`${currentAsRoot.path} is made leaf as it has no children`);
                    }

                    this.treeData = [...this.treeData];
                })
            })
    }

    updateTimeline = action(logMessage => {
        this.timeline.push(logMessage);
    });

}
