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
        const isUpdated = await this.openbis.updateDataSets([ update ]);
        return await isUpdated;
    };

    updateImagingDataset = async (objId, activeImageIdx, preview) => {
        const serviceId = new this.openbis.CustomDssServiceCode(constants.IMAGING_CODE);
        const options = new this.openbis.CustomDSSServiceExecutionOptions();
        options.parameters = new ImagingMapper(this.openbis).mapToImagingUpdateParams(objId, activeImageIdx, preview);
        const updatedImagingDataset = await this.openbis.executeCustomDSSService(serviceId, options);
        return await this.openbis.fromJson(null, updatedImagingDataset);
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
                return JSON.parse(dataset.properties[constants.IMAGING_DATA_CONFIG]);
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
