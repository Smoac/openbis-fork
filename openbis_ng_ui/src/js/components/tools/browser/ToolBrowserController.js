import openbis from '@src/js/services/openbis.js'
import actions from '@src/js/store/actions/actions.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'
import BrowserController from '@src/js/components/common/browser/BrowserController.js'

export default class ToolBrowserController extends BrowserController {
  doGetPage() {
    return pages.TOOLS
  }

  async doLoadNodes() {
    return Promise.all([
      openbis.searchPlugins(
        new openbis.PluginSearchCriteria(),
        new openbis.PluginFetchOptions()
      )
    ]).then(([plugins]) => {
      const dynamicPropertyPluginNodes = plugins
        .getObjects()
        .filter(
          plugin => plugin.pluginType === openbis.PluginType.DYNAMIC_PROPERTY
        )
        .map(plugin => {
          return {
            id: `dynamicPropertyPlugin/${plugin.name}`,
            text: plugin.name,
            object: {
              type: objectType.DYNAMIC_PROPERTY_PLUGIN,
              id: plugin.name
            },
            canMatchFilter: true,
            canRemove: true
          }
        })

      const entityValidationPluginNodes = plugins
        .getObjects()
        .filter(
          plugin => plugin.pluginType === openbis.PluginType.ENTITY_VALIDATION
        )
        .map(plugin => {
          return {
            id: `entityValidationPlugin/${plugin.name}`,
            text: plugin.name,
            object: {
              type: objectType.ENTITY_VALIDATION_PLUGIN,
              id: plugin.name
            },
            canMatchFilter: true,
            canRemove: true
          }
        })

      let nodes = [
        {
          id: 'dynamicPropertyPlugins',
          text: 'Dynamic Property Plugins',
          children: dynamicPropertyPluginNodes,
          childrenType: objectType.NEW_DYNAMIC_PROPERTY_PLUGIN,
          canAdd: true
        },
        {
          id: 'entityValidationPlugins',
          text: 'Entity Validation Plugins',
          children: entityValidationPluginNodes,
          childrenType: objectType.NEW_ENTITY_VALIDATION_PLUGIN,
          canAdd: true
        }
      ]

      return nodes
    })
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
    const reason = 'deleted via ng_ui'

    return this._prepareRemoveOperations(type, id, reason)
      .then(operations => {
        const options = new openbis.SynchronousOperationExecutionOptions()
        options.setExecuteInOrder(true)
        return openbis.executeOperations(operations, options)
      })
      .then(() => {
        this.context.dispatch(actions.objectDelete(this.getPage(), type, id))
      })
      .catch(error => {
        this.context.dispatch(actions.errorChange(error))
      })
  }

  _prepareRemoveOperations(type, id, reason) {
    if (
      type === objectType.DYNAMIC_PROPERTY_PLUGIN ||
      type === objectType.ENTITY_VALIDATION_PLUGIN
    ) {
      return this._prepareRemovePluginOperations(id, reason)
    } else {
      throw new Error('Unsupported type: ' + type)
    }
  }

  _prepareRemovePluginOperations(id, reason) {
    const options = new openbis.PluginDeletionOptions()
    options.setReason(reason)
    return Promise.resolve([
      new openbis.DeletePluginsOperation(
        [new openbis.PluginPermId(id)],
        options
      )
    ])
  }

  doGetObservedModifications() {
    return {
      [objectType.DYNAMIC_PROPERTY_PLUGIN]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.ENTITY_VALIDATION_PLUGIN]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ]
    }
  }
}
