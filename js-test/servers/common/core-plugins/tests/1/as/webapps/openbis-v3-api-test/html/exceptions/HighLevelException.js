/**
 *  An exception that is "high-level" in the sense that we have a pretty good understanding what the
 *  failure means in the context where the exception has been thrown.
 *  
 *  @author Bernd Rinn
 */
var HighLevelException = function(message) {
	this['@type'] = 'HighLevelException';
    RuntimeException.call(this, message);
};
stjs.extend(HighLevelException, RuntimeException, [], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    /**
     *  Returns the assessment of the subsystem throwing the exception whether the failure could be
     *  temporarily and thus retrying the operation (on a higher level) could possibly help to cure
     *  the problem.
     *  <p>
     *  This class will always return <code>false</code>, but sub classes can override the method.
     *  
     *  @return Whether retrying the operation can possibly rectify the situation or not.
     */
    prototype.isRetriable = function() {
        return false;
    };
    /**
     *  Returns the assessment of the subsystem throwing the exception whether the failure could be
     *  temporarily and thus retrying the operation (on a higher level) could possibly help to cure
     *  the problem.
     *  <p>
     *  This class will always return <code>false</code>, but sub classes can override the method.
     *  
     *  @return Whether retrying the operation can possibly rectify the situation or not.
     */
    constructor.isRetriable = function(th) {
        if (stjs.isInstanceOf(th.constructor, HighLevelException)) {
            return (th).isRetriable();
        } else {
            return false;
        }
    };
}, {});
