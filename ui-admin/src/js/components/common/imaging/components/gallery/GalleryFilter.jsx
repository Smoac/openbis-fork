import React from 'react';
import { FormControl, MenuItem, Select, Grid2 } from "@mui/material";
import GridFilterOptions from '@src/js/components/common/grid/GridFilterOptions.js';
import TextField from '@src/js/components/common/form/TextField.jsx';
import SelectField from '@src/js/components/common/form/SelectField.jsx';
import messages from '@src/js/common/messages.js';
import { makeStyles } from '@mui/styles';

const useStyles = makeStyles((theme) => ({
    container: {
        display: 'flex',
        alignItems: 'center',
    },
    operator: {
        flex: '0 0 auto',
        marginRight: theme.spacing(2),
    },
    text: {
        width: '100%',
    },
    tagsSelect: {
        width: '100%',
        marginTop: theme.spacing(2)
    }
}));

const ITEM_HEIGHT = 48;
const ITEM_PADDING_TOP = 8;
const MenuProps = {
    PaperProps: {
        style: {
            maxHeight: ITEM_HEIGHT * 4.5 + ITEM_PADDING_TOP,
            width: 250,
        },
    },
};

const GalleryFilter = ({ id, options, galleryFilter, onGalleryFilterChange }) => {
    const classes = useStyles();
    const [selectedTags, setSelectedTags] = React.useState([]);

    const TAGS_OPTIONS = [{ label: 'Tag1', value: 'TAG1' }, { label: 'Tag2', value: 'TAG2' }];

    const handleGalleryFilterChange = (event) => {
        const { name, value } = event.target;
        console.log('handleGalleryFilterChange: ', name, value)
        console.log('galleryFilter: ', galleryFilter)
        if (onGalleryFilterChange) {
            const newGlobalFilter = { ...galleryFilter }
            newGlobalFilter[name] = value
            onGalleryFilterChange(newGlobalFilter)
        }
    };

    const handleTagsOnChange = (event) => {
        // Handle multi-select changes here
        console.log('handleTagsOnChange:', event);
        const { target: { value, name } } = event;
        setSelectedTags(
            // On autofill we get a stringified value.
            typeof value === 'string' ? value.split(',') : value,
        );

        if (onGalleryFilterChange) {
            const newGlobalFilter = { ...galleryFilter }
            newGlobalFilter['text'] = value.join(' ')
            onGalleryFilterChange(newGlobalFilter)
        }
    };

    return (
        <Grid2 container spacing={1} sx={{ alignItems: 'center', width:'100%' }}>
            <Grid2 size='auto' xs={{ alignSelf: 'center' }}>
                <SelectField
                    name="operator"
                    options={[
                        {
                            label: messages.get(messages.OPERATOR_AND),
                            value: GridFilterOptions.OPERATOR_AND,
                        },
                        {
                            label: messages.get(messages.OPERATOR_OR),
                            value: GridFilterOptions.OPERATOR_OR,
                        },
                    ]}
                    value={galleryFilter.operator}
                    onChange={handleGalleryFilterChange}
                    variant="standard"
                />
            </Grid2>
            <Grid2 size={{xs:12, sm:4}}>
                <SelectField
                    name="property"
                    options={options}
                    value={galleryFilter.property}
                    onChange={handleGalleryFilterChange}
                    variant="standard"
                />
            </Grid2>
            <Grid2 size='grow'>
                {galleryFilter.property === messages.TAGS ?
                    <FormControl variant="standard" className={classes.tagsSelect} >
                        <Select
                            multiple
                            value={selectedTags}
                            onChange={handleTagsOnChange}
                        >
                            {TAGS_OPTIONS.map((tag) => (
                                <MenuItem key={tag.value} value={tag.value}>
                                    {tag.label}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                    :
                    <TextField name="text"
                        id={`${id}.gallery-filter`}
                        value={galleryFilter.text}
                        onChange={handleGalleryFilterChange}
                        placeholder="property value"
                        variant="standard"
                    />
                }
            </Grid2>
        </Grid2>
    );
};

export default GalleryFilter;