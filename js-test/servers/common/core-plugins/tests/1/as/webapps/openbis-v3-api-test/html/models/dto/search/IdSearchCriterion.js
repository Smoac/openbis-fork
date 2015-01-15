/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, AbstractSearchCriterion) {
    var IdSearchCriterion = function() {
        AbstractSearchCriterion.call(this);
    };
    stjs.extend(IdSearchCriterion, AbstractSearchCriterion, [AbstractSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'IdSearchCriterion';
        constructor.serialVersionUID = 1;
        prototype.id = null;
        prototype.thatEquals = function(id) {
            this.id = id;
        };
        prototype.getId = function() {
            return this.id;
        };
        prototype.hashCode = function() {
            return ((this.id == null) ? 0 : this.id.hashCode());
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
            return this.id == null ? this.id == other.id : this.id.equals(other.id);
        };
    }, {});
    return IdSearchCriterion;
})