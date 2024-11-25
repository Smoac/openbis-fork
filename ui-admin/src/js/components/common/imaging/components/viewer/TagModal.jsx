import {Box, Checkbox, Modal, Stack, Typography} from "@mui/material";
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


const TagModal = ({handleTagImage, imagingTags, activePreview}) => {
    const classes = useStyles();
    const [open, setOpen] = React.useState(false);
    const [tags, setTags] = React.useState(activePreview.tags)
    const [inputValue, setInputValue] = React.useState(activePreview.tags);


    React.useEffect(() => {
        //console.log('activePreview.tags: ', activePreview.tags);
        var trasformedTags = []
        for (const activePreviewTag of activePreview.tags) {
            //console.log('activePreviewTag: ', activePreviewTag);
            const matchTag = imagingTags.find(imagingTag => imagingTag.value === activePreviewTag);
            //console.log("matchTag: ", matchTag);
            trasformedTags.push(matchTag);
        }
        //console.log("trasformedTags: ", trasformedTags);
        setTags(trasformedTags);
        setInputValue(trasformedTags);
    }, [activePreview])

    const handleOpen = () => setOpen(true);
    const handleClose = () => setOpen(false);

    const hangleTagsChange = (tagAll) => {
        handleClose();
        const tagsArray = tags.map(tag => tag.value);
        //console.log('hangleTagsChange: ', tagsArray);
        handleTagImage(tagAll, tagsArray);
    }
    
    console.log("TagModal - activePreview: ", activePreview);
    console.log("TagModal - tagOptions: ", imagingTags);
    return (<>
        <Button
            label={messages.get(messages.TAGS)}
            type='final'
            color='inherit'
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
                    inputValue={inputValue}
                    value={tags}
                    onInputChange={(event, newInputValue) => {
                        setInputValue(newInputValue);
                      }}
                    renderInput={(params) => (
                        <TextField {...params} label="Imaging Tags" />
                      )}
                    renderOption={(props, option, { selected }) => {
                        const { key, ...optionProps } = props;
                        return (
                          <li key={key} {...optionProps}>
                            <Checkbox
                              icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
                              checkedIcon={<CheckBoxIcon fontSize="small" />}
                              style={{ marginRight: 8 }}
                              checked={selected}
                            />
                            {option.label}
                          </li>
                        );
                      }}
                    onChange={(event, newValue) => setTags(newValue)}
                />
                <Stack spacing={2} direction="row" sx={{ mt: 2 }}>
                    <Button label={messages.get(messages.TAG) + " Current"} type='secondary' onClick={() => hangleTagsChange(false)} />
                    <Button label={messages.get(messages.TAG) + " " + messages.get(messages.ALL)} type='secondary' onClick={() => hangleTagsChange(true)} />
                    <Button label={messages.get(messages.CANCEL)} type='risky' onClick={handleClose} styles={{ root: classes.risky }} />
                </Stack>
            </Box>

        </Modal>
    </>);
};

export default TagModal;