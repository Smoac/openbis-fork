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
    ImageListItem,
    Paper, Snackbar,
    ThemeProvider
} from "@material-ui/core";
import openbis from "@src/js/services/openbis";

import SaveIcon from '@material-ui/icons/Save';
import DeleteIcon from '@material-ui/icons/Delete';
import RefreshIcon from '@material-ui/icons/Refresh';
import AddToQueueIcon from '@material-ui/icons/AddToQueue';

import Dropdown from "@src/js/components/database/premise/common/Dropdown.js";
import AlertDialog from "@src/js/components/database/premise/common/AlertDialog";
import InputFileUpload from "@src/js/components/database/premise/components/InputFileUpload";
import {convertToBase64} from "@src/js/components/database/premise/utils";
import Export from "@src/js/components/database/premise/components/Exporter";
import Player from "@src/js/components/database/premise/common/Player";
import InputsPanel from "@src/js/components/database/premise/components/InputsPanel";
import CircularProgress from "@material-ui/core/CircularProgress";
import {Alert} from "@material-ui/lab";

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
        border: '3px solid #34b2e4',
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
    },
}));

const ImagingDataSetViewer = () => {
    const classes = useStyles();

    const [open, setOpen] = React.useState(false);
    const [snackbar, setSnackbar] = React.useState({show:false, message:'Default Success', severity:"success"});
    const handleClose = () => {
        setOpen(false);
    };
    const handleOpen = () => {
        setOpen(true);
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

    let resolutions = imagingDataSet.config.resolutions;
    let images = imagingDataSet.images;
    let activeImage = imagingDataSet.images[activeImageIdx];
    let activePreview = imagingDataSet.images[activeImageIdx].previews[activePreviewIdx];
    let activeConfig = imagingDataSet.images[activeImageIdx].config;
    let extendedInputsConfig = imagingDataSet.config.inputs.map(c => Object.assign(c, { 'initValue': imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config[c.label] }));

    //console.log("CONFIG: ", imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config);


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

    const handleExport = (state) => {
        //console.log(state);
        let exportRequest = {
            "type": "export",
            "permId": "999999999-9999",
            "error": null,
            "imageIndex": imagingDataSet.images[activeImageIdx].imageIdx,
            "export": {
                "@type": "ImagingDataSetExport",
                "config": state,
                "bytes": null
            }
        };
        alert(JSON.stringify(exportRequest));

        //getExportResponse(exportRequest);
    };

    const handleUpload = async (file) => {
        //console.log(file);
        const base64 = await convertToBase64(file);
        //console.log('handleUpload: ', base64);
        //onUpload(activeImage.imageIdx, base64, file)
    };

    const updatePreview = () => {
        setOpen(true);
        console.log('UPDATE PREVIEW ', activeImageIdx, activePreviewIdx, imagingDataSet.images[activeImageIdx].config);
        console.log('UPDATE PREVIEW ', imagingDataSet.images[activeImageIdx].previews[activePreviewIdx]);
        //onUpdate(activeImage.imageIdx, activePreview.previewIdx, activeConfig);
        setTimeout(handleClose, 1000);
        setTimeout(() => setSnackbar({show:true, message:"Image updated", severity: "success"}), 1000);
    };

    const savePreview = () => {
        console.log('SAVE PREVIEW ', activeImageIdx, activePreviewIdx);
        //onUpdate(activeImage.imageIdx, activePreview.previewIdx, activeConfig);
    };

    const createNewPreview = () => {
        console.log('NEW PREVIEW ', activeImageIdx, activePreviewIdx);
        //onNew(activeImage.imageIdx);
    };

    const deletePreview = () => {
        console.log("DELETE PREVIEW ", activeImageIdx, activePreviewIdx);
        //onDelete(activeImage.imageIdx, activePreview.previewIdx);
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
                         srcSet={`${preview.img}`}
                         src={`${preview.img}`}
                         alt={preview.bytes}
                    />
                   {/* <img
                        className={classes.imgFullWidth}
                        src={`data:image/${preview.format};base64,${preview.bytes}`}
                        height={164}
                        width={164}
                    />*/}
                </ThemeProvider>
                {/*{activePreview.previewIdx == preview.previewIdx ? moveArrowCompList(activePreview.previewIdx) : ''}*/}
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
                                 extendedConfig={imagingDataSet.config.inputs}
                                 prevConfigValues={imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config}
                                 onConfigChange={handleActiveConfigChange} />

                    <Grid item container
                          justifyContent="center"
                          alignItems="center">
                        <Player speedable={true}></Player>
                    </Grid>
                </Grid>
            </Grid>
            <Divider className={classes.dividerFullWidth} />

        </React.Fragment>
    )

}

export default ImagingDataSetViewer;
