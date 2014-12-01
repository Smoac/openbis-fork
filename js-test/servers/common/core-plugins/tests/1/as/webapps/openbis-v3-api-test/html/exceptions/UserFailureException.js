/**
 *  The <code>UserFailureException</code> is the super class of all exceptions that have their
 *  cause in an inappropriate usage of the system. This implies that the user himself (without help
 *  of an administrator) can fix the problem.
 *  
 *  @author Bernd Rinn
 */
var UserFailureException = function(message) {
	this['@type'] = 'UserFailureException';
    HighLevelException.call(this, message);
};
stjs.extend(UserFailureException, HighLevelException, [], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    /**
     *  Creates an {@link UserFailureException} using a {@link java.util.Formatter}.
     */
    constructor.fromTemplate = function(messageTemplate, args) {
        return new UserFailureException(String.format(messageTemplate, args));
    };
    /**
     *  Creates an {@link UserFailureException} using a {@link java.util.Formatter}.
     */
    constructor.fromTemplate = function(cause, messageTemplate, args) {
        return new UserFailureException(String.format(messageTemplate, args), cause);
    };
}, {});
