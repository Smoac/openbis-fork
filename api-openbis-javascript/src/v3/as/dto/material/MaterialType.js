/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define(['stjs', 'util/Exceptions'], function (stjs, exceptions) {
  var MaterialType = function () {}
  stjs.extend(
    MaterialType,
    null,
    [],
    function (constructor, prototype) {
      prototype['@type'] = 'as.dto.material.MaterialType'
      constructor.serialVersionUID = 1
      prototype.fetchOptions = null
      prototype.permId = null
      prototype.code = null
      prototype.description = null
      prototype.modificationDate = null
      prototype.propertyAssignments = null
      prototype.validationPlugin = null
      prototype.managedInternally = null
      prototype.getPropertyAssignments = function () {
        if (
          this.getFetchOptions() &&
          this.getFetchOptions().hasPropertyAssignments()
        ) {
          return this.propertyAssignments
        } else {
          throw new exceptions.NotFetchedException(
            'Property assignments have not been fetched.'
          )
        }
      }
      prototype.setPropertyAssignments = function (propertyAssignments) {
        this.propertyAssignments = propertyAssignments
      }
      prototype.getValidationPlugin = function () {
        if (
          this.getFetchOptions() &&
          this.getFetchOptions().hasValidationPlugin()
        ) {
          return this.validationPlugin
        } else {
          throw new exceptions.NotFetchedException(
            'Validation plugin have not been fetched.'
          )
        }
      }
      prototype.setValidationPlugin = function (validationPlugin) {
        this.validationPlugin = validationPlugin
      }
      prototype.getFetchOptions = function () {
        return this.fetchOptions
      }
      prototype.setFetchOptions = function (fetchOptions) {
        this.fetchOptions = fetchOptions
      }
      prototype.getPermId = function () {
        return this.permId
      }
      prototype.setPermId = function (permId) {
        this.permId = permId
      }
      prototype.getCode = function () {
        return this.code
      }
      prototype.setCode = function (code) {
        this.code = code
      }
      prototype.getDescription = function () {
        return this.description
      }
      prototype.setDescription = function (description) {
        this.description = description
      }
      prototype.getModificationDate = function () {
        return this.modificationDate
      }
      prototype.setModificationDate = function (modificationDate) {
        this.modificationDate = modificationDate
      }
      prototype.isManagedInternally = function() {
          return this.managedInternally;
      };
      prototype.setManagedInternally = function(managedInternally) {
          this.managedInternally = managedInternally;
      };
      prototype.toString = function () {
        return this.getCode()
      }
    },
    {
      fetchOptions: 'MaterialTypeFetchOptions',
      permId: 'EntityTypePermId',
      modificationDate: 'Date',
      propertyAssignments: {
        name: 'List',
        arguments: ['PropertyAssignment']
      },
      validationPlugin: 'Plugin'
    }
  )
  return MaterialType
})
