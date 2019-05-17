function MicroscopyTechnology() {
    this.init();
}

$.extend(MicroscopyTechnology.prototype, ELNLIMSPlugin.prototype, {
    init: function () {

    },
    forcedDisableRTF: [],
    forceMonospaceFont: [],
    sampleTypeDefinitionsExtension: {
        "MICROSCOPY_SAMPLE_TYPE": {
            "SHOW_ON_NAV": true,
            "SHOW": false,
            "SAMPLE_CHILDREN_DISABLED": true,
            "SAMPLE_PARENTS_DISABLED": true,
            "TOOLBAR": { CREATE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "MICROSCOPY_EXPERIMENT": {
            "SHOW": false,
            "SAMPLE_CHILDREN_DISABLED": false,
            "SAMPLE_PARENTS_DISABLED": false,
            "SAMPLE_PARENTS_ANY_TYPE_DISABLED": true,
            "TOOLBAR": { CREATE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SAMPLE_PARENTS_HINT": [{
                "LABEL": "Organization Units",
                "TYPE": "ORGANIZATION_UNIT",
                "ANNOTATION_PROPERTIES": []
            }]
        }
    },
    dataSetTypeDefinitionsExtension: {
        "MICROSCOPY_ACCESSORY_FILE": {
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: true, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "MICROSCOPY_IMG": {
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: true, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "MICROSCOPY_IMG_OVERVIEW": {
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: true, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "MICROSCOPY_IMG_THUMBNAIL": {
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: true, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "MICROSCOPY_IMG_CONTAINER": {
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: true, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        }
    },
    sampleFormTop: function ($container, model) {
        if (model.sample && model.sample.sampleTypeCode == "MICROSCOPY_EXPERIMENT") {
            this.displayExperimentThumbnails($container, model, model.sample);
        }
        if (model.datasets && model.datasets.length > 0) {
            var imageViewerDataSets = [];
            for (var dsIdx = 0; dsIdx < model.datasets.length; dsIdx++) {
                if (profile.isImageViewerDataSetCode(model.datasets[dsIdx].dataSetTypeCode)) {
                    imageViewerDataSets.push(model.datasets[dsIdx].code);
                }
            }

            if (imageViewerDataSets.length > 0) {
                require(["openbis-screening", "components/imageviewer/ImageViewerWidget"], function (openbis, ImageViewerWidget) {
                    var screningFacade = new openbis(null);
                    screningFacade._internal.sessionToken = mainController.openbisV1._internal.sessionToken;

                    // Create the image viewer component for the specific data sets
                    var widget = new ImageViewerWidget(screningFacade, imageViewerDataSets);

                    // Render the component and add it to the page
                    $container.append($('<legend>').text('Microscopy Viewer'));
                    var $imageWidgetContainer = new $('<div>');
                    $imageWidgetContainer.css("margin", "20px");
                    $container.append($imageWidgetContainer);
                    $imageWidgetContainer.append(widget.render());
                });
            }
        }
    },
    sampleFormBottom: function ($container, model) {

    },
    dataSetFormTop: function ($container, model) {
        if (model.dataSetV3 && profile.isImageViewerDataSetCode(model.dataSetV3.type.code)) {
            require(["openbis-screening", "components/imageviewer/ImageViewerWidget"], function (openbis, ImageViewerWidget) {
                var screningFacade = new openbis(null);
                screningFacade._internal.sessionToken = mainController.openbisV1._internal.sessionToken;

                // Create the image viewer component for the specific data sets
                var widget = new ImageViewerWidget(screningFacade, [model.dataSetV3.permId.permId]);

                // Render the component and add it to the page
                $container.append($('<legend>').text('Microscopy Viewer'));
                var $imageWidgetContainer = new $('<div>');
                $imageWidgetContainer.css("margin", "20px");
                $container.append($imageWidgetContainer);
                $imageWidgetContainer.append(widget.render());
            });
        }
    },
    dataSetFormBottom: function ($container, model) {

    },

    displayExperimentThumbnails: function ($container, model, microscopyExperimentSample) {
        if (microscopyExperimentSample.sampleTypeCode !== "MICROSCOPY_EXPERIMENT") {
            return;
        }

        // Clear the container
        $container.empty();

        if (microscopyExperimentSample.children.length === 0) {
            return;
        }

        // Add legend
        var legend = $("<legend>")
            .text("File previews");
        $container.append(legend);

        // Prepare a div to host the thumbnails
        var sampleView_div = $("<div>");
        $container.append(sampleView_div);

        var newThumbRow = null;
        var numSample = 0;

        var thisObj = this;

        // Prepare the display
        microscopyExperimentSample.children.forEach(function (sample) {

            // Keep track of the number of the sample
            numSample++;

            // Add a new row for the next three thumbnails
            if (numSample % 4 === 1) {
                newThumbRow = $("<div />", { class: "row" });
                sampleView_div.append(newThumbRow);
            }

            // Prepare the name to be shown
            var name;
            if (sample.properties.MICROSCOPY_SAMPLE_NAME) {
                name = sample.properties.MICROSCOPY_SAMPLE_NAME;
            } else {
                name = sample.code;
            }

            // Make sure it is not too long
            var displayName;
            var l = name.length;
            if (l > 40) {
                displayName = name.substring(0, 18) + "..." + name.substring(l - 18);
            } else {
                displayName = name;
            }

            // If the size property exists (this was added later), retrieve it and display it as well
            var datasetSize;
            if (sample.properties.MICROSCOPY_SAMPLE_SIZE_IN_BYTES) {
                datasetSize = thisObj.formatSizeForDisplay(sample.properties.MICROSCOPY_SAMPLE_SIZE_IN_BYTES);
            } else {
                datasetSize = "";
            }

            // A column to be added to current row that will store all
            // elements related to current sample
            var newThumbCol = $("<div />",
                {
                    class: "col-md-3",
                    display: "inline",
                    "text-align": "center",
                    id: sample.code
                });

            // A div element to contain the thumbnail and its info
            var thumbnailView = $("<div />")
                .css("min-height", "320px")
                .css("text-align", "center")
                .css("padding-top", "10px")
                .css("padding-bottom", "10px")

            // Link to the dataset (sample) viewer.
            var link = $("<a>").text(displayName).attr("href", "#").attr("title", name).click(
                function () {
                    window.top.location.hash = "#" + (new Date().getTime());
                    return false;
                });

            // Actual thumbnail. Initially we display a place holder. Later,
            // we will replace it asynchronously.
            var thumbnailImage = $("<img />",
                {
                    src: "./img/wait.png",
                    class: "img-responsive",
                    display: "inline",
                    "text-align": "center",
                    id: "image_" + sample.code,
                    title: name
                });

            // Build the thumbnail viewer
            thumbnailView.append(thumbnailImage);
            thumbnailView.append($("<br />"));
            thumbnailView.append(link);
            if (datasetSize !== "") {
                thumbnailView.append($("<br />"));
                var spanSz = $("<span>").text(datasetSize);
                thumbnailView.append(spanSz);
            }

            // Add the thumbnail to the column and the row
            newThumbCol.append(thumbnailView);
            newThumbRow.append(newThumbCol);

            // Now retrieve the link to the thumbnail image asynchronously and update the <img>
            thisObj.displayThumbnailForSample(model, sample, "image_" + sample.code);

        });
    },

    formatSizeForDisplay: function (datasetSize) {

        // Output
        var formattedDatasetSize = "";

        // Cast datasetSize to float
        var datasetSizeF = parseFloat(datasetSize);

        var sMB = datasetSizeF / 1024.0 / 1024.0;
        if (sMB < 1024.0) {
            formattedDatasetSize = sMB.toFixed(2) + " MiB";
        } else {
            var sGB = sMB / 1024.0;
            formattedDatasetSize = sGB.toFixed(2) + " GiB";
        }

        return formattedDatasetSize;
    },

    displayThumbnailForSample: function (model, sample, img_id) {

        require([
            "as/dto/sample/search/SampleSearchCriteria",
            "as/dto/sample/fetchoptions/SampleFetchOptions",
            "as/dto/dataset/search/DataSetSearchCriteria",
            "dss/dto/datasetfile/search/DataSetFileSearchCriteria",
            "dss/dto/datasetfile/fetchoptions/DataSetFileFetchOptions",
        ],

            function (
                SampleSearchCriteria,
                SampleFetchOptions,
                DataSetSearchCriteria,
                DataSetFileSearchCriteria,
                DataSetFileFetchOptions) {

                // First retrieve the sample again but with the associated datasets
                var criteria = new SampleSearchCriteria();
                criteria.withType().withCode().thatEquals(sample.sampleTypeCode);
                criteria.withPermId().thatEquals(sample.permId);
                var fetchOptions = new SampleFetchOptions();
                fetchOptions.withDataSets().withType();

                // Query the server
                mainController.openbisV3.searchSamples(criteria, fetchOptions).done(function (result) {
                    if (result.getTotalCount() == 0) {
                        return null;
                    }
                    var sample = result.getObjects()[0];
                    for (var i = 0; i < sample.getDataSets().length; i++) {
                        var dataSet = sample.getDataSets()[i];

                        if (dataSet.getType().code === "MICROSCOPY_IMG_THUMBNAIL") {

                            // Now retrieve the thumbnail and add display it

                            // Get the file
                            var criteria = new DataSetFileSearchCriteria();
                            var dataSetCriteria = criteria.withDataSet().withOrOperator();
                            dataSetCriteria.withPermId().thatEquals(dataSet.permId.permId);

                            var fetchOptions = new DataSetFileFetchOptions();

                            // Query the server
                            mainController.openbisV3.getDataStoreFacade().searchFiles(criteria, fetchOptions).done(function (result) {

                                // Thumbnail
                                var imD = $("#" + img_id);

                                // Make sure to reset the display attribute
                                imD.css("display", "inline");

                                if (result.getTotalCount() == 0) {

                                    // Thumbnail not found!
                                    imD.attr("src", "./img/error.png");
                                    imD.attr("title", "Could not find a thumbnail for this dataset!");

                                    return;
                                }

                                // Extract the files
                                var datasetFiles = result.getObjects();

                                // Find the only fcs file and add its name and URL to the DynaTree
                                datasetFiles.forEach(function (f) {

                                    // 	// Build the download URL
                                    // let url = f.getDataStore().getDownloadUrl() + "/datastore_server/" +
                                    // f.permId.dataSetId.permId + "/" + f.getPath() + "?sessionID=" +
                                    // mainController.openbisV3.getWebAppContext().sessionId;

                                    if (!f.isDirectory() && f.getPath().toLowerCase() === "thumbnail.png") {

                                        // Still use the V1 API since the sessionId stored in the 
                                        // webapp context is null in V3.
                                        mainController.openbisV1.getDownloadUrlForFileForDataSetInSession(
                                            f.getDataSetPermId().permId, f.getPath(), function (url) {

                                                // Replace the image
                                                var eUrl = encodeURI(url);
                                                eUrl = eUrl.replace('+', '%2B');
                                                imD.attr("src", eUrl);
                                            });
                                    }

                                });
                            });
                        }
                    }
                });
            });
    }
});