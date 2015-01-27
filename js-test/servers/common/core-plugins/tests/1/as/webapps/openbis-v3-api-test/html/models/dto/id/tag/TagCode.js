/**
 *  Tag code.
 *  
 *  @author Franz-Josef Elmer
 *  @author Jakub Straszewski
 */
define(["support/stjs", "sys/exceptions", "dto/id/tag/ITagId"], function (stjs, exceptions, ITagId) {
    var TagCode = /**
     *  @param code Tag code, e.g. "MY_TAG".
     */
    function(code) {
        this.setCode(code);
    };
    stjs.extend(TagCode, null, [ITagId], function(constructor, prototype) {
        prototype['@type'] = 'TagCode';
        constructor.serialVersionUID = 1;
        prototype.code = null;
        prototype.getCode = function() {
            return this.code;
        };
        prototype.setCode = function(code) {
            if (code == null) {
                 throw new exceptions.IllegalArgumentException("Code cannot be null");
            }
            this.code = code;
        };
        prototype.toString = function() {
            return this.getCode();
        };
        prototype.hashCode = function() {
            return ((this.getCode() == null) ? 0 : this.getCode().hashCode());
        };
        prototype.equals = function(obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            var other = obj;
            return this.getCode() == null ? this.getCode() == other.getCode() : this.getCode().equals(other.getCode());
        };
    }, {});
    return TagCode;
})