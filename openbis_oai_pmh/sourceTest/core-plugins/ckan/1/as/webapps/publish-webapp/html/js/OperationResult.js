var OperationResult = Backbone.Model.extend({

	setSuccessful : function(successful) {
		this.set("successful", successful);
	},

	isSuccessful : function() {
		return this.get("successful");
	},

	addMessage : function(type, text) {
		var messages = this.get("messages");
		if (!messages) {
			messages = [];
			this.set("messages", messages);
		}
		messages.unshift({
			"type" : type,
			"text" : text
		});
	},

	addMessages : function(messages) {
		var thisModel = this;
		if (messages && messages.length > 0) {
			messages.forEach(function(message) {
				thisModel.addMessage(message);
			});
		}
	},

	getMessages : function() {
		var messages = this.get("messages");
		if (messages) {
			return messages;
		} else {
			return [];
		}
	},

	setResult : function(result) {
		this.set("result", result);
	},

	getResult : function() {
		return this.get("result");
	}

});