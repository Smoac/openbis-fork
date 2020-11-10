import _ from 'lodash'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class PluginFormControllerLoad extends PageControllerLoad {
  async load(object, isNew) {
    let loadedPlugin = null

    if (!isNew) {
      loadedPlugin = await this.facade.loadPlugin(object.id)
      if (!loadedPlugin) {
        return
      }
    }

    const plugin = this._createPlugin(object, loadedPlugin)

    return this.context.setState({
      plugin
    })
  }

  _createPlugin(object, loadedPlugin) {
    let pluginKind = null
    let pluginType = null

    if (loadedPlugin) {
      pluginKind = _.get(loadedPlugin, 'pluginKind')
      pluginType = _.get(loadedPlugin, 'pluginType')
    } else {
      pluginKind = openbis.PluginKind.JYTHON

      if (this.controller.isDynamicPropertyType()) {
        pluginType = openbis.PluginType.DYNAMIC_PROPERTY
      } else if (this.controller.isEntityValidationType()) {
        pluginType = openbis.PluginType.ENTITY_VALIDATION
      } else {
        throw new Error('Unsupported object type: ' + object.type)
      }
    }

    const entityKinds = _.get(loadedPlugin, 'entityKinds', [])

    const plugin = {
      id: _.get(loadedPlugin, 'name', null),
      pluginKind,
      pluginType,
      name: FormUtil.createField({
        value: _.get(loadedPlugin, 'name', null),
        enabled: loadedPlugin === null
      }),
      entityKind: FormUtil.createField({
        value: entityKinds.length === 1 ? entityKinds[0] : null,
        enabled: loadedPlugin === null
      }),
      description: FormUtil.createField({
        value: _.get(loadedPlugin, 'description', null)
      }),
      script: FormUtil.createField({
        value: _.get(loadedPlugin, 'script', null)
      })
    }
    if (loadedPlugin) {
      plugin.original = _.cloneDeep(plugin)
    }
    return plugin
  }
}
