import * as React from 'react';
import { styled } from '@mui/material/styles';
import makeStyles from '@mui/styles/makeStyles';
import Button from '@mui/material/Button';
import PublishIcon from '@mui/icons-material/Publish'
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

    const fileChangedHandler = async (event) => {
        let file = event.target.files[0];
        let reader = new FileReader();
        reader.readAsArrayBuffer(event.target.files[0]);
        onInputFile(file);
    };

    return (
        <Button component="label" variant="outlined" color="inherit" startIcon={<PublishIcon/>}>
            {messages.get(messages.UPLOAD)}
            <VisuallyHiddenInput type="file"
                                 onChange={fileChangedHandler}
                                 inputprops={{accept: "image/*"}}
                                 accept=".png,.jpg,.jpeg"/>
        </Button>
    );
}