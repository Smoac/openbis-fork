import React from 'react'
import logger from '@src/js/common/logger.js'
import {createTheme, makeStyles} from "@material-ui/core/styles";
import {
    Box,
    Divider,
    Grid,
    ImageList,
    ImageListItem,
    Paper,
    ThemeProvider
} from "@material-ui/core";
import Dropdown from "@src/js/components/database/premise/common/Dropdown.js";
import openbis from "@src/js/services/openbis";
import Button from "@material-ui/core/Button";

import SaveIcon from '@material-ui/icons/Save';
import DeleteIcon from '@material-ui/icons/Delete';
import RefreshIcon from '@material-ui/icons/Refresh';
import AddToQueueIcon from '@material-ui/icons/AddToQueue';
import AlertDialog from "@src/js/components/database/premise/common/AlertDialog";
import InputFileUpload from "@src/js/components/database/premise/components/InputFileUpload";
import {convertToBase64} from "@src/js/components/database/premise/utils";
import Export from "@src/js/components/database/premise/components/Exporter";
import Player from "@src/js/components/database/premise/common/Player";
import InputsPanel from "@src/js/components/database/premise/components/InputsPanel";
//import ArrowCircleLeftIcon from '@material-ui/icons/ArrowCircleLeft';
//import ArrowCircleRightIcon from '@material-ui/icons/ArrowCircleRight';

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

    const [imagingDataSet, setImaginingDataSet] = React.useState(openbis.getImaginingDataSetConfig());
    const [resolution, setResolution] = React.useState([200, 200]);
    const [activeImageIdx, setActiveImageIdx] = React.useState(0);
    const [activePreviewIdx, setActivePreviewIdx] = React.useState(0);

    let resolutions = imagingDataSet.config.resolutions;
    let images = imagingDataSet.images;
    let activeImage = imagingDataSet.images[activeImageIdx];
    let activePreview = imagingDataSet.images[activeImageIdx].previews[activePreviewIdx];
    let activeConfig = activePreview.config;
    let extendedInputsConfig = imagingDataSet.config.inputs.map(c => Object.assign(c, { 'initValue': activeConfig[c.label] }));

    console.log("CONFIG: ", imagingDataSet);

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
        console.log('handleActiveConfigChange = ', activeConfig === newConfig);
        //setActiveConfig(newConfig);
    }

    const handleExport = (state) => {
        //console.log(state);
        let exportRequest = {
            "type": "export",
            "permId": "999999999-9999",
            "error": null,
            "imageIndex": activeImage.imageIdx,
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
        console.log('handleUpload: ', base64);
        //onUpload(activeImage.imageIdx, base64, file)
    };

    const updatePreview = () => {
        console.log('UPDATE PREVIEW ', activeImageIdx, activePreviewIdx);
        //onUpdate(activeImage.imageIdx, activePreview.previewIdx, activeConfig);
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

    const imagesCompList = images.map((image, idx) => (
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

    const previewsCompList = activeImage.previews.map((preview, idx) => (
            <ImageListItem className={activePreviewIdx === preview.previewIdx ? classes.elevation : classes.trasparency} onClick={() => handleActivePreviewChange(preview.previewIdx)} key={preview.previewIdx}>
                <ThemeProvider theme={themeList}>
                    <img className={classes.imgFullWidth}
                         srcSet={`${preview.img}`}
                         src={`${preview.img}`}
                         alt={preview.bytes}
                    />
                </ThemeProvider>
                {/* <img
                                        src={`data:image/${preview.format};base64,${preview.bytes}`}
                                        height={164}
                                        width={164}
                                    />
                                    {activePreview.previewIdx == preview.previewIdx ? moveArrowCompList(activePreview.previewIdx) : ''}*/}
            </ImageListItem>
        ));

    logger.log(logger.DEBUG, 'ImaginingDataViewer.render');
    return (
        <React.Fragment>
            <h2>Imaging Dataset Viewer</h2>
            <Grid container direction="row">
                <Grid item xs>
                    <Grid item xs={12}>
                        <Box className={classes.imgContainer}>
                            <Paper sx={{width: '100%', height: '100%'}} variant="outlined">
                                <img
                                    src={`${activePreview.img}?w=${resolution[0]}&h=${resolution[1]}&fit=crop&auto=format`}
                                    alt={activePreview.bytes}
                                    loading="lazy"
                                />
                            </Paper>
                        </Box>
                    </Grid>
                    <Grid item xs={3}>
                        <Dropdown onSelectChange={handleResolutionChange} label="Resolutions"
                                  values={resolutions} initValue={[resolution.join('x')]}/>
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
                    <InputsPanel inputsConfig={imagingDataSet.config.inputs} extendedConfig={extendedInputsConfig} prevConfig={activeConfig} onConfigChange={handleActiveConfigChange} />

                    <Grid item container
                          direction="row"
                          justifyContent="right"
                          alignItems="center"
                            className={classes.spaced2}>

                        <Export handleExport={handleExport} config={imagingDataSet.config.export} />

                        <Player speedable={true}></Player>
                    </Grid>
                </Grid>
            </Grid>
            <Divider className={classes.dividerFullWidth} />

        </React.Fragment>
    )

}

export default ImagingDataSetViewer;
