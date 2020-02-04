var CKEditorManager = new function() {

    var editors = new Map();

    this.addEditor = function(id, editor) {
        editors.set(id, editor);
    }

    this.getEditorById = function(id) {
        return editors.get(id);
    }

    this.getMaximized = function() {
        var ckMaximized = false;
        for (let editor of editors.values()) {
            var commands = editor.commands;
            if(commands && commands.maximize && commands.maximize.state == 1) {
                ckMaximized = true;
            }
        }
        return ckMaximized;
    }

	this.destroy = function() {
	    for (let editor of editors.values()) {
	        editor.destroy().catch( error => {console.log( error );});
	    }
	    editors.clear();
	}
}