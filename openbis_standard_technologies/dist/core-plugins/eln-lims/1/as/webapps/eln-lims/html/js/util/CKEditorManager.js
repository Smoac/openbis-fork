var CKEditorManager = new function() {
	this.destroy = function() {
		for(instanceKey in CKEDITOR.instances) {
			CKEDITOR.instances[instanceKey].destroy();
		}
	}
}