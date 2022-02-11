import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'
import actions from '@src/js/store/actions/actions.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'
import BrowserController from '@src/js/components/common/browser/BrowserController.js'
import users from '@src/js/common/consts/users'
import messages from '@src/js/common/messages.js'

export default class TypeBrowserController extends BrowserController {
  doGetPage() {
    return pages.TYPES
  }

  async doLoadNodes() {
    return Promise.all([
      openbis.searchSampleTypes(
        new openbis.SampleTypeSearchCriteria(),
        new openbis.SampleTypeFetchOptions()
      ),
      openbis.searchExperimentTypes(
        new openbis.ExperimentTypeSearchCriteria(),
        new openbis.ExperimentTypeFetchOptions()
      ),
      openbis.searchDataSetTypes(
        new openbis.DataSetTypeSearchCriteria(),
        new openbis.DataSetTypeFetchOptions()
      ),
      openbis.searchMaterialTypes(
        new openbis.MaterialTypeSearchCriteria(),
        new openbis.MaterialTypeFetchOptions()
      ),
      openbis.searchVocabularies(
        new openbis.VocabularySearchCriteria(),
        new openbis.VocabularyFetchOptions()
      ),
      openbis.searchPropertyTypes(
        new openbis.PropertyTypeSearchCriteria(),
        new openbis.PropertyTypeFetchOptions()
      )
    ]).then(
      ([
        objectTypes,
        collectionTypes,
        dataSetTypes,
        materialTypes,
        vocabularyTypes,
        propertyTypes
      ]) => {
        const _createNodes = (types, typeName, callback) => {
          return _.map(types, type => {
            const node = {
              id: `${typeName}s/${type.code}`,
              text: type.code,
              object: { type: typeName, id: type.code },
              canMatchFilter: true,
              canRemove: true
            }
            if (callback) {
              callback(type, node)
            }
            return node
          })
        }

        let objectTypeNodes = _createNodes(
          objectTypes.getObjects(),
          objectType.OBJECT_TYPE
        )
        let collectionTypeNodes = _createNodes(
          collectionTypes.getObjects(),
          objectType.COLLECTION_TYPE
        )
        let dataSetTypeNodes = _createNodes(
          dataSetTypes.getObjects(),
          objectType.DATA_SET_TYPE
        )
        let materialTypeNodes = _createNodes(
          materialTypes.getObjects(),
          objectType.MATERIAL_TYPE
        )
        let vocabularyTypeNodes = _createNodes(
          vocabularyTypes.getObjects(),
          objectType.VOCABULARY_TYPE,
          (type, node) => {
            node.canRemove = !type.managedInternally || this.isSystemUser()
          }
        )
        let propertyTypeNodes = _createNodes(
          propertyTypes.getObjects(),
          objectType.PROPERTY_TYPE,
          (type, node) => {
            node.canRemove = !type.managedInternally || this.isSystemUser()
          }
        )

        let nodes = [
          {
            id: 'objectTypes',
            text: messages.get(messages.OBJECT_TYPES),
            object: { type: objectType.OVERVIEW, id: objectType.OBJECT_TYPE },
            children: objectTypeNodes,
            childrenType: objectType.NEW_OBJECT_TYPE,
            canAdd: true
          },
          {
            id: 'collectionTypes',
            text: messages.get(messages.COLLECTION_TYPES),
            object: {
              type: objectType.OVERVIEW,
              id: objectType.COLLECTION_TYPE
            },
            children: collectionTypeNodes,
            childrenType: objectType.NEW_COLLECTION_TYPE,
            canAdd: true
          },
          {
            id: 'dataSetTypes',
            text: messages.get(messages.DATA_SET_TYPES),
            object: { type: objectType.OVERVIEW, id: objectType.DATA_SET_TYPE },
            children: dataSetTypeNodes,
            childrenType: objectType.NEW_DATA_SET_TYPE,
            canAdd: true
          },
          {
            id: 'materialTypes',
            text: messages.get(messages.MATERIAL_TYPES),
            object: { type: objectType.OVERVIEW, id: objectType.MATERIAL_TYPE },
            children: materialTypeNodes,
            childrenType: objectType.NEW_MATERIAL_TYPE,
            canAdd: true
          },
          {
            id: 'vocabularyTypes',
            text: messages.get(messages.VOCABULARY_TYPES),
            object: {
              type: objectType.OVERVIEW,
              id: objectType.VOCABULARY_TYPE
            },
            children: vocabularyTypeNodes,
            childrenType: objectType.NEW_VOCABULARY_TYPE,
            canAdd: true
          },
          {
            id: 'propertyTypes',
            text: messages.get(messages.PROPERTY_TYPES),
            object: {
              type: objectType.OVERVIEW,
              id: objectType.PROPERTY_TYPE
            },
            children: propertyTypeNodes,
            childrenType: objectType.NEW_PROPERTY_TYPE,
            canAdd: true
          }
        ]

        return nodes
      }
    )
  }

  doNodeAdd(node) {
    if (node && node.childrenType) {
      this.context.dispatch(
        actions.objectNew(this.getPage(), node.childrenType)
      )
    }
  }

  doNodeRemove(node) {
    if (!node.object) {
      return Promise.resolve()
    }

    const { type, id } = node.object

    var operation = this._prepareRemoveOperation(type, id, 'deleted via ng_ui')

    const options = new openbis.SynchronousOperationExecutionOptions()
    options.setExecuteInOrder(true)

    return openbis
      .executeOperations([operation], options)
      .then(() => {
        this.context.dispatch(actions.objectDelete(this.getPage(), type, id))
      })
      .catch(error => {
        this.context.dispatch(actions.errorChange(error))
      })
  }

  _prepareRemoveOperation(type, id, reason) {
    if (type === objectType.OBJECT_TYPE) {
      const options = new openbis.SampleTypeDeletionOptions()
      options.setReason(reason)
      return new openbis.DeleteSampleTypesOperation(
        [new openbis.EntityTypePermId(id)],
        options
      )
    } else if (type === objectType.COLLECTION_TYPE) {
      const options = new openbis.ExperimentTypeDeletionOptions()
      options.setReason(reason)
      return new openbis.DeleteExperimentTypesOperation(
        [new openbis.EntityTypePermId(id)],
        options
      )
    } else if (type === objectType.DATA_SET_TYPE) {
      const options = new openbis.DataSetTypeDeletionOptions()
      options.setReason(reason)
      return new openbis.DeleteDataSetTypesOperation(
        [new openbis.EntityTypePermId(id)],
        options
      )
    } else if (type === objectType.MATERIAL_TYPE) {
      const options = new openbis.MaterialTypeDeletionOptions()
      options.setReason(reason)
      return new openbis.DeleteMaterialTypesOperation(
        [new openbis.EntityTypePermId(id)],
        options
      )
    } else if (type === objectType.VOCABULARY_TYPE) {
      const options = new openbis.VocabularyDeletionOptions()
      options.setReason(reason)
      return new openbis.DeleteVocabulariesOperation(
        [new openbis.VocabularyPermId(id)],
        options
      )
    } else if (type === objectType.PROPERTY_TYPE) {
      const options = new openbis.PropertyTypeDeletionOptions()
      options.setReason(reason)
      return new openbis.DeletePropertyTypesOperation(
        [new openbis.PropertyTypePermId(id)],
        options
      )
    }
  }

  doGetObservedModifications() {
    return {
      [objectType.OBJECT_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.COLLECTION_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.DATA_SET_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.MATERIAL_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.VOCABULARY_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.PROPERTY_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ]
    }
  }

  isSystemUser() {
    return (
      this.context.getProps().session &&
      this.context.getProps().session.userName === users.SYSTEM
    )
  }
}
