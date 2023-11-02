import * as React from 'react';
import { styled } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import CloudUploadIcon from '@material-ui/icons/CloudUpload';

const VisuallyHiddenInput = styled('input')({
    clip: 'rect(0 0 0 0)',
    clipPath: 'inset(50%)',
    height: 1,
    overflow: 'hidden',
    position: 'absolute',
    bottom: 0,
    left: 0,
    whiteSpace: 'nowrap',
    width: 1,
});

export default function InputFileUpload({onInputFile}) {

    const [file, setFile] = React.useState(null);
    const [flag, setFlag] = React.useState(false);

    const fileChangedHandler = async (event) => {
        console.log(event.target.files);
        let file = event.target.files[0];
        let reader = new FileReader();
        reader.onload = function (e) {
            setFile(e.target.result);
            setFlag(true);
            //var arrayBuffer = e.target.result;
            //console.log(arrayBuffer);
            //Download(arrayBuffer, file.type);
        };
        //reader.readAsDataURL(event.target.files[0]);
        reader.readAsArrayBuffer(event.target.files[0]);
        onInputFile(file);
    };

    return (
        <Button component="label" variant="outlined" startIcon={<CloudUploadIcon />}>
            Upload
            <VisuallyHiddenInput type="file"
                                 onChange={fileChangedHandler}
                                 inputprops={{ accept: "image/*" }}
                                 accept=".png,.jpg,.jpeg" />
        </Button>
    );
};