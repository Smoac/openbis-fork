import constants from '@src/js/components/common/imaging/constants.js';
import ImagingMapper from "@src/js/components/common/imaging/ImagingMapper";
import messages from "@src/js/common/messages";

export default class ImagingFacade {

    constructor(extOpenbis) {
        this.openbis = extOpenbis;
    }

    async loadDataSetTypes() {
        const fetchOptions = new this.openbis.DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();

        const result = await this.openbis.searchDataSetTypes(
            new this.openbis.DataSetTypeSearchCriteria(),
            fetchOptions
        )
        let dataSetTypesSetMap = new Map()
        result.getObjects().map(dataSetType =>
            dataSetType.propertyAssignments.map(assignment => {
                if(assignment.propertyType.code !== constants.IMAGING_DATA_CONFIG)
                    dataSetTypesSetMap.set(assignment.propertyType.label, assignment.propertyType.code);
                }
            )
        );
        return Array.from(dataSetTypesSetMap, ([label, code]) => ({ label: label, value: code }))
    }

    loadImagingDataset = async (objId, withProperties = false) => {
        const fetchOptions = new this.openbis.DataSetFetchOptions();
        fetchOptions.withExperiment();
        fetchOptions.withSample();
        fetchOptions.withParents();
        fetchOptions.withProperties();
        const dataset = await this.openbis.getDataSets(
            [new this.openbis.DataSetPermId(objId)],
            fetchOptions
        )
        if (withProperties){
            return dataset[objId].properties;
        } else {
            return await this.openbis.fromJson(null, JSON.parse(dataset[objId].properties[constants.IMAGING_DATA_CONFIG]));
        }
    };

    saveImagingDataset = async (permId, imagingDataset) => {
        let update = new this.openbis.DataSetUpdate();
        update.setDataSetId(new this.openbis.DataSetPermId(permId));
        update.setProperty(constants.IMAGING_DATA_CONFIG, JSON.stringify(imagingDataset));
        const totalPreviews = imagingDataset.images.reduce((count, image) => count + image.previews.length, 0);
        update.getMetaData().put(constants.METADATA_PREVIEW_COUNT, totalPreviews.toString());
        //update.getMetaData().put('filterTest2', 'mmmmm i don\'t know');
        return await this.openbis.updateDataSets([ update ]);
    };

    updatePreview = async (permId, imageIdx, preview) => {
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

    fetchExperimentDataSets = async (objId) => {
        const fetchOptions = new this.openbis.ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withDataSets();
        const experiments = await this.openbis.getExperiments(
            [new this.openbis.ExperimentPermId(objId)],
            fetchOptions
        );
        return await experiments[objId].dataSets;
    }

    fetchDataSetsSortingInfo = (dataSets) => {
        return dataSets.map(dataset => {
            if (constants.METADATA_PREVIEW_COUNT in dataset.metaData) {
                const nDatasets = parseInt(dataset.metaData[constants.METADATA_PREVIEW_COUNT])
                return Array.from(Array(nDatasets), (_, i) => {
                    return {datasetId: dataset.code, sortingId: i, metadata: dataset.metaData}
                });
            }
        }).flat();
    }

    paginateImagingDatasets = async (datasetCodeList, page, pageSize) => {
        let startIdx = page * pageSize;
        const offset = startIdx + pageSize;
        let prevDatasetId = null;
        let loadedImgDS = null;
        let datasetProperties = null;
        let previewContainerList = [];
        while (startIdx < datasetCodeList.length && startIdx < offset) {
            let currDatasetId = datasetCodeList[startIdx].datasetId;
            if (currDatasetId !== prevDatasetId) {
                prevDatasetId = currDatasetId;
                datasetProperties = await this.loadImagingDataset(currDatasetId, true);
                loadedImgDS = await this.openbis.fromJson(null, JSON.parse(datasetProperties[constants.IMAGING_DATA_CONFIG]));
                delete datasetProperties[constants.IMAGING_DATA_CONFIG];
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
                        datasetProperties: datasetProperties,
                        exportConfig: loadedImgDS.config.exports});
                }
                partialIdxCount += loadedImgDS.images[imageIdx].previews.length
            }
        }
        return previewContainerList
    }

    loadPaginatedGalleryDatasets = async (objId, page, pageSize) => {
        const dataSets = await this.fetchExperimentDataSets(objId);
        const datasetCodeList = this.fetchDataSetsSortingInfo(dataSets);

        const totalCount =  datasetCodeList.length;
        const previewContainerList = await this.paginateImagingDatasets(datasetCodeList, page, pageSize);
        //console.log("loadPaginatedGalleryDatasets - previewContainerList: ", previewContainerList);
        return {previewContainerList, totalCount};
    }

    filterGallery = async (objId, operator, filterText, property, page, pageSize) => {
        //console.log(objId, operator, filterText, property, page, pageSize);
        const criteria = new this.openbis.DataSetSearchCriteria();
        criteria.withAndOperator();
        criteria.withExperiment().withPermId().thatEquals(objId);
        // TODO add object type to distinguish
        //criteria.withSample().withPermId().thatEquals(objId);
        if (filterText && filterText.trim().length > 0) {
            const subCriteria = criteria.withSubcriteria();
            operator === messages.get(messages.OPERATOR_AND) ? subCriteria.withAndOperator() : subCriteria.withOrOperator();
            const splittedText = filterText.split(' ');
            for(const value of splittedText){
                if (property === messages.get(messages.ALL)) {
                    subCriteria.withAnyStringProperty().thatContains(value);
                    //subCriteria.withAnyProperty().thatContains(value);
                } else {
                    subCriteria.withStringProperty(property).thatContains(value);
                    //subCriteria.withProperty(property).thatContains(value);
                }
            }
        }

        //console.log('criteria: ', criteria);
        const fetchOptions = new this.openbis.DataSetFetchOptions();
        fetchOptions.withProperties();

        const dataSets = await this.openbis.searchDataSets(
            criteria,
            fetchOptions
        )
        //console.log('searchDataSets: ', dataSets);

        const datasetCodeList = this.fetchDataSetsSortingInfo(dataSets.getObjects());
        const totalCount =  datasetCodeList.length;

        const previewContainerList = await this.paginateImagingDatasets(datasetCodeList, page, pageSize);
        return {previewContainerList, totalCount};
    }
}
