import React from 'react'
import logger from '@src/js/common/logger.js'
import {createTheme, makeStyles} from "@material-ui/core/styles";
import {
    Backdrop,
    Box,
    Button,
    Divider,
    Grid,
    ImageList,
    ImageListItem, ImageListItemBar,
    Paper, Snackbar,
    ThemeProvider, Tooltip, IconButton
} from "@material-ui/core";
import {Alert} from "@material-ui/lab";
import openbis from "@src/js/services/openbis";

import SaveIcon from '@material-ui/icons/Save';
import DeleteIcon from '@material-ui/icons/Delete';
import RefreshIcon from '@material-ui/icons/Refresh';
import AddToQueueIcon from '@material-ui/icons/AddToQueue';
import ArrowLeftIcon from '@material-ui/icons/ArrowLeft';
import ArrowRightIcon from '@material-ui/icons/ArrowRight';

import Dropdown from "@src/js/components/database/premise/common/Dropdown.js";
import AlertDialog from "@src/js/components/database/premise/common/AlertDialog";
import InputFileUpload from "@src/js/components/database/premise/components/InputFileUpload";
import {
    convertToBase64,
    getExportResponse
} from "@src/js/components/database/premise/utils";
import Export from "@src/js/components/database/premise/components/Exporter";
import Player from "@src/js/components/database/premise/common/Player";
import InputsPanel from "@src/js/components/database/premise/components/InputsPanel";
import CircularProgress from "@material-ui/core/CircularProgress";

import MetadataViewer from "@src/js/components/database/premise/components/MetadataViewr";
import Stepper from "@src/js/components/database/premise/components/Stepper";
import ImageListItemBarAction
    from "@src/js/components/database/premise/common/ImageListItemBarAction";

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
    '@global': {
        "&::-webkit-scrollbar, & *::-webkit-scrollbar": {
            backgroundColor: "rgba( 48, 63, 159,0.5)",
            borderRadius: 10,
            height: 10,
            width: 10
        },
        "&::-webkit-scrollbar-thumb, & *::-webkit-scrollbar-thumb": {
            borderRadius: 10,
            backgroundColor: "rgba( 48, 63, 159,0.8)",
            height: 10,
        },
    },
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
    },
    trasparency: {
        opacity: 0.5
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
    }
}));

const ImagingDataSetViewer = () => {
    const classes = useStyles();

    const [open, setOpen] = React.useState(false);
    const [snackbar, setSnackbar] = React.useState({show:false, message:'Default Success', severity:"success"});
    const handleClose = () => {
        setOpen(false);
    };
    const handleSnackbarClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setSnackbar({show:false});
    };

    const [imagingDataSet, setImaginingDataSet] = React.useState(openbis.getImaginingDataSetConfig());
    const [resolution, setResolution] = React.useState([200, 200]);
    const [activeImageIdx, setActiveImageIdx] = React.useState(0);
    const [activePreviewIdx, setActivePreviewIdx] = React.useState(0);

    //let resolutions = imagingDataSet.config.resolutions;
    //let images = imagingDataSet.images;
    //let activeImage = imagingDataSet.images[activeImageIdx];
    //let activePreview = imagingDataSet.images[activeImageIdx].previews[activePreviewIdx];
    //let activeConfig = imagingDataSet.images[activeImageIdx].config;
    //let extendedInputsConfig = imagingDataSet.config.inputs.map(c => Object.assign(c, { 'initValue': imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config[c.label] }));

    //console.log("CONFIG: ", imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config);

    function handleMove(position) {
        console.log(activeImageIdx, activePreviewIdx, position);
        setOpen(true);
        let toUpdateImgDs = { ...imagingDataSet };
        let previewsList = toUpdateImgDs.images[activeImageIdx].previews;
        let tempMovedPreview = previewsList[activePreviewIdx];
        tempMovedPreview.previewIdx += position;
        previewsList[activePreviewIdx] = previewsList[activePreviewIdx+position];
        previewsList[activePreviewIdx].previewIdx -= position;
        previewsList[activePreviewIdx+position] = tempMovedPreview;
        toUpdateImgDs.images[activeImageIdx].previews = previewsList;
        setImaginingDataSet(toUpdateImgDs);
        setTimeout(handleClose, 1000);
        setTimeout(() => setSnackbar({show:true, message:"Preview Moved", severity: "success"}), 1000);
    }

    const moveArrowCompList = (currentIdx) => {
        let previewsLength = imagingDataSet.images[activeImageIdx].previews.length;
        if (currentIdx === 0 && previewsLength === 1) { // only 1 element
            return [];
        } else if (currentIdx === 0) { // first element
            return [<ImageListItemBarAction classNames={'singleActionBar'} position={'right'} onMove={() => handleMove(1)}/>];
        } else if (currentIdx === previewsLength - 1) { // last element
            return [<ImageListItemBarAction classNames={'singleActionBar'} position={'left'} onMove={() => handleMove(-1)}/>];
        } else {
            //console.log('ELEMENT ', currentIdx, (activeImage.previews.length) - 1);
            return [<ImageListItemBarAction classNames={'actionBarL'} position={'left'} onMove={() => handleMove(-1)}/>,
                <ImageListItemBarAction classNames={'actionBarR'} position={'right'}  onMove={() => handleMove(1)}/>];
        }
    };

    const handleResolutionChange = (event) => {
        const v_list = event.target.value.split('x');
        setResolution(v_list);
    };

    const handleActiveImageChange = (selectedImageIdx) => {
        setActiveImageIdx(selectedImageIdx);
        setActivePreviewIdx(0);
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

    const handleActiveConfigChange = (newConfig) => {
        //console.log('handleActiveConfigChange = ', imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config);
        //console.log('handleActiveConfigChange = ', newConfig);
        let toUpdateIDS = {...imagingDataSet};
        toUpdateIDS.images[activeImageIdx].previews[activePreviewIdx].config = newConfig;
        setImaginingDataSet(toUpdateIDS);
        //setActiveConfig(newConfig);
    }

    const handleActiveConfigChange2 = (name, value, update) => {
        //console.log('handleActiveConfigChange = ', imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config);
        console.log('handleActiveConfigChange2 = ', name, value, update);
        let toUpdateIDS = {...imagingDataSet};
        toUpdateIDS.images[activeImageIdx].previews[activePreviewIdx].config[name] = value;
        setImaginingDataSet(toUpdateIDS);
        //setActiveConfig(newConfig);
        if (update) updatePreview();
    }

    const handleExport = (state) => {
        let exportRequest = {
            "type": "export",
            "permId": "999999999-9999",//TODO: implement logic to get correct perm-ID
            "error": null,
            "imageIndex": imagingDataSet.images[activeImageIdx].imageIdx,
            "export": {
                "@type": "ImagingDataSetExport",
                "config": state,
                "bytes": null
            }
        };
        alert(JSON.stringify(exportRequest));
        getExportResponse(exportRequest);
    };

    const handleUpload = async (file) => {
        const base64 = await convertToBase64(file);
        await uploadPreview(activeImageIdx, base64, file);
    };

    const uploadPreview = async (imageIdx, bytes, file) => {
        setOpen(true);
        try {
            const result = await openbis.uploadImaginingDataSet(imagingDataSet, imageIdx, bytes, file);
            console.log('Data => ', result);
            setImaginingDataSet(result);
            setTimeout(handleClose, 1000);
            setTimeout(() => setSnackbar({show:true, message:"Image uploaded", severity: "success"}), 1000);
        }
        catch (err) {
            setTimeout(handleClose, 1000);
            setTimeout(() => setSnackbar({show:true, message:"Image not uploaded", severity: "error"}), 1000);
            console.log('Err => ', err);
        }
    };

    const updatePreview = async () => {
        setOpen(true);
        try {
            const result = await openbis.updateImaginingDataSet(imagingDataSet, activeImageIdx, activePreviewIdx, imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config);
            console.log('Data => ', result);
            setImaginingDataSet(result);
            setTimeout(handleClose, 1000);
            setTimeout(() => setSnackbar({show:true, message:"Image updated", severity: "success"}), 1000);
        }
        catch (err) {
            setTimeout(handleClose, 1000);
            setTimeout(() => setSnackbar({show:true, message:"Image not updated", severity: "error"}), 1000);
            console.log('Err => ', err);
        }
        //console.log('UPDATE PREVIEW ', activeImageIdx, activePreviewIdx, imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config);
        //console.log('UPDATE PREVIEW ', imagingDataSet.images[activeImageIdx].previews[activePreviewIdx]);
        //onUpdate(activeImage.imageIdx, activePreview.previewIdx, activeConfig);
    };

    const savePreview = () => {
        console.log('SAVE PREVIEW ', activeImageIdx, activePreviewIdx);
        //onUpdate(activeImage.imageIdx, activePreview.previewIdx, activeConfig);
    };

    const createNewPreview = async () => {
        console.log('NEW PREVIEW ', activeImageIdx, activePreviewIdx);
        setOpen(true);
        try {
            const result = await openbis.getNewPreview(imagingDataSet, activeImageIdx);
            console.log('createNewPreview Data => ', result);
            setImaginingDataSet(result);
            setTimeout(handleClose, 1000);
            setTimeout(() => setSnackbar({show:true, message:"Created new empty preview", severity: "success"}), 1000);
        }
        catch (err) {
            setTimeout(handleClose, 1000);
            setTimeout(() => setSnackbar({show:true, message:"Failed new empty preview creation", severity: "error"}), 1000);
            console.log('Err => ', err);
        }
    };

    const deletePreview = async () => {
        console.log("DELETE PREVIEW ", activeImageIdx, activePreviewIdx);
        //onDelete(activeImage.imageIdx, activePreview.previewIdx);
        setOpen(true);
        setActivePreviewIdx(0);
        try {
            const result = await openbis.deletePreview(imagingDataSet, activeImageIdx, activePreviewIdx);
            console.log('deletePreview Data => ', result);
            setImaginingDataSet(result);
            setTimeout(handleClose, 1000);
            setTimeout(() => setSnackbar({show:true, message:"Preview deleted", severity: "success"}), 1000);
        }
        catch (err) {
            setTimeout(handleClose, 1000);
            setTimeout(() => setSnackbar({show:true, message:"Failed to delete the preview", severity: "error"}), 1000);
            console.log('Err => ', err);
        }
    };

    const imagesCompList = imagingDataSet.images.map((image, idx) => (
        <ImageListItem className={activeImageIdx === image.imageIdx ? classes.elevation : classes.trasparency} onClick={() => handleActiveImageChange(image.imageIdx)} key={image.imageIdx}>
            <ThemeProvider theme={themeList}>
                <img
                    className={classes.imgFullWidth}
                    src={image.previews[0].img}
                    alt={image.previews[0].bytes}
                />
            </ThemeProvider>
        </ImageListItem>
    ));

    const previewsCompList = imagingDataSet.images[activeImageIdx].previews.map((preview, idx) => (
            <ImageListItem className={activePreviewIdx === preview.previewIdx ? classes.elevation : classes.trasparency} onClick={() => handleActivePreviewChange(preview.previewIdx)} key={preview.previewIdx}>
                <ThemeProvider theme={themeList}>
                    <img className={classes.imgFullWidth}
                         src={preview.img}
                         alt={preview.bytes}
                    />
                   {/* <img
                        className={classes.imgFullWidth}
                        src={`data:image/${preview.format};base64,${preview.bytes}`}
                        height={164}
                        width={164}
                    />*/}
                </ThemeProvider>
                {activePreviewIdx == preview.previewIdx ? moveArrowCompList(activePreviewIdx) : ''}
            </ImageListItem>
        ));

    logger.log(logger.DEBUG, 'ImaginingDataViewer.render');
    return (
        <React.Fragment>
            <Backdrop
                className={classes.backdrop}
                open={open}
            >
                <CircularProgress color="inherit" />
            </Backdrop>
            <Snackbar anchorOrigin={{ vertical:'bottom', horizontal:'right' }}
                      open={snackbar.show}
                      autoHideDuration={5000} onClose={handleSnackbarClose}>
                <Alert onClose={handleSnackbarClose} severity={snackbar.severity} sx={{ width: '100%' }}>
                    {snackbar.message}
                </Alert>
            </Snackbar>
            <h2>Imaging Dataset Viewer</h2>
            <Grid container direction="row">
                <Grid item xs>
                    <Grid item xs={12}>
                        <Box className={classes.imgContainer}>
                            <Paper sx={{width: '100%', height: '100%'}} variant="outlined">
                                <img
                                    src={`${imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].img}?w=${resolution[0]}&h=${resolution[1]}&fit=crop&auto=format`}
                                    alt={imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].bytes}
                                    loading="lazy"
                                    height={resolution[0]}
                                    width={resolution[1]}
                                />
                            </Paper>
                        </Box>
                    </Grid>
                    <Grid item container
                          direction="row"
                          justifyContent="space-between"
                          alignItems="center">
                        <Grid item xs={3}>
                            <Dropdown onSelectChange={handleResolutionChange}
                                      label="Resolutions"
                                      values={imagingDataSet.config.resolutions}
                                      initValue={[resolution.join('x')]}/>
                        </Grid>

                        <Export handleExport={handleExport}
                                config={imagingDataSet.config.export} />
                    </Grid>
                    <Grid item xs={12}>
                        <h2>
                            Images:
                        </h2>
                        <ImageList className={classes.imageList}
                                   cols={3}
                                   rowHeight={200}>
                            {imagesCompList}
                        </ImageList>
                    </Grid>
                    <Grid item xs={12} className={classes.spaced1} >
                        <h2>
                            Previews:
                        </h2>

                        <Button sx={{ margin: '3px' }} variant="outlined" startIcon={<RefreshIcon />}
                                onClick={updatePreview} >Update</Button>

                        <InputFileUpload onInputFile={handleUpload} sx={{ margin: '3px' }} />

                        <Button sx={{ margin: '3px' }} variant="outlined" startIcon={<AddToQueueIcon />}
                                onClick={createNewPreview} >New</Button>

                        <AlertDialog label={'Save'} icon={<SaveIcon />}
                                     title={"Are you sure to save the current configuration?"}
                                     text={"The current configuration will be stored within the current preview."}
                                     onHandleYes={savePreview} />
                        <AlertDialog label={'Delete'} icon={<DeleteIcon />}
                                     title={"Are you sure to delete the current preview?"}
                                     text={"The preview will be definitly delete from the dataset."}
                                     onHandleYes={deletePreview} />
                        <Divider className={classes.dividerFullWidth} />
                    </Grid>
                    <Grid item xs={12} >
                        <ImageList className={classes.imageList}
                                   cols={4}
                                   rowHeight={200}>

                                {previewsCompList}
                        </ImageList>
                    </Grid>
                </Grid>
                <Grid item xs>
                    <InputsPanel inputsConfig={imagingDataSet.config.inputs}
                                 prevConfigValues={imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config}
                                 onConfigChange={handleActiveConfigChange2} />

                    {/*<Grid item container
                          justifyContent="center"
                          alignItems="center">
                        <Player speedable={true}></Player>
                    </Grid>*/}
                </Grid>
            </Grid>
            <Divider className={classes.dividerFullWidth} />

            <MetadataViewer configMetadata={imagingDataSet.config.metadata} previews={imagingDataSet.images[activeImageIdx].previews} />

        </React.Fragment>
    )

}

export default ImagingDataSetViewer;
