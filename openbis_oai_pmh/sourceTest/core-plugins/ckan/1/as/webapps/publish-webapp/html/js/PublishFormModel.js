var PublishFormModel = Backbone.Model.extend({

	initialize : function() {
		this.setPublicationId(this.getExperimentCode())
	},

	setExperiment : function(experiment) {
		this.set("experiment", experiment, {
			validate : true
		});
	},

	getExperiment : function() {
		return this.get("experiment");
	},

	getExperimentCode : function() {
		var experiment = this.getExperiment();
		var regexp = /\/(.*)\/(.*)\/(.*)/g;
		var match = regexp.exec(experiment);
		return match[3];
	},

	setTag : function(tag) {
		this.set("tag", tag, {
			validate : true
		});
	},

	getTag : function() {
		return this.get("tag");
	},

	getTags : function(callback) {
		var thisModel = this;

		var tags = this.get("tags");
		if (tags) {
			return callback(tags);
		} else {
			this.getService().getTags(thisModel.getExperiment(), function(executeResult) {
				if (executeResult.isSuccessful()) {
					var result = executeResult.getResult();
					result.sort();
					thisModel.set("tags", result);
					callback(result);
				} else {
					thisModel.set('formMessages', executeResult.getMessages());
				}
			});
		}
	},

	setSpace : function(space) {
		this.set("space", space, {
			validate : true
		});
	},

	getSpace : function() {
		return this.get("space");
	},

	getSpaces : function(callback) {
		var thisModel = this;

		var spaces = this.get("spaces");
		if (spaces) {
			return callback(spaces);
		} else {
			this.getService().getSpaces(function(executeResult) {
				if (executeResult.isSuccessful()) {
					thisModel.set("spaces", executeResult.getResult());
					callback(executeResult.getResult());
				} else {
					thisModel.set('formMessages', executeResult.getMessages());
				}
			});
		}
	},

	setMeshTerms : function(meshTerms) {
		this.set("meshTerms", meshTerms, {
			validate : true
		});
	},

	getMeshTerms : function() {
		return this.get("meshTerms");
	},

	getMeshTermChildren : function(parent, filter, callback) {
		var thisModel = this;
		this.getService().getMeshTermChildren(parent, filter, function(executeResult) {
			if (executeResult.isSuccessful()) {
				callback(executeResult.getResult());
			} else {
				thisModel.set("formMessages", executeResult.getMessages());
			}
		});
	},

	getPublicationId : function() {
		return this.get("publicationId");
	},

	setPublicationId : function(publicationId) {
		this.set("publicationId", publicationId, {
			validate : true
		});
	},

	getTitle : function() {
		return this.get("title");
	},

	setTitle : function(title) {
		this.set("title", title, {
			validate : true
		});
	},

	getAuthor : function() {
		return this.get("author");
	},

	setAuthor : function(author) {
		this.set("author", author, {
			validate : true
		});
	},

	getAuthorEmail : function() {
		return this.get("authorEmail");
	},

	setAuthorEmail : function(authorEmail) {
		this.set("authorEmail", authorEmail, {
			validate : true
		});
	},

	getLicense : function() {
		return this.get("license");
	},

	setLicense : function(license) {
		this.set("license", license, {
			validate : true
		});
	},

	getLicenses : function(callback) {
		var thisModel = this;

		var licenses = this.get("licenses");
		if (licenses) {
			callback(licenses);
		} else {
			this.getService().getLicenses(function(executeResult) {
				if (executeResult.isSuccessful()) {
					thisModel.set("licenses", executeResult.getResult());
					callback(executeResult.getResult());
				} else {
					thisModel.set('formMessages', executeResult.getMessages());
				}
			});
		}
	},

	getNotes : function() {
		return this.get("notes");
	},

	setNotes : function(notes) {
		this.set("notes", notes, {
			validate : true
		});
	},

	isValidationEnabled : function() {
		return this.get("validationEnabled");
	},

	setValidationEnabled : function(enabled) {
		this.set("validationEnabled", enabled);
		if (enabled) {
			this.validate(this.attributes);
		}
	},

	isValid : function(fieldOrNull) {
		var error = this.getValidationError(fieldOrNull);
		return !error;
	},

	getValidationError : function(fieldOrNull) {
		var errors = this.get("errors");

		if (fieldOrNull) {
			return errors ? errors[fieldOrNull] : null;
		} else {
			return errors;
		}
	},

	validate : function(attributes) {
		if (!this.isValidationEnabled()) {
			return null;
		}

		var errors = {};

		var validateNotEmpty = function(name, label) {
			if (!attributes[name] || !attributes[name].trim()) {
				errors[name] = "Please enter " + label + ".";
				return false;
			}
			return true;
		}

		var validateChosen = function(name, label) {
			if (!attributes[name] || !attributes[name].trim()) {
				errors[name] = "Please choose " + label + ".";
				return false;
			}
			return true;
		}

		validateChosen("space", "a space");
		validateNotEmpty("publicationId", "a publication id");
		validateNotEmpty("title", "a title");
		validateNotEmpty("author", "an author");
		validateChosen("license", "a license");

		if (validateNotEmpty("authorEmail", "an email")) {
			var emailRegExp = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
			if (!emailRegExp.test(attributes["authorEmail"])) {
				errors["authorEmail"] = "Please enter a valid email.";
			}
		}

		var meshTerms = attributes["meshTerms"];
		if (!meshTerms || meshTerms.length == 0) {
			errors["meshTerms"] = "Please choose at least one mesh term.";
		} else if (meshTerms.length > 5) {
			errors["meshTerms"] = "Please choose 5 mesh terms at the most.";
		}

		if (_.isEmpty(errors)) {
			this.set("errors", null);
		} else {
			this.set("errors", errors);
		}

		return null;
	},

	publish : function() {
		var thisModel = this;

		this.setValidationEnabled(true)

		if (this.isValid()) {
			thisModel.set({
				"formMessages" : null,
				"submitDisabled" : true
			});

			var parameters = {
				"experiment" : thisModel.getExperiment(),
				"tag" : thisModel.getTag(),
				"space" : thisModel.getSpace(),
				"publicationId" : thisModel.getPublicationId(),
				"title" : thisModel.getTitle(),
				"author" : thisModel.getAuthor(),
				"authorEmail" : thisModel.getAuthorEmail(),
				"license" : thisModel.getLicense(),
				"notes" : thisModel.getNotes(),
				"meshTerms" : thisModel.getMeshTerms()
			};

			this.getService().publish(parameters, function(executeResult) {
				executeResult.addMessage("timestamp", new Date());
				thisModel.set({
					"formMessages" : executeResult.getMessages(),
					"submitDisabled" : false
				});
			});
		} else {
			var result = new OperationResult();
			result.addMessage("timestamp", new Date());
			result.addMessage("error", "Please fill in the form.")
			thisModel.set({
				"formMessages" : result.getMessages(),
			});
		}
	},

	getService : function() {
		if (!this.service) {
			this.service = new PublishFormService(this.get("facade"));
		}
		return this.service;
	}

});