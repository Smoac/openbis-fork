define([ 'jquery', 'util/Json', 'as/dto/datastore/search/DataStoreSearchCriteria', 'as/dto/datastore/fetchoptions/DataStoreFetchOptions', 'as/dto/common/search/SearchResult' ], function(jquery,
		stjsUtil, DataStoreSearchCriteria, DataStoreFetchOptions, SearchResult) {
	jquery.noConflict();

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

			var dfd = jquery.Deferred();
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

			jquery.ajax(settings).done(success).fail(error);

			return dfd.promise();
		};

		this.loginCommon = function(user, isAnonymousUser, response) {
			var thisPrivate = this;
			var dfd = jquery.Deferred();

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

	var dataStoreFacade = function(facade, dataStoreCodes) {

		this._getDataStoreUrls = function() {
			if (this._dataStoreUrls) {
				var dfd = jquery.Deferred();
				dfd.resolve(this._dataStoreUrls);
				return dfd.promise();
			} else {
				var thisFacade = this;
				var criteria = new DataStoreSearchCriteria();
				criteria.withOrOperator();

				for (var i = 0; i < dataStoreCodes.length; i++) {
					criteria.withCode().thatEquals(dataStoreCodes[i]);
				}

				return facade.searchDataStores(criteria, new DataStoreFetchOptions()).then(function(results) {
					var dataStores = results.getObjects();
					var dfd = jquery.Deferred();

					if (dataStores && dataStores.length > 0) {
						var dataStoreUrls = dataStores.map(function(dataStore) {
							return dataStore.downloadUrl + "/datastore_server/rmi-data-store-server-v3.json";
						});
						thisFacade._dataStoreUrls = dataStoreUrls;
						dfd.resolve(dataStoreUrls);
					} else {
						if (dataStoreCodes.length > 0) {
							dfd.reject("No data stores found for codes: " + dataStoreCodes);
						} else {
							dfd.reject("No data stores found");
						}
					}

					return dfd.promise();
				});
			}
		}

		this.searchFiles = function(criteria, fetchOptions) {
			return this._getDataStoreUrls().then(function(dataStoreUrls) {
				var promises = dataStoreUrls.map(function(dataStoreUrl) {
					return facade._private.ajaxRequest({
						url : dataStoreUrl,
						data : {
							"method" : "searchFiles",
							"params" : [ facade._private.sessionToken, criteria, fetchOptions ]
						},
						returnType : "SearchResult"
					});
				});

				return jquery.when.apply(jquery, promises).then(function() {
					var objects = [];
					var totalCount = 0;

					for (var i = 0; i < arguments.length; i++) {
						var result = arguments[i];

						if (result.getObjects()) {
							Array.prototype.push.apply(objects, result.getObjects());
						}
						if (result.getTotalCount()) {
							totalCount += result.getTotalCount();
						}
					}

					var combinedResult = new SearchResult();
					combinedResult.setObjects(objects);
					combinedResult.setTotalCount(totalCount);
					return combinedResult;
				});
			});
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

		this.loginFromContext = function() {
			this._private.sessionToken = this.getWebAppContext().getSessionId();
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

		this.getSessionInformation = function() {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getSessionInformation",
					"params" : [ thisFacade._private.sessionToken ]
				},
				returnType : "SessionInformation"
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

		this.createExperimentTypes = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createExperimentTypes",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "EntityTypePermId" ]
				}
			});
		}

		this.createExternalDms = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createExternalDataManagementSystems",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "ExternalDmsPermId" ]
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

		this.createSampleTypes = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createSampleTypes",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "EntityTypePermId" ]
				}
			});
		}

		this.createDataSetTypes = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createDataSetTypes",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "EntityTypePermId" ]
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

		this.createMaterialTypes = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createMaterialTypes",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "EntityTypePermId" ]
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

		this.createTags = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createTags",
					"params" : [ thisFacade._private.sessionToken, creations ]
				},
				returnType : {
					name : "List",
					arguments : [ "TagPermId" ]
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

		this.updateExternalDataManagementSystems = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateExternalDataManagementSystems",
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

		this.updateTags = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateTags",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateOperationExecutions = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateOperationExecutions",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.getSpaces = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getSpaces",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "ISpaceId", "Space" ]
				}
			});
		}

		this.getProjects = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getProjects",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IProjectId", "Project" ]
				}
			});
		}

		this.getExperiments = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getExperiments",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IExperimentId", "Experiment" ]
				}
			});
		}

		this.getSamples = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getSamples",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "ISampleId", "Sample" ]
				}
			});
		}

		this.getDataSets = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getDataSets",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IDataSetId", "DataSet" ]
				}
			});
		}

		this.getMaterials = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getMaterials",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IMaterialId", "Material" ]
				}
			});
		}

		this.getVocabularyTerms = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getVocabularyTerms",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IVocabularyTermId", "VocabularyTerm" ]
				}
			});
		}

		this.getTags = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getTags",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "ITagId", "Tag" ]
				}
			});
		}

		this.getExternalDataManagementSystems = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getExternalDataManagementSystems",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IExternalDmsId", "ExternalDms" ]
				}
			});
		}

		this.getOperationExecutions = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getOperationExecutions",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				},
				returnType : {
					name : "Map",
					arguments : [ "IOperationExecutionId", "OperationExecution" ]
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

		this.searchExternalDataManagementSystems = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchExternalDataManagementSystems",
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

		this.searchTags = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchTags",
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

		this.searchOperationExecutions = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchOperationExecutions",
					"params" : [ thisFacade._private.sessionToken, criteria, fetchOptions ]
				},
				returnType : "SearchResult"
			});
		}

		this.searchDataStores = function(criteria, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchDataStores",
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

		this.deleteExternalDataManagementSystems = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteExternalDataManagementSystems",
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

		this.deleteTags = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteTags",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteOperationExecutions = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteOperationExecutions",
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

		this.archiveDataSets = function(ids, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "archiveDataSets",
					"params" : [ thisFacade._private.sessionToken, ids, options ]
				}
			});
		}

		this.unarchiveDataSets = function(ids, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "unarchiveDataSets",
					"params" : [ thisFacade._private.sessionToken, ids, options ]
				}
			});
		}

		this.executeOperations = function(operations, options) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "executeOperations",
					"params" : [ thisFacade._private.sessionToken, operations, options ]
				}
			});
		}

		this.getServerInformation = function() {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getServerInformation",
					"params" : [ thisFacade._private.sessionToken ]
				}
			});
		}

		this.getDataStoreFacade = function() {
			var dataStoreCodes = [];
			for (var i = 0; i < arguments.length; i++) {
				dataStoreCodes.push(arguments[i]);
			}
			return new dataStoreFacade(this, dataStoreCodes);
		}

		this.getMajorVersion = function() {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getMajorVersion",
					"params" : []
				}
			})
		}

		this.getMinorVersion = function() {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "getMinorVersion",
					"params" : []
				}
			})
		}

		/**
		 * ======================= 
		 * OpenBIS webapp context
		 * =======================
		 * 
		 * Provides a context information for webapps that are embedded inside
		 * the OpenBIS UI.
		 * 
		 * @class
		 * 
		 */
		var openbisWebAppContext = function() {
			this.getWebAppParameter = function(parameterName) {
				var match = location.search.match(RegExp("[?|&]" + parameterName + '=(.+?)(&|$)'));
				if (match && match[1]) {
					return decodeURIComponent(match[1].replace(/\+/g, ' '));
				} else {
					return null;
				}
			}

			this.webappCode = this.getWebAppParameter("webapp-code");
			this.sessionId = this.getWebAppParameter("session-id");
			this.entityKind = this.getWebAppParameter("entity-kind");
			this.entityType = this.getWebAppParameter("entity-type");
			this.entityIdentifier = this.getWebAppParameter("entity-identifier");
			this.entityPermId = this.getWebAppParameter("entity-perm-id");

			this.getWebappCode = function() {
				return this.webappCode;
			}

			this.getSessionId = function() {
				return this.sessionId;
			}

			this.getEntityKind = function() {
				return this.entityKind;
			}

			this.getEntityType = function() {
				return this.entityType;
			}

			this.getEntityIdentifier = function() {
				return this.entityIdentifier;
			}

			this.getEntityPermId = function() {
				return this.entityPermId;
			}

			this.getParameter = function(parameterName) {
				return this.getParameter(parameterName);
			}
		}

		this.getWebAppContext = function() {
			return new openbisWebAppContext();
		}
	}

	return facade;

});