define([ 'jquery', 'openbis', 'underscore', 'test/dtos' ], function($, defaultOpenbis, _, dtos) {

	/*
	 * These tests should be run against openBIS instance with screening sprint
	 * server database version
	 */

	var testProtocol = window.location.protocol;
	var testHost = window.location.hostname;
	var testPort = window.location.port;
	var testUrl = testProtocol + "//" + testHost + ":" + testPort;
	var testApiUrl = testUrl + "/openbis/openbis/rmi-application-server-v3.json";

	var testUserId = "openbis_test_js";
	var testUserPassword = "password";

	var Common = function(assert, openbis) {
		this.assert = assert;

		if (!openbis) {
			openbis = defaultOpenbis;
		}

		this.SpaceCreation = dtos.SpaceCreation;
		this.ProjectCreation = dtos.ProjectCreation;
		this.ExperimentCreation = dtos.ExperimentCreation;
		this.SampleCreation = dtos.SampleCreation;
		this.MaterialCreation = dtos.MaterialCreation;
		this.AttachmentCreation = dtos.AttachmentCreation;
		this.VocabularyTermCreation = dtos.VocabularyTermCreation;
		this.TagCreation = dtos.TagCreation;
		this.DataSetCreation= dtos.DataSetCreation;
		this.FullDataSetCreation = dtos.FullDataSetCreation;
		this.DataSetFileCreation = dtos.DataSetFileCreation;
		this.LinkedDataCreation = dtos.LinkedDataCreation;
		this.ContentCopyCreation= dtos.ContentCopyCreation;
		this.ExternalDmsCreation= dtos.ExternalDmsCreation;
		this.ExternalDmsAddressType= require('as/dto/externaldms/ExternalDmsAddressType');
		this.SpaceUpdate = dtos.SpaceUpdate;
		this.ProjectUpdate = dtos.ProjectUpdate;
		this.ExperimentUpdate = dtos.ExperimentUpdate;
		this.SampleUpdate = dtos.SampleUpdate;
		this.DataSetUpdate = dtos.DataSetUpdate;
		this.PhysicalDataUpdate = dtos.PhysicalDataUpdate;
		this.LinkedDataUpdate = dtos.LinkedDataUpdate;
		this.ContentCopyListUpdateValue = dtos.ContentCopyListUpdateValue;
		this.DataStorePermId = dtos.DataStorePermId;
		this.MaterialUpdate = dtos.MaterialUpdate;
		this.VocabularyTermUpdate = dtos.VocabularyTermUpdate;
		this.ExternalDmsUpdate = dtos.ExternalDmsUpdate;
		this.TagUpdate = dtos.TagUpdate;
		this.SpaceDeletionOptions = dtos.SpaceDeletionOptions;
		this.ProjectDeletionOptions = dtos.ProjectDeletionOptions;
		this.ExperimentDeletionOptions = dtos.ExperimentDeletionOptions;
		this.SampleDeletionOptions = dtos.SampleDeletionOptions;
		this.DataSetDeletionOptions = dtos.DataSetDeletionOptions;
		this.MaterialDeletionOptions = dtos.MaterialDeletionOptions;
		this.VocabularyTermDeletionOptions = dtos.VocabularyTermDeletionOptions;
		this.ExternalDmsDeletionOptions = dtos.ExternalDmsDeletionOptions;
		this.TagDeletionOptions = dtos.TagDeletionOptions;
		this.EntityTypePermId = dtos.EntityTypePermId;
		this.SpacePermId = dtos.SpacePermId;
		this.ProjectPermId = dtos.ProjectPermId;
		this.ProjectIdentifier = dtos.ProjectIdentifier;
		this.ExperimentPermId = dtos.ExperimentPermId;
		this.ExperimentIdentifier = dtos.ExperimentIdentifier;
		this.SamplePermId = dtos.SamplePermId;
		this.SampleIdentifier = dtos.SampleIdentifier;
		this.DataSetPermId = dtos.DataSetPermId;
		this.FileFormatTypePermId = dtos.FileFormatTypePermId;
		this.MaterialPermId = dtos.MaterialPermId;
		this.ContentCopyPermId = dtos.ContentCopyPermId;
		this.ExternalDmsPermId = dtos.ExternalDmsPermId;
		this.VocabularyPermId = dtos.VocabularyPermId;
		this.VocabularyTermPermId = dtos.VocabularyTermPermId;
		this.TagPermId = dtos.TagPermId;
		this.TagCode = dtos.TagCode;
		this.SpaceSearchCriteria = dtos.SpaceSearchCriteria;
		this.ProjectSearchCriteria = dtos.ProjectSearchCriteria;
		this.ExperimentSearchCriteria = dtos.ExperimentSearchCriteria;
		this.ExperimentTypeSearchCriteria = dtos.ExperimentTypeSearchCriteria;
		this.SampleSearchCriteria = dtos.SampleSearchCriteria;
		this.SampleTypeSearchCriteria = dtos.SampleTypeSearchCriteria;
		this.DataSetSearchCriteria = dtos.DataSetSearchCriteria;
		this.DataSetTypeSearchCriteria = dtos.DataSetTypeSearchCriteria;
		this.MaterialSearchCriteria = dtos.MaterialSearchCriteria;
		this.MaterialTypeSearchCriteria = dtos.MaterialTypeSearchCriteria;
		this.ExternalDmsSearchCriteria = dtos.ExternalDmsSearchCriteria;
		this.VocabularyTermSearchCriteria = dtos.VocabularyTermSearchCriteria;
		this.DataSetFileSearchCriteria = dtos.DataSetFileSearchCriteria;
		this.TagSearchCriteria = dtos.TagSearchCriteria;
		this.DataStoreSearchCriteria = dtos.DataStoreSearchCriteria;
		this.SpaceFetchOptions = dtos.SpaceFetchOptions;
		this.ProjectFetchOptions = dtos.ProjectFetchOptions;
		this.ExperimentFetchOptions = dtos.ExperimentFetchOptions;
		this.ExperimentTypeFetchOptions = dtos.ExperimentTypeFetchOptions;
		this.SampleFetchOptions = dtos.SampleFetchOptions;
		this.SampleTypeFetchOptions = dtos.SampleTypeFetchOptions;
		this.DataSetFetchOptions = dtos.DataSetFetchOptions;
		this.DataSetTypeFetchOptions = dtos.DataSetTypeFetchOptions;
		this.MaterialFetchOptions = dtos.MaterialFetchOptions;
		this.MaterialTypeFetchOptions = dtos.MaterialTypeFetchOptions;
		this.ExternalDmsFetchOptions = dtos.ExternalDmsFetchOptions;
		this.VocabularyTermFetchOptions = dtos.VocabularyTermFetchOptions;
		this.TagFetchOptions = dtos.TagFetchOptions;
		this.DeletionFetchOptions = dtos.DeletionFetchOptions;
		this.DeletionSearchCriteria = dtos.DeletionSearchCriteria;
		this.CustomASServiceSearchCriteria = dtos.CustomASServiceSearchCriteria;
		this.CustomASServiceFetchOptions = dtos.CustomASServiceFetchOptions;
		this.CustomASServiceCode = dtos.CustomASServiceCode;
		this.CustomASServiceExecutionOptions = dtos.CustomASServiceExecutionOptions;
		this.GlobalSearchCriteria = dtos.GlobalSearchCriteria;
		this.GlobalSearchObjectFetchOptions = dtos.GlobalSearchObjectFetchOptions;
		this.ObjectKindModificationSearchCriteria = dtos.ObjectKindModificationSearchCriteria;
		this.ObjectKindModificationFetchOptions = dtos.ObjectKindModificationFetchOptions;
		this.DataSetArchiveOptions = dtos.DataSetArchiveOptions;
		this.DataSetUnarchiveOptions = dtos.DataSetUnarchiveOptions;
		this.PropertyAssignmentCreation = dtos.PropertyAssignmentCreation;
		this.PropertyTypePermId = dtos.PropertyTypePermId;
		this.PluginPermId = dtos.PluginPermId;
		this.ExperimentTypeCreation = dtos.ExperimentTypeCreation;
		this.SampleTypeCreation = dtos.SampleTypeCreation;
		this.DataSetTypeCreation = dtos.DataSetTypeCreation;
		this.MaterialTypeCreation = dtos.MaterialTypeCreation;

		// operations

		this.GetSessionInformationOperation = dtos.GetSessionInformationOperation;
		this.GetSpacesOperation = dtos.GetSpacesOperation;
		this.GetProjectsOperation = dtos.GetProjectsOperation;
		this.GetExperimentsOperation = dtos.GetExperimentsOperation;
		this.GetSamplesOperation = dtos.GetSamplesOperation;
		this.GetDataSetsOperation = dtos.GetDataSetsOperation;
		this.GetMaterialsOperation = dtos.GetMaterialsOperation;
		this.GetVocabularyTermsOperation = dtos.GetVocabularyTermsOperation;
		this.GetTagsOperation = dtos.GetTagsOperation;
		this.GetExternalDmsOperation = dtos.GetExternalDmsOperation;

		this.CreateSpacesOperation = dtos.CreateSpacesOperation;
		this.CreateProjectsOperation = dtos.CreateProjectsOperation;
		this.CreateExperimentsOperation = dtos.CreateExperimentsOperation;
		this.CreateExperimentTypesOperation = dtos.CreateExperimentTypesOperation;
		this.CreateSamplesOperation = dtos.CreateSamplesOperation;
		this.CreateSampleTypesOperation = dtos.CreateSampleTypesOperation;
		this.CreateDataSetsOperation = dtos.CreateDataSetsOperation;
		this.CreateDataSetTypesOperation = dtos.CreateDataSetTypesOperation;
		this.CreateMaterialsOperation = dtos.CreateMaterialsOperation;
		this.CreateMaterialTypesOperation = dtos.CreateMaterialTypesOperation;
		this.CreateVocabularyTermsOperation = dtos.CreateVocabularyTermsOperation;
		this.CreateTagsOperation = dtos.CreateTagsOperation;
		this.CreateExternalDmsOperation = dtos.CreateExternalDmsOperation;

		this.UpdateSpacesOperation = dtos.UpdateSpacesOperation;
		this.UpdateProjectsOperation = dtos.UpdateProjectsOperation;
		this.UpdateExperimentsOperation = dtos.UpdateExperimentsOperation;
		this.UpdateSamplesOperation = dtos.UpdateSamplesOperation;
		this.UpdateDataSetsOperation = dtos.UpdateDataSetsOperation;
		this.UpdateMaterialsOperation = dtos.UpdateMaterialsOperation;
		this.UpdateVocabularyTermsOperation = dtos.UpdateVocabularyTermsOperation;
		this.UpdateExternalDmsOperation = dtos.UpdateExternalDmsOperation;
		this.UpdateTagsOperation = dtos.UpdateTagsOperation;
		this.UpdateOperationExecutionsOperation = dtos.UpdateOperationExecutionsOperation;

		this.GetSpacesOperation = dtos.GetSpacesOperation;
		this.GetProjectsOperation = dtos.GetProjectsOperation;
		this.GetExperimentsOperation = dtos.GetExperimentsOperation;
		this.GetSamplesOperation = dtos.GetSamplesOperation;
		this.GetDataSetsOperation = dtos.GetDataSetsOperation;
		this.GetMaterialsOperation = dtos.GetMaterialsOperation;
		this.GetVocabularyTermsOperation = dtos.GetVocabularyTermsOperation;
		this.GetTagsOperation = dtos.GetTagsOperation;
		this.GetOperationExecutionsOperation = dtos.GetOperationExecutionsOperation;

		this.SearchSpacesOperation = dtos.SearchSpacesOperation;
		this.SearchProjectsOperation = dtos.SearchProjectsOperation;
		this.SearchExperimentsOperation = dtos.SearchExperimentsOperation;
		this.SearchExperimentTypesOperation = dtos.SearchExperimentTypesOperation;
		this.SearchSamplesOperation = dtos.SearchSamplesOperation;
		this.SearchSampleTypesOperation = dtos.SearchSampleTypesOperation;
		this.SearchDataSetsOperation = dtos.SearchDataSetsOperation;
		this.SearchDataSetTypesOperation = dtos.SearchDataSetTypesOperation;
		this.SearchMaterialsOperation = dtos.SearchMaterialsOperation;
		this.SearchMaterialTypesOperation = dtos.SearchMaterialTypesOperation;
		this.SearchVocabularyTermsOperation = dtos.SearchVocabularyTermsOperation;
		this.SearchExternalDmsOperation = dtos.SearchExternalDmsOperation;
		this.SearchTagsOperation = dtos.SearchTagsOperation;
		this.SearchCustomASServicesOperation = dtos.SearchCustomASServicesOperation;
		this.SearchObjectKindModificationsOperation = dtos.SearchObjectKindModificationsOperation;
		this.SearchGloballyOperation = dtos.SearchGloballyOperation;
		this.SearchOperationExecutionsOperation = dtos.SearchOperationExecutionsOperation;
		this.SearchDeletionsOperation = dtos.SearchDeletionsOperation;
		this.SearchDataStoresOperation = dtos.SearchDataStoresOperation;

		this.DeleteSpacesOperation = dtos.DeleteSpacesOperation;
		this.DeleteProjectsOperation = dtos.DeleteProjectsOperation;
		this.DeleteExperimentsOperation = dtos.DeleteExperimentsOperation;
		this.DeleteSamplesOperation = dtos.DeleteSamplesOperation;
		this.DeleteDataSetsOperation = dtos.DeleteDataSetsOperation;
		this.DeleteMaterialsOperation = dtos.DeleteMaterialsOperation;
		this.DeleteExternalDmsOperation = dtos.DeleteExternalDmsOperation;
		this.DeleteVocabularyTermsOperation = dtos.DeleteVocabularyTermsOperation;
		this.DeleteTagsOperation = dtos.DeleteTagsOperation;
		this.DeleteOperationExecutionsOperation = dtos.DeleteOperationExecutionsOperation;

		this.RevertDeletionsOperation = dtos.RevertDeletionsOperation;
		this.ConfirmDeletionsOperation = dtos.ConfirmDeletionsOperation;
		this.ExecuteCustomASServiceOperation = dtos.ExecuteCustomASServiceOperation;
		this.ArchiveDataSetsOperation = dtos.ArchiveDataSetsOperation;
		this.UnarchiveDataSetsOperation = dtos.UnarchiveDataSetsOperation;

		this.SynchronousOperationExecutionOptions = dtos.SynchronousOperationExecutionOptions;
		this.AsynchronousOperationExecutionOptions = dtos.AsynchronousOperationExecutionOptions;
		this.OperationExecutionDeletionOptions = dtos.OperationExecutionDeletionOptions;
		this.OperationExecutionFetchOptions = dtos.OperationExecutionFetchOptions;
		this.OperationExecutionPermId = dtos.OperationExecutionPermId;
		this.OperationExecutionSearchCriteria = dtos.OperationExecutionSearchCriteria;
		this.OperationExecutionUpdate = dtos.OperationExecutionUpdate;

		this.getDtos = function() {
			return dtos;
		}

		this.generateId = function(base) {
			var date = new Date();
			var parts = [ "V3", base, date.getFullYear(), date.getMonth() + 1, date.getDate(), date.getHours(), date.getMinutes(), Math.round(1000000 * Math.random()) ];
			return parts.join("_");
		},

		this.createSpace = function(facade) {
			var c = this;
			var creation = new dtos.SpaceCreation();
			creation.setCode(c.generateId("SPACE"));
			return facade.createSpaces([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createProject = function(facade) {
			var c = this;
			return c.createSpace(facade).then(function(spacePermId) {
				var creation = new dtos.ProjectCreation();
				creation.setCode(c.generateId("PROJECT"));
				creation.setSpaceId(spacePermId);
				return facade.createProjects([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		}.bind(this);

		this.createExperiment = function(facade) {
			var c = this;
			return c.createProject(facade).then(function(projectPermId) {
				var creation = new dtos.ExperimentCreation();
				creation.setCode(c.generateId("EXPERIMENT"));
				creation.setTypeId(new dtos.EntityTypePermId("UNKNOWN"));
				creation.setProjectId(projectPermId);
				return facade.createExperiments([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		}.bind(this);

		this.createSample = function(facade) {
			var c = this;
			return c.createSpace(facade).then(function(spacePermId) {
				var creation = new dtos.SampleCreation();
				creation.setCode(c.generateId("SAMPLE"));
				creation.setTypeId(new dtos.EntityTypePermId("UNKNOWN"));
				creation.setSpaceId(spacePermId);
				return facade.createSamples([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		}.bind(this);
		
		this.createLinkDataSet = function(facade, path, gitCommitHash) {
			var c = this;
			return c.createExperiment(facade).then(function(experimentPermId) {
				return c.createFileExternalDms(facade).then(function(emdsPermId) {
					var dataSet = new dtos.DataSetCreation();
					dataSet.setAutoGeneratedCode(true);
					dataSet.setTypeId(new c.EntityTypePermId("LINK_TYPE"));
					dataSet.setExperimentId(experimentPermId);
					dataSet.setDataStoreId(new c.DataStorePermId("DSS1"));
					var linkedData = new c.LinkedDataCreation();
					var cc = new c.ContentCopyCreation();
					cc.setExternalDmsId(emdsPermId);
					cc.setPath(path);
					cc.setGitCommitHash(gitCommitHash);
					linkedData.setContentCopies([cc]);
					dataSet.setLinkedData(linkedData);
					return facade.createDataSets([dataSet]).then(function(permIds) {
						return permIds[0];
					});
				});
			});
		}.bind(this);

		this.createDataSet = function(facade) {
			var c = this;
			return this.getResponseFromJSTestAggregationService(facade, {}, function(response) {
				return new dtos.DataSetPermId(response.result.rows[0][0].value);
			});
		}.bind(this);

		this.getResponseFromJSTestAggregationService = function(facade, params, callback) {
			var c = this;
			return $.ajax({
				"url" : "http://" + testHost + ":20001/datastore_server/rmi-dss-api-v1.json",
				"type" : "POST",
				"processData" : false,
				"dataType" : "json",
				"data" : JSON.stringify({
					"method" : "createReportFromAggregationService",
					"params" : [ facade._private.sessionToken, "js-test", params ],
					"id" : "1",
					"jsonrpc" : "2.0"
				})
			}).then(callback);
		}.bind(this);

		this.createMaterial = function(facade) {
			var c = this;
			var creation = new dtos.MaterialCreation();
			creation.setCode(c.generateId("MATERIAL"));
			creation.setTypeId(new dtos.EntityTypePermId("COMPOUND"));
			return facade.createMaterials([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createVocabularyTerm = function(facade) {
			var c = this;
			var creation = new dtos.VocabularyTermCreation();
			creation.setCode(c.generateId("VOCABULARY_TERM"));
			creation.setVocabularyId(new c.VocabularyPermId("TEST-VOCABULARY"));
			return facade.createVocabularyTerms([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);
		
		this.createExternalDms = function(facade) {
			var c = this;
			var creation = new dtos.ExternalDmsCreation();
			creation.setCode(c.generateId("EMDS"));
			creation.setAddressType(c.ExternalDmsAddressType.URL);
			creation.setAddress("https://my-server:8443/my-app/q=${term}")
			return facade.createExternalDms([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);
		
		this.createFileExternalDms = function(facade) {
			var c = this;
			var creation = new c.ExternalDmsCreation();
			creation.setCode(c.generateId("EMDS"));
			creation.setLabel("Test File EDMS");
			creation.setAddressType(c.ExternalDmsAddressType.FILE_SYSTEM);
			creation.setAddress("host:my/path")
			return facade.createExternalDms([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createTag = function(facade) {
			var c = this;
			var creation = new dtos.TagCreation();
			creation.setCode(c.generateId("TAG"));
			return facade.createTags([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createOperationExecution = function(facade) {
			var c = this;
			var operation = new dtos.GetSpacesOperation([ new dtos.SpacePermId("/TEST") ], new dtos.SpaceFetchOptions());
			var options = new dtos.SynchronousOperationExecutionOptions();
			options.setExecutionId(new dtos.OperationExecutionPermId());
			return facade.executeOperations([ operation ], options).then(function() {
				return options.getExecutionId();
			});
		}.bind(this);

		this.findSpace = function(facade, id) {
			var c = this;
			return facade.getSpaces([ id ], c.createSpaceFetchOptions()).then(function(spaces) {
				return spaces[id];
			});
		}.bind(this);

		this.findProject = function(facade, id) {
			var c = this;
			return facade.getProjects([ id ], c.createProjectFetchOptions()).then(function(projects) {
				return projects[id];
			});
		}.bind(this);

		this.findExperiment = function(facade, id) {
			var c = this;
			return facade.getExperiments([ id ], c.createExperimentFetchOptions()).then(function(experiments) {
				return experiments[id];
			});
		}.bind(this);
		
		this.findExperimentType = function(facade, id) {
			var c = this;
			var criteria = new c.ExperimentTypeSearchCriteria();
			criteria.withId().thatEquals(id);
			return facade.searchExperimentTypes(criteria, c.createExperimentTypeFetchOptions()).then(function(results) {
				return results.getObjects()[0];
			});
		}.bind(this);

		this.findSample = function(facade, id) {
			var c = this;
			return facade.getSamples([ id ], c.createSampleFetchOptions()).then(function(samples) {
				return samples[id];
			});
		}.bind(this);

		this.findSampleType = function(facade, id) {
			var c = this;
			var criteria = new c.SampleTypeSearchCriteria();
			criteria.withId().thatEquals(id);
			return facade.searchSampleTypes(criteria, c.createSampleTypeFetchOptions()).then(function(results) {
				return results.getObjects()[0];
			});
		}.bind(this);

		this.findDataSet = function(facade, id) {
			var c = this;
			return facade.getDataSets([ id ], c.createDataSetFetchOptions()).then(function(dataSets) {
				return dataSets[id];
			});
		}.bind(this);

		this.findDataSetType = function(facade, id) {
			var c = this;
			var criteria = new c.DataSetTypeSearchCriteria();
			criteria.withId().thatEquals(id);
			return facade.searchDataSetTypes(criteria, c.createDataSetTypeFetchOptions()).then(function(results) {
				return results.getObjects()[0];
			});
		}.bind(this);

		this.findMaterial = function(facade, id) {
			var c = this;
			return facade.getMaterials([ id ], c.createMaterialFetchOptions()).then(function(materials) {
				return materials[id];
			});
		}.bind(this);
		
		this.findMaterialType = function(facade, id) {
			var c = this;
			var criteria = new c.MaterialTypeSearchCriteria();
			criteria.withId().thatEquals(id);
			return facade.searchMaterialTypes(criteria, c.createMaterialTypeFetchOptions()).then(function(results) {
				return results.getObjects()[0];
			});
		}.bind(this);

		this.findVocabularyTerm = function(facade, id) {
			var c = this;
			return facade.getVocabularyTerms([ id ], c.createVocabularyTermFetchOptions()).then(function(terms) {
				return terms[id];
			});
		}.bind(this);

		this.findTag = function(facade, id) {
			var c = this;
			return facade.getTags([ id ], c.createTagFetchOptions()).then(function(tags) {
				return tags[id];
			});
		}.bind(this);
		
		this.findExternalDms = function(facade, id) {
			var c = this;
			return facade.getExternalDataManagementSystems([ id ], c.createExternalDmsFetchOptions()).then(function(edms) {
				return edms[id];
			});
		}.bind(this);
		
		this.findOperationExecution = function(facade, id) {
			var c = this;
			return facade.getOperationExecutions([ id ], c.createOperationExecutionFetchOptions()).then(function(executions) {
				return executions[id];
			});
		}.bind(this);

		this.deleteSpace = function(facade, id) {
			var c = this;
			var options = new dtos.SpaceDeletionOptions();
			options.setReason("test reason");
			return facade.deleteSpaces([ id ], options);
		}.bind(this);

		this.deleteProject = function(facade, id) {
			var c = this;
			var options = new dtos.ProjectDeletionOptions();
			options.setReason("test reason");
			return facade.deleteProjects([ id ], options);
		}.bind(this);

		this.deleteExperiment = function(facade, id) {
			var c = this;
			var options = new dtos.ExperimentDeletionOptions();
			options.setReason("test reason");
			return facade.deleteExperiments([ id ], options);
		}.bind(this);

		this.deleteSample = function(facade, id) {
			var c = this;
			var options = new dtos.SampleDeletionOptions();
			options.setReason("test reason");
			return facade.deleteSamples([ id ], options);
		}.bind(this);

		this.deleteDataSet = function(facade, id) {
			var c = this;
			var options = new dtos.DataSetDeletionOptions();
			options.setReason("test reason");
			return facade.deleteDataSets([ id ], options);
		}.bind(this);

		this.deleteMaterial = function(facade, id) {
			var c = this;
			var options = new dtos.MaterialDeletionOptions();
			options.setReason("test reason");
			return facade.deleteMaterials([ id ], options);
		}.bind(this);

		this.deleteExternalDms = function(facade, id) {
			var c = this;
			var options = new dtos.ExternalDmsDeletionOptions();
			options.setReason("test reason");
			return facade.deleteExternalDataManagementSystems([ id ], options);
		}.bind(this);

		this.deleteVocabularyTerm = function(facade, id) {
			var c = this;
			var options = new dtos.VocabularyTermDeletionOptions();
			options.setReason("test reason");
			return facade.deleteVocabularyTerms([ id ], options);
		}.bind(this);
		
		this.replaceVocabularyTerm = function(facade, id) {
			var c = this;
			var options = new dtos.VocabularyTermDeletionOptions();
			options.setReason("test reason");
			options.replace(id, new c.VocabularyTermPermId("TEST-TERM-1", "TEST-VOCABULARY"));
			return facade.deleteVocabularyTerms([ id ], options);
		}.bind(this);

		this.deleteTag = function(facade, id) {
			var c = this;
			var options = new dtos.TagDeletionOptions();
			options.setReason("test reason");
			return facade.deleteTags([ id ], options);
		}.bind(this);

		this.deleteOperationExecution = function(facade, id) {
			var c = this;
			var options = new dtos.OperationExecutionDeletionOptions();
			options.setReason("test reason");
			return facade.deleteOperationExecutions([ id ], options);
		}.bind(this);

		this.getObjectProperty = function(object, propertyName) {
			var propertyNames = propertyName.split('.');
			for ( var pn in propertyNames) {
				object = object[propertyNames[pn]];
			}
			return object;
		};

		this.createFacade = function() {
			var dfd = $.Deferred();
			dfd.resolve(new openbis(testApiUrl));
			return dfd.promise();
		};

		this.createFacadeAndLogin = function() {
			var dfd = $.Deferred();

			this.createFacade().then(function(facade) {
				facade.login(testUserId, testUserPassword).done(function() {
					dfd.resolve(facade);
				}).fail(function() {
					dfd.reject(arguments);
				});
			});

			return dfd.promise();
		};

		this.createSpaceFetchOptions = function() {
			var fo = new dtos.SpaceFetchOptions();
			fo.withProjects();
			fo.withSamples();
			fo.withRegistrator();
			return fo;
		};

		this.createProjectFetchOptions = function() {
			var fo = new dtos.ProjectFetchOptions();
			fo.withSpace();
			fo.withExperiments();
			fo.withRegistrator();
			fo.withModifier();
			fo.withLeader();
			fo.withAttachments().withContent();
			return fo;
		};

		this.createExperimentFetchOptions = function() {
			var fo = new dtos.ExperimentFetchOptions();
			fo.withType();
			fo.withProject().withSpace();
			fo.withDataSets();
			fo.withSamples();
			fo.withHistory();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			fo.withAttachments().withContent();
			return fo;
		};
		
		this.createExperimentTypeFetchOptions = function() {
			var fo = new dtos.ExperimentTypeFetchOptions();
			fo.withPropertyAssignments().withPropertyType();
			fo.withPropertyAssignments().withRegistrator();
			return fo;
		};

		this.createSampleFetchOptions = function() {
			var fo = new dtos.SampleFetchOptions();
			fo.withType();
			fo.withExperiment().withProject().withSpace();
			fo.withSpace();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withParents();
			fo.withChildren();
			fo.withContainer();
			fo.withComponents();
			fo.withDataSets();
			fo.withHistory();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			fo.withAttachments().withContent();
			fo.withChildrenUsing(fo);
			return fo;
		};

		this.createSampleTypeFetchOptions = function() {
			var fo = new dtos.SampleTypeFetchOptions();
			fo.withPropertyAssignments().withPropertyType();
			fo.withPropertyAssignments().withRegistrator();
			return fo;
		};

		this.createDataSetFetchOptions = function() {
			var fo = new dtos.DataSetFetchOptions();
			fo.withType();
			fo.withExperiment().withProject().withSpace();
			fo.withSample();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withParents();
			fo.withChildren();
			fo.withContainers();
			fo.withComponents();
			fo.withPhysicalData().withFileFormatType();
			fo.withPhysicalData().withLocatorType();
			fo.withPhysicalData().withStorageFormat();
			fo.withLinkedData().withExternalDms();
			fo.withHistory();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			return fo;
		};
		
		this.createDataSetTypeFetchOptions = function() {
			var fo = new dtos.DataSetTypeFetchOptions();
			fo.withPropertyAssignments().withPropertyType();
			fo.withPropertyAssignments().withRegistrator();
			return fo;
		};
		
		this.createMaterialFetchOptions = function() {
			var fo = new dtos.MaterialFetchOptions();
			fo.withType();
			fo.withHistory();
			fo.withRegistrator();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withTags();
			return fo;
		};
		
		this.createMaterialTypeFetchOptions = function() {
			var fo = new dtos.MaterialTypeFetchOptions();
			fo.withPropertyAssignments().withPropertyType();
			fo.withPropertyAssignments().withRegistrator();
			return fo;
		};

		this.createVocabularyTermFetchOptions = function() {
			var fo = new dtos.VocabularyTermFetchOptions();
			fo.withVocabulary();
			fo.withRegistrator();
			return fo;
		};

		this.createGlobalSearchObjectFetchOptions = function() {
			var fo = new dtos.GlobalSearchObjectFetchOptions();
			fo.withExperiment();
			fo.withSample();
			fo.withDataSet();
			fo.withMaterial();
			return fo;
		};

		this.createObjectKindModificationFetchOptions = function() {
			var fo = new dtos.ObjectKindModificationFetchOptions();
			return fo;
		};

		this.createTagFetchOptions = function() {
			var fo = new dtos.TagFetchOptions();
			fo.withExperiments();
			fo.withSamples();
			fo.withDataSets();
			fo.withMaterials();
			return fo;
		};
		
		this.createExternalDmsFetchOptions = function() {
			var fo = new dtos.ExternalDmsFetchOptions();
			return fo;
		};
		
		this.createOperationExecutionFetchOptions = function() {
			var fo = new dtos.OperationExecutionFetchOptions();
			fo.withOwner().withSpace();
			fo.withOwner().withRegistrator();
			fo.withNotification();
			fo.withSummary().withOperations();
			fo.withSummary().withProgress();
			fo.withSummary().withError();
			fo.withSummary().withResults();
			fo.withDetails().withOperations();
			fo.withDetails().withProgress();
			fo.withDetails().withError();
			fo.withDetails().withResults();
			return fo;
		};

		this.createDataStoreFetchOptions = function() {
			var fo = new dtos.DataStoreFetchOptions();
			return fo;
		};
		
		this.createDataSetFileFetchOptions = function() {
			var fo = new dtos.DataSetFileFetchOptions();
			return fo;
		};
		
		this.assertNull = function(actual, msg) {
			this.assertEqual(actual, null, msg)
		};

		this.assertNotNull = function(actual, msg) {
			this.assertNotEqual(actual, null, msg);
		};

		this.assertTrue = function(actual, msg) {
			this.assertEqual(actual, true, msg);
		};

		this.assertFalse = function(actual, msg) {
			this.assertEqual(actual, false, msg);
		};

		this.assertContains = function(actual, expected, msg) {
			actual = actual ? actual : "";
			this.assertTrue(actual.indexOf(expected) >= 0, msg);
		};

		this.assertEqual = function(actual, expected, msg) {
			this.assert.equal(actual, expected, msg);
		};

		this.assertNotEqual = function(actual, expected, msg) {
			this.assert.notEqual(actual, expected, msg);
		};

		this.assertDate = function(millis, msg, year, month, day, hour, minute) {
			var date = new Date(millis);
			var actual = "";
			var expected = "";

			if (year) {
				actual += date.getUTCFullYear();
				expected += year;
			}
			if (month) {
				actual += "-" + (date.getUTCMonth() + 1);
				expected += "-" + month;
			}
			if (day) {
				actual += "-" + date.getUTCDate();
				expected += "-" + day;
			}
			if (hour) {
				actual += " " + date.getUTCHours();
				expected += " " + hour;
			}
			if (minute) {
				actual += ":" + date.getUTCMinutes();
				expected += ":" + minute;
			}

			this.assertEqual(actual, expected, msg);
		};

		this.assertToday = function(millis, msg) {
			var today = new Date();
			this.assertDate(millis, msg, today.getUTCFullYear(), today.getUTCMonth() + 1, today.getUTCDate());
		};

		this.assertObjectsCount = function(objects, count) {
			this.assertEqual(objects.length, count, 'Got ' + count + ' object(s)');
		};

		this.assertObjectsWithValues = function(objects, propertyName, propertyValues) {
			var thisCommon = this;
			var values = {};

			$.each(objects, function(index, object) {
				var value = thisCommon.getObjectProperty(object, propertyName);
				if (value in values == false) {
					values[value] = true;
				}
			});

			this.assert.deepEqual(Object.keys(values).sort(), propertyValues.sort(), 'Objects have correct ' + propertyName + ' values')
		};

		this.assertObjectsWithOrWithoutCollections = function(objects, accessor, checker) {
			var theObjects = null;

			if ($.isArray(objects)) {
				theObjects = objects;
			} else {
				theObjects = [ objects ];
			}

			var theAccessor = null;

			if ($.isFunction(accessor)) {
				theAccessor = accessor;
			} else {
				theAccessor = function(object) {
					return object[accessor];
				}
			}

			checker(theObjects, theAccessor);
		};

		this.assertObjectsWithCollections = function(objects, accessor) {
			var thisCommon = this;
			this.assertObjectsWithOrWithoutCollections(objects, accessor, function(objects, accessor) {
				thisCommon.assert.ok(objects.some(function(object) {
					var value = accessor(object);
					return value && Object.keys(value).length > 0;
				}), 'Objects have non-empty collections accessed via: ' + accessor);
			});
		};

		this.assertObjectsWithoutCollections = function(objects, accessor) {
			var thisCommon = this;
			this.assertObjectsWithOrWithoutCollections(objects, accessor, function(objects, accessor) {
				thisCommon.assert.ok(objects.every(function(object) {
					var value = accessor(object);
					return !value || Object.keys(value).length == 0;
				}), 'Objects have empty collections accessed via: ' + accessor);
			});
		};

		this.shallowEqual = function(actual, expected, message) {
			function oneWay(from, to) {
				var isBad = _.chain(_.keys(from)).filter(function(k) {
					return !_.isFunction(from[k]) && !_.isArray(from[k]) && !_.isObject(from[k]) && !_.isFunction(to[k]) && !_.isArray(to[k]) && !_.isObject(to[k]);
				}).any(function(k) {
					if (from[k] !== to[k]) {
						return true;
					}
				}).value();

				if (isBad) {
					assert.propEqual(actual, expected);
				}
			}

			oneWay(actual, expected);
			oneWay(expected, actual);
		};

		this.start = function() {
			this.done = this.assert.async();
		};

		this.finish = function() {
			if (this.done) {
				this.done();
			}
		};

		this.ok = function(msg) {
			this.assert.ok(true, msg);
		};

		this.section = function(msg) {
			this.assert.ok(true, "******************************************************");
			this.assert.ok(true, msg);
			this.assert.ok(true, "******************************************************");
		};

		this.fail = function(msg) {
			this.assert.ok(false, msg);
		};

	};

	return Common;
})
