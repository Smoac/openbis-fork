import * as React from 'react';
import Button from '@material-ui/core/Button';
import ConfirmationDialog from "@src/js/components/common/dialog/ConfirmationDialog.jsx";

export default function AlertDialog({label, icon, title, content, disabled, onHandleYes}) {
    const [open, setOpen] = React.useState(false);

    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleYes = () => {
        setOpen(false);
        onHandleYes(true);
    };

    return (
        <>
            <Button variant="outlined" onClick={handleClickOpen} startIcon={icon} disabled={disabled}>
                {label}
            </Button>
            <ConfirmationDialog open={open}
                                onConfirm={handleYes}
                                onCancel={() => setOpen(false)}
                                title={title}
                                content={content}/>
        </>
    );
}