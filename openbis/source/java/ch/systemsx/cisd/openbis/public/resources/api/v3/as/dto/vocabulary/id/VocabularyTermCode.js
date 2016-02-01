/**
 * Vocabulary term code.
 * 
 * @author pkupczyk
 */
define([ "stjs", "util/Exceptions", "as/dto/vocabulary/id/IVocabularyTermId" ], function(stjs, exceptions, IVocabularyTermId) {
	/**
	 * @param code
	 *            Vocabulary term code, e.g. "MY_TERM".
	 */
	var VocabularyTermCode = function(code) {
		this.setCode(code);
	};
	stjs.extend(VocabularyTermCode, null, [ IVocabularyTermId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.id.VocabularyTermCode';
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
	return VocabularyTermCode;
})