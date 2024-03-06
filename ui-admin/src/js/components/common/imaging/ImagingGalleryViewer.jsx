import React from 'react';
import { Grid,
    Typography,
    IconButton,
} from "@material-ui/core";
import ViewListIcon from '@material-ui/icons/ViewList';
import GridOnIcon from '@material-ui/icons/GridOn';
import OutlinedBox from "@src/js/components/common/imaging/components/common/OutlinedBox.js";
import ImagingFacade from "@src/js/components/common/imaging/ImagingFacade.js";
import LoadingDialog from "@src/js/components/common/loading/LoadingDialog.jsx";
import ErrorDialog from "@src/js/components/common/error/ErrorDialog.jsx";
import messages from "@src/js/common/messages.js";
import PaperBox from "@src/js/components/common/imaging/components/common/PaperBox.js";
import CustomSwitch from "@src/js/components/common/imaging/components/common/CustomSwitch.jsx";
import GalleryPaging from "@src/js/components/common/imaging/components/gallery/GalleryPaging.jsx";
import GridPagingOptions from "@src/js/components/common/grid/GridPagingOptions.js";
import Export from "@src/js/components/common/imaging/components/viewer/Exporter.jsx";
import GalleryFilter from "@src/js/components/common/imaging/components/gallery/GalleryFilter.jsx";
import GalleryGridView from "@src/js/components/common/imaging/components/gallery/GalleryGridView.js";
import GalleryListView from "@src/js/components/common/imaging/components/gallery/GalleryListView.js";

const ImagingGalleryViewer = ({objId, objType, extOpenbis, onOpenPreview, onStoreDisplaySettings = null, onLoadDisplaySettings = null}) => {
    const [gridView, setGridView] = React.useState(true);
    const [isLoaded, setIsLoaded] = React.useState(false);
    const [open, setOpen] = React.useState(false);
    const [error, setError] = React.useState({open: false, error: null});
    const [previewsInfo, setPreviewsInfo] = React.useState({
        previewContainerList: [],
        totalCount: 0
    });
    const [paging, setPaging] = React.useState({page: 0, pageSize: 8, pageColumns: 4});
    const [showAll, setShowAll] = React.useState(true);
    const [selectAll, setSelectAll] = React.useState(true);
    const [galleryFilter, setGalleryFilter] = React.useState({
        operator: 'AND',
        text: '',
        property: messages.get(messages.ALL)
    });
    const [dataSetTypes, setDataSetTypes] = React.useState(new ImagingFacade(extOpenbis).loadDataSetTypes());

    React.useEffect(() => {
        async function loadDataSetTypes() {
            const dataSetTypes = await new ImagingFacade(extOpenbis).loadDataSetTypes();
            dataSetTypes.push({label: 'All Properties', value: messages.get(messages.ALL)});
            setDataSetTypes(dataSetTypes);
        }

        // Set the config for the gallery view from previous store config in ELN-LIMS
        if(onLoadDisplaySettings !== null){
            const setDisplaySettings = (config) => {
                if(config) {
                    const objConfig = JSON.parse(config);
                    setPaging(objConfig.paging);
                    setShowAll(objConfig.showAll);
                    setSelectAll(objConfig.selectAll);
                }
            }
            onLoadDisplaySettings(setDisplaySettings)
        }

        loadDataSetTypes();
    }, [])

    React.useEffect(() => {
        if(onStoreDisplaySettings !== null){
            onStoreDisplaySettings(JSON.stringify({paging: paging, showAll: showAll, selectAll: selectAll}), null)
        }
    }, [paging, showAll, selectAll])

    React.useEffect(() => {
        async function load() {
            const imagingFacade = new ImagingFacade(extOpenbis);
            let {previewContainerList, totalCount} = galleryFilter.text.length >= 3 ?
                await imagingFacade.filterGallery(objId, objType, galleryFilter.operator, galleryFilter.text, galleryFilter.property, paging.page, paging.pageSize)
                : await imagingFacade.loadPaginatedGalleryDatasets(objId, objType, paging.page, paging.pageSize)
            setPreviewsInfo({previewContainerList, totalCount});
            setIsLoaded(true);
        }
        load();
    }, [paging, galleryFilter])

    const handleErrorCancel = () => {
        setError({open: false, error: null});
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
            await new ImagingFacade(extOpenbis).updatePreview(previewContainer.datasetId, previewContainer.imageIdx, selectedPreview);
        } catch (error) {
            handleError(error);
        } finally {
            setOpen(false);
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

    const handleEditComment = async (comment, previewContainer, idx) => {
        handleOpen();
        let selectedPreviewContainer = previewContainer;
        selectedPreviewContainer.preview.metadata['comment'] = comment;
        try {
            const isSaved = await new ImagingFacade(extOpenbis).updatePreview(previewContainer.datasetId, previewContainer.imageIdx, selectedPreviewContainer.preview);
            if (isSaved === null) {
                let updatedContainerList = [...previewsInfo.previewContainerList];
                updatedContainerList[idx] = selectedPreviewContainer;
                setPreviewsInfo({...previewsInfo, previewContainerList: updatedContainerList});
            }
        } catch (error) {
            handleError(error);
        } finally {
            setOpen(false);
        }
    }

    const onGalleryFilterChange = (newGalleryFilter) => {
        setGalleryFilter(newGalleryFilter);
    }

    const renderControlsBar = (isExportDisable, configExports = []) => {
        const options = GridPagingOptions.GALLERY_PAGE_SIZE_OPTIONS[paging.pageColumns - 1].map(pageSize => ({
            label: pageSize,
            value: pageSize
        }))
        return (
            <PaperBox>
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
                                           onColumnChange={(value) => setPaging({
                                               page: 0,
                                               pageSize: value,
                                               pageColumns: value
                                           })}
                                           onPageChange={(value) => setPaging({
                                               ...paging,
                                               page: value
                                           })}
                                           onPageSizeChange={(value) => setPaging({
                                               ...paging,
                                               page: 0,
                                               pageSize: value
                                           })}
                            />
                        </OutlinedBox>
                    </Grid>
                    <Grid item xs>
                        <OutlinedBox style={{width: 'fit-content'}}
                                     label={messages.get(messages.SHOW)}>
                            <CustomSwitch isChecked={showAll} onChange={setShowAll}/>
                        </OutlinedBox>
                    </Grid>
                    <Grid item xs>
                        <OutlinedBox style={{width: 'fit-content'}} label='Select'>
                            <CustomSwitch disabled={!gridView} isChecked={selectAll}
                                          onChange={handleSelectAll}/>
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
                    <Grid item xs={8}>
                        <OutlinedBox label='Filter'>
                            <GalleryFilter options={dataSetTypes}
                                           galleryFilter={galleryFilter}
                                           onGalleryFilterChange={onGalleryFilterChange}/>
                        </OutlinedBox>
                    </Grid>
                    <Grid item xs>
                        {configExports.length > 0 &&
                            <Export config={configExports} handleExport={handleExport}
                                    disabled={isExportDisable}/>}
                    </Grid>
                </Grid>
            </PaperBox>
        );
    }

    const extractCommonExportsConfig = () => {
        const commonConfig = [];
        previewsInfo.previewContainerList.flatMap(previewContainer => previewContainer.exportConfig)
            .map(exportConfig => {
                let evalIdx = commonConfig.findIndex(x => x.label === exportConfig.label);
                if (evalIdx === -1) {
                    commonConfig.push(exportConfig);
                } else {
                    commonConfig[evalIdx].values = commonConfig[evalIdx].values.filter(value => exportConfig.values.includes(value));
                }
            });
        //console.log('exports - loadedExportConfig: ', commonConfig);
        return commonConfig
    }

    if (!isLoaded) return null;
    if (previewsInfo.previewContainerList.length === 0) {
        return (
            <>
                <LoadingDialog loading={open}/>
                <ErrorDialog open={error.state} error={error.error} onClose={handleErrorCancel}/>
                {renderControlsBar(true, {})}
                <Grid container justifyContent={"space-evenly"}>
                    <Typography key="no-dataset-comment" gutterBottom variant="h6">
                        No Datasets to display
                    </Typography>
                    .
                </Grid>
            </>
        );
    }
    //console.log("RENDER.ImagingGalleryViewer - previewsInfo: ", previewsInfo);
    const previewContainerList = showAll ? previewsInfo.previewContainerList : previewsInfo.previewContainerList.filter(previewContainer => previewContainer.preview.show);
    const isExportDisable = (!(previewContainerList.filter(previewContainer => previewContainer.select === true).length > 0) || !gridView)
    const commonExportConfig = extractCommonExportsConfig();
    return (
        <>
            <LoadingDialog loading={open}/>
            <ErrorDialog open={error.state} error={error.error} onClose={handleErrorCancel}/>
            {renderControlsBar(isExportDisable, commonExportConfig)}
            {gridView ? <GalleryGridView previewContainerList={previewContainerList}
                                         cols={paging.pageColumns}
                                         selectAll={selectAll}
                                         onOpenPreview={onOpenPreview}
                                         handleShowPreview={handleShowPreview}
                                         handleSelectPreview={handleSelectPreview} />
                : <GalleryListView previewContainerList={previewContainerList} onOpenPreview={onOpenPreview} onEditComment={handleEditComment}/> }

        </>
    );
}

export default ImagingGalleryViewer;