/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function DataSetViewerModel(containerId, profile, entity, serverFacade, datastoreDownloadURL, datasets, 
		enableUpload, enableDeepUnfolding) {
	this.containerId = containerId;
	this.containerIdTitle = containerId + "-title";
	this.containerIdContent = containerId + "-content";
	
	this.profile = profile;
	this.serverFacade = serverFacade;
	
	this.entity = entity;
	this.isExperiment = function() {
		return this.entity && this.entity["@type"] === "as.dto.experiment.Experiment";
	}
	
	this.datasets = datasets;
	
	this.enableUpload = enableUpload;
	this.enableDeepUnfolding = enableDeepUnfolding;
	this.entityDataSets = {};
	this.datastoreDownloadURL = datastoreDownloadURL;
	
	this.getDownloadLink = function(datasetCode, datasetFile, isShowSize) {
		var downloadUrl = this.datastoreDownloadURL + '/' + datasetCode + "/" + encodeURIComponent(datasetFile.pathInDataSet) + "?sessionID=" + mainController.serverFacade.getSession();
		
		var size = null;
		if(parseInt(datasetFile.fileSize) / 1024 > 1024) {
			size = parseInt(datasetFile.fileSize) / 1024 / 1024;
			unit = "Mb";
		} else {
			size = parseInt(datasetFile.fileSize) / 1024;
			unit = "Kb";
		}
		var size = Math.floor(size * 10) / 10; //Rounded to one decimal
		
		var $link = $("<a>").attr("href", downloadUrl)
							.attr("target", "_blank")
							.append(datasetFile.pathInListing)
							.append(" ("+ size + unit +")")
							.click(function(event) {
								event.stopPropagation();
							});
		
		return $link;
	}

    this._isPreviewableImage = function(pathInDataSet) {
        return this._hasExtension(pathInDataSet, ["jpg", "jpeg", "png", "gif", "svg"]);
    }

	this._isIconableImage = function(pathInDataSet) {
        return this._hasExtension(pathInDataSet, ["jpg", "jpeg", "png", "gif"]);
    }
	
	this._isJupyterNotebook = function(pathInDataSet) {
        return this._hasExtension(pathInDataSet, ["ipynb"]);
    }

	this._hasExtension = function(pathInDataSet, extensions) {
        var haveExtension = pathInDataSet.lastIndexOf(".");
        if( haveExtension !== -1 && (haveExtension + 1 < pathInDataSet.length)) {
            var extension = pathInDataSet.substring(haveExtension + 1, pathInDataSet.length).toLowerCase();
            return ($.inArray(extension, extensions) !== -1);
        }
        return false;	    
	}

	this.getDirectDirectoryLink = function(datasetCode, pathInDataSet) {
		var directLinkComponent = null;
		if(profile.directLinkEnabled && (profile.cifsFileServer || profile.sftpFileServer)) {
			var path = null;
			
			if(this.isExperiment()) {
				path = this.entity.identifier.identifier.substring(1) + "/" + datasetCode + "/" + pathInDataSet + "/";
			} else {
				path = this.entity.experimentIdentifierOrNull.substring(1) + "/" + datasetCode + "/" + pathInDataSet + "/";
			}
			
			directLinkComponent = "<span onclick=\"" + "Util.showDirectLink('" + path + "')" + "\" class='glyphicon glyphicon-hdd'></span>";
		}
		return directLinkComponent;
	}

	this.getJupyterNotebookLink = function(datasetCode, datasetFile) {
		if(this._isJupyterNotebook(datasetFile.pathInDataSet)) {
	        var notebookURL = profile.getDefaultDataStoreURL() + "/" + datasetCode + "/" + datasetFile.pathInDataSet + "?sessionID=" + mainController.serverFacade.getSession();
	        var onclick = "JupyterUtil.copyNotebook(\"" + datasetCode + "\",\"" + notebookURL + "\");"
	        return "<span onclick='" + onclick + "' class='glyphicon glyphicon-log-in'></span>";
		}
		return null;
	}
	
	this.getPreviewLink = function(datasetCode, datasetFile) {
		if(this._isPreviewableImage(datasetFile.pathInDataSet)) {
	        var imageURLAsString = this.getImageUrl(datasetCode, datasetFile);
	        var onclick = "Util.showImage(\"" + imageURLAsString + "\");"
	        return "<span onclick='" + onclick + "' class='glyphicon glyphicon-search'></span>";
		}
		return null;
	}

	this.getImageUrl = function(datasetCode, datasetFile) {
		if(this._isPreviewableImage(datasetFile.pathInDataSet)) {
			return profile.getDefaultDataStoreURL() + "/" + datasetCode + "/" + datasetFile.pathInDataSet + "?sessionID=" + mainController.serverFacade.getSession();
		}
		return null;
	}

	this.getImageIconUrl = function(datasetCode, datasetFile) {
        if(this._isIconableImage(datasetFile.pathInDataSet)) {
            return this.getImageUrl(datasetCode, datasetFile) + "&mode=thumbnail";
        }
        return null;
	}

}