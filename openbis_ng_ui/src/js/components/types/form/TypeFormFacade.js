import openbis from '@src/js/services/openbis.js'
import TypeFormControllerStrategies from '@src/js/components/types/form/TypeFormControllerStrategies.js'

export default class TypeFormFacade {
  async loadType(object) {
    const strategy = this._getStrategy(object.type)
    const id = new openbis.EntityTypePermId(object.id)
    const fo = strategy.createTypeFetchOptions()
    fo.withValidationPlugin()
    fo.withPropertyAssignments().withPlugin()
    fo.withPropertyAssignments().withRegistrator()
    fo.withPropertyAssignments().withPropertyType().withRegistrator()
    fo.withPropertyAssignments().withPropertyType().withMaterialType()
    fo.withPropertyAssignments().withPropertyType().withSampleType()
    fo.withPropertyAssignments().withPropertyType().withVocabulary()
    fo.withPropertyAssignments().sortBy().ordinal()

    return strategy.getTypes([id], fo).then(map => {
      return map[object.id]
    })
  }

  async loadUsages(object) {
    return Promise.all([
      this._loadTypeUsages(object),
      this._loadPropertyLocalUsages(object),
      this._loadPropertyGlobalUsages(object)
    ]).then(([type, propertyLocal, propertyGlobal]) => ({
      type,
      propertyLocal,
      propertyGlobal
    }))
  }

  async loadAssignments(object) {
    return Promise.all([
      this.loadLocalPropertyTypes(object),
      this.loadGlobalPropertyTypes()
    ]).then(([localPropertyTypes, globalPropertyTypes]) => {
      const codes = [
        ...localPropertyTypes,
        ...globalPropertyTypes
      ].map(propertyType => propertyType.getCode())

      const criteria = new openbis.PropertyAssignmentSearchCriteria()
      criteria.withPropertyType().withCodes().thatIn(codes)

      const fo = new openbis.PropertyAssignmentFetchOptions()
      fo.withPropertyType()

      return openbis.searchPropertyAssignments(criteria, fo).then(result => {
        const map = {}

        result.getObjects().forEach(assignment => {
          const code = assignment.getPropertyType().getCode()
          map[code] = (map[code] || 0) + 1
        })

        return map
      })
    })
  }

  async loadLocalPropertyTypes(object) {
    const strategy = this._getStrategy(object.type)

    if (object.type === strategy.getNewObjectType()) {
      return Promise.resolve([])
    }

    const id = new openbis.EntityTypePermId(object.id)
    const fo = strategy.createTypeFetchOptions()
    fo.withPropertyAssignments().withPropertyType().withRegistrator()

    return strategy.getTypes([id], fo).then(map => {
      const type = map[object.id]

      if (type) {
        return type.getPropertyAssignments().map(assignment => {
          return assignment.getPropertyType()
        })
      } else {
        return []
      }
    })
  }

  async loadGlobalPropertyTypes() {
    const criteria = new openbis.PropertyTypeSearchCriteria()
    const fo = new openbis.PropertyTypeFetchOptions()
    fo.withMaterialType()
    fo.withVocabulary()
    fo.withRegistrator()
    return openbis.searchPropertyTypes(criteria, fo).then(results => {
      return results.getObjects().filter(propertyType => {
        return !propertyType.getCode().includes('.')
      })
    })
  }

  async loadValidationPlugins(type) {
    const criteria = new openbis.PluginSearchCriteria()
    criteria.withPluginType().thatEquals(openbis.PluginType.ENTITY_VALIDATION)
    const fo = new openbis.PluginFetchOptions()
    return this._loadPlugins(criteria, fo, type)
  }

  async loadDynamicPlugins(type) {
    const criteria = new openbis.PluginSearchCriteria()
    criteria.withPluginType().thatEquals(openbis.PluginType.DYNAMIC_PROPERTY)
    const fo = new openbis.PluginFetchOptions()
    return this._loadPlugins(criteria, fo, type)
  }

  async loadVocabularies() {
    let criteria = new openbis.VocabularySearchCriteria()
    let fo = new openbis.VocabularyFetchOptions()
    return openbis
      .searchVocabularies(criteria, fo)
      .then(result => result.objects)
  }

  async loadVocabularyTerms(vocabulary) {
    const criteria = new openbis.VocabularyTermSearchCriteria()
    criteria.withVocabulary().withCode().thatEquals(vocabulary)

    const fo = new openbis.VocabularyTermFetchOptions()
    fo.sortBy().code().asc()
    fo.from(0).count(10)

    return openbis
      .searchVocabularyTerms(criteria, fo)
      .then(result => result.objects)
  }

  async loadMaterialTypes() {
    let criteria = new openbis.MaterialTypeSearchCriteria()
    let fo = new openbis.MaterialTypeFetchOptions()
    return openbis
      .searchMaterialTypes(criteria, fo)
      .then(result => result.objects)
  }

  async loadSampleTypes() {
    let criteria = new openbis.SampleTypeSearchCriteria()
    let fo = new openbis.SampleTypeFetchOptions()
    return openbis
      .searchSampleTypes(criteria, fo)
      .then(result => result.objects)
  }

  async loadMaterials(materialType) {
    const criteria = new openbis.MaterialSearchCriteria()
    if (materialType) {
      criteria.withType().withCode().thatEquals(materialType)
    }

    const fo = new openbis.MaterialFetchOptions()
    fo.sortBy().code().asc()
    fo.from(0).count(10)

    return openbis.searchMaterials(criteria, fo).then(result => result.objects)
  }

  async loadSamples(sampleType) {
    const criteria = new openbis.SampleSearchCriteria()
    if (sampleType) {
      criteria.withType().withCode().thatEquals(sampleType)
    }

    const fo = new openbis.SampleFetchOptions()
    fo.sortBy().identifier().asc()
    fo.from(0).count(10)

    return openbis.searchSamples(criteria, fo).then(result => result.objects)
  }

  async executeOperations(operations, options) {
    return openbis.executeOperations(operations, options)
  }

  _loadPlugins(criteria, fo, type) {
    const strategy = this._getStrategy(type)
    return openbis.searchPlugins(criteria, fo).then(results => {
      return results.getObjects().filter(plugin => {
        return plugin.getEntityKinds().includes(strategy.getEntityKind())
      })
    })
  }

  _loadTypeUsages(object) {
    const strategy = this._getStrategy(object.type)

    if (object.type === strategy.getNewObjectType()) {
      return Promise.resolve(0)
    }

    function createTypeUsedOperation(typeCode) {
      const criteria = strategy.createEntitySearchCriteria()
      criteria.withType().withCode().thatEquals(typeCode)

      const fo = strategy.createEntityFetchOptions()
      fo.from(0)
      fo.count(0)

      return strategy.createEntitySearchOperation(criteria, fo)
    }

    const id = new openbis.EntityTypePermId(object.id)
    const fo = strategy.createTypeFetchOptions()

    return strategy.getTypes([id], fo).then(map => {
      const type = map[object.id]

      if (type) {
        const operations = [createTypeUsedOperation(type.getCode())]
        const options = new openbis.SynchronousOperationExecutionOptions()

        return openbis.executeOperations(operations, options).then(result => {
          const results = result.getResults()
          return results[0].getSearchResult().getTotalCount()
        })
      } else {
        return 0
      }
    })
  }

  async _loadPropertyLocalUsages(object) {
    const strategy = this._getStrategy(object.type)

    if (object.type === strategy.getNewObjectType()) {
      return Promise.resolve({})
    }

    function createPropertyUsedOperation(typeCode, propertyTypeCode) {
      const criteria = strategy.createEntitySearchCriteria()
      criteria.withType().withCode().thatEquals(typeCode)
      criteria.withProperty(propertyTypeCode)

      const fo = strategy.createEntityFetchOptions()
      fo.from(0)
      fo.count(0)

      return strategy.createEntitySearchOperation(criteria, fo)
    }

    const id = new openbis.EntityTypePermId(object.id)
    const fo = strategy.createTypeFetchOptions()
    fo.withPropertyAssignments().withPropertyType()

    return strategy.getTypes([id], fo).then(map => {
      const type = map[object.id]

      if (type) {
        const operations = []

        type.getPropertyAssignments().forEach(assignment => {
          operations.push(
            createPropertyUsedOperation(
              type.getCode(),
              assignment.getPropertyType().getCode()
            )
          )
        })

        const options = new openbis.SynchronousOperationExecutionOptions()
        options.setExecuteInOrder(true)

        return openbis.executeOperations(operations, options).then(result => {
          const results = result.getResults()
          const map = {}

          type.getPropertyAssignments().forEach((assignment, index) => {
            map[assignment.getPropertyType().getCode()] = results[index]
              .getSearchResult()
              .getTotalCount()
          })

          return map
        })
      } else {
        return {}
      }
    })
  }

  async _loadPropertyGlobalUsages(object) {
    const strategies = [
      new ObjectTypeStrategy(),
      new CollectionTypeStrategy(),
      new DataSetTypeStrategy(),
      new MaterialTypeStrategy()
    ]

    function createPropertyUsedOperation(strategy, propertyTypeCode) {
      const criteria = strategy.createEntitySearchCriteria()
      criteria.withProperty(propertyTypeCode)

      const fo = strategy.createEntityFetchOptions()
      fo.from(0)
      fo.count(0)

      return strategy.createEntitySearchOperation(criteria, fo)
    }

    return Promise.all([
      this.loadLocalPropertyTypes(object),
      this.loadGlobalPropertyTypes()
    ]).then(([localPropertyTypes, globalPropertyTypes]) => {
      const propertyTypes = [...localPropertyTypes, ...globalPropertyTypes]
      const operations = []

      propertyTypes.forEach(globalPropertyType => {
        strategies.forEach(strategy => {
          operations.push(
            createPropertyUsedOperation(strategy, globalPropertyType.getCode())
          )
        })
      })

      const options = new openbis.SynchronousOperationExecutionOptions()
      options.setExecuteInOrder(true)

      return openbis.executeOperations(operations, options).then(result => {
        const results = result.getResults()
        const map = {}

        propertyTypes.forEach((globalPropertyType, i) => {
          let usages = 0
          strategies.forEach((strategy, j) => {
            const index = i * strategies.length + j
            usages += results[index].getSearchResult().getTotalCount()
          })
          map[globalPropertyType.getCode()] = usages
        })

        return map
      })
    })
  }

  _getStrategy(type) {
    const strategies = new TypeFormControllerStrategies()
    strategies.extendObjectTypeStrategy(new ObjectTypeStrategy())
    strategies.extendCollectionTypeStrategy(new CollectionTypeStrategy())
    strategies.extendDataSetTypeStrategy(new DataSetTypeStrategy())
    strategies.extendMaterialTypeStrategy(new MaterialTypeStrategy())
    return strategies.getStrategy(type)
  }
}

class ObjectTypeStrategy {
  createTypeFetchOptions() {
    return new openbis.SampleTypeFetchOptions()
  }
  createEntityFetchOptions() {
    return new openbis.SampleFetchOptions()
  }
  createEntitySearchCriteria() {
    return new openbis.SampleSearchCriteria()
  }
  createEntitySearchOperation(criteria, fo) {
    return new openbis.SearchSamplesOperation(criteria, fo)
  }
  getTypes(ids, fo) {
    return openbis.getSampleTypes(ids, fo)
  }
}

class CollectionTypeStrategy {
  createTypeFetchOptions() {
    return new openbis.ExperimentTypeFetchOptions()
  }
  createEntityFetchOptions() {
    return new openbis.ExperimentFetchOptions()
  }
  createEntitySearchCriteria() {
    return new openbis.ExperimentSearchCriteria()
  }
  createEntitySearchOperation(criteria, fo) {
    return new openbis.SearchExperimentsOperation(criteria, fo)
  }
  getTypes(ids, fo) {
    return openbis.getExperimentTypes(ids, fo)
  }
}

class DataSetTypeStrategy {
  createTypeFetchOptions() {
    return new openbis.DataSetTypeFetchOptions()
  }
  createEntityFetchOptions() {
    return new openbis.DataSetFetchOptions()
  }
  createEntitySearchCriteria() {
    return new openbis.DataSetSearchCriteria()
  }
  createEntitySearchOperation(criteria, fo) {
    return new openbis.SearchDataSetsOperation(criteria, fo)
  }
  getTypes(ids, fo) {
    return openbis.getDataSetTypes(ids, fo)
  }
}

class MaterialTypeStrategy {
  createTypeFetchOptions() {
    return new openbis.MaterialTypeFetchOptions()
  }
  createEntityFetchOptions() {
    return new openbis.MaterialFetchOptions()
  }
  createEntitySearchCriteria() {
    return new openbis.MaterialSearchCriteria()
  }
  createEntitySearchOperation(criteria, fo) {
    return new openbis.SearchMaterialsOperation(criteria, fo)
  }
  getTypes(ids, fo) {
    return openbis.getMaterialTypes(ids, fo)
  }
}
