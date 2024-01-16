import React from 'react';
import Grid from "@material-ui/core/Grid";
import OutlinedBox from "@src/js/components/database/imaging/components/common/OutlinedBox";
import {
    CardActionArea, CardActions, CardContent, CardMedia,
    ImageList,
    ImageListItem, TextareaAutosize
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
import GridOnIcon from '@material-ui/icons/GridOn';
import GalleryPaging from "@src/js/components/database/imaging/components/gallery/GalleryPaging.jsx";
import GridPagingOptions from "@src/js/components/common/grid/GridPagingOptions";
import IconButton from "@material-ui/core/IconButton";
import Export from "@src/js/components/database/imaging/components/viewer/Exporter";
import Checkbox from "@material-ui/core/Checkbox";
import FormControlLabel from "@material-ui/core/FormControlLabel";

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'space-around',
        overflow: 'hidden',
        backgroundColor: theme.palette.background.paper,
    },
    card:{
        margin: '5px',
    },
    details: {
        display: 'flex',
        flexDirection: 'row',
    },
    content: {
        flex: '1 0 auto',
        alignSelf: 'center',
    },
    imageList: {
        width: '100%',
        /*height: 800,*/
    },
    imageListView: {
        height: '350px',
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
    /*sticky: {
        position: 'sticky',
        top: '0px',
        zIndex: 1
    },*/
}));

const ImagingGalleryViewer = ({objId, extOpenbis, onOpenPreview}) => {
    const classes = useStyles();
    const [gridView, setGridView] = React.useState(true);
    const [isLoaded, setIsLoaded] = React.useState(false);
    const [open, setOpen] = React.useState(false);
    const [error, setError] = React.useState({open: false, error: null});
    const [previewsInfo, setPreviewsInfo] = React.useState({previewContainerList: [], totalCount: 0});
    const [paging, setPaging] = React.useState({page: 0, pageSize:8, pageColumns:4});
    const [showAll, setShowAll] = React.useState(true);
    const [selectAll, setSelectAll] = React.useState(true);

    React.useEffect( ()=> {
        async function load() {
            const imagingFacade = new ImagingFacade(extOpenbis);
            let {previewContainerList, totalCount} = await imagingFacade.loadPaginatedGalleryDatasets(objId, paging.page, paging.pageSize);
            setPreviewsInfo({previewContainerList, totalCount});
            setIsLoaded(true);
        }
        load();
    }, [paging])

    const handleErrorCancel = () => {
        setError( {open: false, error: null});
    }

    const handleError = (error) => {
        setError({open: true, error: error});
    }

    const handleOpen = () => {
        setOpen(true);
    }

    const handleViewChange = (isGridView) => {
        if (gridView !== isGridView) setGridView(isGridView);
    }

    const handleSelectAll = (val) => {
        //console.log('handleSelectAll: ', val, previewContainerList.map(previewContainer => previewContainer.select === false));
        if (!val) {
            let updatedContainerList = [...previewsInfo.previewContainerList];
            updatedContainerList = updatedContainerList.map(previewContainer => {
                return {...previewContainer, select: false}
            });
            setPreviewsInfo({...previewsInfo, previewContainerList: updatedContainerList});
        }
        setSelectAll(val);
    }

    const handleShowPreview = async (previewContainer) => {
        handleOpen();
        let selectedPreview = previewContainer.preview;
        selectedPreview.show = !selectedPreview.show;
        try {
            const isSaved = await new ImagingFacade(extOpenbis).updateShowInGalleryView(previewContainer.datasetId, previewContainer.imageIdx, selectedPreview);
            if (isSaved === null) {
                setOpen(false);
            }
        } catch (error) {
            setOpen(false);
            handleError(error);
        }
    }

    const handleSelectPreview = (idx) => {
        let updatedList = [...previewsInfo.previewContainerList];
        updatedList[idx].select = !updatedList[idx].select;
        setPreviewsInfo({...previewsInfo, previewContainerList: updatedList});
    }

    const handleExport = async (currentConfigExport) => {
        setOpen(true);
        const exportList = previewsInfo.previewContainerList.filter(previewObj => previewObj.select);
        try {
            const downloadableURL = await new ImagingFacade(extOpenbis)
                .multiExportImagingDataset(currentConfigExport, exportList);
            if (downloadableURL)
                window.open(downloadableURL, '_blank');
            setOpen(false);
        } catch (error) {
            setOpen(false);
            handleError(error);
        }
    }

    const renderControlsBar = (isExportDisable, configExports = []) => {
        const options = GridPagingOptions.GALLERY_PAGE_SIZE_OPTIONS[paging.pageColumns - 1].map(pageSize => ({
            label: pageSize,
            value: pageSize
        }))
        return (
            <PaperBox className={classes.sticky}>
                <Typography variant='h6'>
                    Gallery View
                </Typography>
                <Grid container alignItems="center" direction="row" spacing={2}>
                    <Grid item xs={8}>
                        <OutlinedBox label='Paging'>
                            <GalleryPaging id='gallery-paging'
                                           count={previewsInfo.totalCount}
                                           page={paging.page}
                                           pageSize={paging.pageSize}
                                           pageColumns={paging.pageColumns}
                                           options={options}
                                           isGridView={gridView}
                                           onColumnChange={(value) => setPaging({page:0, pageSize: value, pageColumns: value})}
                                           onPageChange={(value) => setPaging({...paging, page: value})}
                                           onPageSizeChange={(value) => setPaging({...paging, page:0, pageSize: value})}
                            />
                        </OutlinedBox>
                    </Grid>
                    <Grid item xs>
                        <OutlinedBox style={{width: 'fit-content'}} label={messages.get(messages.SHOW)}>
                            <CustomSwitch isChecked={showAll} onChange={setShowAll}/>
                        </OutlinedBox>
                        <OutlinedBox style={{width: 'fit-content'}} label='Select'>
                            <CustomSwitch isChecked={selectAll} onChange={handleSelectAll}/>
                        </OutlinedBox>
                    </Grid>
                    <Grid item xs>
                        <OutlinedBox style={{width: 'fit-content'}} label='View Mode'>
                            <IconButton color={gridView ? 'primary' : 'default'}
                                        onClick={() => handleViewChange(true)}>
                                <GridOnIcon fontSize="large"/>
                            </IconButton>
                            <IconButton color={!gridView ? 'primary' : 'default'}
                                        onClick={() => handleViewChange(false)}>
                                <ViewListIcon fontSize="large"/>
                            </IconButton>
                        </OutlinedBox>
                    </Grid>
                    <Grid item xs>
                        {configExports.length > 0 &&
                            <Export config={configExports} handleExport={handleExport} disabled={isExportDisable}/>}
                    </Grid>
                </Grid>
            </PaperBox>
        );
    }

    const renderGallery = (previewContainerList) => {
        return (
            <div className={classes.root}>
                <ImageList className={classes.imageList} cols={paging.pageColumns} gap={5}>
                    {previewContainerList.map((previewContainer, idx) => (
                        <ImageListItem style={{height: 'unset'}} key={`ImageListItem-${idx}`} >
                            <Card className={classes.card}>
                                <CardActionArea>
                                    <CardMedia component="img"
                                               alt={""}
                                               className={classes.imgFullWidth}
                                               src={previewContainer.preview.bytes ? `data:image/${previewContainer.preview.format};base64,${previewContainer.preview.bytes}` : constants.BLANK_IMG_SRC}
                                               onClick={() => onOpenPreview(previewContainer.datasetId)}
                                    />
                                </CardActionArea>
                                {selectAll && <CardActions style={{ justifyContent: 'space-evenly'}}>
                                    <CustomSwitch size="small"
                                                  label="Show"
                                                  labelPlacement="start"
                                                  isChecked={previewContainer.preview.show}
                                                  onChange={() => handleShowPreview(previewContainer)}/>
                                    <FormControlLabel
                                        value="start"
                                        control={<Checkbox value={previewContainer.select} onChange={() => handleSelectPreview(idx)} color="primary" />}
                                        label="Export"
                                        labelPlacement="start"
                                    />
                                </CardActions>}
                            </Card>
                        </ImageListItem>
                    ))}
                </ImageList>
            </div>
        );
    }

    const renderListView = (previewContainerList) => {
        return (
            <div className={classes.root}>
                {previewContainerList.map((previewContainer, idx) => (
                    <Card className={classes.card} key={'card-'+idx}>
                        <div className={classes.details}>
                            <CardActionArea>
                                <CardMedia component="img"
                                           alt={""}
                                           className={classes.imageListView}
                                           src={previewContainer.preview.bytes ? `data:image/${previewContainer.preview.format};base64,${previewContainer.preview.bytes}` : constants.BLANK_IMG_SRC}
                                           onClick={() => onOpenPreview(previewContainer.datasetId)}
                                />
                            </CardActionArea>
                            <CardContent className={classes.content}>
                                <Typography key={`card-content-metadata-h2-${idx}`} gutterBottom
                                            variant="h5">
                                    Metadata - {previewContainer.datasetId}
                                </Typography>
                                <Typography key={`card-content-dataset-metadata-p-${idx}`}
                                            variant="body2" color="textSecondary" component="p">
                                    {JSON.stringify(previewContainer.datasetMetadata)}
                                </Typography>
                                <Typography key={`card-content-metadata-p-${idx}`} variant="body2"
                                            color="textSecondary" component="p">
                                    {JSON.stringify(previewContainer.preview.metadata)}
                                </Typography>
                                <Typography key={`card-content-comments-${idx}`} gutterBottom variant="h6">
                                    Comments:
                                </Typography>
                                <TextareaAutosize aria-label="empty textarea" placeholder="TODO: missing comment field in data model" />
                            </CardContent>
                        </div>
                    </Card>
                ))}
            </div>
        );
    }

    const extractCommonExportsConfig = () => {
        const commonConfig = [];
        previewsInfo.previewContainerList.flatMap(previewContainer => previewContainer.exportConfig)
            .map(exportConfig => {
                let evalIdx = commonConfig.findIndex(x => x.label === exportConfig.label);
                if(evalIdx === -1){
                    commonConfig.push(exportConfig);
                } else {
                    commonConfig[evalIdx].values = commonConfig[evalIdx].values.filter(value => exportConfig.values.includes(value));
                }
            });
        //console.log('exports - loadedExportConfig: ', commonConfig);
        return commonConfig
    }

    if (!isLoaded) return null;
    if (previewsInfo.previewContainerList.length === 0) return <Grid item xs={12}> No Datasets to display </Grid>
    console.log("RENDER.ImagingGalleryViewer - previewsInfo: ", previewsInfo);
    const previewContainerList = showAll ? previewsInfo.previewContainerList : previewsInfo.previewContainerList.filter(previewContainer => previewContainer.preview.show);
    const isExportDisable = !(previewContainerList.filter(previewContainer => previewContainer.select === true).length > 0)
    const commonExportConfig = extractCommonExportsConfig();
    //console.log('loadedExportConfig: ', commonExportConfig);
    return (
        <>
            <LoadingDialog loading={open}/>
            <ErrorDialog open={error.state} error={error.error} onClose={handleErrorCancel}/>
            {renderControlsBar(isExportDisable, commonExportConfig)}
            {gridView ? renderGallery(previewContainerList) :  renderListView(previewContainerList)}
        </>
    );
}

export default ImagingGalleryViewer;