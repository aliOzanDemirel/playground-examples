export default class MockApi {

    responseBody = objToReturn => {
        return new Promise((resolve, reject) => {
            resolve(objToReturn);
        }).catch((err) => {
            console.error('error in mock service: ', err);
        });
    };

    getRandomInt = (min, max) => {
        min = Math.ceil(min);
        max = Math.floor(max);
        return Math.floor(Math.random() * (max - min + 1)) + min;
    };

    getRandomBool = () => this.getRandomInt(0, 1) === 1;

    getDummyFileResponse = path => {
        return {
            path: path,
            sizeInBytes: this.getRandomInt(0, 10000000000),
            created: this.getRandomInt(1333333333, 1571414550),
            modified: this.getRandomInt(1333333333, 1571414550),
            isFolder: this.getRandomBool(),
            isHidden: this.getRandomBool()
        }
    };

    fetchFileSystemRoots = () => this.responseBody({
        content: ['C:/', 'D:/', 'E:/']
    });

    fetchFilesAndFolders = (path, depth) => {

        const respPath = path + '/parent';
        const response = this.getDummyFileResponse(respPath);
        response.isFolder = true;
        response.children = [
            this.getDummyFileResponse(respPath + '/dummy'),
            this.getDummyFileResponse(respPath + '/dummier'),
            this.getDummyFileResponse(respPath + '/mock'),
            this.getDummyFileResponse(respPath + '/mockier')
        ];

        if (depth !== 1) {
            response.children.forEach(
                it => it.children = [
                    this.getDummyFileResponse(it.path + '/oneMoreLevel'),
                    this.getDummyFileResponse(it.path + '/levelMoreOne'),
                ]
            )
        }

        return this.responseBody(response)
    };

    deleteFile = filePath =>  this.responseBody({})

}
