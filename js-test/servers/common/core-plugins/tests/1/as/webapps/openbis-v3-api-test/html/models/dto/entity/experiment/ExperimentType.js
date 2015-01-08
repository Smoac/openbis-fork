/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([], function () {
    var ExperimentType = function() {};
    stjs.extend(ExperimentType, null, [], function(constructor, prototype) {
        prototype['@type'] = 'ExperimentType';
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
    }, {fetchOptions: "ExperimentTypeFetchOptions", permId: "EntityTypePermId", modificationDate: "Date"});
    return ExperimentType;
})