import * as React from 'react';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import {makeStyles} from "@material-ui/core/styles";

const useStyles = makeStyles((theme) => ({
    risky: {
        backgroundColor: theme.palette.error.main,
        color: theme.palette.error.contrastText,
        '&:hover': {
            backgroundColor: theme.palette.error.dark
        },
        '&:disabled': {
            backgroundColor: theme.palette.error.light
        }
    }
}));
export default function AlertDialog({label, icon, title, text, onHandleYes}) {
    const classes = useStyles();
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
            <Button variant="outlined" onClick={handleClickOpen} startIcon={icon}>
                {label}
            </Button>
            <Dialog
                open={open}
                onClose={() => setOpen(false)}
                aria-labelledby="alert-dialog-title"
                aria-describedby="alert-dialog-description"
            >
                <DialogTitle id="alert-dialog-title">
                    {title}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        {text}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpen(false)} className={classes.risky}>CANCEL</Button>
                    <Button onClick={handleYes} variant="outlined" color="secondary" autoFocus>
                        YES
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
}