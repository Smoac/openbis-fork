var PublishFormView = Backbone.View.extend({

	initialize : function() {
		var thisView = this;
		this.model.on("change", this.refresh, this);
	},

	render : function() {
		var thisView = this;

		var header = $("<h2>").text("Publication").appendTo(this.$el);
		var form = $("<form>").addClass("publicationForm fuelux").appendTo(this.$el);

		$(form).keydown(function(event) {
			if (event.keyCode == 13) {
				event.preventDefault();
				return false;
			}
		});

		$("<div>").addClass("spaceContainer").appendTo(form);
		$("<div>").addClass("publicationIdContainer").appendTo(form);
		$("<div>").addClass("titleContainer").appendTo(form);
		$("<div>").addClass("authorContainer").appendTo(form);
		$("<div>").addClass("authorEmailContainer").appendTo(form);
		$("<div>").addClass("notesContainer").appendTo(form);
		$("<div>").addClass("meshTermsContainer").appendTo(form);
		$("<div>").addClass("licenseContainer").appendTo(form);
		$("<div>").addClass("tagContainer").appendTo(form);
		$("<div>").addClass("buttonsContainer").appendTo(form);
		$("<div>").addClass("messagesContainer").appendTo(form);

		this.refresh();

		return this.$el;
	},

	refresh : function() {
		this.refreshOrRenderSpace();
		this.refreshOrRenderPublicationId();
		this.refreshOrRenderTitle();
		this.refreshOrRenderAuthor();
		this.refreshOrRenderAuthorEmail();
		this.refreshOrRenderNotes();
		this.refreshOrRenderMeshTerms();
		this.refreshOrRenderLicense();
		this.refreshOrRenderTag();
		this.refreshOrRenderButtons();
		this.refreshOrRenderMessages();
	},

	refreshOrRenderGroup : function(name, createWidget, updateWidget) {
		if (!this.widgets) {
			this.widgets = {};
		}

		var container = this.$el.find("." + name + "Container");
		var group = null;
		var widget = this.widgets[name];

		if (widget) {
			console.log("Updating widget: " + name);

			group = container.find(".form-group");
			updateWidget(widget);
		} else {
			console.log("Creating widget: " + name);

			group = $("<div>").addClass("form-group").appendTo(container);

			widget = createWidget();
			$(widget).appendTo(group);
			this.widgets[name] = widget;
		}
	},

	refreshOrRenderField : function(name, label, createWidget, updateWidget) {
		if (!this.widgets) {
			this.widgets = {};
		}

		var container = this.$el.find("." + name + "Container");
		var group = null;
		var errors = null;
		var widget = this.widgets[name];

		if (widget) {
			console.log("Updating widget: " + name);

			group = container.find(".form-group");
			errors = container.find(".errors");
			updateWidget(widget);
		} else {
			console.log("Creating widget: " + name);

			group = $("<div>").addClass("form-group").appendTo(container);
			$("<label>").text(label).appendTo(group);

			widget = createWidget();
			$(widget).appendTo(group);
			this.widgets[name] = widget;

			errors = $("<div>").addClass("errors text-danger").appendTo(group);
		}

		if (this.model.isValid(name)) {
			group.removeClass("has-error");
			errors.text("");
			errors.css("display", "none");
		} else {
			group.addClass("has-error");
			errors.text(this.model.getValidationError(name));
			errors.css("display", "block");
		}
	},

	refreshOrRenderTag : function() {
		var thisView = this;

		var create = function() {
			var widget = $("<div>").text("Loading...");

			thisView.model.getTags(function(tags) {
				if (tags && tags.length > 0) {
					var select = $("<select>").addClass("form-control");
					$("<option>").appendTo(select);

					tags.forEach(function(tag) {
						$("<option>").text(tag).appendTo(select);
					});

					select.change(function() {
						thisView.model.setTag(select.val());
					});

					widget.empty();
					widget.append(select);
				} else {
					widget.text("None of the data sets have been tagged.")
				}
			});

			return widget;
		}

		var update = function(widget) {
			var select = widget.find("select");
			select.val(thisView.model.getTag());
		}

		this.refreshOrRenderField("tag", "openBIS Tag", create, update);
	},

	refreshOrRenderSpace : function() {
		var thisView = this;

		var create = function() {
			var select = $("<select>").addClass("form-control");
			$("<option>").appendTo(select);
			thisView.model.getSpaces(function(spaces) {
				spaces.forEach(function(space) {
					var option = $("<option>").text(space).appendTo(select);
				});
			});

			select.change(function() {
				thisView.model.setSpace(select.val());
			});

			return select;
		}

		var update = function(select) {
			select.val(thisView.model.getSpace());
		}

		this.refreshOrRenderField("space", "Space*", create, update);
	},

	refreshOrRenderPublicationId : function() {
		var thisView = this;

		var create = function() {
			var input = $("<input>").attr("type", "text").attr("placeholder", "Publication Id").addClass("form-control");
			input.change(function() {
				thisView.model.setPublicationId(input.val());
			});
			return input;
		}

		var update = function(input) {
			input.val(thisView.model.getPublicationId());
		}

		this.refreshOrRenderField("publicationId", "Publication Id*", create, update);
	},

	refreshOrRenderTitle : function() {
		var thisView = this;

		var create = function() {
			var input = $("<input>").attr("type", "text").attr("placeholder", "Title").addClass("form-control");
			input.change(function() {
				thisView.model.setTitle(input.val());
			});
			return input;
		}

		var update = function(input) {
			input.val(thisView.model.getTitle());
		}

		this.refreshOrRenderField("title", "Title*", create, update);
	},

	refreshOrRenderAuthor : function() {
		var thisView = this;

		var create = function() {
			var input = $("<input>").attr("type", "text").attr("placeholder", "Author").addClass("form-control");
			input.change(function() {
				thisView.model.setAuthor(input.val());
			});
			return input;
		}

		var update = function(input) {
			input.val(thisView.model.getAuthor());
		}

		this.refreshOrRenderField("author", "Author*", create, update);
	},

	refreshOrRenderAuthorEmail : function() {
		var thisView = this;

		var create = function() {
			var input = $("<input>").attr("type", "text").attr("placeholder", "Author's Email").addClass("form-control");
			input.change(function() {
				thisView.model.setAuthorEmail(input.val());
			});
			return input;
		}

		var update = function(input) {
			input.val(thisView.model.getAuthorEmail());
		}

		this.refreshOrRenderField("authorEmail", "Author's Email*", create, update);
	},

	refreshOrRenderLicense : function() {
		var thisView = this;

		var create = function() {
			var select = $("<select>").addClass("form-control");

			$("<option>").appendTo(select);
			thisView.model.getLicenses(function(dictionary) {
				dictionary.terms.forEach(function(term) {
					$("<option>").text(term.label).attr("value", term.code).appendTo(select);
				});
			});

			select.change(function() {
				thisView.model.setLicense(select.val());
			});

			return select;
		}

		var update = function(select) {
			select.val(thisView.model.getLicense());
		}

		this.refreshOrRenderField("license", "License*", create, update);
	},

	refreshOrRenderNotes : function() {
		var thisView = this;

		var create = function() {
			var textarea = $("<textarea>").attr("rows", "5").attr("placeholder", "Notes").addClass("form-control");
			textarea.change(function() {
				thisView.model.setNotes(textarea.val());
			});
			return textarea;
		}

		var update = function(textarea) {
			textarea.val(thisView.model.getNotes());
		}

		this.refreshOrRenderField("notes", "Notes", create, update);
	},

	refreshOrRenderMeshTerms : function() {
		var thisView = this;

		var create = function() {
			thisView.meshTermsPillbox = thisView.createMeshTermsPillbox();
			thisView.meshTermsFilter = thisView.createMeshTermsFilter();
			thisView.meshTermsTree = thisView.createMeshTermsTree();

			var widget = $("<div>");
			widget.append(thisView.meshTermsPillbox);
			widget.append(thisView.meshTermsFilter);
			widget.append($("<div>").append(thisView.meshTermsTree));

			return widget;
		}

		var update = function(widget) {
		}

		this.refreshOrRenderField("meshTerms", "Mesh Terms*", create, update);
	},

	createMeshTermsFilter : function() {
		var thisView = this;

		var input = $("<input>").attr("type", "text").attr("placeholder", "Filter").addClass("form-control filter");
		var lastFilter = input.val();
		var timeout = null;
		var scheduleTimeout = function() {
			if (input.val() == lastFilter) {
				return;
			} else {
				lastFilter = input.val();
			}

			if (timeout) {
				clearTimeout(timeout);
			}

			timeout = setTimeout(function() {
				var treeContainer = thisView.meshTermsTree.parent();
				treeContainer.empty();
				thisView.meshTermsTree = thisView.createMeshTermsTree();
				treeContainer.append(thisView.meshTermsTree);
			}, 1000);
		}

		input.keyup(function(e) {
			scheduleTimeout();
		});

		input.blur(function(e) {
			scheduleTimeout();
		})

		return input;
	},

	createMeshTermsPillbox : function() {
		var thisView = this;

		var pillbox = $("<div>").addClass("pillbox").attr("data-initialize", "pillbox");
		var pillGroup = $("<ul>").addClass("clearfix pill-group").appendTo(pillbox);

		pillbox.hide();
		pillbox.pillbox({
			onRemove : function(data, callback) {
				thisView.meshTermsTree.selectItem(data, false);

				if (pillbox.pillbox("itemCount") <= 0) {
					pillbox.hide();
				}

				callback(data);
			}
		});

		pillbox.getItems = function() {
			return pillbox.pillbox("items");
		};

		pillbox.addItem = function(item) {
			var exists = pillbox.pillbox("items").some(function(i) {
				return i.data.id == item.data.id;
			});

			if (!exists) {
				pillbox.pillbox("addItems", item);
				if (pillbox.pillbox("itemCount") > 0) {
					pillbox.show();
				}
			}
		};

		pillbox.removeItem = function(value) {
			pillbox.pillbox("removeByValue", value);

			if (pillbox.pillbox("itemCount") <= 0) {
				pillbox.hide();
			}
		};

		return pillbox;
	},

	createMeshTermsTree : function() {
		var thisView = this;

		var tree = $("<ul>").css("display", "none").addClass("tree tree-folder-select").attr("role", "tree")
		var branch = $("<li>").addClass("tree-branch hide").attr("data-template", "treebranch").attr("role", "treeitem").attr("aria-expanded", "false").appendTo(tree);
		var branchHeader = $("<div>").addClass("tree-branch-header").appendTo(branch);
		var branchButton = $("<button>").addClass("glyphicon icon-caret glyphicon-play").appendTo(branchHeader);
		$("<span>").addClass("sr-only").text("Open").appendTo(branchButton);

		var branchName = $("<button>").addClass("tree-branch-name").appendTo(branchHeader);
		$("<span>").addClass("glyphicon icon-folder glyphicon-folder-close").appendTo(branchName);
		$("<span>").addClass("tree-label").appendTo(branchName);

		var branchChildren = $("<ul>").addClass("tree-branch-children").attr("role", "group").appendTo(branch);
		var branchLoading = $("<div>").addClass("tree-loader").attr("role", "alert").text("Loading...").appendTo(branch)

		var item = $("<li>").addClass("tree-item hide").attr("data-template", "treeitem").attr("role", "treeitem").appendTo(tree);
		var itemButton = $("<button>").addClass("tree-item-name").appendTo(item);
		$("<span>").addClass("glyphicon icon-item fueluxicon-bullet").appendTo(itemButton);
		$("<span>").addClass("tree-label").appendTo(itemButton);

		var dataSource = function(parentData, callback) {
			var filter = thisView.meshTermsFilter;
			var pillbox = thisView.meshTermsPillbox;

			thisView.model.getMeshTermChildren(parentData.identifier, filter.val(), function(meshTerms) {
				var data = [];

				meshTerms.forEach(function(meshTerm) {
					data.push({
						"identifier" : meshTerm.identifier,
						"fullName" : meshTerm.fullName,
						"text" : meshTerm.name,
						"type" : (meshTerm.hasChildren ? "folder" : "item"),
						"attr" : {
							"id" : "meshTerm_" + meshTerm.id,
						},
					});
				});

				tree.css("display", "block");

				callback({
					"data" : data
				});

				pillbox.getItems().forEach(function(item) {
					tree.selectItem(item, true);
				});
			});
		}

		tree.tree({
			dataSource : dataSource,
			multiSelect : true,
			folderSelect : true,
			cacheItems : true
		});

		tree.click(function(event) {
			// do not submit the form
			event.preventDefault();
		});

		tree.on("selected.fu.tree", function(event, selection) {
			thisView.meshTermsPillbox.addItem({
				"text" : selection.target.fullName,
				"value" : selection.target.identifier,
				"data" : {
					"id" : selection.target.attr.id
				}
			});
		});

		tree.on("deselected.fu.tree", function(event, selection) {
			thisView.meshTermsPillbox.removeItem(selection.target.identifier);
		});

		tree.selectItem = function(item, selectItem) {
			var selectedItems = tree.tree("selectedItems");
			var isItemSelected = selectedItems.some(function(i) {
				return i.identifier == item.value;
			});

			if (isItemSelected ^ selectItem) {
				var node = tree.find("#" + item.data.id);
				if (node.length > 0) {
					tree.tree("selectItem", node);
				}
			}
		};

		return tree;
	},

	refreshOrRenderButtons : function() {
		var thisView = this;

		var create = function() {
			var submit = $("<button>").text("Publish").attr("type", "submit").addClass("btn btn-default");

			submit.click(function(event) {
				var space = $(".spaceContainer select").val();
				var publicationId = $(".publicationIdContainer input").val();
				var title = $(".titleContainer input").val();
				var author = $(".authorContainer input").val();
				var authorEmail = $(".authorEmailContainer input").val();
				var license = $(".licenseContainer select").val();
				var notes = $(".notesContainer textarea").val();
				var meshTerms = $(".meshTermsContainer .pillbox").pillbox("items").map(function(item) {
					return item.value;
				});

				thisView.model.setSpace(space);
				thisView.model.setPublicationId(publicationId);
				thisView.model.setTitle(title);
				thisView.model.setAuthor(author);
				thisView.model.setAuthorEmail(authorEmail);
				thisView.model.setNotes(notes);
				thisView.model.setMeshTerms(meshTerms);
				thisView.model.setLicense(license);

				thisView.model.publish();

				return false;
			});

			return submit;
		}

		var update = function(submit) {
			var disabled = thisView.model.get("submitDisabled");
			if (disabled) {
				submit.attr("disabled", "disabled");
			} else {
				submit.removeAttr("disabled");
			}
		}

		this.refreshOrRenderGroup("buttons", create, update);
	},

	refreshOrRenderMessages : function() {
		var thisView = this;

		var create = function() {
			return $("<div>");
		}

		var update = function(list) {
			list.empty();

			var messages = thisView.model.get('formMessages');
			if (messages && messages.length > 0) {
				messages.forEach(function(message) {
					var messageCss = null;

					if (message.type == "success") {
						var link = $("<a>").text("Publication " + message.text + " has been successfully created.").click(function() {
							window.top.location.hash = "#entity=EXPERIMENT&permId=" + message.text + "&timestamp=" + (new Date().getTime());
						});
						$("<div>").append(link).addClass("form-group").addClass("text-success").appendTo(list);
					} else if (message.type == "error") {
						$("<div>").text(message.text).addClass("form-group").addClass("text-danger").appendTo(list);
					}
				});

				if (false == _.isEqual(thisView.messages, messages)) {
					thisView.messages = messages;
					setTimeout(function() {
						$("html, body").animate({
							scrollTop : $(document).height()
						}, "slow");

					}, 250);
				}
			}
		}

		this.refreshOrRenderGroup("messages", create, update);
	}

});
