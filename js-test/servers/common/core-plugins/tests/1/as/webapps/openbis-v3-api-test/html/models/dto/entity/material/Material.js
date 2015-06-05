/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "support/stjs", "sys/exceptions" ], function(stjs, exceptions) {
	var Material = function() {
	};
	stjs.extend(Material, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.material.Material';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.code = null;
		prototype.type = null;
		prototype.history = null;
		prototype.registrationDate = null;
		prototype.registrator = null;
		prototype.modificationDate = null;
		prototype.properties = null;
		prototype.materialProperties = null;
		prototype.tags = null;
		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getType = function() {
			if (this.getFetchOptions().hasType()) {
				return this.type;
			} else {
				throw new exceptions.NotFetchedException("Material type has not been fetched.");
			}
		};
		prototype.setType = function(type) {
			this.type = type;
		};
		prototype.getHistory = function() {
			if (this.getFetchOptions().hasHistory()) {
				return this.history;
			} else {
				throw new exceptions.NotFetchedException("History has not been fetched.");
			}
		};
		prototype.setHistory = function(history) {
			this.history = history;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.getRegistrator = function() {
			if (this.getFetchOptions().hasRegistrator()) {
				return this.registrator;
			} else {
				throw new exceptions.NotFetchedException("Registrator has not been fetched.");
			}
		};
		prototype.setRegistrator = function(registrator) {
			this.registrator = registrator;
		};
		prototype.getModificationDate = function() {
			return this.modificationDate;
		};
		prototype.setModificationDate = function(modificationDate) {
			this.modificationDate = modificationDate;
		};
		prototype.getProperties = function() {
			if (this.getFetchOptions().hasProperties()) {
				return this.properties;
			} else {
				throw new exceptions.NotFetchedException("Properties has not been fetched.");
			}
		};
		prototype.setProperties = function(properties) {
			this.properties = properties;
		};
		prototype.getMaterialProperties = function() {
			if (this.getFetchOptions().hasMaterialProperties()) {
				return this.materialProperties;
			} else {
				throw new exceptions.NotFetchedException("Material properties has not been fetched.");
			}
		};
		prototype.setMaterialProperties = function(materialProperties) {
			this.materialProperties = materialProperties;
		};
		prototype.getTags = function() {
			if (this.getFetchOptions().hasTags()) {
				return this.tags;
			} else {
				throw new exceptions.NotFetchedException("Tags has not been fetched.");
			}
		};
		prototype.setTags = function(tags) {
			this.tags = tags;
		};
	}, {
		fetchOptions : "MaterialFetchOptions",
		permId : "MaterialPermId",
		type : "MaterialType",
		history : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		registrationDate : "Date",
		registrator : "Person",
		modificationDate : "Date",
		properties : {
			name : "Map",
			arguments : [ null, null ]
		},
		materialProperties : {
			name : "Map",
			arguments : [ null, "Material" ]
		},
		tags : {
			name : "Set",
			arguments : [ "Tag" ]
		}
	});
	return Material;
})