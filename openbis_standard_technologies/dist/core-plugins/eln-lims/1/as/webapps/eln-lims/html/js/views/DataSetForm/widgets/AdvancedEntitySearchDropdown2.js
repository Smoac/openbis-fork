function AdvancedEntitySearchDropdown2(placeholder) {
	var placeholder = placeholder;
	this.isMultiple = true;
	this.isRequired = false;
	var onChangeCallback = null;
	
	this.onChange = function(onChangeCallbackGiven) {
		onChangeCallback = onChangeCallbackGiven
	}

	this.search = function(query, callback) {
		callback([]);
	}
	
	this.renderResult = function(result) {
		return [];
	}
	
	var $select = FormUtil.getPlainDropdown({}, "");
	
	this.init = function($container) {
		var _this = this;
		$select.attr("multiple", "multiple");
		if (this.isRequired) {
			$select.attr("required", "required");
		}
		if (this.isMultiple) {
			maximumSelectionLength = 9999;
		} else {
			maximumSelectionLength = 1;
		}
		$container.append($select);

		$select.select2({
			width: '100%', 
			theme: "bootstrap",
			maximumSelectionLength: maximumSelectionLength,
			minimumInputLength: 2,
			placeholder : placeholder,
			ajax: {
				delay: 1000,
				processResults: function (data) {
					return {
						"results": _this.renderResult(data),
						"pagination": {
							"more": false
						}
					};
				},
				transport: function (params, success, failure) {
					var query = params.data.q;
					_this.search(query, success);
					return {
						abort : function() { /*Not implemented*/ }
					}
				}
			}
		});

		if (onChangeCallback) {
			var onSelectOrUnselect = function (e) {
				onChangeCallback($select.select2('data'));
			};
			$select.on('select2:select', onSelectOrUnselect);
			$select.on('select2:unselect', onSelectOrUnselect);
		}
	}

}