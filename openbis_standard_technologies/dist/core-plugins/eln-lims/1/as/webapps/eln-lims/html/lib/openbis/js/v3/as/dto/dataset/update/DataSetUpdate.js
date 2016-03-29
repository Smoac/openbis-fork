/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue", "as/dto/common/update/IdListUpdateValue" ], function(stjs, FieldUpdateValue, IdListUpdateValue) {
	var DataSetUpdate = function() {
		this.experimentId = new FieldUpdateValue();
		this.sampleId = new FieldUpdateValue();
		this.physicalData = new FieldUpdateValue();
		this.linkedData = new FieldUpdateValue();
		this.properties = {};
		this.tagIds = new IdListUpdateValue();
		this.containerIds = new IdListUpdateValue();
		this.componentIds = new IdListUpdateValue();
		this.parentIds = new IdListUpdateValue();
		this.childIds = new IdListUpdateValue();
	};
	stjs.extend(DataSetUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.update.DataSetUpdate';
		constructor.serialVersionUID = 1;
		prototype.dataSetId = null;
		prototype.experimentId = null;
		prototype.sampleId = null;
		prototype.physicalData = null;
		prototype.linkedData = null;
		prototype.properties = null;
		prototype.tagIds = null;
		prototype.containerIds = null;
		prototype.componentIds = null;
		prototype.parentIds = null;
		prototype.childIds = null;

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
		prototype.getPhysicalData = function() {
			return this.physicalData;
		};
		prototype.setPhysicalData = function(physicalData) {
			this.physicalData.setValue(physicalData);
		};
		prototype.getLinkedData = function() {
			return this.linkedData;
		};
		prototype.setLinkedData = function(linkedData) {
			this.linkedData.setValue(linkedData);
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
		prototype.getComponentIds = function() {
			return this.componentIds;
		};
		prototype.setComponentActions = function(actions) {
			this.componentIds.setActions(actions);
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
		physicalData : {
			name : "FieldUpdateValue",
			arguments : [ "PhysicalDataUpdate" ]
		},
		linkedData : {
			name : "FieldUpdateValue",
			arguments : [ "LinkedDataUpdate" ]
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
		componentIds : {
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