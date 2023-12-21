import * as React from 'react';
import {makeStyles, styled} from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import CloudUploadIcon from '@material-ui/icons/CloudUpload';
import messages from "@src/js/common/messages.js";

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
        let file = event.target.files[0];
        let reader = new FileReader();
        reader.onload = function (e) {
            setFile(e.target.result);
            setFlag(true);
        };
        reader.readAsArrayBuffer(event.target.files[0]);
        onInputFile(file);
    };

    return (
        <Button component="label" variant="outlined" startIcon={<CloudUploadIcon/>}>
            {messages.get(messages.UPLOAD)}
            <VisuallyHiddenInput type="file"
                                 onChange={fileChangedHandler}
                                 inputprops={{accept: "image/*"}}
                                 accept=".png,.jpg,.jpeg"/>
        </Button>
    );
};