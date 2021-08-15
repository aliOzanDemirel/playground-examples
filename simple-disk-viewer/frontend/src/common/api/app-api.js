import {doDelete, doGet} from "../request-service";

export default class AppApi {

    /**
     * returns available roots in filesystem. windows can have multiple.
     */
    fetchFileSystemRoots = () => {
        return doGet({
            url: '/roots'
        });
    };

    /**
     * returns a root FileResponse:
     *     path: String
     *     sizeInBytes: Long
     *     created: Boolean
     *     modified: Boolean
     *     isDirectory: Boolean
     *     isHidden: Boolean
     *     children: List<FileResponse>
     */
    fetchFilesAndFolders = (pathToScan, depthLevel) => {
        return doGet({
            url: '/files'
        }, {
            path: pathToScan,
            depth: depthLevel
        });
    };

    /**
     * returns 204 when file is successfully removed.
     */
    deleteFile = (filePath) => {
        return doDelete({
            url: '/files'
        }, {path: filePath});
    };

}
