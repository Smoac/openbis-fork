function Grid(columns, getDataList, showAllColumns, tableSettings, onChangeState, isMultiselectable) {
	this.init(columns, getDataList, showAllColumns, tableSettings, onChangeState, isMultiselectable);
}

$.extend(Grid.prototype, {
	init : function(columns, getDataList, showAllColumns, tableSettings, onChangeState, isMultiselectable) {
		this.columns = columns;
		this.getDataList = getDataList;
		this.showAllColumns = showAllColumns;
		this.tableSettings = tableSettings;
		if(!this.tableSettings) {
			this.tableSettings = {
					columns : null,
					pageSize : null,
					sort : {
							sortProperty : null,
							sortDirection : null
					}
			}
		}
		this.onChangeState = onChangeState;
		
		this.isMultiselectable = isMultiselectable;
		this.selectedItems = [];
		if(isMultiselectable) {
			this.addMultiSelect(columns);
		}
	},
	addMultiSelect : function(columns) {
		var _this = this;
		columns.unshift({
				showByDefault: true,
				label : function() {
					var $selectall = $("<input>", { type : 'checkbox' });
					$selectall.change(function(){
						var allCheckboxes = _this.panel.find(".multi-selectable-checkbox");
						var isChecked = $(this).is(":checked");
						//check / uncheck all
						allCheckboxes.each(function() { 
			                this.checked = isChecked;
			            });
						//If check, add to the selectedItems list
						_this.selectedItems = [];
						if(isChecked) { //select all
							_this.getDataList(function(dataList) {
								for(var dIdx = 0; dIdx < dataList.length; dIdx++) {
									_this.selectedItems.push(dataList[dIdx].$object);
								}
							});
						}
					});
					
					return $selectall;
				},
				property : '$selected',
				isExportable: false,
				sortable : false,
				render : function(data) {
					var $checkbox = $("<input>", { type : 'checkbox' , class: "repeater-checkbox multi-selectable-checkbox"});
					$checkbox.change(function() {
						var isChecked = $(this).is(":checked");
						if(isChecked){ //add data to selectedItems
							_this.selectedItems.push(data.$object);
						} else { //remove data from selectedItems
							var toRemoveId = null;
							if(data.$object.permId) {
								toRemoveId = data.$object.permId;
							}
							if(toRemoveId) {
								for(var idx = 0; idx < _this.selectedItems.length; idx++) {
									var foundId = null;
									if(_this.selectedItems[idx].permId) {
										foundId = _this.selectedItems[idx].permId;
									}
									if(toRemoveId === foundId) {
										_this.selectedItems.splice(idx, 1);
									}
								}
							}
						}
					});
					$checkbox.click(function(e) {
						e.stopPropagation();
					});
					return $checkbox;
				}
		});
	},
	getSelected : function() {
		return this.selectedItems;
	},
	render : function() {
		var thisGrid = this;

		thisGrid.panel = $("<div>").addClass("fuelux");

		$.get("./lib/grid/js/Grid.html", function(template) {
			
			//Set default pageSize on template - there is no programming API and we don't want to modify the library
			var templateToReplace = null;
			var templateToReplaceFor = null;
			if(thisGrid.onChangeState && thisGrid.tableSettings && thisGrid.tableSettings.pageSize) {
				templateToReplace = "<li data-value=\"" + thisGrid.tableSettings.pageSize + "\">";
				templateToReplaceFor = "<li data-value=\"" + thisGrid.tableSettings.pageSize + "\" data-selected=\"true\">";
			} else {
				templateToReplace = "<li data-value=\"10\">";
				templateToReplaceFor = "<li data-value=\"10\" data-selected=\"true\">";
			}
			template = template.replace(templateToReplace, templateToReplaceFor);
			//
			thisGrid.panel.html(template);
			thisGrid.renderColumnDropdown();
			thisGrid.renderDropDownOptions();
			
			if(thisGrid.rowClickListeners && thisGrid.rowClickListeners.length > 0) {
				thisGrid.panel.addClass("fuelux-selectable");
			}
			
			thisGrid.panel.repeater({
				defaultView : "list",
				dataSource : function(options, callback) {
					//Set default sort
					if(	thisGrid.onChangeState && //If settings are stored
						thisGrid.tableSettings && //And settings are available
						thisGrid.tableSettings.sort && thisGrid.tableSettings.sort.sortProperty && thisGrid.tableSettings.sort.sortDirection && //And sort settings are available
						!options.sortProperty && !options.sortDirection //And no sort options are available by default because the table just loaded
					) { //Set stored sort options to default
						options.sortProperty = thisGrid.tableSettings.sort.sortProperty;
						options.sortDirection = thisGrid.tableSettings.sort.sortDirection;
					}
					//
					if (options.view == "list") {
						//Save default pageSize
						if(thisGrid.onChangeState && thisGrid.tableSettings) {
							thisGrid.tableSettings.pageSize = options.pageSize;
							thisGrid.onChangeState(thisGrid.tableSettings);
						}
						//
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
							"data" : thisGrid.result.datas[rowIndex-1],
							"item" : thisGrid.result.items[rowIndex-1]
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
		
		var defaultNumColumns = 5; //Including last always
		
		thisGrid.columns.forEach(function(column, columnIndex) {
			if(!column.showByDefault) {
				var checkbox = $("<input>")
				.attr("type", "checkbox")
				.attr("value", column.property)
				.attr("style", "margin-left: 5px;");
			
				if(thisGrid.tableSettings && thisGrid.tableSettings.columns && Object.keys(thisGrid.tableSettings.columns).length !== 0) {
					if((thisGrid.tableSettings.columns[column.property] === true)) { //If settings are present
						checkbox.attr("checked", "checked");
					}
				} else if(thisGrid.showAllColumns || columnIndex < (defaultNumColumns - 1) || (columnIndex+1 === thisGrid.columns.length)) { //Defaults
					checkbox.attr("checked", "checked");
				}
				
				checkbox.change(function() {
					thisGrid.panel.repeater('render');
				});
				var label = $("<label>", { style : 'white-space: nowrap;' }).attr("role", "menuitem").append(checkbox).append("&nbsp;").append(column.label);
				var item = $("<li>").attr("role", "presentation").append(label);
				columnList.append(item);
			}
		});
	},

	getAllColumns : function() {
		return this.columns;
	},

	addExtraOptions : function(extraOptions) {
		this.extraOptions = extraOptions;
	},
	
	renderDropDownOptions: function() {
		var thisGrid = this;
		var columnList = thisGrid.panel.find(".optionsDropdown").find("ul");
		
		
		var options = {
				"" : false,
				"(plain text)" : true
		}
		
		var getOptionClickEvent = function(isAllRowsOrVisible, isAllColumnsOrVisible, plainText) {
			return function() {
				thisGrid.exportTSV(isAllRowsOrVisible, isAllColumnsOrVisible, plainText);
			}
		}
		// Export shown rows with shown columns
		for(option in options) {
			var labelSRSC = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
							.attr("role", "menuitem")
							.append("Export visible columns with visible rows " + option);

			var itemSRSC = $("<li>")
							.attr("role", "presentation")
							.attr("style", "margin-left: 5px; margin-right: 5px;")
							.append(labelSRSC);

			itemSRSC.click(getOptionClickEvent(false, false, options[option]));

			columnList.append(itemSRSC);
		}
		
		// Export shown rows with all columns
		for(option in options) {
			var labelSRAC = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
							.attr("role", "menuitem")
							.append("Export all columns with visible rows " + option);
	
			var itemSRAC = $("<li>")
							.attr("role", "presentation")
							.attr("style", "margin-left: 5px; margin-right: 5px;")
							.append(labelSRAC);
			
			itemSRAC.click(getOptionClickEvent(false, true, options[option]));
			
			columnList.append(itemSRAC);
		}
		
		// Export all rows with visible columns
		for(option in options) {
			var labelARSC = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
							.attr("role", "menuitem")
							.append("Export visible columns with all rows " + option);
	
			var itemARSC = $("<li>")
							.attr("role", "presentation")
							.attr("style", "margin-left: 5px; margin-right: 5px;")
							.append(labelARSC);
			
			var plainTextVal = options[option];
			itemARSC.click(getOptionClickEvent(true, false, options[option]));
			
			columnList.append(itemARSC);
		}
		
		// Export all rows with all columns
		for(option in options) {
			var labelARAC = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
							.attr("role", "menuitem")
							.append("Export all columns with all rows " + option);
	
			var itemARAC = $("<li>")
							.attr("role", "presentation")
							.attr("style", "margin-left: 5px; margin-right: 5px;")
							.append(labelARAC);
			
			var plainTextVal = options[option];
			itemARAC.click(getOptionClickEvent(true, true, options[option]));
			
			columnList.append(itemARAC);
		}
		
		
		if(this.extraOptions) {
			for(var oIdx = 0; oIdx < this.extraOptions.length; oIdx++) {
				var option = this.extraOptions[oIdx];
				var extraLabel = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
									.attr("role", "menuitem")
									.append(option.name);

				var extraItem = $("<li>")
							.attr("role", "presentation")
							.attr("style", "margin-left: 5px; margin-right: 5px;")
							.append(extraLabel);
				
				var getClick = function(option) {
					return function() {
						option.action(thisGrid.getSelected());
					}
				}
				
				extraItem.click(getClick(option));
				
				columnList.append(extraItem);
			}
		}
	},
	
	exportTSV : function(isAllRowsOrVisible, isAllColumnsOrVisible, plainText) {
		var _this = this;
		var disablePlanTextExportWarning = this.tableSettings.disablePlanTextExportWarning;
		if(plainText  && !disablePlanTextExportWarning) {
			var dontShowAnymore = "<input type='checkbox' id='disablePlanTextExportWarning'> Don't show this warning again.";
			
			Util.showWarning("<b>DO NOT USE THIS FILE FOR BATCH UPDATE!</b><br>This file does not contain rich text format. If used for Batch Update, all rich text format in the updated entries will be lost!<br><br>" + dontShowAnymore, function() {
				var isSelected = $("#disablePlanTextExportWarning")[0].checked;
				if(_this.onChangeState) {
					_this.tableSettings.disablePlanTextExportWarning = isSelected;
					_this.onChangeState(_this.tableSettings);
				}
				_this.exportTSVB(isAllRowsOrVisible, isAllColumnsOrVisible, plainText);
			});
		} else {
			_this.exportTSVB(isAllRowsOrVisible, isAllColumnsOrVisible, plainText);
		}
	},
	exportTSVB : function(isAllRowsOrVisible, isAllColumnsOrVisible, plainText) {
		var thisGrid = this;
		
		var exportColumnsFromData = function(namePrefix, data, headings) {
			
			var arrayOfRowArrays = [];
			arrayOfRowArrays.push(headings);
			for(var dIdx = 0; dIdx < data.length; dIdx++) {
				var rowAsArray = [];
				for(var hIdx = 0; hIdx < headings.length; hIdx++) {
					var headerKey = headings[hIdx];
					var rowValue = data[dIdx][headerKey];
					if(!rowValue) {
						rowValue = "";
					} else {
						var specialCharsRemover = document.createElement("textarea");
						specialCharsRemover.innerHTML = rowValue;
						rowValue = specialCharsRemover.value; //Removes special HTML Chars
						rowValue = String(rowValue).replace(/\r?\n|\r|\t/g, " "); //Remove carriage returns and tabs
						if(plainText === true){
							rowValue = String(rowValue).replace(/<(?:.|\n)*?>/gm, '');
						}
					}
					rowAsArray.push(rowValue);
				}
				arrayOfRowArrays.push(rowAsArray);
			}
			var tsv = $.tsv.formatRows(arrayOfRowArrays);
			var indexOfFirstLine = tsv.indexOf('\n');
			var tsvWithoutNumbers = tsv.substring(indexOfFirstLine + 1);
			
			//
			var csvContentEncoded = null;
			var out = null;
			var charType = null;
			try { //USE UTF-16 if available
				csvContentEncoded = (new TextEncoder("utf-16le")).encode([tsvWithoutNumbers]);
				var bom = new Uint8Array([0xFF, 0xFE]);
				out = new Uint8Array( bom.byteLength + csvContentEncoded.byteLength );
				out.set( bom , 0 );
				out.set( csvContentEncoded, bom.byteLength );
				charType = 'text/tsv;charset=UTF-16LE;';
			} catch(error) { //USE UTF-8
				csvContentEncoded = tsvWithoutNumbers;
				out = new Uint8Array(csvContentEncoded.length);
				for(var ii = 0,jj = csvContentEncoded.length; ii < jj; ++ii){
					out[ii] = csvContentEncoded.charCodeAt(ii);
				}
				charType = 'text/tsv;charset=UTF-8;';
			}
			//
			
			var blob = new Blob([out], {type: charType});
			saveAs(blob,'exportedTable' + namePrefix + '.tsv');
		}
		
		var headings = [];
		var data = [];
		var prefix = "";
		if(isAllColumnsOrVisible) {
			thisGrid.columns.forEach(function(head) {
				if(head.isExportable === true || head.isExportable === undefined) {
					headings.push(head.property);
				}
			});
			prefix += "AllColumns";
		} else {
			thisGrid.getVisibleColumns().forEach(function(head) {
				if(head.isExportable === true || head.isExportable === undefined) {
					headings.push(head.property);
				}
			});
			prefix += "VisibleColumns";
		}
		
		if(isAllRowsOrVisible) {
			var allData = thisGrid.getDataList(function(allData) {
				data = allData;
				prefix += "AllRows";
				exportColumnsFromData(prefix, data, headings);
			});
		} else {
			data = thisGrid.result.datas;
			prefix += "VisibleRows";
			exportColumnsFromData(prefix, data, headings);
		}
	},
	
	getVisibleColumns : function() {
		var _this = this;
		var columns = [];

		var columnsModel = {};
		
		_this.getAllColumns().forEach(function(column) {
			var checkBoxForColumn = _this.panel.find(".columnDropdown").find("[value='" + column.property + "']");
			var isChecked = (checkBoxForColumn.length === 1 && checkBoxForColumn[0] && checkBoxForColumn[0].checked)?true:false;
			if(column.showByDefault || isChecked) {
				columns.push(column);
				columnsModel[column.property] = true;
			}
		});

		if(this.onChangeState) {
			this.tableSettings.columns = columnsModel;
			this.onChangeState(this.tableSettings);
		}
		
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
			filterKeywords = filter.toLowerCase().split(/[ ,]+/); //Split by regular space or comma
			dataList = dataList.filter(function(data) {
				var isValid = new Array(filterKeywords.length);
				
				for(cIdx = 0; cIdx < thisGrid.columns.length; cIdx++) {
					var column = thisGrid.columns[cIdx];
					for(var fIdx = 0; fIdx < filterKeywords.length; fIdx++) {
						var filterKeyword = filterKeywords[fIdx];
						if (column.filter) {
							isValid[fIdx] = isValid[fIdx] || column.filter(data, filterKeyword);
						} else {
							var value = "" + data[column.property];
							isValid[fIdx] = isValid[fIdx] || (value !== null && value.toLowerCase().indexOf(filterKeyword) !== -1);
						}
					}
				}
				
				var isFinallyValid = true;
				for(var fIdx = 0; fIdx < filterKeywords.length; fIdx++) {
					isFinallyValid = isFinallyValid && isValid[fIdx];
				}
				
				return isFinallyValid;
			});
		}

		return dataList;
	},

	sortData : function(dataList, sortProperty, sortDirection) {
		var thisGrid = this;

		if (sortProperty && sortDirection) {
			//Save sort configuration
			if(thisGrid.onChangeState && thisGrid.tableSettings) {
				thisGrid.tableSettings.sort = {
						sortProperty : sortProperty,
						sortDirection : sortDirection
				};
				thisGrid.onChangeState(thisGrid.tableSettings);
			}
			//
			
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
		var maxLineLength = 200;
		
		dataList.forEach(function(data) {
			var item = {};
			thisGrid.getVisibleColumns().forEach(function(column) {
				//1. Render
				var value = null;
				if (column.render) {
					value = column.render(data);
				} else {
					value = data[column.property];
				}
				
				//2. Sanitize
				var value = FormUtil.sanitizeRichHTMLText(value);
				
				//3. Shorten
				var finalValue = null;
				if(value && value.length > maxLineLength) {
					finalValue = value.substring(0, maxLineLength) + "...";
				} else {
					finalValue = value;
				}
				
				//4. Tooltip
				if(value !== finalValue) {
					finalValue = $("<div>").html(finalValue);
					finalValue.tooltipster({
		                content: $("<span>").html(value)
		            });
				}
				
				item[column.property] = finalValue;
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
			result.count = dataList.length;
			result.datas = [];
			result.items = [];
			result.columns = thisGrid.getVisibleColumns();
			result.page = options.pageIndex;
			result.pages = Math.ceil(result.count / options.pageSize);
			result.start = options.pageIndex * options.pageSize;
			result.end = result.start + options.pageSize;
			result.end = (result.end <= result.count) ? result.end : result.count;
			
			dataList = dataList.slice(result.start, result.end);
			
			if(dataList.length === 0) { //Special case, empty table
				result.start = 0;
			} else { //Normal Case
				result.start = result.start + 1;
			}
			
			itemList = thisGrid.renderData(dataList);
			itemList.forEach(function(item, index) {
				result.datas.push(dataList[index]);
				result.items.push(item);
			});

			// add some delay (repeater does not properly layout columns without it)
			setTimeout(function() {
				thisGrid.result = result;
				callback(result);
				
				 //HACK: Fixes extra headers added on this fuelux 3.1.0 when rendering again
				var tableHeads = $(thisGrid.panel).find('thead');
				if(tableHeads.length > 1) {
					for(var hIdx = 0; hIdx < tableHeads.length - 1; hIdx++) {
						$(tableHeads[hIdx]).remove();
					}
				}
				//HACK:	Legacy Hacks no longer needed
				$(window).trigger('resize'); // HACK: Fixes table rendering issues when refreshing the grid on fuelux 3.1.0 for all browsers
				$(thisGrid.panel).hide().show(0); // HACK: Fixes Chrome rendering issues when refreshing the grid on fuelux 3.1.0
				
				// HACK: Fix that only works if there is only one table at a time (dont works Safari)
				var newWidth = $(".repeater-list-wrapper > .table").width();
				$(".repeater").width(newWidth);
				
				var optionsDropdowns = $(".dropdown.table-options-dropdown");
				for(var i = 0; i < optionsDropdowns.length; i++) {
					var $dropdownTD = $(optionsDropdowns[i]).parent();
					$dropdownTD.css({ "overflow" : "visible" });
				}
				
				if(thisGrid.isMultiselectable) {
					var allCheckboxes = thisGrid.panel.find(".multi-selectable-checkbox");
					for(var cIdx = 0; cIdx < allCheckboxes.length; cIdx++) {
						var $parent = $($(allCheckboxes[cIdx]).parent());
						$parent.click(function(e) {
							e.stopPropagation();
						});
						$parent.css("cursor", "initial");
					}
				}
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
