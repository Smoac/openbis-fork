var NotFetchedException = function(message, cause) {
	this['@type'] = 'NotFetchedException';
    UserFailureException.call(this, message, cause);
};

stjs.extend(NotFetchedException, UserFailureException, [], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
}, {});
