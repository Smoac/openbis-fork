import autoBind from 'auto-bind'

class Facade {
  constructor() {
    autoBind(this)
  }

  _init() {
    let _this = this
    return new Promise((resolve, reject) => {
      /* eslint-disable-next-line no-undef */
      requirejs(
        ['openbis'],
        openbis => {
          _this.v3 = new openbis()
          resolve()
        },
        error => {
          reject(error)
        }
      )
    })
  }

  useSession(sessionToken) {
    this.v3._private.sessionToken = sessionToken
  }

  getSessionInformation() {
    return this.promise(this.v3.getSessionInformation())
  }

  getServerInformation() {
    return this.promise(this.v3.getServerInformation())
  }

  getServerPublicInformation() {
    return this.promise(this.v3.getServerPublicInformation())
  }

  login(user, password) {
    return this.promise(this.v3.login(user, password))
  }

  logout() {
    return this.promise(this.v3.logout())
  }

  getSpaces(ids, fo) {
    return this.promise(this.v3.getSpaces(ids, fo))
  }

  getProjects(ids, fo) {
    return this.promise(this.v3.getProjects(ids, fo))
  }

  getExperiments(ids, fo) {
    return this.promise(this.v3.getExperiments(ids, fo))
  }

  getSamples(ids, fo) {
    return this.promise(this.v3.getSamples(ids, fo))
  }

  getDataSets(ids, fo) {
    return this.promise(this.v3.getDataSets(ids, fo))
  }

  getPlugins(ids, fo) {
    return this.promise(this.v3.getPlugins(ids, fo))
  }

  getQueries(ids, fo) {
    return this.promise(this.v3.getQueries(ids, fo))
  }

  getPropertyTypes(ids, fo) {
    return this.promise(this.v3.getPropertyTypes(ids, fo))
  }

  getAuthorizationGroups(ids, fo) {
    return this.promise(this.v3.getAuthorizationGroups(ids, fo))
  }

  getPersons(ids, fo) {
    return this.promise(this.v3.getPersons(ids, fo))
  }

  updatePersons(updates) {
    return this.promise(this.v3.updatePersons(updates))
  }

  searchSpaces(criteria, fo) {
    return this.promise(this.v3.searchSpaces(criteria, fo))
  }

  searchProjects(criteria, fo) {
    return this.promise(this.v3.searchProjects(criteria, fo))
  }

  searchPropertyTypes(criteria, fo) {
    return this.promise(this.v3.searchPropertyTypes(criteria, fo))
  }

  searchPlugins(criteria, fo) {
    return this.promise(this.v3.searchPlugins(criteria, fo))
  }

  searchPersonalAccessTokens(criteria, fo) {
    return this.promise(this.v3.searchPersonalAccessTokens(criteria, fo))
  }

  searchQueries(criteria, fo) {
    return this.promise(this.v3.searchQueries(criteria, fo))
  }

  searchQueryDatabases(criteria, fo) {
    return this.promise(this.v3.searchQueryDatabases(criteria, fo))
  }

  searchMaterials(criteria, fo) {
    return this.promise(this.v3.searchMaterials(criteria, fo))
  }

  searchSamples(criteria, fo) {
    return this.promise(this.v3.searchSamples(criteria, fo))
  }

  searchExperiments(criteria, fo) {
    return this.promise(this.v3.searchExperiments(criteria, fo))
  }

  searchDataSets(criteria, fo) {
    return this.promise(this.v3.searchDataSets(criteria, fo))
  }

  searchVocabularies(criteria, fo) {
    return this.promise(this.v3.searchVocabularies(criteria, fo))
  }

  searchVocabularyTerms(criteria, fo) {
    return this.promise(this.v3.searchVocabularyTerms(criteria, fo))
  }

  searchPersons(criteria, fo) {
    return this.promise(this.v3.searchPersons(criteria, fo))
  }

  searchAuthorizationGroups(criteria, fo) {
    return this.promise(this.v3.searchAuthorizationGroups(criteria, fo))
  }

  searchPropertyAssignments(criteria, fo) {
    return this.promise(this.v3.searchPropertyAssignments(criteria, fo))
  }

  searchEvents(criteria, fo) {
    return this.promise(this.v3.searchEvents(criteria, fo))
  }

  getSampleTypes(ids, fo) {
    return this.promise(this.v3.getSampleTypes(ids, fo))
  }

  getExperimentTypes(ids, fo) {
    return this.promise(this.v3.getExperimentTypes(ids, fo))
  }

  getDataSetTypes(ids, fo) {
    return this.promise(this.v3.getDataSetTypes(ids, fo))
  }

  getMaterialTypes(ids, fo) {
    return this.promise(this.v3.getMaterialTypes(ids, fo))
  }

  getVocabularies(ids, fo) {
    return this.promise(this.v3.getVocabularies(ids, fo))
  }

  updateSampleTypes(updates) {
    return this.promise(this.v3.updateSampleTypes(updates))
  }

  updateExperimentTypes(updates) {
    return this.promise(this.v3.updateExperimentTypes(updates))
  }

  updateDataSetTypes(updates) {
    return this.promise(this.v3.updateDataSetTypes(updates))
  }

  updateMaterialTypes(updates) {
    return this.promise(this.v3.updateMaterialTypes(updates))
  }

  searchSampleTypes(criteria, fo) {
    return this.promise(this.v3.searchSampleTypes(criteria, fo))
  }

  searchExperimentTypes(criteria, fo) {
    return this.promise(this.v3.searchExperimentTypes(criteria, fo))
  }

  searchDataSetTypes(criteria, fo) {
    return this.promise(this.v3.searchDataSetTypes(criteria, fo))
  }

  searchMaterialTypes(criteria, fo) {
    return this.promise(this.v3.searchMaterialTypes(criteria, fo))
  }

  deleteSampleTypes(ids, options) {
    return this.promise(this.v3.deleteSampleTypes(ids, options))
  }

  deleteExperimentTypes(ids, options) {
    return this.promise(this.v3.deleteExperimentTypes(ids, options))
  }

  deleteDataSetTypes(ids, options) {
    return this.promise(this.v3.deleteDataSetTypes(ids, options))
  }

  deleteMaterialTypes(ids, options) {
    return this.promise(this.v3.deleteMaterialTypes(ids, options))
  }

  evaluatePlugin(options) {
    return this.promise(this.v3.evaluatePlugin(options))
  }

  executeService(id, options) {
    return this.promise(this.v3.executeCustomASService(id, options))
  }

  executeQuery(id, options) {
    return this.promise(this.v3.executeQuery(id, options))
  }

  executeSql(sql, options) {
    return this.promise(this.v3.executeSql(sql, options))
  }

  executeOperations(operations, options) {
    return this.promise(this.v3.executeOperations(operations, options))
  }

  promise(dfd) {
    return new Promise((resolve, reject) => {
      dfd.then(
        result => {
          resolve(result)
        },
        error => {
          reject(error)
        }
      )
    })
  }
}

const facade = new Facade()
export default facade
