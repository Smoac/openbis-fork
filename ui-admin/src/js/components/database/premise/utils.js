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

export function Download(arrayBuffer, type) {
    var blob = new Blob([arrayBuffer], { type: type });
    //console.log(blob);
    var url = URL.createObjectURL(blob);
    //console.log(url);
    window.open(url);
}