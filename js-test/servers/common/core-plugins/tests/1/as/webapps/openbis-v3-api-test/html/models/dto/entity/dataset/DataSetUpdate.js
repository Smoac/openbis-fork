/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/entity/FieldUpdateValue", "dto/entity/IdListUpdateValue" ], function(stjs, FieldUpdateValue, IdListUpdateValue) {
	var DataSetUpdate = function() {
		this.experimentId = new FieldUpdateValue();
		this.sampleId = new FieldUpdateValue();
		this.externalData = new FieldUpdateValue();
		this.properties = {};
		this.tagIds = new IdListUpdateValue();
		this.containerIds = new IdListUpdateValue();
		this.containedIds = new IdListUpdateValue();
		this.parentIds = new IdListUpdateValue();
		this.childIds = new IdListUpdateValue();
	};
	stjs.extend(DataSetUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.dataset.DataSetUpdate';
		constructor.serialVersionUID = 1;
		prototype.dataSetId = null;

		prototype.getDataSetId = function() {
			return this.dataSetId;
		};
		prototype.setDataSetId = function(dataSetId) {
			this.dataSetId = dataSetId;
		};
		prototype.getExperimentId = function() {
			return this.experimentId;
		};
		prototype.setExperimentId = function(experimentId) {
			this.experimentId.setValue(experimentId);
		};
		prototype.getSampleId = function() {
			return this.sampleId;
		};
		prototype.setSampleId = function(sampleId) {
			this.sampleId.setValue(sampleId);
		};
		prototype.getExternalData = function() {
			return this.externalData;
		};
		prototype.setExternalData = function(externalData) {
			this.externalData.setValue(externalData);
		};
		prototype.setProperty = function(key, value) {
			this.properties[key] = value;
		};
		prototype.getProperties = function() {
			return this.properties;
		};
		prototype.getTagIds = function() {
			return this.tagIds;
		};
		prototype.setTagActions = function(actions) {
			this.tagIds.setActions(actions);
		};
		prototype.getContainerIds = function() {
			return this.containerIds;
		};
		prototype.setContainerActions = function(actions) {
			this.containerIds.setActions(actions);
		};
		prototype.getContainedIds = function() {
			return this.containedIds;
		};
		prototype.setContainedActions = function(actions) {
			this.containedIds.setActions(actions);
		};
		prototype.getParentIds = function() {
			return this.parentIds;
		};
		prototype.setParentActions = function(actions) {
			this.parentIds.setActions(actions);
		};
		prototype.getChildIds = function() {
			return this.childIds;
		};
		prototype.setChildActions = function(actions) {
			this.childIds.setActions(actions);
		};
	}, {
		dataSetId : "IDataSetId",
		experimentId : {
			name : "FieldUpdateValue",
			arguments : [ "IExperimentId" ]
		},
		sampleId : {
			name : "FieldUpdateValue",
			arguments : [ "ISampleId" ]
		},
		externalData : {
			name : "FieldUpdateValue",
			arguments : [ "ExternalDataUpdate" ]
		},
		properties : {
			name : "Map",
			arguments : [ null, null ]
		},
		tagIds : {
			name : "IdListUpdateValue",
			arguments : [ "ITagId" ]
		},
		containerIds : {
			name : "IdListUpdateValue",
			arguments : [ "IDataSetId" ]
		},
		containedIds : {
			name : "IdListUpdateValue",
			arguments : [ "IDataSetId" ]
		},
		parentIds : {
			name : "IdListUpdateValue",
			arguments : [ "IDataSetId" ]
		},
		childIds : {
			name : "IdListUpdateValue",
			arguments : [ "IDataSetId" ]
		}
	});
	return DataSetUpdate;
})