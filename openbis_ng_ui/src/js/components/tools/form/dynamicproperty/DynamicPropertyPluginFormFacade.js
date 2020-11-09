import openbis from '@src/js/services/openbis.js'

export default class DynamicPropertyPluginFormFacade {
  async loadPlugin(pluginName) {
    const id = new openbis.PluginPermId(pluginName)
    const fo = new openbis.PluginFetchOptions()
    fo.withScript()
    return openbis.getPlugins([id], fo).then(map => {
      return map[pluginName]
    })
  }

  async executeOperations(operations, options) {
    return openbis.executeOperations(operations, options)
  }
}
