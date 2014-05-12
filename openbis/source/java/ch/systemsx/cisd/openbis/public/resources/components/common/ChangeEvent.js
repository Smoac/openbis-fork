define([ "jquery" ], function($) {

	//
	// CHANGE EVENT
	//

	function ChangeEvent(oldValue, newValue) {
		this.init(oldValue, newValue);
	}

	$.extend(ChangeEvent.prototype, {

		init : function(oldValue, newValue) {
			this.oldValue = oldValue;
			this.newValue = newValue;
		},

		getOldValue : function() {
			return this.oldValue;
		},

		getNewValue : function() {
			return this.newValue;
		}

	});

	return ChangeEvent;

});