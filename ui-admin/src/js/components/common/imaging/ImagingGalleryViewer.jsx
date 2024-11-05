import React from 'react';
import { Typography } from "@mui/material";

import ImagingFacade from "@src/js/components/common/imaging/ImagingFacade.js";
import LoadingDialog from "@src/js/components/common/loading/LoadingDialog.jsx";
import ErrorDialog from "@src/js/components/common/error/ErrorDialog.jsx";
import messages from "@src/js/common/messages.js";

import GalleryGridView from "@src/js/components/common/imaging/components/gallery/GalleryGridView.js";
import GalleryListView from "@src/js/components/common/imaging/components/gallery/GalleryListView.js";
import GalleryControlsBar from '@src/js/components/common/imaging/components/gallery/GalleryControlsBar';

import {loadDataSetTypes, loadPreviewsInfo} from '@src/js/components/common/imaging/dataHandlers.js'

const ImagingGalleryViewer = ({objId, objType, extOpenbis, onOpenPreview, onStoreDisplaySettings = null, onLoadDisplaySettings = null}) => {
    const imagingFacade = React.useMemo(() => new ImagingFacade(extOpenbis), [extOpenbis]);

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
    const [dataSetTypes, setDataSetTypes] = React.useState(imagingFacade.loadDataSetTypes());

    React.useEffect(() => {

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

        loadDataSetTypes(imagingFacade, setDataSetTypes);
    }, [])

    React.useEffect(() => {
        if(onStoreDisplaySettings !== null){
            onStoreDisplaySettings(JSON.stringify({paging: paging, showAll: showAll, selectAll: selectAll}), null)
        }
    }, [paging, showAll, selectAll])

    React.useEffect(() => {
        loadPreviewsInfo(imagingFacade, objId, objType, galleryFilter, paging, setPreviewsInfo, setIsLoaded);
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
            await imagingFacade.updatePreview(previewContainer.datasetId, previewContainer.imageIdx, selectedPreview);
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
            const downloadableURL = await imagingFacade
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
            const isSaved = await imagingFacade.updatePreview(previewContainer.datasetId, previewContainer.imageIdx, selectedPreviewContainer.preview);
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
        return (<>
            <LoadingDialog loading={open}/>
            <ErrorDialog open={error.state} error={error.error} onClose={handleErrorCancel}/>
            {renderControlsBar(true, {})}
            <Grid container sx={{
                justifyContent: "space-evenly"
            }}>
                <Typography key="no-dataset-comment" gutterBottom variant="h6">
                    No Datasets to display
                </Typography>
                .
            </Grid>
        </>);
    }
    //console.log("RENDER.ImagingGalleryViewer - previewsInfo: ", previewsInfo);
    const previewContainerList = showAll ? previewsInfo.previewContainerList : previewsInfo.previewContainerList.filter(previewContainer => previewContainer.preview.show);
    const isExportDisable = (!(previewContainerList.filter(previewContainer => previewContainer.select === true).length > 0) || !gridView)
    const commonExportConfig = extractCommonExportsConfig();
    return (
        <>
            <LoadingDialog loading={open}/>
            <ErrorDialog open={error.state} error={error.error} onClose={handleErrorCancel}/>
            <GalleryControlsBar isExportDisable = {isExportDisable}
                configExports = {commonExportConfig}
                gridView = {gridView}
                handleViewChange = {handleViewChange}
                paging = {paging}
                setPaging = {setPaging}
                showAll = {showAll}
                setShowAll = {setShowAll}
                selectAll = {selectAll}
                handleSelectAll = {handleSelectAll}
                galleryFilter = {galleryFilter}
                onGalleryFilterChange = {onGalleryFilterChange}
                count = {previewsInfo.totalCount}
                handleExport = {handleExport}
                dataSetTypes = {dataSetTypes}
            />
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