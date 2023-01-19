import _ from 'lodash'
import BrowserCommon from '@src/js/components/common/browser/BrowserCommon.js'
import DatabaseBrowserCommon from '@src/js/components/database/browser/DatabaseBrowserCommon.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class DatabaseBrowserControllerLoadNodesUnfiltered {
  async doLoadUnfilteredNodes(params) {
    const { node } = params

    const rootNode = BrowserCommon.rootNode()

    if (node.internalRoot) {
      return {
        nodes: [rootNode]
      }
    } else if (node.object.type === rootNode.object.type) {
      const nodes = []

      await this.addSpacesNode(params, nodes)
      await this.addSamplesNode(params, nodes)

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.SPACE) {
      const nodes = []

      await this.addProjectsNode(params, nodes)
      await this.addSamplesNode(params, nodes)

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.PROJECT) {
      const nodes = []

      await this.addExperimentsNode(params, nodes)
      await this.addSamplesNode(params, nodes)

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.COLLECTION) {
      const nodes = []

      await this.addSamplesNode(params, nodes)
      await this.addDataSetsNode(params, nodes)

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.OBJECT) {
      const nodes = []

      await this.addSamplesNode(params, nodes)
      await this.addDataSetsNode(params, nodes)

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.DATA_SET) {
      const nodes = []

      await this.addDataSetsNode(params, nodes)

      return {
        nodes: nodes
      }
    } else if (node.object.type === DatabaseBrowserCommon.TYPE_SPACES) {
      const spaces = await this.searchSpaces(params)
      const nodes = spaces.objects.map(space => {
        const spaceNode = DatabaseBrowserCommon.spaceNode(node, space.getCode())
        spaceNode.canHaveChildren = true
        return spaceNode
      })
      return {
        nodes,
        totalCount: spaces.totalCount
      }
    } else if (node.object.type === DatabaseBrowserCommon.TYPE_PROJECTS) {
      const projects = await this.searchProjects(params)
      const nodes = projects.objects.map(project => {
        const projectNode = DatabaseBrowserCommon.projectNode(
          node,
          project.getPermId().getPermId(),
          project.getCode()
        )
        projectNode.canHaveChildren = true
        return projectNode
      })
      return {
        nodes,
        totalCount: projects.totalCount
      }
    } else if (node.object.type === DatabaseBrowserCommon.TYPE_COLLECTIONS) {
      const experiments = await this.searchExperiments(params)
      const nodes = experiments.objects.map(experiment => {
        const experimentNode = DatabaseBrowserCommon.collectionNode(
          node,
          experiment.getPermId().getPermId(),
          experiment.getCode()
        )
        experimentNode.canHaveChildren = true
        return experimentNode
      })
      return {
        nodes,
        totalCount: experiments.totalCount
      }
    } else if (
      node.object.type === DatabaseBrowserCommon.TYPE_OBJECTS ||
      node.object.type === DatabaseBrowserCommon.TYPE_OBJECT_CHILDREN
    ) {
      const samples = await this.searchSamples(params)
      const nodes = samples.objects.map(sample => {
        const sampleNode = DatabaseBrowserCommon.objectNode(
          node,
          sample.getPermId().getPermId(),
          sample.getCode()
        )
        sampleNode.canHaveChildren = true
        return sampleNode
      })
      return {
        nodes,
        totalCount: samples.totalCount
      }
    } else if (
      node.object.type === DatabaseBrowserCommon.TYPE_DATA_SETS ||
      node.object.type === DatabaseBrowserCommon.TYPE_DATA_SET_CHILDREN
    ) {
      const dataSets = await this.searchDataSets(params)
      const nodes = dataSets.objects.map(dataSet => {
        const dataSetNode = DatabaseBrowserCommon.dataSetNode(
          node,
          dataSet.getCode()
        )
        dataSetNode.canHaveChildren = true
        return dataSetNode
      })
      return {
        nodes,
        totalCount: dataSets.totalCount
      }
    }

    return {
      nodes: []
    }
  }

  async searchSpaces(params) {
    const { node, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.SpaceSearchCriteria()
    if (!_.isEmpty(childrenIn)) {
      criteria.withCodes().thatIn(childrenIn.map(child => child.object.id))
    }

    const fetchOptions = new openbis.SpaceFetchOptions()
    if (!_.isEmpty(node.sortings) && !_.isNil(node.sortingId)) {
      const sorting = node.sortings[node.sortingId]
      if (!_.isNil(sorting)) {
        fetchOptions.sortBy()[sorting.sortBy]()[sorting.sortDirection]()
      }
    }

    const result = await openbis.searchSpaces(criteria, fetchOptions)

    if (!_.isEmpty(childrenNotIn)) {
      const childrenNotInMap = {}
      childrenNotIn.forEach(child => {
        childrenNotInMap[child.object.id] = child
      })
      result.objects = result.objects.filter(object =>
        _.isNil(childrenNotInMap[object.getCode()])
      )
      result.totalCount = result.objects.length
    }

    if (!_.isNil(offset) && !_.isNil(limit)) {
      result.objects = result.objects.slice(offset, offset + limit)
    }

    return result
  }

  async searchProjects(params) {
    const { node, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.ProjectSearchCriteria()
    if (node.parent.object.type === objectType.SPACE) {
      criteria.withSpace().withCode().thatEquals(node.parent.object.id)
    }

    const fetchOptions = new openbis.ProjectFetchOptions()
    if (!_.isEmpty(node.sortings) && !_.isNil(node.sortingId)) {
      const sorting = node.sortings[node.sortingId]
      if (!_.isNil(sorting)) {
        fetchOptions.sortBy()[sorting.sortBy]()[sorting.sortDirection]()
      }
    }

    const result = await openbis.searchProjects(criteria, fetchOptions)

    if (!_.isEmpty(childrenIn)) {
      const childrenInMap = {}
      childrenIn.forEach(child => {
        childrenInMap[child.object.id] = child
      })
      result.objects = result.objects.filter(
        object => !_.isNil(childrenInMap[object.getPermId().getPermId()])
      )
      result.totalCount = result.objects.length
    }

    if (!_.isEmpty(childrenNotIn)) {
      const childrenNotInMap = {}
      childrenNotIn.forEach(child => {
        childrenNotInMap[child.object.id] = child
      })
      result.objects = result.objects.filter(object =>
        _.isNil(childrenNotInMap[object.getPermId().getPermId()])
      )
      result.totalCount = result.objects.length
    }

    if (!_.isNil(offset) && !_.isNil(limit)) {
      result.objects = result.objects.slice(offset, offset + limit)
    }

    return result
  }

  async searchExperiments(params) {
    const { node, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.ExperimentSearchCriteria()
    if (node.parent.object.type === objectType.PROJECT) {
      criteria.withProject().withPermId().thatEquals(node.parent.object.id)
    }
    if (!_.isEmpty(childrenIn)) {
      const subcriteria = criteria.withSubcriteria()
      subcriteria.withOrOperator()
      childrenIn.forEach(child => {
        subcriteria.withPermId().thatEquals(child.object.id)
      })
    }
    if (!_.isEmpty(childrenNotIn)) {
      const subcriteria = criteria.withSubcriteria().negate()
      subcriteria.withOrOperator()
      childrenNotIn.forEach(child => {
        subcriteria.withPermId().thatEquals(child.object.id)
      })
    }

    const fetchOptions = new openbis.ExperimentFetchOptions()
    if (!_.isEmpty(node.sortings) && !_.isNil(node.sortingId)) {
      const sorting = node.sortings[node.sortingId]
      if (!_.isNil(sorting)) {
        fetchOptions.sortBy()[sorting.sortBy]()[sorting.sortDirection]()
      }
    }
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    return await openbis.searchExperiments(criteria, fetchOptions)
  }

  async searchSamples(params) {
    const { node, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.SampleSearchCriteria()
    criteria.withAndOperator()

    if (node.parent.object.type === BrowserCommon.TYPE_ROOT) {
      criteria.withoutSpace()
      criteria.withoutProject()
      criteria.withoutExperiment()
    }
    if (node.parent.object.type === objectType.SPACE) {
      criteria.withSpace().withPermId().thatEquals(node.parent.object.id)
      criteria.withoutProject()
    }
    if (node.parent.object.type === objectType.PROJECT) {
      criteria.withProject().withPermId().thatEquals(node.parent.object.id)
      criteria.withoutExperiment()
    }
    if (node.parent.object.type === objectType.COLLECTION) {
      criteria.withExperiment().withPermId().thatEquals(node.parent.object.id)
    }
    if (node.parent.object.type === objectType.OBJECT) {
      criteria.withParents().withPermId().thatEquals(node.parent.object.id)
    }
    if (!_.isEmpty(childrenIn)) {
      const subcriteria = criteria.withSubcriteria()
      subcriteria.withOrOperator()
      childrenIn.forEach(child => {
        subcriteria.withPermId().thatEquals(child.object.id)
      })
    }
    if (!_.isEmpty(childrenNotIn)) {
      const subcriteria = criteria.withSubcriteria().negate()
      subcriteria.withOrOperator()
      childrenNotIn.forEach(child => {
        subcriteria.withPermId().thatEquals(child.object.id)
      })
    }

    const fetchOptions = new openbis.SampleFetchOptions()
    if (!_.isEmpty(node.sortings) && !_.isNil(node.sortingId)) {
      const sorting = node.sortings[node.sortingId]
      if (!_.isNil(sorting)) {
        fetchOptions.sortBy()[sorting.sortBy]()[sorting.sortDirection]()
      }
    }
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    return await openbis.searchSamples(criteria, fetchOptions)
  }

  async searchDataSets(params) {
    const { node, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.DataSetSearchCriteria()
    criteria.withAndOperator()

    if (node.parent.object.type === objectType.COLLECTION) {
      criteria.withExperiment().withPermId().thatEquals(node.parent.object.id)
      criteria.withoutSample()
    }
    if (node.parent.object.type === objectType.OBJECT) {
      criteria.withSample().withPermId().thatEquals(node.parent.object.id)
    }

    if (node.parent.object.type === objectType.DATA_SET) {
      criteria.withParents().withPermId().thatEquals(node.parent.object.id)
    }
    if (!_.isEmpty(childrenIn)) {
      const subcriteria = criteria.withSubcriteria()
      subcriteria.withOrOperator()
      childrenIn.forEach(child => {
        subcriteria.withCode().thatEquals(child.object.id)
      })
    }
    if (!_.isEmpty(childrenNotIn)) {
      const subcriteria = criteria.withSubcriteria().negate()
      subcriteria.withOrOperator()
      childrenNotIn.forEach(child => {
        subcriteria.withCode().thatEquals(child.object.id)
      })
    }

    const fetchOptions = new openbis.DataSetFetchOptions()
    if (!_.isEmpty(node.sortings) && !_.isNil(node.sortingId)) {
      const sorting = node.sortings[node.sortingId]
      if (!_.isNil(sorting)) {
        fetchOptions.sortBy()[sorting.sortBy]()[sorting.sortDirection]()
      }
    }
    fetchOptions.from(offset)
    fetchOptions.count(limit)

    return await openbis.searchDataSets(criteria, fetchOptions)
  }

  async addSpacesNode(params, nodes) {
    const { node, sortingIds } = params

    const spacesNode = DatabaseBrowserCommon.spacesFolderNode(node)
    spacesNode.sortings = DatabaseBrowserCommon.SORTINGS
    spacesNode.sortingId =
      sortingIds[spacesNode.id] || DatabaseBrowserCommon.SORT_BY_CODE_ASC

    const spaces = await this.searchSpaces({
      node: spacesNode,
      offset: 0,
      limit: 0
    })

    if (spaces.totalCount > 0) {
      nodes.push(spacesNode)
    }
  }

  async addProjectsNode(params, nodes) {
    const { node, sortingIds } = params

    const projectsNode = DatabaseBrowserCommon.projectsFolderNode(node)
    projectsNode.sortings = DatabaseBrowserCommon.SORTINGS
    projectsNode.sortingId =
      sortingIds[projectsNode.id] || DatabaseBrowserCommon.SORT_BY_CODE_ASC

    const projects = await this.searchProjects({
      node: projectsNode,
      offset: 0,
      limit: 0
    })

    if (projects.totalCount > 0) {
      nodes.push(projectsNode)
    }
  }

  async addExperimentsNode(params, nodes) {
    const { node, sortingIds } = params

    const experimentsNode = DatabaseBrowserCommon.collectionsFolderNode(node)
    experimentsNode.sortings = DatabaseBrowserCommon.SORTINGS
    experimentsNode.sortingId =
      sortingIds[experimentsNode.id] || DatabaseBrowserCommon.SORT_BY_CODE_ASC

    const experiments = await this.searchExperiments({
      node: experimentsNode,
      offset: 0,
      limit: 0
    })

    if (experiments.totalCount > 0) {
      nodes.push(experimentsNode)
    }
  }

  async addSamplesNode(params, nodes) {
    const { node, sortingIds } = params

    let samplesNode = null

    if (node.object.type === objectType.OBJECT) {
      samplesNode = DatabaseBrowserCommon.objectsChildrenFolderNode(node)
    } else {
      samplesNode = DatabaseBrowserCommon.objectsFolderNode(node)
    }

    samplesNode.sortings = DatabaseBrowserCommon.SORTINGS
    samplesNode.sortingId =
      sortingIds[samplesNode.id] || DatabaseBrowserCommon.SORT_BY_CODE_ASC

    const samples = await this.searchSamples({
      node: samplesNode,
      offset: 0,
      limit: 0
    })

    if (samples.totalCount > 0) {
      nodes.push(samplesNode)
    }
  }

  async addDataSetsNode(params, nodes) {
    const { node, sortingIds } = params

    let dataSetsNode = null

    if (node.object.type === objectType.DATA_SET) {
      dataSetsNode = DatabaseBrowserCommon.dataSetsChildrenFolderNode(node)
    } else {
      dataSetsNode = DatabaseBrowserCommon.dataSetsFolderNode(node)
    }

    dataSetsNode.sortings = DatabaseBrowserCommon.SORTINGS
    dataSetsNode.sortingId =
      sortingIds[dataSetsNode.id] || DatabaseBrowserCommon.SORT_BY_CODE_ASC

    const dataSets = await this.searchDataSets({
      node: dataSetsNode,
      offset: 0,
      limit: 0
    })

    if (dataSets.totalCount > 0) {
      nodes.push(dataSetsNode)
    }
  }
}
