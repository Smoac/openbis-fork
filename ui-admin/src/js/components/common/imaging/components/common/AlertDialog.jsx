import * as React from 'react';
import ConfirmationDialog from "@src/js/components/common/dialog/ConfirmationDialog.jsx";
import Button from "@src/js/components/common/form/Button.jsx";

export default function AlertDialog({label, icon, title, content, disabled, onHandleYes}) {
    const [open, setOpen] = React.useState(false);

    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleYes = () => {
        setOpen(false);
        onHandleYes(true);
    };

    return (<>
        <Button
            label={label}
            variant='outlined'
            color='default'
            onClick={handleClickOpen}
            startIcon={icon}
            disabled={disabled} />
        <ConfirmationDialog open={open}
                            onConfirm={handleYes}
                            onCancel={() => setOpen(false)}
                            title={title}
                            content={content}/>
    </>);
}