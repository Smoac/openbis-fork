function BBBHubTechnology() {
    this.init();
}

$.extend(BBBHubTechnology.prototype, ELNLIMSPlugin.prototype, {
    // This code is copy of openBis plugin. It was taken from here:
    // https://sissource.ethz.ch/sis/bbb-hub/tree/master/openbis/core-plugins/bbb-hub/1/as/webapps/snakemake/html/js
    // Now it is a ELN plugin.

	init: function() {
        loadJSResorce("./plugins/bbb-hub/UiComponents.js");
        loadJSResorce("./plugins/bbb-hub/BBBServerFacade.js");
        loadJSResorce("./plugins/bbb-hub/snakemake-table.js");
        loadJSResorce("./plugins/bbb-hub/snakemake-trigger.js");
        this.setNewNameById("LAB_NOTEBOOK", "Datasets");
        this.setNewNameById("lab-notebook-id", "Datasets");
        this.setNewNameById("backwards-compatible-main-container-id", "Welcome to the BBBHub");
	},

	experimentFormTop : function($container, model) {
	    BBBServerFacade.getExperiment($container, model);
    },

    experimentFormBottom : function($container, model) {
    },

    getExtraUtilities : function() {
        var _this = this;
        return [{
            icon : "fa fa-table",
            uniqueViewName : "BBB_VIEW_NAME_TEST",
            label : "Index Page",
            paintView : function($header, $content) {
                    $header.append($("<h1>").append("Public Index Page"));
                    BBBServerFacade.getExperiments($content);
                }
            }, {
                icon : "glyphicon glyphicon-list-alt",
                uniqueViewName : "HELP_VIEW",
                label : "Help",
                paintView : function($header, $content) {
                   $header.append($("<h1>").append("Help"));
                   _this.paintContent($content);
            }
        }];
    },

    paintContent : function($content) {
        var src = "https://openbis.ch/index.php/docs/user-documentation-19-06-4/";
        $content.append($("<iframe src=" + src + " width='99%' height='700px'></iframe>"));
    },

    setNewNameById : function(elementId, newName, timeout) {
        var DEFAULT_TIMEOUT_STEP = 100;

        return new Promise(function executor(resolve, reject) {
            if($("#" + elementId).length <= 0) {
                setTimeout(executor.bind(null, resolve, reject), DEFAULT_TIMEOUT_STEP);
            } else {
                $("#" + elementId).html(newName);
                resolve();
            }
        });
    }
});

profile.plugins.push(new BBBHubTechnology());