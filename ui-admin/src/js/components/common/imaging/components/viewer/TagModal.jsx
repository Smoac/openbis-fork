import {Box, Checkbox, Modal, Typography} from "@mui/material";
import React from "react";
import Dropdown from "@src/js/components/common/imaging/components/common/Dropdown.jsx";
import makeStyles from '@mui/styles/makeStyles';
import StyleIcon from '@mui/icons-material/Style';
import messages from "@src/js/common/messages.js";
import constants from "@src/js/components/common/imaging/constants.js";
import Button from "@src/js/components/common/form/Button.jsx";
import {isObjectEmpty} from "@src/js/components/common/imaging/utils.js";
import TextField from '@mui/material/TextField';
import Autocomplete from '@mui/material/Autocomplete';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';

const icon = <CheckBoxOutlineBlankIcon fontSize="small" />;
const checkedIcon = <CheckBoxIcon fontSize="small" />;

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
    },
    mt: {
        marginTop: '10px'
    }
}));


const TagModal = ({imagingTags, activePreview}) => {
    const classes = useStyles();
    const [open, setOpen] = React.useState(false);
    

    const handleOpen = () => setOpen(true);
    const handleClose = () => setOpen(false);

    console.log("TagModal - activePreview: ", activePreview);
    console.log("TagModal - tagOptions: ", imagingTags);
    return (<>
        <Button
            label={messages.get(messages.TAGS)}
            type='final'
            color='primary'
            variant='outlined'
            onClick={handleOpen}
            startIcon={<StyleIcon/>} />
        <Modal open={open}
               onClose={handleClose}
               aria-labelledby="modal-title"
               aria-describedby="modal-description"
        >
            <Box sx={style}>
                <Typography id="modal-title" variant="h6" component="h2">
                    Current Preview {messages.get(messages.TAGS)}
                </Typography>
                <Autocomplete
                    multiple
                    id="tags-outlined"
                    options={imagingTags}
                    disableCloseOnSelect
                    getOptionLabel={(option) => option.label}
                    defaultValue={activePreview.tags}
                    renderInput={(params) => (
                        <TextField {...params} label="Imaging Tags" />
                      )}
                    renderOption={(props, option, { selected }) => {
                        const { key, ...optionProps } = props;
                        return (
                          <li key={key} {...optionProps}>
                            <Checkbox
                              icon={icon}
                              checkedIcon={checkedIcon}
                              style={{ marginRight: 8 }}
                              checked={selected}
                            />
                            {option.label}
                          </li>
                        );
                      }}
                />
                <div className={classes.mt} >
                    <Button label={messages.get(messages.TAG)} type='secondary' onClick={() => console.log('CLICKED')} />
                    <Button label={messages.get(messages.CANCEL)} type='risky' onClick={handleClose} styles={{ root: classes.risky }} />
                </div>
            </Box>

        </Modal>
    </>);
};

export default TagModal;