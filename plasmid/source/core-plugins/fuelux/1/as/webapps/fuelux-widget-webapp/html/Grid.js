function Grid(columns, getDataList) {
	this.init(columns, getDataList);
}

$.extend(Grid.prototype, {
	init : function(columns, getDataList) {
		this.columns = columns;
		this.getDataList = getDataList;
	},

	render : function() {
		var thisGrid = this;

		thisGrid.panel = $("<div>").addClass("fuelux");

		$.get("Grid.html", function(template) {
			thisGrid.panel.html(template);

			var columnList = thisGrid.panel.find(".columnDropdown").find("ul");
			columnList.click(function(e) {
				e.stopPropagation();
			});

			thisGrid.columns.forEach(function(column, columnIndex) {
				var checkbox = $("<input>").attr("type", "checkbox").attr("value", column.property).attr("checked", "checked");
				checkbox.change(function() {
					thisGrid.panel.repeater('render');
				});
				var label = $("<label>").attr("role", "menuitem").addClass("checkbox").text(column.label).append(checkbox);
				var item = $("<li>").attr("role", "presentation").append(label);
				columnList.append(item);
			});

			thisGrid.panel.repeater({
				defaultView : "list",
				dataSource : function(options, callback) {
					if (options.view == "list") {
						thisGrid.list(options, callback);
					}
				},
				list_selectable : false,
				list_rowRendered : function(helpers, callback) {
					$(helpers.item).click(function() {
						var rowIndex = helpers.item[0].rowIndex;
						thisGrid.notifyRowClickListeners({
							"index" : rowIndex,
							"data" : thisGrid.result.datas[rowIndex],
							"item" : thisGrid.result.items[rowIndex]
						});
					});

					callback();
				},
				list_noItemsHTML : 'No items found'
			});
		});

		return thisGrid.panel;
	},

	list : function(options, callback) {
		var thisGrid = this;

		thisGrid.getDataList(function(dataList) {
			var result = {
				"columns" : []
			};

			// columns
			thisGrid.panel.find(".columnDropdown").find("input:checked").each(function(index, element) {
				thisGrid.columns.forEach(function(column) {
					var checkbox = $(element);
					if (column.property == checkbox.val()) {
						result.columns.push(column);
					}
				});
			});

			// add a dummy empty column (repeater does not properly handle
			// visibility of the last column)
			result.columns.push({
				label : null,
				property : null,
				sortable : false
			});

			// filter
			if (options.search) {
				var filter = options.search.toLowerCase();
				dataList = dataList.filter(function(data) {
					return thisGrid.columns.some(function(column) {
						if (column.filter) {
							return column.filter(data, filter);
						} else {
							var value = "" + data[column.property];
							return value != null && value.toLowerCase().indexOf(filter) != -1;
						}
					});
				});
			}

			// sort
			if (options.sortDirection && options.sortProperty) {
				var sortColumn = null;
				thisGrid.columns.forEach(function(column) {
					if (column.property == options.sortProperty) {
						sortColumn = column;
					}
				});
				if (sortColumn) {
					var sortFunction = null;
					var sortDirection = options.sortDirection == "asc" ? 1 : -1;

					if (sortColumn.sort) {
						sortFunction = sortColumn.sort;
					} else {
						sortFunction = function(data1, data2) {
							var value1 = data1[sortColumn.property];
							var value2 = data2[sortColumn.property];
							return naturalSort(value1, value2);
						};
					}

					dataList.sort(function(data1, data2) {
						return sortDirection * sortFunction(data1, data2);
					});
				}
			}

			// page
			result.count = dataList.length;
			result.datas = [];
			result.items = [];

			var defaultPageSize = 50;
			var startIndex = options.pageIndex * (options.pageSize || defaultPageSize);
			var endIndex = startIndex + (options.pageSize || defaultPageSize);
			endIndex = (endIndex <= result.count) ? endIndex : result.count;

			result.page = options.pageIndex;
			result.pages = Math.ceil(result.count / (options.pageSize || defaultPageSize));
			result.start = startIndex + 1;
			result.end = endIndex;

			// render
			var items = [];
			for (var i = startIndex; i < endIndex; i++) {
				var data = dataList[i];
				var item = {};
				thisGrid.columns.forEach(function(column) {
					var value = null;
					if (column.render) {
						value = column.render(data);
					} else {
						value = data[column.property];
					}
					item[column.property] = value;
				});
				result.datas.push(data);
				result.items.push(item);
			}

			thisGrid.result = result;

			// add some delay (repeater does not properly layout columns without
			// it)
			setTimeout(function() {
				callback(result);
			}, 1);
		});
	},

	addRowClickListener : function(listener) {
		if (!this.rowClickListeners) {
			this.rowClickListeners = [];
		}
		this.rowClickListeners.push(listener);
	},

	notifyRowClickListeners : function(event) {
		if (this.rowClickListeners) {
			this.rowClickListeners.forEach(function(listener) {
				listener(event);
			});
		}
	}

});
