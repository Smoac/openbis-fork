import constants from '@src/js/components/database/imaging/constants.js';
import ImagingMapper from "@src/js/components/database/imaging/ImagingMapper";

export default class ImagingFacade {

    constructor(extOpenbis) {
        this.openbis = extOpenbis;
    }

    multiFromJson = async (jsonObjArray) => {
        console.log("multiFromJson: ", jsonObjArray);
        const JSObjArray = [];
        const all = jsonObjArray.map(async jsonObj => JSObjArray.push(await this.openbis.fromJson(null, jsonObj)));
        console.log("all: ", all);
        console.log("test: ", JSObjArray);
        return JSObjArray;
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
        return await this.openbis.updateDataSets([ update ]);
    };

    updateImagingDataset = async (objId, activeImageIdx, preview) => {
        const serviceId = new this.openbis.CustomDssServiceCode(constants.IMAGING_CODE);
        const options = new this.openbis.CustomDSSServiceExecutionOptions();
        options.parameters = new ImagingMapper(this.openbis).mapToImagingUpdateParams(objId, activeImageIdx, preview);
        const updatedImagingDataset = await this.openbis.executeCustomDSSService(serviceId, options);
        return await this.openbis.fromJson(null, updatedImagingDataset);
    }

    exportImagingDataset = async (objId, activeImageIdx, exportConfig, metadata) => {
        const serviceId = new this.openbis.CustomDssServiceCode(constants.IMAGING_CODE);
        const options = new this.openbis.CustomDSSServiceExecutionOptions();
        options.parameters = new ImagingMapper(this.openbis).mapToImagingExportParams(objId, activeImageIdx, exportConfig, metadata);
        const exportedImagingDataset = await this.openbis.executeCustomDSSService(serviceId, options);
        return await exportedImagingDataset.url;
    }

    preLoadGalleryDatasetsMetadata = async (objId) => {
        const fetchOptions = new this.openbis.ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withDataSets();
        const experiments = await this.openbis.getExperiments(
            [new this.openbis.ExperimentPermId(objId)],
            fetchOptions
        );
        let totalCount = 0;
        const datasetStats = await experiments[objId].dataSets.map(dataset => {
            if (constants.METADATA_PREVIEW_COUNT in dataset.metaData) {
                const nDatasets = parseInt(dataset.metaData[constants.METADATA_PREVIEW_COUNT])
                totalCount += nDatasets;
                return {'permId': dataset.code, 'nDatasets': nDatasets}
            }
        })
        console.log("preLoadGalleryDatasetsMetadata: ", totalCount, datasetStats);
        return {totalCount, datasetStats};
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
                    return {'id': dataset.code, 'previewIdx': i}
                });
            }
        }).flat();
    }

    loadPaginatedGalleryDatasets = async (objId, page, pageSize) => {
        /*const {totalCount, datasetStats} = await this.preLoadGalleryDatasetsMetadata(objId);
        console.log("loadPaginatedGalleryDatasets - preLoadGalleryDatasetsMetadata: ", totalCount, datasetStats);*/
        const datasetCodeList = await this.preLoadGalleryDatasetsCodeList(objId);
        console.log("loadPaginatedGalleryDatasets - preLoadGalleryDatasetsCodeList: ", datasetCodeList.length, datasetCodeList);
        const startIdx = page * pageSize;
        const offset = startIdx + pageSize;
        let prevDatasetId = null;
        let loadedImgDS = null;
        let previewList = [];
        for (let i = startIdx; i < offset; i++){
            let currDatasetId = datasetCodeList[i].id;
            if (currDatasetId !== prevDatasetId) {
                prevDatasetId = currDatasetId;
                loadedImgDS = await this.loadImagingDataset(currDatasetId);
                previewList.push(loadedImgDS.images[0].previews[datasetCodeList[i].previewIdx])
            } else {
                previewList.push(loadedImgDS.images[0].previews[datasetCodeList[i].previewIdx])
            }
        }
        /*const codesSet = new Set(datasetCodeList.slice(startIdx, offset+1))
        console.log(codesSet);
        let datasets = [];
        for (const {id, previewIdx} of codesSet){
            const img = await this.loadImagingDataset(id);
            datasets.push({'permId': id, 'imagingDataset': img})
        }*/
        console.log("loadPaginatedGalleryDatasets - datasets: ", previewList);
        const totalCount =  datasetCodeList.length;
        return {previewList, totalCount};
    }

    loadGalleryDatasets = async (objId) => {
        const fetchOptions = new this.openbis.ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withDataSets().withProperties();
        const experiments = await this.openbis.getExperiments(
            [new this.openbis.ExperimentPermId(objId)],
            fetchOptions
        );
        console.log("experiments: ", experiments[objId]);
        const datasets = experiments[objId].dataSets;
        const imagingDatasets = datasets.map(dataset => {
            if (constants.IMAGING_DATA_CONFIG in dataset.properties)
                return {'permId': dataset.code, 'metadata': dataset.metaData, 'imagingDataset': JSON.parse(dataset.properties[constants.IMAGING_DATA_CONFIG])};
        });
        return await imagingDatasets;
    }

    filterGallery = async () => {
        const criteria = new this.openbis.DataSetSearchCriteria()
        // Use these options
        criteria.withAndOperator(); // If needed
        criteria.withAnyStringProperty().thatContains(); // for every string split by space
        // Use one of this two
        criteria.withExperiment().withPermId().thatEquals("");
        criteria.withSample().withPermId().thatEquals("");
        // Don't need to fetch anything if previews are on metadata
        const options = new this.openbis.DataSetFetchOptions();
        // Imaging datasets are datasets that contain the metadata field indicating they have previews
    }
    async loadDataSets(value, count) {
        const criteria = new openbis.DataSetSearchCriteria()
        criteria.withOrOperator()

        if (value && value.trim().length > 0) {
            criteria.withCode().thatContains(value)
            criteria.withProperty(ENTITY_NAME_PROPERTY).thatContains(value)

            const experimentCriteria = criteria.withExperiment()
            experimentCriteria.withOrOperator()
            //experimentCriteria.withCode().thatContains(value)
            experimentCriteria.withIdentifier().thatContains(value)
            experimentCriteria.withProperty(ENTITY_NAME_PROPERTY).thatContains(value)

            const sampleCriteria = criteria.withSample()
            sampleCriteria.withOrOperator()
            //sampleCriteria.withCode().thatContains(value)
            sampleCriteria.withIdentifier().thatContains(value)
            sampleCriteria.withProperty(ENTITY_NAME_PROPERTY).thatContains(value)
        }

        const fo = new openbis.DataSetFetchOptions()
        fo.withProperties()
        fo.withExperiment()
        fo.withSample()
        fo.from(0).count(count)
        fo.sortBy().code().asc()

        const results = await openbis.searchDataSets(criteria, fo)

        return {
            options: results.getObjects().map(object => {
                return {
                    label: this.createOptionLabel(openbis.EntityKind.DATA_SET, object),
                    fullLabel: this.createOptionFullLabel(
                        openbis.EntityKind.DATA_SET,
                        object
                    ),
                    entityKind: openbis.EntityKind.DATA_SET,
                    entityId: object.code
                }
            }),
            totalCount: results.totalCount
        }
    }
}
