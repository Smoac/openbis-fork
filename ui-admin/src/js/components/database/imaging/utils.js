
export const convertToBase64 = (file) => {
    return new Promise((resolve, reject) => {
        const fileReader = new FileReader();
        fileReader.readAsDataURL(file);
        fileReader.onload = () => {
            resolve(fileReader.result);
        };
        fileReader.onerror = (error) => {
            reject(error);
        };
    });
};

function download(arrayBuffer, type) {
    let blob = new Blob([arrayBuffer], { type: type });
    //console.log(blob);
    let url = URL.createObjectURL(blob);
    //console.log(url);
    window.open(url);
}

export const getExportResponse = async (exportRequest) => {
    try {
        const result = await openbis.getImaginingDataSetExport(exportRequest);
        console.log('Data => ', result);
        download(result.export.bytes, result.export.config.Format);
    }
    catch (err) {
        console.log('Err => ', err);
    }
}

export const isObjectEmpty = (objectName) => {
    return (
        objectName &&
        Object.keys(objectName).length === 0 &&
        objectName.constructor === Object
    );
};