import constants from '@src/js/components/database/imaging/constants.js';
import ImagingMapper from "@src/js/components/database/imaging/ImagingMapper";
import openbis from "@src/js/services/openbis";
import messages from "@src/js/common/messages";

export default class ImagingFacade {

    constructor(extOpenbis) {
        this.openbis = extOpenbis;
    }

    async loadDataSetTypes() {
        const result = await this.openbis.searchDataSetTypes(
            new this.openbis.DataSetTypeSearchCriteria(),
            new this.openbis.DataSetTypeFetchOptions()
        )
        return result.getObjects().map(dataSetType => {
            return {label: dataSetType.description, value: dataSetType.code}
        })
    }

    loadImagingDataset = async (objId) => {
        const fetchOptions = new this.openbis.DataSetFetchOptions();
        fetchOptions.withExperiment();
        fetchOptions.withSample();
        fetchOptions.withParents();
        fetchOptions.withProperties();
        const dataset = await this.openbis.getDataSets(
            [new this.openbis.DataSetPermId(objId)],
            fetchOptions
        )
        return await this.openbis.fromJson(null, JSON.parse(dataset[objId].properties[constants.IMAGING_DATA_CONFIG]));
    };

    saveImagingDataset = async (permId, imagingDataset) => {
        let update = new this.openbis.DataSetUpdate();
        update.setDataSetId(new this.openbis.DataSetPermId(permId));
        update.setProperty(constants.IMAGING_DATA_CONFIG, JSON.stringify(imagingDataset));
        const totalPreviews = imagingDataset.images.reduce((count, image) => count + image.previews.length, 0);
        update.getMetaData().put(constants.METADATA_PREVIEW_COUNT, totalPreviews.toString());
        //update.getMetaData().put('filterTest', 'unique dataset to display');
        return await this.openbis.updateDataSets([ update ]);
    };

    updateShowInGalleryView = async (permId, imageIdx, preview) => {
        let toUpdateImgDS = await this.loadImagingDataset(permId);
        toUpdateImgDS.images[imageIdx].previews[preview.index] = preview;
        let update = new this.openbis.DataSetUpdate();
        update.setDataSetId(new this.openbis.DataSetPermId(permId));
        update.setProperty(constants.IMAGING_DATA_CONFIG, JSON.stringify(toUpdateImgDS));
        return await this.openbis.updateDataSets([ update ]);
    }

    updateImagingDataset = async (objId, activeImageIdx, preview) => {
        const serviceId = new this.openbis.CustomDssServiceCode(constants.IMAGING_CODE);
        const options = new this.openbis.CustomDSSServiceExecutionOptions();
        options.parameters = new ImagingMapper(this.openbis).mapToImagingUpdateParams(objId, activeImageIdx, preview);
        const updatedImagingDataset = await this.openbis.executeCustomDSSService(serviceId, options);
        return await this.openbis.fromJson(null, updatedImagingDataset);
    }

    multiExportImagingDataset = async (exportConfig, exportList) => {
        const serviceId = new this.openbis.CustomDssServiceCode(constants.IMAGING_CODE);
        const options = new this.openbis.CustomDSSServiceExecutionOptions();
        options.parameters = new ImagingMapper(this.openbis).mapToImagingMultiExportParams(exportConfig, exportList);
        const exportedImagingDataset = await this.openbis.executeCustomDSSService(serviceId, options);
        return await exportedImagingDataset.url;
    }

    exportImagingDataset = async (objId, activeImageIdx, exportConfig, metadata) => {
        const serviceId = new this.openbis.CustomDssServiceCode(constants.IMAGING_CODE);
        const options = new this.openbis.CustomDSSServiceExecutionOptions();
        options.parameters = new ImagingMapper(this.openbis).mapToImagingExportParams(objId, activeImageIdx, exportConfig, metadata);
        const exportedImagingDataset = await this.openbis.executeCustomDSSService(serviceId, options);
        return await exportedImagingDataset.url;
    }

    preLoadGalleryDatasetsCodeList = async (objId) => {
        const fetchOptions = new this.openbis.ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withDataSets();
        const experiments = await this.openbis.getExperiments(
            [new this.openbis.ExperimentPermId(objId)],
            fetchOptions
        );
        return await experiments[objId].dataSets.map(dataset => {
            if (constants.METADATA_PREVIEW_COUNT in dataset.metaData) {
                const nDatasets = parseInt(dataset.metaData[constants.METADATA_PREVIEW_COUNT])
                //return Array(nDatasets).fill({'id': dataset.code, 'previewIdx':})
                return Array.from(Array(nDatasets), (_, i) => {
                    return {datasetId: dataset.code, sortingId: i, metadata: dataset.metaData}
                });
            }
        }).flat();
    }

    loadPaginatedGalleryDatasets = async (objId, page, pageSize) => {
        /*const {totalCount, datasetStats} = await this.preLoadGalleryDatasetsMetadata(objId);
        console.log("loadPaginatedGalleryDatasets - preLoadGalleryDatasetsMetadata: ", totalCount, datasetStats);*/
        const datasetCodeList = await this.preLoadGalleryDatasetsCodeList(objId);
        const totalCount =  datasetCodeList.length;
        //console.log("loadPaginatedGalleryDatasets - preLoadGalleryDatasetsCodeList: ", datasetCodeList.length, datasetCodeList);
        let startIdx = page * pageSize;
        const offset = startIdx + pageSize;
        let prevDatasetId = null;
        let loadedImgDS = null;
        let previewContainerList = [];
        while (startIdx < datasetCodeList.length && startIdx < offset) {
            let currDatasetId = datasetCodeList[startIdx].datasetId;
            if (currDatasetId !== prevDatasetId) {
                prevDatasetId = currDatasetId;
                loadedImgDS = await this.loadImagingDataset(currDatasetId);
                //console.log(loadedImgDS);
            }
            let partialIdxCount = 0
            for (let imageIdx = 0; imageIdx < loadedImgDS.images.length; imageIdx++){
                let hypoteticalPreviewIdx = datasetCodeList[startIdx].sortingId;
                for (let previewIdx = hypoteticalPreviewIdx - partialIdxCount;
                     previewIdx < loadedImgDS.images[imageIdx].previews.length && startIdx < offset;
                     previewIdx++, startIdx++) {
                    previewContainerList.push({datasetId: currDatasetId,
                        preview: loadedImgDS.images[imageIdx].previews[previewIdx],
                        imageIdx: imageIdx,
                        select: false,
                        datasetMetadata: datasetCodeList[startIdx].metadata,
                        exportConfig: loadedImgDS.config.exports});
                }
                partialIdxCount += loadedImgDS.images[imageIdx].previews.length
            }
        }
        //console.log("loadPaginatedGalleryDatasets - previewContainerList: ", previewContainerList);
        return {previewContainerList, totalCount};
    }

    filterGallery = async (objId, operator, filterText, property, count) => {
        // WHERE experID = 233444
        //     AND/OR withProp = 'dsasa'
        //      AND/OR with = 'adwad'
        const criteria = new this.openbis.DataSetSearchCriteria();
        criteria.withAndOperator();
        criteria.withExperiment().withPermId().thatEquals(objId);

        if (filterText && filterText.trim().length > 0) {
            const subCriteria = criteria.withSubcriteria();
            operator === messages.get(messages.OPERATOR_AND) ? subCriteria.withAndOperator() : subCriteria.withOrOperator();

            const splittedText = filterText.split(' ');
            for(const value in splittedText){
                if (property === messages.get(messages.ALL)) {
                    //subCriteria.withAnyStringProperty().thatContains(value);
                    subCriteria.withAnyProperty().thatContains(value);
                } else {
                    //subCriteria.withStringProperty(property).thatContains(value);
                    subCriteria.withProperty(property).thatContains(value);
                }
            }
        }

        // TODO add object type to distinguish
        //criteria.withSample().withPermId().thatEquals(objId);

        const fetchOptions = new this.openbis.DataSetFetchOptions();
        fetchOptions.withProperties();
        //fetchOptions.from(3).count(count);
        //fo.sortBy().code().asc()

        const dataSets = await this.openbis.searchDataSets(
            criteria,
            fetchOptions
        )
        console.log('searchDataSets: ', dataSets);
        let loadedImg = []
        dataSets.getObjects().forEach(dataSet => {
            console.log("dataSet: ", dataSet);
            //loadedImg.push(this.openbis.fromJson(null, JSON.parse(dataSet.properties[constants.IMAGING_DATA_CONFIG])));
        })
        //console.log(loadedImg);
    }
}
