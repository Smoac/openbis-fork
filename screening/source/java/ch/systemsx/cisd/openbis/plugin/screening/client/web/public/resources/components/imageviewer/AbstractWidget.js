define([ "jquery", "components/common/ListenerManager" ], function($, ListenerManager) {

	//
	// ABSTRACT WIDGET
	//

	function AbstractWidget(view) {
		this.init(view);
	}

	$.extend(AbstractWidget.prototype, {
		init : function(view) {
			this.setView(view);
			this.listeners = new ListenerManager();
			this.panel = $("<div>").addClass("widget");
		},

		load : function(callback) {
			callback();
		},

		render : function() {
			if (this.rendered) {
				return this.panel;
			}

			var thisWidget = this;

			this.load(function() {
				if (thisWidget.getView()) {
					thisWidget.panel.append(thisWidget.getView().render());
					thisWidget.rendered = true;
				}
			});

			return this.panel;
		},

		refresh : function() {
			if (!this.rendered) {
				return;
			}

			if (this.getView()) {
				this.getView().refresh();
			}
		},

		getView : function() {
			return this.view;
		},

		setView : function(view) {
			this.view = view;
			this.refresh();
		},

		addListener : function(eventType, listener) {
			this.listeners.addListener(eventType, listener);
		},

		notifyListeners : function(eventType, event) {
			this.listeners.notifyListeners(eventType, event);
		},

		addChangeListener : function(listener) {
			this.addListener("change", listener);
		},

		notifyChangeListeners : function(event) {
			this.notifyListeners("change", event);
		}

	});

	return AbstractWidget;

});
