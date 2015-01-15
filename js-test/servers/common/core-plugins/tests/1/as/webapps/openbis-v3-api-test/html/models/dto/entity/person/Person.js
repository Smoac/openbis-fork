/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define(["support/stjs"], function (stjs) {
    var Person = function() {};
    stjs.extend(Person, null, [], function(constructor, prototype) {
        prototype['@type'] = 'Person';
        constructor.serialVersionUID = 1;
        prototype.fetchOptions = null;
        prototype.userId = null;
        prototype.firstName = null;
        prototype.lastName = null;
        prototype.email = null;
        prototype.registrationDate = null;
        prototype.active = null;
        prototype.space = null;
        prototype.registrator = null;
        prototype.getFetchOptions = function() {
            return this.fetchOptions;
        };
        prototype.setFetchOptions = function(fetchOptions) {
            this.fetchOptions = fetchOptions;
        };
        prototype.getUserId = function() {
            return this.userId;
        };
        prototype.setUserId = function(userId) {
            this.userId = userId;
        };
        prototype.getFirstName = function() {
            return this.firstName;
        };
        prototype.setFirstName = function(firstName) {
            this.firstName = firstName;
        };
        prototype.getLastName = function() {
            return this.lastName;
        };
        prototype.setLastName = function(lastName) {
            this.lastName = lastName;
        };
        prototype.getEmail = function() {
            return this.email;
        };
        prototype.setEmail = function(email) {
            this.email = email;
        };
        prototype.getRegistrationDate = function() {
            return this.registrationDate;
        };
        prototype.setRegistrationDate = function(registrationDate) {
            this.registrationDate = registrationDate;
        };
        prototype.isActive = function() {
            return this.active;
        };
        prototype.setActive = function(active) {
            this.active = active;
        };
        prototype.getSpace = function() {
            if (this.getFetchOptions().hasSpace()) {
                return this.space;
            } else {
                 throw new NotFetchedException("Space has not been fetched.");
            }
        };
        prototype.setSpace = function(space) {
            this.space = space;
        };
        prototype.getRegistrator = function() {
            if (this.getFetchOptions().hasRegistrator()) {
                return this.registrator;
            } else {
                 throw new NotFetchedException("Registrator has not been fetched.");
            }
        };
        prototype.setRegistrator = function(registrator) {
            this.registrator = registrator;
        };
    }, {fetchOptions: "PersonFetchOptions", registrationDate: "Date", space: "Space", registrator: "Person"});
    return Person;
})