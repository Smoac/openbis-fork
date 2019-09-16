function MoveEntityView(moveEntityController, moveEntityModel) {
	this.repaint = function() {
		var $window = $('<form>', { 'action' : 'javascript:void(0);' });
		$window.submit(function() {
			Util.unblockUI();
			moveEntityController.move();
		});
		
		switch(moveEntityModel.entity["@type"]) {
			case "as.dto.experiment.Experiment":
			case "as.dto.sample.Sample":
			case "as.dto.project.Project":
				$window.append($('<legend>').append("Moving " + moveEntityModel.entity.getIdentifier() + " To:"));
				break;
			case "as.dto.dataset.DataSet":
				$window.append($('<legend>').append("Moving " + moveEntityModel.entity.getPermId() + " To:"));
				break;
		}
		
		var $searchBox = $('<div>');
		$window.append($searchBox);
		var advancedEntitySearchDropdown = null;
		
		switch(moveEntityModel.entity["@type"]) {
			case "as.dto.experiment.Experiment":
				advancedEntitySearchDropdown = new AdvancedEntitySearchDropdown(false, true, "search entity to move to",
						false, false, false, true, false);
				break;
			case "as.dto.sample.Sample":
				advancedEntitySearchDropdown = new AdvancedEntitySearchDropdown(false, true, "search entity to move to",
						true, false, false, false, false);
				break;
			case "as.dto.project.Project":
				advancedEntitySearchDropdown = new AdvancedEntitySearchDropdown(false, true, "search entity to move to",
						false, false, false, false, true);
				break;
			case "as.dto.dataset.DataSet":
				advancedEntitySearchDropdown = new AdvancedEntitySearchDropdown(false, true, "search entity to move to",
						true, true, false, false, false);
				break;
		}
		
		var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept' });		
		var $btnCancel = $('<a>', { 'class' : 'btn btn-default' }).append('Cancel');
		$btnCancel.click(function() {
			Util.unblockUI();
		});
		
		$window.append('<br>').append($btnAccept).append('&nbsp;').append($btnCancel);
		advancedEntitySearchDropdown.onChange(function(selected) {
			moveEntityModel.selected = selected[0];
		});
		
		advancedEntitySearchDropdown.init($searchBox);
		
		var css = {
				'text-align' : 'left',
				'top' : '15%',
				'width' : '70%',
				'left' : '15%',
				'right' : '20%',
				'overflow' : 'hidden'
		};
		
		Util.blockUI($window, css);
	}
}