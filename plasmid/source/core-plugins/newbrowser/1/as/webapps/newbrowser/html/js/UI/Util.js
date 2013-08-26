var Util = new function() {
	this.getEmptyIfNull = function(toCheck) {
		if(	toCheck === undefined ||
			toCheck === null ||
			toCheck === "�(undefined)") {
			return "";
		} else {
			return toCheck;
		}
	}
	
	this.blockUI = function() {
		$.blockUI({ message: '<h1><img src="./js/busy.gif" /> Just a moment...</h1>' });
	}
	
	this.unblockUI = function() {
		$.unblockUI();
	}
} 