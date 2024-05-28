import openbis from '@src/js/services/openbis.js'

export default class ImportAllFormFacade {
  async import(file, updateMode) {
    return new Promise((resolve, reject) => {
      openbis.uploadToSessionWorkspace(file)
        .then(() => {
          const serviceId = new openbis.CustomASServiceCode('xls-import')
          const serviceOptions = new openbis.CustomASServiceExecutionOptions()

          serviceOptions.withParameter('method', 'import')
          serviceOptions.withParameter('mode', updateMode)
          serviceOptions.withParameter('fileName', file.name)

          return openbis.executeService(serviceId, serviceOptions)
            .then(result => resolve(result))
            .catch(error => reject(error))
        }, (error) => reject(error));
    });
  }
}
