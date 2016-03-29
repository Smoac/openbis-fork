define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/experiment/fetchoptions/ExperimentFetchOptions", "as/dto/sample/fetchoptions/SampleFetchOptions",
		"as/dto/dataset/fetchoptions/DataSetFetchOptions", "as/dto/material/fetchoptions/MaterialFetchOptions", "as/dto/global/fetchoptions/GlobalSearchObjectSortOptions" ], function(require, stjs,
		FetchOptions) {
	var GlobalSearchObjectFetchOptions = function() {
	};
	stjs.extend(GlobalSearchObjectFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.global.fetchoptions.GlobalSearchObjectFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.experiment = null;
		prototype.sample = null;
		prototype.dataSet = null;
		prototype.material = null;
		prototype.sort = null;

		prototype.withExperiment = function() {
			if (this.experiment == null) {
				var ExperimentFetchOptions = require("as/dto/experiment/fetchoptions/ExperimentFetchOptions");
				this.experiment = new ExperimentFetchOptions();
			}
			return this.experiment;
		};
		prototype.withExperimentUsing = function(fetchOptions) {
			return this.experiment = fetchOptions;
		};
		prototype.hasExperiment = function() {
			return this.experiment != null;
		};

		prototype.withSample = function() {
			if (this.sample == null) {
				var SampleFetchOptions = require("as/dto/sample/fetchoptions/SampleFetchOptions");
				this.sample = new SampleFetchOptions();
			}
			return this.sample;
		};
		prototype.withSampleUsing = function(fetchOptions) {
			return this.sample = fetchOptions;
		};
		prototype.hasSample = function() {
			return this.sample != null;
		};

		prototype.withDataSet = function() {
			if (this.dataSet == null) {
				var DataSetFetchOptions = require("as/dto/dataset/fetchoptions/DataSetFetchOptions");
				this.dataSet = new DataSetFetchOptions();
			}
			return this.dataSet;
		};
		prototype.withDataSetUsing = function(fetchOptions) {
			return this.dataSet = fetchOptions;
		};
		prototype.hasDataSet = function() {
			return this.dataSet != null;
		};

		prototype.withMaterial = function() {
			if (this.material == null) {
				var MaterialFetchOptions = require("as/dto/material/fetchoptions/MaterialFetchOptions");
				this.material = new MaterialFetchOptions();
			}
			return this.material;
		};
		prototype.withMaterialUsing = function(fetchOptions) {
			return this.material = fetchOptions;
		};
		prototype.hasMaterial = function() {
			return this.material != null;
		};

		prototype.sortBy = function() {
			if (this.sort == null) {
				var GlobalSearchObjectSortOptions = require("as/dto/global/fetchoptions/GlobalSearchObjectSortOptions");
				this.sort = new GlobalSearchObjectSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		experiment : "ExperimentFetchOptions",
		sample : "SampleFetchOptions",
		dataSet : "DataSetFetchOptions",
		material : "MaterialFetchOptions",
		sort : "GlobalSearchObjectSortOptions"
	});
	return GlobalSearchObjectFetchOptions;
})