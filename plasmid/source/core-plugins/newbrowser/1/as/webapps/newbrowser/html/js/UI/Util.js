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
	
	this.showError = function(withHTML) {
		jError(
				withHTML,
				{
				  autoHide : false,
				  clickOverlay : true,
				  MinWidth : 250,
				  TimeShown : 2000,
				  ShowTimeEffect : 200,
				  HideTimeEffect : 200,
				  LongTrip :20,
				  HorizontalPosition : 'center',
				  VerticalPosition : 'top',
				  ShowOverlay : true,
		   		  ColorOverlay : '#000',
				  OpacityOverlay : 0.3,
				  onClosed : function(){ },
				  onCompleted : function(){ }
		});
	}
	
	this.showSuccess = function(withHTML) {
		jSuccess(
				withHTML,
				{
				  autoHide : false,
				  clickOverlay : true,
				  MinWidth : 250,
				  TimeShown : 2000,
				  ShowTimeEffect : 200,
				  HideTimeEffect : 200,
				  LongTrip :20,
				  HorizontalPosition : 'center',
				  VerticalPosition : 'top',
				  ShowOverlay : true,
		   		  ColorOverlay : '#000',
				  OpacityOverlay : 0.3,
				  onClosed : function(){ },
				  onCompleted : function(){ }
		});
	}
	
	this.showInfo = function(withHTML) {
		jNotify(
				withHTML,
				{
				  autoHide : false,
				  clickOverlay : true,
				  MinWidth : 250,
				  TimeShown : 2000,
				  ShowTimeEffect : 200,
				  HideTimeEffect : 200,
				  LongTrip :20,
				  HorizontalPosition : 'center',
				  VerticalPosition : 'top',
				  ShowOverlay : true,
		   		  ColorOverlay : '#000',
				  OpacityOverlay : 0.3,
				  onClosed : function(){ },
				  onCompleted : function(){ }
		});
	}
} 