var SnakemakeTrigger = new function() {

    var groups;
    var propTypeCodeToLabelMap = {};
    var dataSetPaths;
    var $formContainerAll;
    var $formContainerGroups;
    var $dropdownGroupBy;
    var $dropdownGroup1;
    var $dropdownGroup2;
    var $button;
    var $model;

    this.paintTriggerContainers = function($container, model, experiment) {
        $model = model;
        SnakemakeTrigger.paintFormContainers($container);
        SnakemakeTrigger.paintProcessAll();
        SnakemakeTrigger.paintGroupBy(experiment);
        SnakemakeTrigger.paintGroupValues();
        groups = SnakemakeTrigger.getGroups(experiment);
        files = SnakemakeTrigger.getFiles(experiment);
    }

    this.paintFormContainers = function($container) {
        var $fieldsetAll = UiComponents.getFieldset("Process all comparisons");
        var $fieldsetGroups = UiComponents.getFieldset("Pick groups to compare");
        $formContainerAll = $('<div>').addClass('form-group');
        $formContainerGroups = $('<div>').addClass('form-group');
        $fieldsetAll.append($formContainerAll);
        $fieldsetGroups.append($formContainerGroups);
        $container.append($fieldsetAll).append($fieldsetGroups);
    }

    this.paintProcessAll = function() {
        var $buttonAll = UiComponents.getButton("Start processing", SnakemakeTrigger.startProcessingAll);
        $formContainerAll.append($buttonAll);
    }

    this.paintGroupBy = function(experiment) {
        var groupByOptions = [];
        if (experiment.samples.length > 0) {
            var sample = experiment.samples[0];
            for (var i = 0; i < sample.type.propertyAssignments.length; i++) {
                var propertyAssignment = sample.type.propertyAssignments[i];
                groupByOptions.push({
                    label: propertyAssignment.propertyType.label,
                    value: propertyAssignment.propertyType.code,
                });
                propTypeCodeToLabelMap[propertyAssignment.propertyType.code] = propertyAssignment.propertyType.label;
            }
        }

        $dropdownGroupBy = UiComponents.addDropdown("Group by", groupByOptions, $formContainerGroups, SnakemakeTrigger.paintGroupValues);
    }

    this.paintGroupValues = function(propertyType) {
        if (propertyType) {
            var groupOptions = groups[propertyType].map(function(value) { return {
                label: value,
                value: value,
            }});
        } else {
            var groupOptions = [];
        }

        if ($dropdownGroup1) {
            $dropdownGroup1.remove();
        }
        if ($dropdownGroup2) {
            $dropdownGroup2.remove();
        }
        if ($button) {
            $button.remove();
        }

        $dropdownGroup1 = UiComponents.addDropdown("Group 1", groupOptions, $formContainerGroups);
        $dropdownGroup2 = UiComponents.addDropdown("Group 2", groupOptions, $formContainerGroups);

        $button = UiComponents.getButton("Start processing", SnakemakeTrigger.startProcessing);
        $button.css("margin-top", "20px");
        $formContainerGroups.append($button);
    }

    this.getGroups = function(experiment) {
        var groups = {};
        for (var i = 0; i < experiment.samples.length; i++) {
            var sample = experiment.samples[i];
            // collect groups
            for (var propertyKey in sample.properties) {
                if (sample.properties.hasOwnProperty(propertyKey)) {
                    var propertyValue = sample.properties[propertyKey];
                    if (groups.hasOwnProperty(propertyKey) == false) {
                        groups[propertyKey] = [];
                    }
                    if (groups[propertyKey].indexOf(propertyValue) == -1) {
                        groups[propertyKey].push(propertyValue);
                    }
                }
            }
        }
        return groups;
    }

    this.getFiles = function(experiment) {
        dataSetPaths = [];
        for (var i = 0; i < experiment.dataSets.length; i++) {
            var dataSet = experiment.dataSets[i];
            if (dataSet.type.code == "FASTQ" || dataSet.type.code == "METADATA") {
                var path = dataSet.physicalData.shareId + "/" + dataSet.physicalData.location;
                dataSetPaths.push(path);
            }
        }
    }

    this.startProcessingAll = function() {
        UiComponents.startLoading();
        var jsonrpc = {
            "method": "start_processing_all",
            "params": {
                "data_set_paths": dataSetPaths
            }
        };
        BBBServerFacade.callCustomASService(jsonrpc, $model, function(result) {
            UiComponents.stopLoading();
            if (result.success) {
              alert("Processing started.");
            } else {
              alert("Error: " + result.data);
            }
        });
    }

    this.startProcessing = function() {
        var groupSelection = {
            groupBy: propTypeCodeToLabelMap[$dropdownGroupBy.getValue()],
            group1: $dropdownGroup1.getValue(),
            group2: $dropdownGroup2.getValue(),
        }
        if (!groupSelection.groupBy || !groupSelection.group1 || !groupSelection.group2) {
            alert("Select both groups first.");
            return;
        }
        UiComponents.startLoading();

        var jsonrpc = {
            "method": "start_processing",
            "params": {
                "group_selection": groupSelection,
                "data_set_paths": dataSetPaths
            }
        };
        BBBServerFacade.callCustomASService(jsonrpc, $model, function(result) {
            UiComponents.stopLoading();
            if (result.success) {
                alert("Processing started.");
            } else {
                alert("Error: " + result.data);
            }
        });
    }
}