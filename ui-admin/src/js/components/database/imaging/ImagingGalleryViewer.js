import React from 'react';
import Grid from "@material-ui/core/Grid";
import OutlinedBox from "@src/js/components/database/imaging/components/common/OutlinedBox";
import {
    CardActionArea, CardActions, CardMedia,
    ImageList,
    ImageListItem
} from "@material-ui/core";
import ImagingFacade from "@src/js/components/database/imaging/ImagingFacade";
import LoadingDialog from "@src/js/components/common/loading/LoadingDialog.jsx";
import ErrorDialog from "@src/js/components/common/error/ErrorDialog.jsx";
import Typography from "@material-ui/core/Typography";
import messages from "@src/js/common/messages";
import PaperBox from "@src/js/components/database/imaging/components/common/PaperBox";
import {makeStyles} from "@material-ui/core/styles";
import CustomSwitch from "@src/js/components/database/imaging/components/common/CustomSwitch.js";
import Card from "@material-ui/core/Card";
import constants from "@src/js/components/database/imaging/constants.js"
import ViewListIcon from '@material-ui/icons/ViewList';
import Button from "@src/js/components/common/form/Button.jsx";
import GridOnIcon from '@material-ui/icons/GridOn';
import GalleryPaging from "@src/js/components/database/imaging/components/gallery/GalleryPaging.jsx";

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'space-around',
        overflow: 'hidden',
        backgroundColor: theme.palette.background.paper,
    },
    imageList: {
        width: '100%',
        /*height: 800,*/
    },
    paperFullHeight: {
        height: '90%'
    },
    imgFullWidth: {
        width: '100%',
        height: 'unset'
    },
    pageSize: {
        display: 'none'
    },
    sticky: {
        position: 'sticky',
        top: '0px',
        zIndex: 1
    }
}));

const ImagingGalleryViewer = ({objId, extOpenbis, onOpenPreview}) => {
    const classes = useStyles();
    const [isLoaded, setIsLoaded] = React.useState(false);
    const [open, setOpen] = React.useState(false);
    const [error, setError] = React.useState({open: false, error: null});
    const [imagingDatasets, setImagingDatasets] = React.useState([]);
    const [zoom, setZoom] = React.useState(4);
    const [paging, setPaging] = React.useState({page: 0, pageSize:5});
    const [showAll, setShowAll] = React.useState(true);
    const [selectAll, setSelectAll] = React.useState(true);

    React.useEffect( ()=> {
        async function load() {
            const imagingFacade = new ImagingFacade(extOpenbis);
            const imagingDatasets = await imagingFacade.loadGalleryDatasets(objId);

            /*const serializedImagingDatasets = await new ImagingFacade(extOpenbis).multiFromJson(imagingDatasets);
            console.log("serializedImagingDatasets: ", serializedImagingDatasets);*/

            setImagingDatasets(imagingDatasets);
            setIsLoaded(true);
        }
        load();
        console.log("AFTER load() imagingDataSetPropertyConfig: ", imagingDatasets)
    }, [])

    /*React.useEffect(() => {
        async function convert() {
            console.log('convert: ', imagingDatasets);
            const serializedImagingDatasets = await new ImagingFacade(extOpenbis).multiFromJson(imagingDatasets);
            console.log("serializedImagingDatasets: ", serializedImagingDatasets);
            setImagingDatasets(serializedImagingDatasets);
        }
        if (isLoaded) {
            convert();
        }
    }, [isLoaded])*/

    const handleErrorCancel = () => {
        setError( {open: false, error: null});
    }

    const handleError = (error) => {
        setError({open: true, error: error});
    }

    const handleOpen = () => {
        setOpen(true);
    }

    const handleZoomChange = (newValue) => {
        setZoom(newValue);
    };

    const openImagingDataset = (permId) => {
        onOpenPreview(permId);
    }

    const renderControlsBar = (totalItems) => {
        return (
            <PaperBox className={classes.sticky}>
                <Typography variant='h6'>
                    Gallery View
                </Typography>

                <Grid container alignItems="center" direction="row">
                    <Grid item xs>
                        <OutlinedBox label='Paging'>
                            <GalleryPaging id='gallery-paging'
                                           xsOptions={true}
                                           count={totalItems}
                                           page={paging.page}
                                           pageSize={paging.pageSize}
                                           cols={zoom}
                                           onColumnChange={handleZoomChange}
                                           onPageChange={(value) => setPaging({...paging, page: value})}
                                           onPageSizeChange={(value) => setPaging({...paging, pageSize: value})}
                            />
                        </OutlinedBox>
                    </Grid>
                </Grid>
                <Grid container alignItems="center" direction="row">
                    <Grid item xs={3}>
                        <CustomSwitch size="small"
                                      label="Show"
                                      labelPlacement="start"
                                      isChecked={showAll}
                                      onChange={checked => setShowAll(checked)}/>
                    </Grid>
                    <Grid item xs={3}>
                        <CustomSwitch size="small"
                                      label="Select"
                                      labelPlacement="start"
                                      isChecked={selectAll}
                                      onChange={checked => setSelectAll(checked)}/>
                    </Grid>
                </Grid>
                <Button startIcon={<GridOnIcon/>} />
                <Button startIcon={<ViewListIcon/>} />
            </PaperBox>
            );
    }

    const renderGallery = (previews) => {
        return (
            <div className={classes.root}>
                <ImageList className={classes.imageList} cols={zoom}>
                    {previews.map((previewObj, idx) => (
                        <ImageListItem style={{height: 'unset'}} key={`item-${idx}-${previewObj.datasetIdx}-${previewObj.imageIdx}`}>
                            {/*<PaperBox className={classes.paperFullHeight}>
                                <Grid container justifyContent='space-around'>
                                    <Grid container item alignItems="flex-start">
                                        {previewObj.preview.bytes ? <img alt={""}
                                                              className={classes.imgFullWidth}
                                                              src={`data:image/${previewObj.preview.format};base64,${previewObj.preview.bytes}`}
                                        /> : <BlankImage className={classes.imgFullWidth}/>}
                                    </Grid>
                                    <Grid container item alignItems="flex-end">
                                        <CustomSwitch size="small"
                                                      label="Show"
                                                      labelPlacement="start"
                                                      isChecked={previewObj.preview.show} />
                                        <CustomSwitch size="small"
                                                      label="Select"
                                                      labelPlacement="start"
                                                      isChecked={previewObj.preview.show} />
                                    </Grid>
                                </Grid>
                            </PaperBox>*/}
                            <Card>
                                <CardActionArea>
                                    <CardMedia component="img"
                                               alt={""}
                                               className={classes.imgFullWidth}
                                               src={previewObj.preview.bytes ? `data:image/${previewObj.preview.format};base64,${previewObj.preview.bytes}` : constants.BLANK_IMG_SRC}
                                               onClick={() => onOpenPreview(previewObj.permId)}
                                    />
                                </CardActionArea>
                                <CardActions>
                                    <CustomSwitch size="small"
                                                  label="Show"
                                                  labelPlacement="start"
                                                  isChecked={previewObj.preview.show} />
                                    <CustomSwitch size="small"
                                                  label="Select"
                                                  labelPlacement="start"
                                                  isChecked={previewObj.preview.show} />
                                </CardActions>
                            </Card>
                        </ImageListItem>
                    ))}
                </ImageList>
            </div>
        );
    }

    if (!isLoaded) return null;
    if (imagingDatasets.length === 0) return <Grid item xs={12}> No Datasets to display </Grid>
    console.log("RENDER.ImagingGalleryViewer: ", imagingDatasets);
    const previews = imagingDatasets.map((item, datasetIdx) => (
        item.imagingDataset.images.map(image => (
            image.previews.map(preview => {
                if (showAll)
                    if (preview.show)
                        return {"permId": item.permId, "datasetIdx": datasetIdx, "imageIdx": image.index, "preview": preview}
                else
                    return {"permId": item.permId, "datasetIdx": datasetIdx, "imageIdx": image.index, "preview": preview}
            })
        )).flat()
    )).flat();
    return (
        <>
            <LoadingDialog loading={open}/>
            <ErrorDialog open={error.state} error={error.error} onClose={handleErrorCancel}/>
            {renderControlsBar(previews.length)}
            {renderGallery(previews)}
            <Grid item xs={12}>
                Gallery View
                <pre>{imagingDatasets.map(imagingDataset => JSON.stringify(imagingDataset['@type'] || {}, null, 2))}</pre>
            </Grid>
        </>
    );
}

export default ImagingGalleryViewer;