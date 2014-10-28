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

		$.get("./lib/grid/js/Grid.html", function(template) {
			thisGrid.panel.html(template);
			thisGrid.renderColumnDropdown();
			thisGrid.renderCSVButton();
			thisGrid.panel.repeater({
				defaultView : "list",
				dataSource : function(options, callback) {
					if (options.view == "list") {
						thisGrid.list(options, callback);
					}
				},
				list_selectable : false,
				list_noItemsHTML : 'No items found',
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
				}
			});
		});

		return thisGrid.panel;
	},

	renderColumnDropdown : function() {
		var thisGrid = this;

		var columnList = thisGrid.panel.find(".columnDropdown").find("ul");
		columnList.click(function(e) {
			e.stopPropagation();
		});

		thisGrid.columns.forEach(function(column, columnIndex) {
			var checkbox = $("<input>").attr("type", "checkbox").attr("value", column.property).attr("checked", "checked").attr("style", "margin-left: 5px;");
			checkbox.change(function() {
				thisGrid.panel.repeater('render');
			});
			var label = $("<label>").attr("role", "menuitem").addClass("checkbox").append(column.label).append(checkbox);
			var item = $("<li>").attr("role", "presentation").append(label);
			columnList.append(item);
		});
	},

	getAllColumns : function() {
		return this.columns;
	},

	renderCSVButton: function() {
		var thisGrid = this;
		var a = thisGrid.panel.find("#download-button");
		a.click(function() {
			var headings = [];
			thisGrid.getVisibleColumns().forEach(function(head) {
				headings.push(head.property);
			});
			var data = thisGrid.result.items;
			
			var csv = CSV.objectToCsv(data, {columns: headings});
			var blob = new Blob([csv], {type: 'text'});
			saveAs(blob,'exportedTable.csv');
		});
	},
	
	getVisibleColumns : function() {
		var thisGrid = this;
		var columns = [];

		thisGrid.panel.find(".columnDropdown").find("input:checked").each(function(index, element) {
			thisGrid.getAllColumns().forEach(function(column) {
				var checkbox = $(element);
				if (column.property == checkbox.val()) {
					columns.push(column);
				}
			});
		});

		// HACK: Add a dummy empty column (repeater does not properly handle visibility of the last column)
		columns.push({
			label : null,
			property : null,
			sortable : false
		});

		return columns;
	},

	filterData : function(dataList, filter) {
		var thisGrid = this;

		if (filter) {
			filter = filter.toLowerCase();
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

		return dataList;
	},

	sortData : function(dataList, sortProperty, sortDirection) {
		var thisGrid = this;

		if (sortProperty && sortDirection) {
			var sortColumn = null;
			thisGrid.columns.forEach(function(column) {
				if (column.property == sortProperty) {
					sortColumn = column;
				}
			});
			if (sortColumn) {
				var sortFunction = null;
				var sortDirection = sortDirection == "asc" ? 1 : -1;

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

		return dataList;
	},

	renderData : function(dataList) {
		var thisGrid = this;
		var items = [];

		dataList.forEach(function(data) {
			var item = {};
			thisGrid.getVisibleColumns().forEach(function(column) {
				var value = null;
				if (column.render) {
					value = column.render(data);
				} else {
					value = data[column.property];
				}
				item[column.property] = value;
			});
			items.push(item);
		});

		return items;
	},

	list : function(options, callback) {
		var thisGrid = this;

		thisGrid.getDataList(function(dataList) {

			dataList = thisGrid.filterData(dataList, options.search);
			dataList = thisGrid.sortData(dataList, options.sortProperty, options.sortDirection);

			var result = {};
			var defaultPageSize = 50;
			var startIndex = options.pageIndex * (options.pageSize || defaultPageSize);
			var endIndex = startIndex + (options.pageSize || defaultPageSize);
			endIndex = (endIndex <= result.count) ? endIndex : dataList.length;

			result.count = dataList.length;
			result.datas = [];
			result.items = [];
			result.columns = thisGrid.getVisibleColumns();
			result.page = options.pageIndex;
			result.pages = Math.ceil(result.count / (options.pageSize || defaultPageSize));
			result.start = startIndex + 1;
			result.end = endIndex;

			dataList = dataList.slice(startIndex, endIndex);
			itemList = thisGrid.renderData(dataList);
			itemList.forEach(function(item, index) {
				result.datas.push(dataList[index]);
				result.items.push(item);
			});

			// add some delay (repeater does not properly layout columns without it)
			setTimeout(function() {
				thisGrid.result = result;
				callback(result);
				$(thisGrid.panel).hide().show(0); // HACK: Fixes Chrome rendering issues when refreshing the grid
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
