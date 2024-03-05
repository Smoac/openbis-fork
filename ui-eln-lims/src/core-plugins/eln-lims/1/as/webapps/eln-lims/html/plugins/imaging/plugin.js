function ImagingTechnology() {
    this.init();
}

$.extend(ImagingTechnology.prototype, ELNLIMSPlugin.prototype, {
    init: function () {

    },
    forcedDisableRTF: [],
    forceMonospaceFont: [],
    displayImagingTechViewer: function ($container, isDataset, objId, objType, onActionCallback) {
        let $element = $("<div>")
        require([ "dss/dto/service/id/CustomDssServiceCode",
                "dss/dto/service/CustomDSSServiceExecutionOptions",
                "imaging/dto/ImagingPreviewContainer",
                "imaging/dto/ImagingDataSetExport",
                "imaging/dto/ImagingDataSetMultiExport",
                "imaging/dto/ImagingDataSetPreview",
                "as/dto/experiment/fetchoptions/ExperimentFetchOptions",
                "as/dto/experiment/id/ExperimentPermId",
                "as/dto/sample/fetchoptions/SampleFetchOptions",
                "as/dto/sample/id/SamplePermId",
                "as/dto/dataset/search/DataSetSearchCriteria",
                "as/dto/dataset/search/DataSetTypeSearchCriteria",
                "as/dto/dataset/search/SearchDataSetsOperation",
                "as/dto/dataset/update/DataSetUpdate",
                "as/dto/dataset/id/DataSetPermId",
                "as/dto/dataset/fetchoptions/DataSetFetchOptions",
                "as/dto/dataset/fetchoptions/DataSetTypeFetchOptions",
                "util/Json"],
            function(CustomDssServiceCode, CustomDSSServiceExecutionOptions,
                     ImagingPreviewContainer, ImagingDataSetExport,
                     ImagingDataSetMultiExport, ImagingDataSetPreview,
                     ExperimentFetchOptions, ExperimentPermId,
                     SampleFetchOptions, SamplePermId,
                     DataSetSearchCriteria, DataSetTypeSearchCriteria,
                     SearchDataSetsOperation, DataSetUpdate, DataSetPermId,
                     DataSetFetchOptions, DataSetTypeFetchOptions,
                     utilJson) {
                let props = {
                    objId: objId,
                    objType: objType,
                    extOpenbis: {
                        CustomDssServiceCode: CustomDssServiceCode,
                        CustomDSSServiceExecutionOptions: CustomDSSServiceExecutionOptions,
                        ImagingPreviewContainer: ImagingPreviewContainer,
                        ImagingDataSetExport: ImagingDataSetExport,
                        ImagingDataSetMultiExport: ImagingDataSetMultiExport,
                        ImagingDataSetPreview: ImagingDataSetPreview,
                        SampleFetchOptions: SampleFetchOptions,
                        SamplePermId: SamplePermId,
                        ExperimentFetchOptions: ExperimentFetchOptions,
                        ExperimentPermId: ExperimentPermId,
                        DataSetSearchCriteria: DataSetSearchCriteria,
                        DataSetTypeSearchCriteria: DataSetTypeSearchCriteria,
                        SearchDataSetsOperation: SearchDataSetsOperation,
                        DataSetUpdate: DataSetUpdate,
                        DataSetPermId: DataSetPermId,
                        DataSetFetchOptions: DataSetFetchOptions,
                        DataSetTypeFetchOptions: DataSetTypeFetchOptions,
                        getDataSets: mainController.openbisV3.getDataSets.bind(mainController.openbisV3),
                        searchDataSets: mainController.openbisV3.searchDataSets.bind(mainController.openbisV3),
                        searchDataSetTypes: mainController.openbisV3.searchDataSetTypes.bind(mainController.openbisV3),
                        updateDataSets: mainController.openbisV3.updateDataSets.bind(mainController.openbisV3),
                        executeCustomDSSService: mainController.openbisV3.getDataStoreFacade().executeCustomDSSService.bind(mainController.openbisV3.getDataStoreFacade()),
                        getExperiments: mainController.openbisV3.getExperiments.bind(mainController.openbisV3),
                        getSamples: mainController.openbisV3.getSamples.bind(mainController.openbisV3),
                        fromJson: utilJson.fromJson.bind(utilJson)
                    }
                }
                let reactImagingComponent = null;
                if (isDataset) {
                    props['onUnsavedChanges'] = onActionCallback
                    reactImagingComponent = React.createElement(window.NgComponents.default.ImagingDatasetViewer, props)
                } else {
                    props['onOpenPreview'] = onActionCallback
                    reactImagingComponent = React.createElement(window.NgComponents.default.ImagingGalleryViewer, props)
                }
                ReactDOM.render(
                    React.createElement(
                        window.NgComponents.default.ThemeProvider,
                        {},
                        reactImagingComponent),
                    $element.get(0)
                );
            }
        );
        $container.append($element);
    },
    experimentFormTop : function($container, model) {
        if (model.mode === FormMode.VIEW) {
            let isGalleryView = model.experiment &&
                                model.experiment.properties["$DEFAULT_COLLECTION_VIEW"] &&
                                model.experiment.properties["$DEFAULT_COLLECTION_VIEW"] === "IMAGING_GALLERY_VIEW";
            if (isGalleryView) {

// TODO
//                var configKey = "IMAGING_GALLERY_VIEW-" + model.experiment.experimentTypeCode;
//
//                var readConfig = function(callback) {
//                        mainController.serverFacade.getSetting(configKey, function(config) {
//                        callback(config);
//                    });
//                }
//
//                var writeConfig = function(config, callback) {
//                    mainController.serverFacade.setSetting(configKey, config);
//                    callback();
//                }

                this.displayImagingTechViewer($container, false, model.experiment.permId, 'collection',
                    function(objId){mainController.changeView('showViewDataSetPageFromPermId', objId)});
            }
        }
    },
    sampleFormTop: function ($container, model) {
        if (model.mode === FormMode.VIEW) {
            let isGalleryView = model.sample &&
                                model.sample.properties["$DEFAULT_OBJECT_VIEW"] &&
                                model.sample.properties["$DEFAULT_OBJECT_VIEW"] === "IMAGING_GALLERY_VIEW";
            if (isGalleryView) {
                this.displayImagingTechViewer($container, false, model.sample.permId, 'object',
                    function(objId){mainController.changeView('showViewDataSetPageFromPermId', objId)});
            }
        }
    },
    dataSetFormTop: function ($container, model) {
        if (model.mode === FormMode.VIEW) {
            // Potentially any DataSet Type can be an Imaging DataSet Type. The system will know what DataSet Types
            // are an Imaging DataSet by convention, those Types SHOULD end with IMAGING_DATA on their Type Code.
            let isImagingDatasetView = model.dataSetV3 &&
                                model.dataSetV3.type.code.endsWith("IMAGING_DATA") &&
                                model.dataSetV3.properties["$DEFAULT_DATASET_VIEW"] &&
                                model.dataSetV3.properties["$DEFAULT_DATASET_VIEW"] === "IMAGING_DATASET_VIEWER";
            if (isImagingDatasetView) {
// TODO
//                var viewDirty = function() {
//                    model.isFormDirty = true;
//                }
                this.displayImagingTechViewer($container, true, model.dataSetV3.permId.permId, '', null);
            }
        }
    }
});

profile.plugins.push( new ImagingTechnology());