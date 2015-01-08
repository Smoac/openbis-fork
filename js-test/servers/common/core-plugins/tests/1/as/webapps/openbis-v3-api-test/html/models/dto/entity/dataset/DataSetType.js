/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([], function () {
    var DataSetType = function() {};
    stjs.extend(DataSetType, null, [], function(constructor, prototype) {
        prototype['@type'] = 'DataSetType';
        constructor.serialVersionUID = 1;
        prototype.fetchOptions = null;
        prototype.permId = null;
        prototype.code = null;
        prototype.description = null;
        prototype.modificationDate = null;
        prototype.getFetchOptions = function() {
            return this.fetchOptions;
        };
        prototype.setFetchOptions = function(fetchOptions) {
            this.fetchOptions = fetchOptions;
        };
        prototype.getPermId = function() {
            return this.permId;
        };
        prototype.setPermId = function(permId) {
            this.permId = permId;
        };
        prototype.getCode = function() {
            return this.code;
        };
        prototype.setCode = function(code) {
            this.code = code;
        };
        prototype.getDescription = function() {
            return this.description;
        };
        prototype.setDescription = function(description) {
            this.description = description;
        };
        prototype.getModificationDate = function() {
            return this.modificationDate;
        };
        prototype.setModificationDate = function(modificationDate) {
            this.modificationDate = modificationDate;
        };
    }, {fetchOptions: "DataSetTypeFetchOptions", permId: "EntityTypePermId", modificationDate: "Date"});
    return DataSetType;
})