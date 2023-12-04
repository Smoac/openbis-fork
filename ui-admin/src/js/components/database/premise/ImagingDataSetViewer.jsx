import React from 'react'
import {createTheme, makeStyles} from "@material-ui/core/styles";
import {
    Box,
    Button,
    Grid,
    ImageList,
    ImageListItem,
    ThemeProvider, Switch,
} from "@material-ui/core";

import SaveIcon from '@material-ui/icons/Save';
import DeleteIcon from '@material-ui/icons/Delete';
import RefreshIcon from '@material-ui/icons/Refresh';
import AddToQueueIcon from '@material-ui/icons/AddToQueue';

import {
    convertToBase64
} from "@src/js/components/database/premise/utils";
import Dropdown from "@src/js/components/database/premise/common/Dropdown";
import AlertDialog from "@src/js/components/database/premise/common/AlertDialog";
import InputFileUpload from "@src/js/components/database/premise/components/InputFileUpload";
import Export from "@src/js/components/database/premise/components/Exporter";
import InputsPanel from "@src/js/components/database/premise/components/InputsPanel";
import MetadataViewer from "@src/js/components/database/premise/components/MetadataViewr";
import ImageListItemBarAction from "@src/js/components/database/premise/common/ImageListItemBarAction";
import OutlinedBox from "@src/js/components/database/premise/common/OutlinedBox";
import InputSlider from "@src/js/components/database/premise/common/InputSlider";
import InputRangeSlider from "@src/js/components/database/premise/common/InputRangeSlider";
import Typography from "@material-ui/core/Typography";
import PaperBox from "@src/js/components/database/premise/common/PaperBox";
import ColorMap from "@src/js/components/database/premise/components/ColorMap";

const themeList = createTheme({
    overrides: {
        // Style sheet name ⚛️
        MuiImageListItem: {
            // Name of the rule
            imgFullWidth: {
                // Some CSS
                width: '100%'
            }
        },
    },
});

const useStyles = makeStyles((theme) => ({
    backdrop: {
        zIndex: theme.zIndex.drawer + 1,
        color: '#fff',
    },
    imgContainer: {
        maxWidth: '800px',
        maxHeight: '800px',
        textAlign: 'center',
        overflow: 'auto',
    },
    imgFullWidth:{
        width: '100%'
    },
    elevation: {
        boxShadow: '0 3px 10px rgb(0 0 0 / 0.2)',
        border: '3px solid #039be5',
        width: '150px !important',
    },
    trasparency: {
        opacity: 0.5,
        width: '150px !important',
    },
    imageList: {
        flexWrap: 'nowrap',
        // Promote the list into his own layer on Chrome. This cost memory but helps keeping high FPS.
        transform: 'translateZ(0)',
        height: 'auto',
    },
    dividerFullWidth: {
        margin: `${theme.spacing(2)}px 0 0 0`,
    },
    spaced1: {
        '& > *': {
            margin: theme.spacing(1),
        },
    },
    spaced2: {
        '& > *': {
            margin: theme.spacing(2),
        },
    },
    gridDirection: {
        flexDirection: "row",
        [theme.breakpoints.down('md')]: {
            flexDirection: "column",
        },
    },
    topSticky: {
        position: 'sticky',
        top: 0,
        zIndex: 1000,
        background: 'white',
    },
    bottomSticky: {
        position: 'sticky',
        bottom: 0,
        zIndex: 1000,
        background: 'white',
    },
    noBorderNoShadow: {
        border: 'unset',
        boxShadow: 'none',
    }
}));


const ImagingDataSetViewer = ({images, config, onDelete, onNew, onUpload, onUpdate, onSave, onMove, onExport}) => {
    const classes = useStyles();
    const [loading, setLoading] = React.useState(true);
    //console.log('ImagingDataSetViewer - dataset: ', dataset);
    const [imagingDataSet, setImaginingDataSet] = React.useState(images);
    //const [images, setImages] = React.useState(images);
    const [resolution, setResolution] = React.useState([200, 200]);
    const [activeImageIdx, setActiveImageIdx] = React.useState(0);
    const [activePreviewIdx, setActivePreviewIdx] = React.useState(0);
    console.log('ImagingDataSetViewer - imagingDataSet: ', imagingDataSet);
    //let resolutions = imagingDataSet.config.resolutions;
    //let images = imagingDataSet.images;
    //let activeImage = imagingDataSet !== undefined && imagingDataSet.images[activeImageIdx];
    //let activePreview = imagingDataSet.images[activeImageIdx].previews[activePreviewIdx];
    //let activeConfig = imagingDataSet.images[activeImageIdx].config;
    //let extendedInputsConfig = imagingDataSet.config.inputs.map(c => Object.assign(c, { 'initValue': imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config[c.label] }));
    /*let inputValues = !loading && Object.fromEntries(imagingDataSet.config.inputs.map(input => {
        switch (input.type) {
            case 'Dropdown':
                return [input.label, input.values[0]];
            case 'Slider':
                return [input.label, [0]];
            case 'Range':
                return [input.label, [0,0]];
            case 'Colormap':
                return [input.label, input.values[0]];
        }
    }));*/
    //console.log('inputValues = ', inputValues);
    //console.log("CONFIG: ", imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config);

    /*    React.useEffect( () => {
        if(imagingDataSet !== undefined && imagingDataSet !== null)
            setLoading(false);
        else
            setLoading(true);
    }, [imagingDataSet]);*/

    if (loading) return null;

    const moveArrowCompList = (currentIdx) => {
        let previewsLength = imagingDataSet.images[activeImageIdx].previews.length;
        if (currentIdx === 0 && previewsLength === 1) { // only 1 element
            return [];
        } else if (currentIdx === 0) { // first element
            return [<ImageListItemBarAction key={"ImageListItemBarAction-left-"+currentIdx} classNames={'singleActionBar'} position={'right'} onMove={() => onMove(activeImageIdx, activePreviewIdx, 1)}/>];
        } else if (currentIdx === previewsLength - 1) { // last element
            return [<ImageListItemBarAction key={"ImageListItemBarAction-right-"+currentIdx} classNames={'singleActionBar'} position={'left'} onMove={() => onMove(activeImageIdx, activePreviewIdx, -1)}/>];
        } else {
            //console.log('ELEMENT ', currentIdx, (activeImage.previews.length) - 1);
            return [<ImageListItemBarAction key={"ImageListItemBarAction-left-"+currentIdx} classNames={'actionBarL'} position={'left'} onMove={() => onMove(activeImageIdx, activePreviewIdx, -1)}/>,
                <ImageListItemBarAction key={"ImageListItemBarAction-right-"+currentIdx} classNames={'actionBarR'} position={'right'}  onMove={() => onMove(activeImageIdx, activePreviewIdx, 1)}/>];
        }
    };

    const handleResolutionChange = (event) => {
        //setOpen(true);
        const v_list = event.target.value.split('x');
        setResolution(v_list);
        //setTimeout(handleClose, 1000);
    };

    const handleActiveImageChange = (selectedImageIdx) => {
        //setOpen(true);
        setActiveImageIdx(selectedImageIdx);
        setActivePreviewIdx(0);
        //setTimeout(handleClose, 1000);
        //setActiveConfig(selectedImage.previews[0].config);
        //var extendedConfig = [...extendedInputsConfig].map(c => Object.assign(c, { 'initValue': selectedImage.previews[0].config[c.label] }));
        //setExtendedInputsConfig('handleActiveImageChange', extendedConfig);
    };

    const handleActivePreviewChange = (selectedPreviewIdx) => {
        setActivePreviewIdx(selectedPreviewIdx);
        //setActiveConfig(selectedPreview.config);
        //var extendedConfig = [...extendedInputsConfig].map(c => Object.assign(c, { 'initValue': selectedPreview.config[c.label] }));
        //setExtendedInputsConfig(extendedConfig);
        //console.log('handleActivePreviewChange = ', selectedPreview.config);
    };

    const handleActiveConfigChange = (name, value, update) => {
        //console.log('handleActiveConfigChange = ', imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config);
        //console.log('handleActiveConfigChange = ', name, value, update);
        let toUpdateIDS = {...imagingDataSet};
        toUpdateIDS.images[activeImageIdx].previews[activePreviewIdx].config[name] = value;
        setImaginingDataSet(toUpdateIDS);
        //setActiveConfig(newConfig);
        if (update) onUpdate();
    }

    const handleUpload = async (file) => {
        const base64 = await convertToBase64(file);
        await onUpload(activeImageIdx, base64, file);
    };

    const missingText = (text) => (
        <Typography variant='body2'>
            {text}
        </Typography>)

    //const blankImage = <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAJYAAACWCAQAAACWCLlpAAAA4ElEQVR42u3QQREAAAgDoK1/HvtZwbcHEWgmHFWWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZMmSJUuWLFmyZP20rIxqpeLaMHMAAAAASUVORK5CYII="/>;
    const blankImage = <img className={classes.imgFullWidth} src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAB+0lEQVR4nOXbSW7FIBBF0eso+99yMglSErmAKqoB+03+4EvG78g9Al6e6+f3q3Qv6nJ9VO9BdT47/12d/07M7VHeOwKedFqIXUanwBMQuh3+A9wd9icj3O37n453R8BTEIblQT4FTkeYKg/9a8CpCNPlYXwRPA1BVR7GANIGdkRQl4c5AGlDOyGYysM8gLTBHRDM5UEHIG24EmGpPOgBpAEqEJbLgw1AGigTwaU82AGkATMQ3MrDGoA0cCSCa3lYB5B2IALBvTz4AEA8Qkh58AOAOISw8uALAP4IoeXBHwD8EMLLQwwArCOklIc4ALAjpJWHWADQI6SWh3gAmEdILw85ADBGKCkPeQAgI5SVh1wAmCuWOiVXMTnaK5g+H/n62eEKAO1tMDTZANYHobBkAkhX+9LPa1kAo1tdGUIGwOx9vgQhGkD7kJOOEAlgfcJLRYgCWH28TUOIAPB6tk9B8AbwfrEJR/AEiHqrC0XwAoh+pQ1D8ADIep8PQVgFyP6Y4Y6wAlD1JccVwQpQ+hlLGMuEYAGoLt8bU42gBdilfG9sFYIGYLfyLUsIswC7lm8xI8wA7F6+xYRgWTCxY/kWNYJ2yczO5VtUCBLAqeVbphHuAE4v3zKFMLNw8sTyv9PtZlkwcVrMCyaeUL5F7NJbObrDWoDwvH52+PX5BmNDa373V091AAAAAElFTkSuQmCC"/>;


    const imagesCompList = imagingDataSet.images.map(image => (
        image.previews.length === 0 ? missingText('No Images are present in the Dataset') :
        <ImageListItem className={activeImageIdx === image.imageIdx ? classes.elevation : classes.trasparency} onClick={() => handleActiveImageChange(image.imageIdx)} key={image.imageIdx}>
            <ThemeProvider theme={themeList}>
                {/*<img
                    className={classes.imgFullWidth}
                    src={image.previews[0].img}
                    alt={image.previews[0].bytes}
                />*/}
                {image.previews[0].bytes ? <img
                    className={classes.imgFullWidth}
                    src={`data:image/${image.previews[0].format};base64,${image.previews[0].bytes}`}
                />: blankImage}
            </ThemeProvider>
        </ImageListItem>
    ));

    const previewsCompList = !loading && imagingDataSet.images[activeImageIdx].previews.map(preview => (
        //preview ? missingText('No Images are present in the Dataset') :
            <ImageListItem key={`imagelistitem-${preview.previewIdx}`}
                           className={activePreviewIdx === preview.previewIdx ? classes.elevation : classes.trasparency}
                           onClick={() => handleActivePreviewChange(preview.previewIdx)}>
                <ThemeProvider theme={themeList}>
                    {/*<img className={classes.imgFullWidth}
                         src={preview.img}
                         alt={preview.bytes}
                    />*/}
                    {preview.bytes ? <img
                        className={classes.imgFullWidth}
                        src={`data:image/${preview.format};base64,${preview.bytes}`}
                    /> : blankImage}
                </ThemeProvider>
                {activePreviewIdx === preview.previewIdx ? moveArrowCompList(activePreviewIdx) : ''}
            </ImageListItem>
    ));

    const bigPreviewImageComp = !loading &&
        (imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].bytes === null ?
            <p>No preview available</p> : <img
                src={`${imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].img}?w=${resolution[0]}&h=${resolution[1]}&fit=crop&auto=format`}
                alt={imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].bytes}
                loading="lazy"
                height={resolution[0]}
                width={resolution[1]}
            />)

    const [show, setShow] = React.useState(true);

    return (
        <React.Fragment>
            <React.Suspense fallback={<h2>{"Loading with suspense......."}</h2>}>

                <PaperBox>
                    <Typography variant='h6'>
                        Images
                    </Typography>
                    <ImageList className={classes.imageList}
                               cols={3}
                               rowHeight={150}>
                        {images.config.images.map(img => <p>{img.imageIdx}</p>)}
                    </ImageList>
                </PaperBox>
            </React.Suspense>
            <PaperBox>
                <Grid container direction='row'>

                    <Grid item xs={10}>
                        <Typography variant='h6'>
                            Previews
                        </Typography>
                        <ImageList className={classes.imageList}
                                   cols={4}
                                   rowHeight={200}>
                            {previewsCompList}
                        </ImageList>
                    </Grid>

                    <Grid item xs={2} container direction='column' justifyContent="space-around">
                        <InputFileUpload onInputFile={handleUpload} />

                        <Button variant="outlined" startIcon={<AddToQueueIcon />}
                                onClick={onNew}>New</Button>

                        <AlertDialog label={'Save'} icon={<SaveIcon />}
                                     title={"Are you sure to save the current configuration?"}
                                     text={"The current configuration will be stored within the current preview."}
                                     onHandleYes={onSave} />
                        <AlertDialog label={'Delete'} icon={<DeleteIcon />}
                                     title={"Are you sure to delete the current preview?"}
                                     text={"The preview will be definitly delete from the dataset."}
                                     onHandleYes={onDelete} />

                        {imagingDataSet.config !== undefined && imagingDataSet.config.exports.length > 0 ? <Export handleExport={onExport}
                                                                            config={imagingDataSet.config.exports}/>: <></>}
                    </Grid>

                </Grid>
            </PaperBox>

            <PaperBox>
                <Grid container className={classes.gridDirection}>
                    <Grid container item xs
                          justifyContent="center"
                          alignItems="center">
                        <Box className={classes.imgContainer}>
                            {bigPreviewImageComp}
                        </Box>
                    </Grid>

                    <PaperBox className={classes.noBorderNoShadow}>
                        <Grid item xs>
                            <Grid container item xs justifyContent="space-around"
                                  alignItems="center">
                                <Button variant="outlined" startIcon={<RefreshIcon />}
                                    onClick={onUpdate} >Update</Button>
                                {imagingDataSet.config.exports.length > 0 ? <Export handleExport={handleExport}
                                         config={imagingDataSet.config.exports}/>: <></>}
                                <Dropdown onSelectChange={handleResolutionChange}
                                          label="Resolutions"
                                          values={imagingDataSet.config.resolutions}
                                          initValue={[resolution.join('x')]}/>
                            </Grid>

                            {activeImage.previews.length === 0 ? <p>No config</p>:<InputsPanel inputsConfig={imagingDataSet.config.inputs}
                                          prevConfigValues={imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config}
                                          onConfigChange={handleActiveConfigChange}/>}
                            <OutlinedBox style={{width:'fit-content'}} label="Show">
                                <Switch checked={show} onChange={() => setShow(!show)} color="primary"  />
                            </OutlinedBox>
                            {imagingDataSet.config.inputs.map((c, idx) => {
                                //const prevConfigValues = imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config;
                                //console.log(panelConfig, initConfig);
                                switch (c.type) {
                                    case 'Dropdown':
                                        return <Dropdown key={"InputsPanel-" + c.type + "-" + idx}
                                                         label={c.label}
                                                         initValue={inputValues[c.label]}
                                                         values={c.values}
                                                         isMulti={c.multiselect}
                                                         onSelectChange={(event) => handleActiveConfigChange(event.target.name, event.target.value)}/>;
                                    case 'Slider':
                                        if (c.visibility) {
                                            for (const condition of c.visibility) {
                                                if (condition.values.includes(inputValues[condition.label])) {
                                                    c.range = condition.range;
                                                    c.unit = condition.unit;
                                                }
                                            }
                                        }
                                        return <InputSlider
                                            key={"InputsPanel-" + c.type + "-" + idx}
                                            label={c.label}
                                            initValue={inputValues[c.label]}
                                            range={c.range}
                                            unit={c.unit}
                                            playable={c.playable}
                                            speeds={c.speeds}
                                            onChange={(name, value, update) => handleActiveConfigChange(name, value, update)}/>;
                                    case 'Range':
                                        console.log(c);
                                        if (c.visibility) {
                                            for (const condition of c.visibility) {
                                                if (condition.values.includes(inputValues[condition.label])) {
                                                    c.range = condition.range;
                                                    c.unit = condition.unit;
                                                }
                                            }
                                        }
                                        return <InputRangeSlider
                                            key={"InputsPanel-" + c.type + "-" + idx}
                                            label={c.label}
                                            initValue={inputValues[c.label]}
                                            range={c.range}
                                            unit={c.unit}
                                            playable={c.playable}
                                            speeds={c.speeds}
                                            onChange={(name, value, update) => console.log(name, value, update)}/>;
                                    //onChange={(name, value, update) => handleActiveConfigChange(name, value, update)}/>;
                                    case 'Colormap':
                                        return <ColorMap key={"InputsPanel-" + c.type + "-" + idx}
                                                         values={c.values}/>
                                }
                            })
                            }
                        </Grid>
                    </PaperBox>
                </Grid>
            </PaperBox>

            <PaperBox>
                <MetadataViewer configMetadata={imagingDataSet.config.metadata} previews={imagingDataSet.images[activeImageIdx].previews} />
            </PaperBox>
        </React.Fragment>
    )

}

export default ImagingDataSetViewer;
