/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define(["support/stjs"], function (stjs) {
    var Tag = function() {};
    stjs.extend(Tag, null, [], function(constructor, prototype) {
        prototype['@type'] = 'Tag';
        constructor.serialVersionUID = 1;
        prototype.fetchOptions = null;
        prototype.permId = null;
        prototype.code = null;
        prototype.description = null;
        prototype.isPrivate = null;
        prototype.registrationDate = null;
        prototype.owner = null;
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
        prototype.isPrivate = function() {
            return this.isPrivate;
        };
        prototype.setPrivate = function(isPrivate) {
            this.isPrivate = isPrivate;
        };
        prototype.getRegistrationDate = function() {
            return this.registrationDate;
        };
        prototype.setRegistrationDate = function(registrationDate) {
            this.registrationDate = registrationDate;
        };
        prototype.getOwner = function() {
            if (this.getFetchOptions().hasOwner()) {
                return this.owner;
            } else {
                 throw new NotFetchedException("Owner has not been fetched.");
            }
        };
        prototype.setOwner = function(owner) {
            this.owner = owner;
        };
    }, {fetchOptions: "TagFetchOptions", permId: "TagPermId", registrationDate: "Date", owner: "Person"});
    return Tag;
})