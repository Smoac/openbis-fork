define([ 'jquery', 'util/Json' ], function($, stjsUtil) {

	var __private = function() {

		this.ajaxRequest = function(settings) {
			var thisPrivate = this;

			settings.type = "POST";
			settings.processData = false;
			settings.dataType = "json";

			var returnType = settings.returnType;
			if (returnType) {
				delete settings.returnType;
			}

			var data = settings.data;
			data["id"] = "1";
			data["jsonrpc"] = "2.0";

			// decycle each parameter separately (jackson does not recognize
			// object ids across different parameters)

			if (data.params && data.params.length > 0) {
				var newParams = [];
				data.params.forEach(function(param) {
					var newParam = stjsUtil.decycle(param);
					newParams.push(newParam);
				});
				data.params = newParams;
			}

			settings.data = JSON.stringify(data);

			var originalSuccess = settings.success || function() {
			};
			var originalError = settings.error || function() {
			};

			var dfd = $.Deferred();
			function success(response) {
				if (response.error) {
					thisPrivate.log("Request failed - data: " + JSON.stringify(settings.data) + ", error: " + JSON.stringify(response.error));
					originalError(response.error);
					dfd.reject(response.error);
				} else {
					thisPrivate.log("Request succeeded - data: " + JSON.stringify(settings.data));
					stjsUtil.fromJson(returnType, response.result).done(function(dtos) {
						originalSuccess(dtos);
						dfd.resolve(dtos);
					}).fail(function() {
						originalError(arguments);
						dfd.reject(arguments);
					});
				}
			}

			function error(xhr, status, error) {
				thisPrivate.log("Request failed - data: " + JSON.stringify(settings.data) + ", error: " + JSON.stringify(error));
				originalError(error);
				dfd.reject(error);
			}

			$.ajax(settings).done(success).fail(error);

			return dfd.promise();
		};

		this.loginCommon = function(user, isAnonymousUser, response) {
			var thisPrivate = this;
			var dfd = $.Deferred();

			response.done(function(sessionToken) {
				if (sessionToken && (isAnonymousUser || sessionToken.indexOf(user) > -1)) {
					thisPrivate.sessionToken = sessionToken;
					dfd.resolve(sessionToken);
				} else {
					dfd.reject();
				}
			}).fail(function() {
				dfd.reject();
			});
			return dfd.promise();
		};

		this.log = function(msg) {
			if (console) {
				console.log(msg);
			}
		}
	}

	var facade = function(openbisUrl) {

		if (!openbisUrl) {
			openbisUrl = "/openbis/openbis/rmi-application-server-v3.json";
		}

		this._private = new __private();

		this.login = function(user, password) {
			var thisFacade = this;
			return thisFacade._private.loginCommon(user, false, thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "login",
					"params" : [ user, password ]
				}
			}));
		}

		this.loginAs = function(user, password, asUserId) {
			var thisFacade = this;
			return thisFacade._private.loginCommon(asUserId, false, thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "loginAs",
					"params" : [ user, password, asUserId ]
				}
			}));
		}

		this.loginAsAnonymousUser = function() {
			var thisFacade = this;
			return thisFacade._private.loginCommon(null, true, thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "loginAsAnonymousUser",
					"params" : []
				}
			}));
		}

		this.logout = function() {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "logout",
					"params" : [ thisFacade._private.sessionToken ]
				}
			}).done(function() {
				thisFacade._private.sessionToken = null;
			});
		}

		this.createSpaces = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createSpaces",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "SpacePermId" ]
				}
			});
		}

		this.createProjects = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createProjects",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "ProjectPermId" ]
				}
			});
		}

		this.createExperiments = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createExperiments",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "ExperimentPermId" ]
				}
			});
		}

		this.createSamples = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createSamples",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "SamplePermId" ]
				}
			});
		}

		this.createMaterials = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createMaterials",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "MaterialPermId" ]
				}
			});
		}

		this.createVocabularyTerms = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createVocabularyTerms",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "VocabularyTermPermId" ]
				}
			});
		}

		this.updateSpaces = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateSpaces",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateProjects = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateProjects",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateExperiments = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateExperiments",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateSamples = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateSamples",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateDataSets = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateDataSets",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateMaterials = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateMaterials",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateVocabularyTerms = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateVocabularyTerms",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.mapSpaces = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapSpaces",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "ISpaceId", "Space" ]
				}
			});
		}

		this.mapProjects = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapProjects",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IProjectId", "Project" ]
				}
			});
		}

		this.mapExperiments = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapExperiments",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IExperimentId", "Experiment" ]
				}
			});
		}

		this.mapSamples = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapSamples",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "ISampleId", "Sample" ]
				}
			});
		}

		this.mapDataSets = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapDataSets",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IDataSetId", "DataSet" ]
				}
			});
		}

		this.mapMaterials = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapMaterials",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IMaterialId", "Material" ]
				}
			});
		}

		this.mapVocabularyTerms = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapVocabularyTerms",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IVocabularyTermId", "VocabularyTerm" ]
				}
			});
		}

		this.searchSpaces = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchSpaces",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchProjects = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchProjects",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchExperiments = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchExperiments",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			})
		}

		this.searchExperimentTypes = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchExperimentTypes",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			})
		}
		
		this.searchSamples = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchSamples",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchSampleTypes = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchSampleTypes",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}
		
		this.searchDataSets = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchDataSets",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchDataSetTypes = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchDataSetTypes",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}
		
		this.searchMaterials = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchMaterials",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchMaterialTypes = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchMaterialTypes",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchVocabularyTerms = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchVocabularyTerms",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchCustomASServices = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchCustomASServices",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchObjectKindModifications = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchObjectKindModifications",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchGlobally = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchGlobally",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.deleteSpaces = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteSpaces",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteProjects = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteProjects",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteExperiments = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteExperiments",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				},
				returnType : "IDeletionId"
			});
		}

		this.deleteSamples = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteSamples",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				},
				returnType : "IDeletionId"
			});
		}

		this.deleteDataSets = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteDataSets",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				},
				returnType : "IDeletionId"
			});
		}

		this.deleteMaterials = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteMaterials",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteVocabularyTerms = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteVocabularyTerms",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.searchDeletions = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchDeletions",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : {
					name : "List",
					arguments : [ "Deletion" ]
				}
			});
		}

		this.revertDeletions = function(ids) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "revertDeletions",
					"params" : [ thisFacade._private.sessionToken, ids ]
				}
			});
		}

		this.confirmDeletions = function(ids) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "confirmDeletions",
					"params" : [ thisFacade._private.sessionToken, ids ]
				}
			});
		}

		this.executeCustomASService = function(serviceId, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "executeCustomASService",
					"params" : [ thisFacade._private.sessionToken, serviceId, options ]
				}
			});
		}
	}

	return facade;

});