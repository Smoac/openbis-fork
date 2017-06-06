/**
 * @license Copyright (c) 2003-2016, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function( config ) {
	// Define changes to default configuration here. For example:
	// config.language = 'fr';
	// config.uiColor = '#AADC6E';
	config.specialChars = config.specialChars.concat( ['&alpha;', '&beta;', '&gamma;', '&delta;', 
	                                                    '&epsilon;', '&zeta;', '&eta;', '&theta;', '&kappa;', '&lambda;', '&mu;', '&xi;', '&pi;', '&rho;', '&sigma;', 
	                                                    '&upsilon;', '&phi;', '&psi;', '&omega;', '&Delta;', '&Sigma;', '&Phi;', '&Omega;'] );
	config.extraPlugins = 'confighelper';
	config.filebrowserUploadUrl = "/openbis/file-service/eln-lims?sessionID=" + mainController.serverFacade.getSession();
	config.stylesSet = false;
	config.toolbarGroups = [
	    					{ name: 'document', groups: [ 'mode', 'document', 'doctools' ] },
	    					{ name: 'clipboard', groups: [ 'clipboard', 'undo' ] },
	    					{ name: 'editing', groups: [ 'find', 'selection', 'spellchecker', 'editing' ] },
	    					{ name: 'forms', groups: [ 'forms' ] },
	    					{ name: 'links', groups: [ 'links' ] },
	    					{ name: 'insert', groups: [ 'insert' ] },
	    					'/',
	    					{ name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] },
	    					{ name: 'paragraph', groups: [ 'list', 'indent', 'blocks', 'align', 'bidi', 'paragraph' ] },
	    					'/',
	    					{ name: 'styles', groups: [ 'styles' ] },
	    					{ name: 'colors', groups: [ 'colors' ] },
	    					{ name: 'tools', groups: [ 'tools' ] },
	    					{ name: 'others', groups: [ 'others' ] },
	    					{ name: 'about', groups: [ 'about' ] }
	    				];
	config.removeButtons = 'Save,NewPage,Templates,About,Flash,Smiley,Iframe,Form,Checkbox,Radio,TextField,Textarea,Select,Button,ImageButton,HiddenField,Styles,Source';
	
};