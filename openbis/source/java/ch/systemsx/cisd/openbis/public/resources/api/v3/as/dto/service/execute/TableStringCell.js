define([ "stjs", "util/Exceptions", "as/dto/service/execute/ITableCell" ], function(stjs, exceptions, ITableCell) {
	var TableStringCell = function(value) {
		this.setValue(value);
	};
	stjs.extend(TableStringCell, null, [ ITableCell ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.TableStringCell';
		constructor.serialVersionUID = 1;
		prototype.value = null;
		prototype.getValue = function() {
			return this.value;
		};
		prototype.setValue = function(value) {
			this.value = value;
		};
		prototype.toString = function() {
			return this.value;
		};
		prototype.hashCode = function() {
			return this.value == null ? 0 : this.value;
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
			var that = obj;
			if (this.value == null) {
				return that.value === null;
			}
			return this.value === that.value;
		};
	}, {});
	return TableStringCell;
})
