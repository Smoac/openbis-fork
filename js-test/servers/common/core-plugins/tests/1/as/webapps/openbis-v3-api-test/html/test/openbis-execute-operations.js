define([ 'jquery', 'openbis', 'test/common' ], function($, openbis, common) {

	var c = new common();

	var facade = function() {

		this._private = {};
		this._openbis = new openbis();

		this._executeOperation = function(operation) {
			return this._openbis.executeOperations([ operation ], new c.SynchronousOperationExecutionOptions());
		}

		this._executeCreateOperation = function(operation) {
			return this._executeOperation(operation).then(function(results) {
				return results.getResults()[0].getObjectIds();
			});
		}

		this._executeUpdateOperation = function(operation) {
			return this._executeOperation(operation).then(function(results) {
				return results.getResults()[0].getObjectIds();
			});
		}

		this._executeGetOperation = function(operation) {
			return this._executeOperation(operation).then(function(results) {
				return results.getResults()[0].getObjectMap();
			});
		}

		this._executeSearchOperation = function(operation) {
			return this._executeOperation(operation).then(function(results) {
				return results.getResults()[0].getSearchResult();
			});
		}

		this._executeDeleteOperation = function(operation) {
			return this._executeOperation(operation).then(function(results) {
				var result = results.getResults()[0];
				if (result.getDeletionId) {
					return result.getDeletionId();
				}
			});
		}

		this.login = function(user, password) {
			var thisFacade = this;
			return this._openbis.login(user, password).done(function(sessionToken) {
				thisFacade._private.sessionToken = sessionToken;
			});
		}

		this.loginAs = function(user, password, asUserId) {
			var thisFacade = this;
			return this._openbis.loginAs(user, password, asUserId).done(function(sessionToken) {
				thisFacade._private.sessionToken = sessionToken;
			});
		}

		this.loginAsAnonymousUser = function() {
			var thisFacade = this;
			return this._openbis.loginAsAnonymousUser().done(function(sessionToken) {
				thisFacade._private.sessionToken = sessionToken;
			});
		}

		this.logout = function() {
			var thisFacade = this;
			return this._openbis.logout().done(function() {
				thisFacade._private.sessionToken = null;
			});
		}

		this.getSessionInformation = function() {
			return this._executeOperation(new c.GetSessionInformationOperation()).then(function() {
				return results.getResults()[0].getSessionInformation();
			});
		}

		this.createSpaces = function(creations) {
			return this._executeCreateOperation(new c.CreateSpacesOperation(creations));
		}

		this.createProjects = function(creations) {
			return this._executeCreateOperation(new c.CreateProjectsOperation(creations));
		}

		this.createExperiments = function(creations) {
			return this._executeCreateOperation(new c.CreateExperimentsOperation(creations));
		}

		this.createSamples = function(creations) {
			return this._executeCreateOperation(new c.CreateSamplesOperation(creations));
		}

		this.createSampleTypes = function(creations) {
			return this._executeCreateOperation(new c.CreateSampleTypesOperation(creations));
		}

		this.createDataSetTypes = function(creations) {
			return this._executeCreateOperation(new c.CreateDataSetTypesOperation(creations));
		}

		this.createMaterials = function(creations) {
			return this._executeCreateOperation(new c.CreateMaterialsOperation(creations));
		}

		this.createVocabularyTerms = function(creations) {
			return this._executeCreateOperation(new c.CreateVocabularyTermsOperation(creations));
		}

		this.createTags = function(creations) {
			return this._executeCreateOperation(new c.CreateTagsOperation(creations));
		}

		this.updateSpaces = function(updates) {
			return this._executeUpdateOperation(new c.UpdateSpacesOperation(updates));
		}

		this.updateProjects = function(updates) {
			return this._executeUpdateOperation(new c.UpdateProjectsOperation(updates));
		}

		this.updateExperiments = function(updates) {
			return this._executeUpdateOperation(new c.UpdateExperimentsOperation(updates));
		}

		this.updateSamples = function(updates) {
			return this._executeUpdateOperation(new c.UpdateSamplesOperation(updates));
		}

		this.updateDataSets = function(updates) {
			return this._executeUpdateOperation(new c.UpdateDataSetsOperation(updates));
		}

		this.updateMaterials = function(updates) {
			return this._executeUpdateOperation(new c.UpdateMaterialsOperation(updates));
		}

		this.updateVocabularyTerms = function(updates) {
			return this._executeUpdateOperation(new c.UpdateVocabularyTermsOperation(updates));
		}

		this.updateTags = function(updates) {
			return this._executeUpdateOperation(new c.UpdateTagsOperation(updates));
		}

		this.updateOperationExecutions = function(updates) {
			return this._executeUpdateOperation(new c.UpdateOperationExecutionsOperation(updates));
		}

		this.getSpaces = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetSpacesOperation(ids, fetchOptions));
		}

		this.getProjects = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetProjectsOperation(ids, fetchOptions));
		}

		this.getExperiments = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetExperimentsOperation(ids, fetchOptions));
		}

		this.getSamples = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetSamplesOperation(ids, fetchOptions));
		}

		this.getDataSets = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetDataSetsOperation(ids, fetchOptions));
		}

		this.getMaterials = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetMaterialsOperation(ids, fetchOptions));
		}

		this.getVocabularyTerms = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetVocabularyTermsOperation(ids, fetchOptions));
		}

		this.getTags = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetTagsOperation(ids, fetchOptions));
		}

		this.getOperationExecutions = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetOperationExecutionsOperation(ids, fetchOptions));
		}

		this.searchSpaces = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchSpacesOperation(criteria, fetchOptions));
		}

		this.searchProjects = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchProjectsOperation(criteria, fetchOptions));
		}

		this.searchExperiments = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchExperimentsOperation(criteria, fetchOptions));
		}

		this.searchExperimentTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchExperimentTypesOperation(criteria, fetchOptions));
		}

		this.searchSamples = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchSamplesOperation(criteria, fetchOptions));
		}

		this.searchSampleTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchSampleTypesOperation(criteria, fetchOptions));
		}

		this.searchDataSets = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchDataSetsOperation(criteria, fetchOptions));
		}

		this.searchDataSetTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchDataSetTypesOperation(criteria, fetchOptions));
		}

		this.searchMaterials = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchMaterialsOperation(criteria, fetchOptions));
		}

		this.searchMaterialTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchMaterialTypesOperation(criteria, fetchOptions));
		}

		this.searchVocabularyTerms = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchVocabularyTermsOperation(criteria, fetchOptions));
		}

		this.searchTags = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchTagsOperation(criteria, fetchOptions));
		}

		this.searchCustomASServices = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchCustomASServicesOperation(criteria, fetchOptions));
		}

		this.searchObjectKindModifications = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchObjectKindModificationsOperation(criteria, fetchOptions));
		}

		this.searchGlobally = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchGloballyOperation(criteria, fetchOptions));
		}

		this.searchOperationExecutions = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchOperationExecutionsOperation(criteria, fetchOptions));
		}

		this.deleteSpaces = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteSpacesOperation(ids, deletionOptions));
		}

		this.deleteProjects = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteProjectsOperation(ids, deletionOptions));
		}

		this.deleteExperiments = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteExperimentsOperation(ids, deletionOptions));
		}

		this.deleteSamples = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteSamplesOperation(ids, deletionOptions));
		}

		this.deleteDataSets = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteDataSetsOperation(ids, deletionOptions));
		}

		this.deleteMaterials = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteMaterialsOperation(ids, deletionOptions));
		}

		this.deleteVocabularyTerms = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteVocabularyTermsOperation(ids, deletionOptions));
		}

		this.deleteTags = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteTagsOperation(ids, deletionOptions));
		}

		this.deleteOperationExecutions = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteOperationExecutionsOperation(ids, deletionOptions));
		}

		this.searchDeletions = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchDeletionsOperation(criteria, fetchOptions));
		}

		this.revertDeletions = function(ids) {
			return this._executeOperation(new c.RevertDeletionsOperation(ids)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.confirmDeletions = function(ids) {
			return this._executeOperation(new c.ConfirmDeletionsOperation(ids)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.executeCustomASService = function(serviceId, options) {
			return this._executeOperation(new c.ExecuteCustomASServiceOperation(serviceId, options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.archiveDataSets = function(ids, options) {
			return this._executeOperation(new c.ArchiveDataSetsOperation(ids, options)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.unarchiveDataSets = function(ids, options) {
			return this._executeOperation(new c.UnarchiveDataSetsOperation(ids, options)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.executeOperations = function(operations, options) {
			return this._openbis.executeOperations(operations, options);
		}

	}

	return facade;

});