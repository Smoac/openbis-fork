import { Box, Button, Modal, Typography } from "@material-ui/core";
import React from "react";
import Dropdown from "@src/js/components/database/premise/common/Dropdown";
import { makeStyles } from "@material-ui/core/styles";
import CloudDownloadIcon from '@material-ui/icons/CloudDownload';
import SelectField from "@src/js/components/common/form/SelectField.jsx";
import GridPagingOptions from "@src/js/components/common/grid/GridPagingOptions";

const style = {
    position: 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: 400,
    bgcolor: 'background.paper',
    border: '2px solid #000',
    boxShadow: 24,
    p: 4,
};

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


const Export = ({ config, handleExport }) => {
    const classes = useStyles();
    const [open, setOpen] = React.useState(false);
    const [exportState, setExportState] = React.useState(Object.fromEntries(config.map(c => {
        switch (c.type) {
            case 'Dropdown':
                return [c.label, c.multiselect ? [c.values[0]] : c.values[0]];
        }
    })));

    const handleOpen = () => setOpen(true);
    const handleClose = () => setOpen(false);

    const handleExportChange = (event) => {
        setExportState(prevState => {
            let newState = {...prevState};
            newState[event.target.name] = event.target.value;
            return newState;
        });
    };

    const sendExportRequest = () => {
        handleExport(exportState);
        handleClose();
    };

    const listExportsComp = config.map((c, idx) => {
        switch (c.type) {
            case 'Dropdown':
                return <Dropdown key={"export-" + c.type + "-" + idx}
                                 label={c.label}
                                 initValue={c.multiselect ? [c.values[0]] : c.values[0]}
                                 values={c.values}
                                 isMulti={c.multiselect}
                                 onSelectChange={handleExportChange}/>
                /*return <SelectField key={"export-" + c.type + "-" + idx}
                                        label={c.label}
                                        value={c.values[0]}
                                        options={c.values.map(pageSize => ({
                                            label: pageSize,
                                            value: pageSize
                                        }))}
                                        isMulti={c.multiselect}
                                        onSelectChange={handleExportChange}></SelectField>*/
            default:
                return <h2>UNKOWN TYPE: {c.type}</h2>
        }
    });

    return (
        <>
            <Button sx={{ height: 'fit-content' }} variant="outlined" onClick={handleOpen} startIcon={<CloudDownloadIcon/>}>Export</Button>
            <Modal open={open}
                   onClose={handleClose}
                   aria-labelledby="modal-modal-title"
                   aria-describedby="modal-modal-description"
            >
                <Box sx={style}>
                    <Typography id="modal-modal-title" variant="h6" component="h2">
                        Select export options
                    </Typography>
                    {listExportsComp}
                    <Button onClick={sendExportRequest} variant="contained" color="secondary">Export</Button>
                    <Button onClick={handleClose} variant="contained" className={classes.risky}>Close</Button>
                </Box>

            </Modal>
        </>
    );
};

export default Export;