import constants from '@src/js/components/database/imaging/constants.js';
export default class ImagingFacade {

    constructor(extOpenbis) {
        this.openbis = extOpenbis;
    }

    loadImagingDataset = async (objId) => {
        const fetchOptions = new this.openbis.DataSetFetchOptions();
        fetchOptions.withExperiment();
        fetchOptions.withSample();
        fetchOptions.withParents();
        fetchOptions.withProperties();
        return this.openbis.getDataSets(
            [new this.openbis.DataSetPermId(objId)],
            fetchOptions
        ).then(result => {
            return JSON.parse(result[objId].properties[constants.IMAGING_DATA_CONFIG]);
        });
    };

    saveImagingDataset = async (permId, imagingDataset) => {
        let update = new this.openbis.DataSetUpdate();
        update.setDataSetId(new this.openbis.DataSetPermId(permId));
        update.setProperty(constants.IMAGING_DATA_CONFIG, JSON.stringify(imagingDataset));
        return this.openbis.updateDataSets([ update ])
            .then(result => {
                //console.log('saveImagingDataset: ', result); //why result is null?
                return true;
            });
    };

    updateImagingDataset = async (objId, preview) => {
        const serviceId = new this.openbis.CustomDssServiceCode('imaging');
        const options = new this.openbis.CustomDSSServiceExecutionOptions();
        options.parameters = {
            "type" : "preview",
            "permId" : objId,
            "preview" :  preview
        };
        return this.openbis.executeCustomDSSService(serviceId, options);
    }
}