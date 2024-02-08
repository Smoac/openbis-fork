import React from 'react'
import {withStyles} from "@material-ui/core/styles";
import {
    Box,
    Divider,
    Grid,
    Typography
} from "@material-ui/core";

import {convertToBase64, inRange, isObjectEmpty} from "@src/js/components/common/imaging/utils.js";
import PaperBox from "@src/js/components/common/imaging/components/common/PaperBox.js";
import InputFileUpload from "@src/js/components/common/imaging/components/viewer/InputFileUpload.js";
import AlertDialog from "@src/js/components/common/imaging/components/common/AlertDialog.jsx";
import Export from "@src/js/components/common/imaging/components/viewer/Exporter.jsx";
import Dropdown from "@src/js/components/common/imaging/components/common/Dropdown.jsx";
import OutlinedBox from "@src/js/components/common/imaging/components/common/OutlinedBox.js";
import InputSlider from "@src/js/components/common/imaging/components/common/InputSlider.jsx";
import InputRangeSlider from "@src/js/components/common/imaging/components/common/InputRangeSlider.jsx";
import ColorMap from "@src/js/components/common/imaging/components/viewer/ColorMap.jsx";
import ImagingFacade from "@src/js/components/common/imaging/ImagingFacade.js";
import constants from "@src/js/components/common/imaging/constants.js";
import ImagingMapper from "@src/js/components/common/imaging/ImagingMapper.js";
import CustomSwitch from "@src/js/components/common/imaging/components/common/CustomSwitch.jsx";
import ImageListItemSection from "@src/js/components/common/imaging/components/common/ImageListItemSection.js";

import AddToQueueIcon from "@material-ui/icons/AddToQueue";
import SaveIcon from "@material-ui/icons/Save";
import DeleteIcon from "@material-ui/icons/Delete";
import RefreshIcon from "@material-ui/icons/Refresh";

import messages from '@src/js/common/messages.js'
import LoadingDialog from "@src/js/components/common/loading/LoadingDialog.jsx";
import Message from '@src/js/components/common/form/Message.jsx'
import ErrorDialog from "@src/js/components/common/error/ErrorDialog.jsx";
import Button from '@src/js/components/common/form/Button.jsx'
import DefaultMetadaField
    from "@src/js/components/common/imaging/components/gallery/DefaultMetadaField.js";

const styles = theme => ({
    imgContainer: {
        maxHeight: '800px',
        textAlign: 'center',
        overflow: 'auto',
    },
    gridDirection: {
        flexDirection: "row",
        [theme.breakpoints.down('sm')]: {
            flexDirection: "column",
        },
    },
    noBorderNoShadow: {
        border: 'unset',
        boxShadow: 'none',
    }
});

class ImagingDataSetViewer extends React.PureComponent {
    constructor(props) {
        super(props)
        this.state = {
            error: {open: false, error: null},
            isSaved: true,
            isChanged: false,
            open: true,
            loaded: false,
            imagingDataset: {},
            activeImageIdx: 0,
            activePreviewIdx: 0,
            resolution: ['original']
        }
    }

    async componentDidMount() {
        if (!this.state.loaded) {
            const {objId, extOpenbis} = this.props;
            try {
                const imagingDataSetPropertyConfig = await new ImagingFacade(extOpenbis).loadImagingDataset(objId);
                if (isObjectEmpty(imagingDataSetPropertyConfig.images[0].previews[0].config)) {
                    imagingDataSetPropertyConfig.images[0].previews[0].config = this.createInitValues(imagingDataSetPropertyConfig.config.inputs, {});
                    this.setState({open: false, loaded: true, isChanged: true, imagingDataset: imagingDataSetPropertyConfig});
                } else {
                    this.setState({open: false, loaded: true, imagingDataset: imagingDataSetPropertyConfig});
                }
                //console.log("componentDidMount: ", imagingDataSetPropertyConfig);
            } catch(error) {
                this.handleError(error);
            }
        }
    }

    saveDataset = async () => {
        const {objId, extOpenbis, onUnsavedChanges} = this.props;
        const {imagingDataset} = this.state;
        this.handleOpen();
        try {
            const isSaved = await new ImagingFacade(extOpenbis).saveImagingDataset(objId, imagingDataset);
            if (isSaved === null) {
                this.setState({open: false, isChanged: false, isSaved: true});
                if (onUnsavedChanges !== null)
                    onUnsavedChanges(this.props.objId, false);
            }
        } catch (error) {
            this.setState({open: false, isChanged: false, isSaved: false});
            this.handleError(error);
        }
    }

    handleUpdate = async () => {
        this.handleOpen();
        const {imagingDataset, activeImageIdx, activePreviewIdx} = this.state;
        const {objId, extOpenbis, onUnsavedChanges} = this.props;
        try {
            const updatedImagingDataset = await new ImagingFacade(extOpenbis)
                .updateImagingDataset(objId, activeImageIdx, imagingDataset.images[activeImageIdx].previews[activePreviewIdx]);
            if (updatedImagingDataset.error){
                this.setState({open: false, isChanged: true, isSaved: false});
                this.handleError(updatedImagingDataset.error);
            }
            delete updatedImagingDataset.preview['@id']; //@id are duplicated across different previews on update, need to be deleted
            let toUpdateImgDs = { ...imagingDataset };
            toUpdateImgDs.images[activeImageIdx].previews[activePreviewIdx] = updatedImagingDataset.preview;
            this.setState({open: false, imagingDataset : toUpdateImgDs, isChanged: false, isSaved: false});
            if (onUnsavedChanges !== null)
                onUnsavedChanges(this.props.objId, true);
        } catch (error) {
            this.setState({open: false, isChanged: true, isSaved: false});
            this.handleError(error);
        }
    };

    onExport = async (state) => {
        this.handleOpen();
        const {activeImageIdx} = this.state;
        const {objId, extOpenbis} = this.props;
        try {
            const downloadableURL = await new ImagingFacade(extOpenbis)
                .exportImagingDataset(objId, activeImageIdx, state, {});
            if (downloadableURL)
                window.open(downloadableURL, '_blank');
            this.setState({open: false});
        } catch (error) {
            this.setState({open: false});
            this.handleError(error);
        }
    };

    handleErrorCancel = () => {
        this.setState({
            error: {open: false, error: null}
        })
    }

    handleError = (error) => {
        this.setState({
            error: {open: true, error: error}
        })
    }

    handleOpen = () => {
        this.setState({open: true});
    }

    handleActiveImageChange = (selectedImageIdx) => {
        this.setState({activeImageIdx: selectedImageIdx, activePreviewIdx: 0});
    };

    handleActivePreviewChange = (selectedPreviewIdx) => {
        this.setState({activePreviewIdx: selectedPreviewIdx});
    };

    handleResolutionChange = (event) => {
        /*console.log('handleResolutionChange: ', event);
        let val = event.target.value;
        console.log(val);
        if (val === 'original'){
            const {imagingDataset, activeImageIdx, activePreviewIdx,} = this.state;
            const {width, height} = imagingDataset.images[activeImageIdx].previews[activePreviewIdx];
            this.setState({resolution: [width, height]});
        } else if (val.includes('x')){*/
        const v_list = event.target.value.split('x');
        this.setState({resolution: v_list});
    };

    handleActiveConfigChange = (name, value, update = false) => {
        const {imagingDataset, activeImageIdx, activePreviewIdx,} = this.state;
        let toUpdateIDS = {...imagingDataset};
        toUpdateIDS.images[activeImageIdx].previews[activePreviewIdx].config[name] = value;
        this.setState({imagingDataset: toUpdateIDS, isChanged: true});
        // Used by the player to autoupdate
        if (update) {
            this.handleUpdate();
        }
    }

    handleShowPreview = () => {
        const {imagingDataset, activeImageIdx, activePreviewIdx} = this.state;
        let toUpdateIDS = {...imagingDataset};
        toUpdateIDS.images[activeImageIdx].previews[activePreviewIdx].show = !toUpdateIDS.images[activeImageIdx].previews[activePreviewIdx].show;
        this.setState({imagingDataset: toUpdateIDS, isSaved: false});
    }

    onMove = (position) => {
        const {imagingDataset, activeImageIdx, activePreviewIdx} = this.state;
        this.handleOpen();
        let toUpdateImgDs = { ...imagingDataset };
        let previewsList = toUpdateImgDs.images[activeImageIdx].previews;
        let tempMovedPreview = previewsList[activePreviewIdx];
        tempMovedPreview.index += position;
        previewsList[activePreviewIdx] = previewsList[activePreviewIdx+position];
        previewsList[activePreviewIdx].index -= position;
        previewsList[activePreviewIdx+position] = tempMovedPreview;
        toUpdateImgDs.images[activeImageIdx].previews = previewsList;
        this.setState({open: false, imagingDataset: toUpdateImgDs, isSaved: false});
    }

    createInitValues = (inputsConfig, activeConfig) => {
        const isActiveConfig = isObjectEmpty(activeConfig);
        return Object.fromEntries(inputsConfig.map(input => {
            switch (input.type) {
                case constants.DROPDOWN:
                    if (!isActiveConfig)
                        return [input.label, activeConfig[input.label] ? activeConfig[input.label] : input.multiselect ? [input.values[0]] : input.values[0]];
                    else
                        return [input.label, input.multiselect ? [input.values[0]] : input.values[0]];
                case constants.SLIDER:
                    if (!isActiveConfig) {
                        if (input.visibility) {
                            for (const condition of input.visibility) {
                                if (condition.values.includes(activeConfig[condition.label])) {
                                    input.range = condition.range;
                                    input.unit = condition.unit;
                                }
                            }
                        }
                        return [input.label, activeConfig[input.label] ? activeConfig[input.label] : input.range[0]];
                    } else {
                        if (input.visibility) {
                            input.range = input.visibility[0].range[0];
                        }
                        return [input.label, input.range[0]];
                    }
                case constants.RANGE:
                    if (!isActiveConfig) {
                        if (input.visibility) {
                            for (const condition of input.visibility) {
                                if (condition.values.includes(activeConfig[condition.label])) {
                                    input.range = condition.range;
                                    input.unit = condition.unit;
                                }
                            }
                        }
                        let rangeInitValue = [inRange(activeConfig[input.label][0], input.range[0], input.range[1]) ? activeConfig[input.label][0] : input.range[0],
                            inRange(activeConfig[input.label][1], input.range[0], input.range[1]) ? activeConfig[input.label][1] : input.range[1]]
                        return [input.label, rangeInitValue];
                    } else {
                        if (input.visibility) {
                            return [input.label, [input.visibility[0].range[0], input.visibility[0].range[1]]];
                        }
                        return [input.label, [input.range[0], input.range[1]]];
                    }
                case constants.COLORMAP:
                    if (!isActiveConfig)
                        return [input.label, activeConfig[input.label] ? activeConfig[input.label] : input.values[0]];
                    else
                        return [input.label, input.values[0]];
            }
        }));
    };

    createNewPreview = () => {
        const {imagingDataset, activeImageIdx, activePreviewIdx} = this.state;
        const {extOpenbis} = this.props;
        let toUpdateImgDs = {...imagingDataset};
        let newLastIdx = toUpdateImgDs.images[activeImageIdx].previews.length;
        let inputValues = this.createInitValues(imagingDataset.config.inputs, toUpdateImgDs.images[activeImageIdx].previews[activePreviewIdx].config);
        let imagingDataSetPreview = new ImagingMapper(extOpenbis)
            .getImagingDataSetPreview(inputValues, 'png', null, null, null, newLastIdx, false, {});
        toUpdateImgDs.images[activeImageIdx].previews = [...toUpdateImgDs.images[activeImageIdx].previews, imagingDataSetPreview];
        this.setState({activePreviewIdx: newLastIdx, imagingDataset: toUpdateImgDs, isChanged:true, isSaved: false})
    };

    handleUpload = async (file) => {
        this.handleOpen();
        const base64 = await convertToBase64(file);
        const {imagingDataset, activeImageIdx} = this.state;
        try {
            let toUpdateImgDs = {...imagingDataset};
            let newLastIdx = toUpdateImgDs.images[activeImageIdx].previews.length;
            let previewTemplate = new ImagingMapper(this.props.extOpenbis).getImagingDataSetPreview(
                {},
                file.type.split('/')[1],
                base64.split(',')[1],
                null,
                null,
                newLastIdx,
                false,
                {"file": file}
            )
            toUpdateImgDs.images[activeImageIdx].previews = [...toUpdateImgDs.images[activeImageIdx].previews, previewTemplate];
            this.setState({open: false, imagingDataset: toUpdateImgDs, isSaved: false})
        } catch (error) {
            this.setState({open: false});
            this.handleError(error);
        }
    };

    deletePreview = () => {
        this.handleOpen();
        const {imagingDataset, activeImageIdx, activePreviewIdx} = this.state;
        let toUpdateImgDs = {...imagingDataset};
        toUpdateImgDs.images[activeImageIdx].previews.splice(activePreviewIdx,1);
        toUpdateImgDs.images[activeImageIdx].previews = toUpdateImgDs.images[activeImageIdx].previews.map(p => {
            if (p.index > activePreviewIdx)
                p.index -= 1;
            return p;
        });
        this.setState({imagingDataset: toUpdateImgDs, activePreviewIdx: 0});
        this.saveDataset();
    };

    render() {
        const { loaded, open, error } = this.state;
        if (!loaded) return null;
        const {imagingDataset, activeImageIdx, activePreviewIdx, resolution, isSaved, isChanged} = this.state;
        const {classes} = this.props;
        const activePreview = imagingDataset.images[activeImageIdx].previews[activePreviewIdx];
        //console.log('ImagingDataSetViewer.render: ', this.state);
        return (
            <React.Fragment>
                <LoadingDialog loading={open} />
                <ErrorDialog open={error.state} error={error.error} onClose={this.handleErrorCancel} />
                {this.renderImageSection(imagingDataset.images, activeImageIdx, imagingDataset.config.exports)}
                {this.renderPreviewsSection(imagingDataset.images[activeImageIdx].previews, imagingDataset.config.exports, activeImageIdx, activePreviewIdx, isSaved)}
                <PaperBox>
                    <Grid container className={classes.gridDirection}>
                        {this.renderBigPreview(classes, activePreview, resolution)}
                        {this.renderInputControls(classes, activePreview, imagingDataset.config.inputs, imagingDataset.config.resolutions, resolution, isChanged)}
                    </Grid>
                </PaperBox>
                {this.renderMetadataSection(classes, activePreview, imagingDataset.images[activeImageIdx], imagingDataset.config.metadata)}
            </React.Fragment>
        )
    };

    renderImageSection(images, activeImageIdx, configExports) {
        return (
            <PaperBox>
                <Grid container direction='row' spacing={1}>
                    <Grid item xs={9} sm={10}>
                        <ImageListItemSection title={messages.get(messages.IMAGES)}
                                              cols={3} rowHeight={150}
                                              type={constants.IMAGE_TYPE}
                                              items={images}
                                              activeImageIdx={activeImageIdx}
                                              onActiveItemChange={this.handleActiveImageChange}/>
                    </Grid>
                    <Grid item xs={3} sm={2} container direction='column' justifyContent="space-around">
                        {configExports.length > 0 ?
                            <Export handleExport={this.onExport}
                                    config={configExports}/> : <></>}
                    </Grid>
                </Grid>
            </PaperBox>
        )
    };

    renderPreviewsSection(previews, configExports, activeImageIdx, activePreviewIdx, isSaved) {
        const nPreviews = previews.length;
        return (
            <PaperBox>
                <Grid container direction='row' spacing={1}>
                    <Grid item xs={9} sm={10}>
                        <ImageListItemSection title={messages.get(messages.PREVIEWS)}
                                              cols={4} rowHeight={200}
                                              type={constants.PREVIEW_TYPE}
                                              items={previews}
                                              activeImageIdx={activeImageIdx}
                                              activePreviewIdx={activePreviewIdx}
                                              onActiveItemChange={this.handleActivePreviewChange}
                                              onMove={this.onMove}/>
                    </Grid>
                    <Grid item xs={3} sm={2} container direction='column' justifyContent="space-around">
                        {!isSaved && (
                            <Message type='warning'>
                                {messages.get(messages.UNSAVED_CHANGES)}
                            </Message>
                        )}
                        <Button name="btn-save-preview"
                                label={messages.get(messages.SAVE)}
                                variant='outlined'
                                type='final'
                                startIcon={<SaveIcon/>}
                                disabled={isSaved}
                                onClick={this.saveDataset}/>

                        <AlertDialog label={messages.get(messages.REMOVE)} icon={<DeleteIcon/>}
                                     title={messages.get(messages.CONFIRMATION_REMOVE, 'current preview')}
                                     content={messages.get(messages.CONTENT_REMOVE_PREVIEW)}
                                     disabled={nPreviews === 1}
                                     onHandleYes={this.deletePreview}/>

                        <Button name='btn-new-preview'
                                label={messages.get(messages.NEW)}
                                type='final'
                                variant='outlined'
                                color='default'
                                startIcon={<AddToQueueIcon/>}
                                onClick={this.createNewPreview}/>

                        <InputFileUpload onInputFile={this.handleUpload}/>
                    </Grid>
                </Grid>
            </PaperBox>
        )
    };

    renderBigPreview(classes, activePreview, resolution) {
        return (
            <Grid container item xs={12} sm={8}
                  justifyContent="center"
                  alignItems="center">
                <Box className={classes.imgContainer}>
                    {activePreview.bytes === null ?
                        <Typography variant='body2'>
                            {messages.get(messages.NO_PREVIEW)}
                        </Typography>
                        : <img src={`data:image/${activePreview.format};base64,${activePreview.bytes}`}
                               alt={""}
                               height={resolution[0]}
                               width={resolution[1]}
                        />}
                </Box>
            </Grid>
        );
    };

    renderInputControls(classes, activePreview, configInputs, configResolutions, resolution, isChanged) {
        const inputValues = this.createInitValues(configInputs, activePreview.config);
        const currentMetadata = activePreview.metadata;
        const isUploadedPreview = isObjectEmpty(currentMetadata) ?  false : ("file" in currentMetadata);
        return (
            <Grid item xs={12} sm={4}>
                <PaperBox className={classes.noBorderNoShadow}>
                    <Grid item xs>
                        <Grid container justifyContent="space-between" alignItems="center">
                            <Button label={messages.get(messages.UPDATE)}
                                    variant='outlined'
                                    color='primary'
                                    startIcon={<RefreshIcon/>}
                                    onClick={this.handleUpdate}
                                    disabled={!isChanged || isUploadedPreview}/>

                            {isChanged && !isUploadedPreview && (
                                <Message type='info'>
                                    {messages.get(messages.UPDATE_CHANGES)}
                                </Message>
                            )}

                            <OutlinedBox style={{width: 'fit-content'}} label={messages.get(messages.SHOW)}>
                                <CustomSwitch isChecked={activePreview.show}
                                              onChange={this.handleShowPreview} />
                            </OutlinedBox>

                            <Dropdown onSelectChange={this.handleResolutionChange}
                                  label={messages.get(messages.RESOLUTIONS)}
                                  values={configResolutions}
                                  initValue={resolution.join('x')}/>
                        </Grid>

                        {configInputs.map((c, idx) => {
                            switch (c.type) {
                                case constants.DROPDOWN:
                                    return <Dropdown key={`InputsPanel-${c.type}-${idx}`}
                                                     label={c.label}
                                                     initValue={inputValues[c.label]}
                                                     values={c.values}
                                                     isMulti={c.multiselect}
                                                     disabled={isUploadedPreview}
                                                     onSelectChange={(event) => this.handleActiveConfigChange(event.target.name, event.target.value)}/>;
                                case constants.SLIDER:
                                    return <InputSlider key={`InputsPanel-${c.type}-${idx}`}
                                                        label={c.label}
                                                        initValue={inputValues[c.label]}
                                                        range={c.range}
                                                        unit={c.unit}
                                                        playable={c.playable && !isUploadedPreview}
                                                        speeds={c.speeds}
                                                        disabled={isUploadedPreview}
                                                        onChange={(name, value, update) => this.handleActiveConfigChange(name, value, update)}/>;
                                case constants.RANGE:
                                    return <InputRangeSlider key={`InputsPanel-${c.type}-${idx}`}
                                                             label={c.label}
                                                             initValue={inputValues[c.label]}
                                                             range={c.range}
                                                             disabled={isUploadedPreview || c.range.findIndex(n => n === 'nan') !== -1}
                                                             unit={c.unit}
                                                             playable={c.playable && !isUploadedPreview}
                                                             speeds={c.speeds}
                                                             onChange={(name, value, update) => this.handleActiveConfigChange(name, value, update)}/>;
                                case constants.COLORMAP:
                                    return <ColorMap key={`InputsPanel-${c.type}-${idx}`}
                                                     values={c.values}
                                                     disabled={isUploadedPreview}
                                                     initValue={inputValues[c.label]}
                                                     label={c.label}
                                                     onSelectChange={(event) => this.handleActiveConfigChange(event.target.name, event.target.value)}/>;
                            }
                        })
                        }
                    </Grid>
                </PaperBox>
            </Grid>
        );
    };

    renderMetadataSection(classes, activePreview, activeImage, configMetadata) {
        const currPreviewMetadata = activePreview.metadata;
        /*if (!isObjectEmpty(currPreviewMetadata))
            return JSON.stringify(currPreviewMetadata)*/
        if (isObjectEmpty(configMetadata) && isObjectEmpty(currPreviewMetadata))
            return (
                <PaperBox>
                    <Typography gutterBottom variant='h6'>
                        No Metadata to display
                    </Typography>
                </PaperBox>
            );
        return (
            <PaperBox>
                <Typography gutterBottom variant='h6'>
                    Preview Metadata Section
                </Typography>
                <Typography key={`preview-metadata-${activePreview.index}`} variant="body2"
                            color="textSecondary" component={'span'}>
                    {isObjectEmpty(currPreviewMetadata) ?
                        <p>No preview metadata to display</p>
                        : Object.entries(currPreviewMetadata).map(([key, value], pos) =>
                            <DefaultMetadaField key={'preview-property-' + pos} keyProp={key} valueProp={value} idx={activeImage.index} pos={pos}/>)
                    }
                </Typography>
                <Divider/>
                <Typography gutterBottom variant='h6'>
                    Image Metadata Section
                </Typography>
                <Typography key={`image-metadata-${activeImage.index}`} variant="body2"
                            color="textSecondary" component={'span'}>
                    {isObjectEmpty(activeImage.metadata) ?
                        <p>No image metadata to display</p>
                        : Object.entries(activeImage.metadata).map(([key, value], pos) =>
                            <DefaultMetadaField key={'image-property-' + pos} keyProp={key} valueProp={value} idx={activePreview.index} pos={pos}/>)
                    }
                </Typography>
                <Divider/>
                    <Typography gutterBottom variant='h6'>
                        Config Metadata section
                    </Typography>
                <Typography key={`config-metadata`} variant="body2"
                            color="textSecondary" component={'span'}>
                    {isObjectEmpty(configMetadata) ?
                        <p>No config metadata to display</p>
                        : Object.entries(configMetadata).map(([key, value], pos) =>
                            <DefaultMetadaField key={'config-property-' + pos} keyProp={key} valueProp={value} idx={activePreview.index} pos={pos}/>)
                    }
                </Typography>
            </PaperBox>
        )
    };
}

export default withStyles(styles)(ImagingDataSetViewer);
