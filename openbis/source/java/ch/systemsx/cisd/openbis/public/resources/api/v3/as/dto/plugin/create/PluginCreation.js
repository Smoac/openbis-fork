define([ "stjs" ], function(stjs) {
	var PluginCreation = function() {
	};
	stjs.extend(PluginCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.create.PluginCreation';
		constructor.serialVersionUID = 1;
		prototype.name = null;
		prototype.description = null;
		prototype.script = null;
		prototype.pluginType = null;
		prototype.scriptType = null;
		prototype.available = true;
		prototype.entityKinds = null;

		prototype.getName = function() {
			return this.name;
		};
		prototype.setName = function(name) {
			this.name = name;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getScript = function() {
			return this.script;
		};
		prototype.setScript = function(script) {
			this.script = script;
		};
		prototype.getPluginType = function() {
			return this.pluginType;
		};
		prototype.setPluginType = function(pluginType) {
			this.pluginType = pluginType;
		};
		prototype.getScriptType = function() {
			return this.scriptType;
		};
		prototype.setScriptType = function(scriptType) {
			this.scriptType = scriptType;
		};
		prototype.isAvailable = function() {
			return this.available;
		};
		prototype.setAvailable = function(available) {
			this.available = available;
		};
		prototype.getEntityKinds = function() {
			return this.entityKinds;
		};
		prototype.setEntityKinds = function(entityKinds) {
			this.entityKinds = entityKinds;
		};
	}, {
		pluginType : "PluginType",
		scriptType : "ScriptType",
		entityKinds : {
			name : "Set",
			arguments : [ "EntityKind" ]
		}
	});
	return PluginCreation;
})
