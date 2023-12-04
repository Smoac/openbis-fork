import React from 'react'
import {withStyles} from "@material-ui/core/styles";
import {
    Box,
    Button,
    Grid,
    ImageList, ImageListItem, Switch,
} from "@material-ui/core";

import Typography from "@material-ui/core/Typography";
import PaperBox from "@src/js/components/database/premise/common/PaperBox";
import openbis from "@src/js/services/openbis";
import BlankImage from "@src/js/components/database/premise/common/BlankImage";
import InputFileUpload from "@src/js/components/database/premise/components/InputFileUpload";
import AddToQueueIcon from "@material-ui/icons/AddToQueue";
import AlertDialog from "@src/js/components/database/premise/common/AlertDialog";
import SaveIcon from "@material-ui/icons/Save";
import DeleteIcon from "@material-ui/icons/Delete";
import Export from "@src/js/components/database/premise/components/Exporter";
import ImageListItemBarAction
    from "@src/js/components/database/premise/common/ImageListItemBarAction";
import {convertToBase64} from "@src/js/components/database/premise/utils";
import RefreshIcon from "@material-ui/icons/Refresh";
import Dropdown from "@src/js/components/database/premise/common/Dropdown";
import OutlinedBox from "@src/js/components/database/premise/common/OutlinedBox";
import InputSlider from "@src/js/components/database/premise/common/InputSlider";
import InputRangeSlider from "@src/js/components/database/premise/common/InputRangeSlider";
import ColorMap from "@src/js/components/database/premise/components/ColorMap";
import MetadataViewer from "@src/js/components/database/premise/components/MetadataViewr";
import LoadingDialog from "@src/js/components/common/loading/LoadingDialog.jsx";

const styles = theme => ({
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
    imgFullWidth: {
        width: '100%',
        height: 'unset'
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
        [theme.breakpoints.down('sm')]: {
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
});

class ImagingDataSetViewer extends React.PureComponent {
    constructor(props) {
        super(props)
        this.state = {
            open: true,
            loaded: false,
            imagingDataset: {},
            activeImageIdx: 0,
            activePreviewIdx: 0,
            resolution: [200, 200]
        }
    }

    async componentDidMount() {
        if (!this.state.loaded) {
            try {
                const {objId} = this.props
                const fetchOptions = new openbis.DataSetFetchOptions()
                fetchOptions.withExperiment()
                fetchOptions.withSample()
                fetchOptions.withParents()
                fetchOptions.withProperties()
                const dataSets = await openbis.getDataSets(
                    [new openbis.DataSetPermId(objId)],
                    fetchOptions
                )
                //console.log("fetchData - dataSets: ", dataSets);
                let dataset = JSON.parse(dataSets[objId].properties['$IMAGING_DATA_CONFIG'])
                this.setState({open: false, loaded: true, imagingDataset: dataset})
            } catch (error) {
                console.log(error)
            }
        }
    }

    handleActiveImageChange(selectedImageIdx) {
        this.setState({activeImageIdx: selectedImageIdx, activePreviewIdx: 0});
    };

    handleActivePreviewChange(selectedPreviewIdx) {
        this.setState({activePreviewIdx: selectedPreviewIdx});
    };

    handleResolutionChange = (event) => {
        //setOpen(true);
        const v_list = event.target.value.split('x');
        this.setState({resolution: v_list});
        //setTimeout(handleClose, 1000);
    };

    handleActiveConfigChange = (name, value, update) => {
        const {imagingDataset, activeImageIdx, activePreviewIdx,} = this.state;
        //console.log('handleActiveConfigChange = ', imagingDataset.images[activeImageIdx].previews[activePreviewIdx].config);
        //console.log('handleActiveConfigChange = ', name, value, update);
        let toUpdateIDS = {...imagingDataset};
        toUpdateIDS.images[activeImageIdx].previews[activePreviewIdx].config[name] = value;
        this.setState({imagingDataset: toUpdateIDS});
        if (update) console.log('onUpdate');
    }

    handleShowPreview = () => {
        const {imagingDataset, activeImageIdx, activePreviewIdx} = this.state;
        let toUpdateIDS = {...imagingDataset};
        toUpdateIDS.images[activeImageIdx].previews[activePreviewIdx].show = !toUpdateIDS.images[activeImageIdx].previews[activePreviewIdx].show;
        this.setState({imagingDataset: toUpdateIDS});
    }

    onMove = (position) => {
        const {imagingDataset, activeImageIdx, activePreviewIdx} = this.state;
        console.log(activeImageIdx, activePreviewIdx, position);
        //setOpen(true);
        let toUpdateImgDs = { ...imagingDataset };
        let previewsList = toUpdateImgDs.images[activeImageIdx].previews;
        let tempMovedPreview = previewsList[activePreviewIdx];
        tempMovedPreview.index += position;
        previewsList[activePreviewIdx] = previewsList[activePreviewIdx+position];
        previewsList[activePreviewIdx].index -= position;
        previewsList[activePreviewIdx+position] = tempMovedPreview;
        toUpdateImgDs.images[activeImageIdx].previews = previewsList;
        this.setState({imagingDataset: toUpdateImgDs});
        //setTimeout(handleClose, 1000);
        //setTimeout(() => setSnackbar({show:true, message:"Preview Moved", severity: "success"}), 1000);
    }

    createNewPreview = () => {
        const {imagingDataset, activeImageIdx} = this.state;
        //setOpen(true);
        let toUpdateImgDs = {...imagingDataset};
        let newLastIdx = toUpdateImgDs.images[activeImageIdx].previews.length;
        let previewTemplate = {
            "@type": "dss.dto.imaging.ImagingDataSetPreview",
            "index": newLastIdx,
            "config": {
                "X-axis": ["0", "3.0"],
                "Y-axis": ["0", "3.0"],
                "Channel": "z",
                "Scaling": "logarithmic",
                "Colormap": "gray",
                "Color-scale": ["-700.0", "700.0"]
            },
            "format": 'image/png',
            "bytes": null,
            "show": false,
            "height": 256,
            "width": 256,
            "metadata": {}
        }
        //console.log('createNewPreview Data => ', previewTemplate);
        toUpdateImgDs.images[activeImageIdx].previews = [...toUpdateImgDs.images[activeImageIdx].previews, previewTemplate];
        this.setState({imagingDataset: toUpdateImgDs})
        //setTimeout(handleClose, 1000);
        //setTimeout(() => setSnackbar({show:true, message:"Created new empty preview", severity: "success"}), 1000);
    };

    handleUpload = async (file) => {
        const base64 = await convertToBase64(file);
        const {imagingDataset, activeImageIdx} = this.state;
        //setOpen(true);
        try {
            let toUpdateImgDs = {...imagingDataset};
            let newLastIdx = toUpdateImgDs.images[activeImageIdx].previews.length;
            let previewTemplate = {
                "@type": "dss.dto.imaging.ImagingDataSetPreview",
                "index": newLastIdx,
                "config": {
                    "X-axis": ["0", "3.0"],
                    "Y-axis": ["0", "3.0"],
                    "Channel": "z",
                    "Scaling": "logarithmic",
                    "Colormap": "gray",
                    "Color-scale": ["-700.0", "700.0"]
                },
                "format": file.type,
                "bytes": base64.split(',')[1],
                "show": false,
                "height": 256,
                "width": 256,
                "metadata": {}
            }
            toUpdateImgDs.images[activeImageIdx].previews = [...toUpdateImgDs.images[activeImageIdx].previews, previewTemplate];
            //console.log('Data => ', toUpdateImgDs);
            this.setState({imagingDataset: toUpdateImgDs})
            //setTimeout(handleClose, 1000);
            //setTimeout(() => setSnackbar({show:true, message:"Image uploaded", severity: "success"}), 1000);
        } catch (err) {
            //setTimeout(handleClose, 1000);
            //setTimeout(() => setSnackbar({show:true, message:"Image not uploaded", severity: "error"}), 1000);
            console.log('Err => ', err);
        }

    };

    deletePreview = () => {
        const {imagingDataset, activeImageIdx, activePreviewIdx} = this.state;
        //console.log("DELETE PREVIEW ", activeImageIdx, activePreviewIdx);
        //onDelete(activeImage.imageIdx, activePreview.previewIdx);
        //setActivePreviewIdx(0);
        let toUpdateImgDs = {...imagingDataset};
        toUpdateImgDs.images[activeImageIdx].previews.splice(activePreviewIdx,1);
        toUpdateImgDs.images[activeImageIdx].previews = toUpdateImgDs.images[activeImageIdx].previews.map(p => {
            if (p.index > activePreviewIdx) p.index -= 1;
            return p
        });
        this.setState({imagingDataset: toUpdateImgDs, activePreviewIdx: 0})
    };

    handleClose = () => {
        this.setState({open: false})
    };

    handleUpdate = async () => {
        this.setState({open: true});
        setTimeout(this.handleClose, 1000);
        /*let toUpdateImgDs = {...imagingDataSet};
        toUpdateImgDs.images[imageIdx].previews[previewIdx].config = activeConfig;
        resolve(toUpdateImgDs);*/
    };

    render() {
        if (!this.state.loaded) return null;
        console.log('ImagingDataSetViewer.render: ', this.state);
        return (
            <React.Fragment>
                <LoadingDialog loading={this.state.open} />
                {this.renderImageSection()}
                {this.renderPreviewsSection()}
                {this.renderMainSection()}
                {this.renderMetadataSection()}
            </React.Fragment>
        )
    };

    renderImageSection() {
        const {classes} = this.props;
        const {imagingDataset, activeImageIdx} = this.state;
        return (
            <PaperBox>
                <Typography variant='h6'>
                    Images
                </Typography>
                <ImageList cols={3}
                           rowHeight={150}>
                    {imagingDataset.images.map(image => (
                        <ImageListItem className={activeImageIdx === image.index ? classes.elevation : classes.trasparency}
                            onClick={() => this.handleActiveImageChange(image.index)}
                            key={`imagelistitem-image-${image.index}`}>
                                {image.previews[0].bytes ? <img alt={""}
                                    className={classes.imgFullWidth}
                                    src={`data:image/${image.previews[0].format};base64,${image.previews[0].bytes}`}
                                /> : <BlankImage className={classes.imgFullWidth}/>}
                        </ImageListItem>
                    ))}
                </ImageList>
            </PaperBox>
        )
    };

    moveArrowCompList(currentIdx) {
        const {imagingDataset, activeImageIdx} = this.state;
        let previewsLength = imagingDataset.images[activeImageIdx].previews.length;
        if (currentIdx === 0 && previewsLength === 1) { // only 1 element
            return [];
        } else if (currentIdx === 0) { // first element
            return [<ImageListItemBarAction key={"ImageListItemBarAction-left-" + currentIdx}
                                            classNames={'singleActionBar'} position={'right'}
                                            onMove={() => this.onMove(1)}/>];
        } else if (currentIdx === previewsLength - 1) { // last element
            return [<ImageListItemBarAction key={"ImageListItemBarAction-right-" + currentIdx}
                                            classNames={'singleActionBar'} position={'left'}
                                            onMove={() => this.onMove(-1)}/>];
        } else {
            //console.log('ELEMENT ', currentIdx, (activeImage.previews.length) - 1);
            return [<ImageListItemBarAction key={"ImageListItemBarAction-left-" + currentIdx}
                                            classNames={'actionBarL'} position={'left'}
                                            onMove={() => this.onMove(-1)}/>,
                <ImageListItemBarAction key={"ImageListItemBarAction-right-" + currentIdx}
                                        classNames={'actionBarR'} position={'right'}
                                        onMove={() => this.onMove(1)}/>];
        }
    };

    renderPreviewsSection() {
        const {classes} = this.props;
        const {imagingDataset, activeImageIdx, activePreviewIdx} = this.state;
        return (
            <PaperBox>
                <Grid container direction='row'>
                    <Grid item xs={10}>
                        <Typography variant='h6'>
                            Previews
                        </Typography>
                        <ImageList className={classes.imageList}
                                   cols={4}
                                   rowHeight={200}>
                            {imagingDataset.images[activeImageIdx].previews.map(preview => (
                                <ImageListItem key={`imagelistitem-preview-${activeImageIdx}-${preview.index}`}
                                               className={activePreviewIdx === preview.index ? classes.elevation : classes.trasparency}
                                               onClick={() => this.handleActivePreviewChange(preview.index)}>
                                    {preview.bytes ? <img alt={""}
                                                          className={classes.imgFullWidth}
                                                          src={`data:image/${preview.format};base64,${preview.bytes}`}/>
                                        : <BlankImage className={classes.imgFullWidth}/>}
                                    {activePreviewIdx === preview.index ? this.moveArrowCompList(activePreviewIdx) : ''}
                                </ImageListItem>
                            ))}
                        </ImageList>
                    </Grid>
                    {this.renderActionButtonSection()}
                </Grid>
            </PaperBox>
        )
    };

    renderActionButtonSection() {
        const { imagingDataset } = this.state;
        return (
            <Grid item xs={2} container direction='column' justifyContent="space-around">
                <InputFileUpload onInputFile={this.handleUpload}/>

                <Button variant="outlined" startIcon={<AddToQueueIcon/>}
                        onClick={this.createNewPreview}>New</Button>

                <AlertDialog label={'Save'} icon={<SaveIcon/>}
                             title={"Are you sure to save the current configuration?"}
                             text={"The current configuration will be stored within the current preview."}
                             onHandleYes={() => console.log('SAVE FILE')}/>
                <AlertDialog label={'Delete'} icon={<DeleteIcon/>}
                             title={"Are you sure to delete the current preview?"}
                             text={"The preview will be definitly delete from the dataset."}
                             onHandleYes={this.deletePreview}/>

                {imagingDataset.config.exports.length > 0 ?
                    <Export handleExport={() => console.log('onExport')}
                            config={imagingDataset.config.exports}/> : <></>}
            </Grid>
        )
    };

    renderMainSection() {
        const {classes} = this.props;
        return (
            <PaperBox>
                <Grid container className={classes.gridDirection}>
                    {this.renderBigPreview()}
                    {this.renderInputControls()}
                </Grid>
            </PaperBox>
        );
    };

    renderBigPreview() {
        const {classes} = this.props;
        const {imagingDataset, activeImageIdx, activePreviewIdx, resolution} = this.state;
        return (
            <Grid container item xs={8} sm={8}
                  justifyContent="center"
                  alignItems="center">
                <Box className={classes.imgContainer}>
                    {imagingDataset.images[activeImageIdx].previews[activePreviewIdx].bytes === null ?
                        <Typography variant='body2'>
                            No preview available
                        </Typography>
                        : <img src={`data:image/${imagingDataset.images[activeImageIdx].previews[activePreviewIdx].format};base64,${imagingDataset.images[activeImageIdx].previews[activePreviewIdx].bytes}`}
                               alt={""}
                               height={resolution[0]}
                               width={resolution[1]}
                        />}
                </Box>
            </Grid>
        );
    };

    renderInputControls() {
        const {classes} = this.props;
        const {imagingDataset, activeImageIdx, activePreviewIdx, resolution} = this.state;
        //const current
        const inputValues = Object.fromEntries(imagingDataset.config.inputs.map(input => {
            const activeConfig = imagingDataset.images[activeImageIdx].previews[activePreviewIdx].config;
            switch (input.type) {
                case 'Dropdown':
                    return [input.label, activeConfig[input.label] ? activeConfig[input.label] : input.values[0]];
                case 'Slider':
                    return [input.label, activeConfig[input.label] ? activeConfig[input.label] : [0]];
                case 'Range':
                    return [input.label, activeConfig[input.label] ? activeConfig[input.label] : [0, 0]];
                case 'Colormap':
                    return [input.label, activeConfig[input.label] ? activeConfig[input.label] : input.values[0]];
            }
        }));
        return (
            <Grid item xs={4} sm={4}>
                <PaperBox className={classes.noBorderNoShadow}>
                    <Grid item xs>
                        <Grid container item xs justifyContent="space-around"
                              alignItems="center">
                            <Button variant="outlined" startIcon={<RefreshIcon/>}
                                    onClick={this.handleUpdate}>Update</Button>

                            <Dropdown onSelectChange={this.handleResolutionChange}
                                      label="Resolutions"
                                      values={imagingDataset.config.resolutions}
                                      initValue={[resolution.join('x')]}/>
                        </Grid>

                        <OutlinedBox style={{width: 'fit-content'}} label="Show">
                            <Switch
                                checked={imagingDataset.images[activeImageIdx].previews[activePreviewIdx].show}
                                onChange={this.handleShowPreview} color="primary"/>
                        </OutlinedBox>

                        {imagingDataset.config.inputs.map((c, idx) => {
                            //const prevConfigValues = imagingDataSet.images[activeImageIdx].previews[activePreviewIdx].config;
                            //console.log(panelConfig, initConfig);
                            switch (c.type) {
                                case 'Dropdown':
                                    return <Dropdown key={`InputsPanel-${c.type}-${idx}`}
                                                     label={c.label}
                                                     initValue={inputValues[c.label]}
                                                     values={c.values}
                                                     isMulti={c.multiselect}
                                                     onSelectChange={(event) => this.handleActiveConfigChange(event.target.name, event.target.value)}/>;
                                case 'Slider':
                                    if (c.visibility) {
                                        for (const condition of c.visibility) {
                                            if (condition.values.includes(inputValues[condition.label])) {
                                                c.range = condition.range;
                                                c.unit = condition.unit;
                                            }
                                        }
                                    }
                                    return <InputSlider key={`InputsPanel-${c.type}-${idx}`}
                                                        label={c.label}
                                                        initValue={inputValues[c.label]}
                                                        range={c.range}
                                                        unit={c.unit}
                                                        playable={c.playable}
                                                        speeds={c.speeds}
                                                        onChange={(name, value, update) => this.handleActiveConfigChange(name, value, update)}/>;
                                case 'Range':
                                    if (c.visibility) {
                                        for (const condition of c.visibility) {
                                            if (condition.values.includes(inputValues[condition.label])) {
                                                c.range = condition.range;
                                                c.unit = condition.unit;
                                            }
                                        }
                                    }
                                    return <InputRangeSlider key={`InputsPanel-${c.type}-${idx}`}
                                                             label={c.label}
                                                             initValue={inputValues[c.label]}
                                                             range={c.range}
                                                             unit={c.unit}
                                                             playable={c.playable}
                                                             speeds={c.speeds}
                                                             onChange={(name, value, update) => this.handleActiveConfigChange(name, value, update)}/>;
                                case 'Colormap':
                                    return <ColorMap key={`InputsPanel-${c.type}-${idx}`}
                                                     values={c.values}
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

    renderMetadataSection() {
        const {imagingDataset, activeImageIdx} = this.state;
        if (imagingDataset.config.metadata === null || Object.keys(imagingDataset.config.metadata).length === 0) return null;
        return (
            <PaperBox>
                <MetadataViewer configMetadata={imagingDataset.config.metadata}
                                previews={imagingDataset.images[activeImageIdx].previews}/>
            </PaperBox>
        )
    };
}

export default withStyles(styles)(ImagingDataSetViewer);
