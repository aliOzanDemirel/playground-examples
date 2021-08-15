import {action, extendObservable} from 'mobx';
import {goodNews, warn} from "../../../common/service/notifier-service";

export default class CodeEditorStore {

    constructor(api) {
        this.api = api;

        extendObservable(this, {
            inputPath: null,
            inputTextArea: null
        });
    };

    loadFile = action('loadFile', indexerName => {
        if (this.isValidPath(this.inputPath)) {
            this.api.getCodeFileContent({
                jspPath: this.inputPath
            }).then(action.bound(resp => this.inputTextArea = resp.loadedContent));
        }
    });

    saveFile = action('saveFile', indexerName => {
        if (this.isValidPath(this.inputPath) && this.inputTextArea !== null) {
            this.api.postCodeFileContent({
                jspPath: this.inputPath,
                contentToBeWritten: this.inputTextArea
            }).then(respBodies => respBodies.forEach(it => goodNews('Saved JSP in ' + it.serverIP)))
        }
    });

    isValidPath = (path) => {
        if (!path.startsWith('/')) {
            warn('Enter a valid path');
            return false;
        }
        return true;
    }

}