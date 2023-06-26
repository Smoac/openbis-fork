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

function CommentsModel(entity, mode, form) {
	this.entity = entity;
	this.mode = mode;
	this.form = form;
	
	this.getComments = function() {
	    return this._getProperties()["$XMLCOMMENTS"];
	}
	
	this.setComments = function(commentsXML) {
	    this._getProperties()["$XMLCOMMENTS"] = commentsXML;
		this.form.isFormDirty = true;
	}

	this._getProperties = function() {
	    if(!this.entity) {
            this.entity = { properties : {} };
        }
        return this.entity.properties;
	}

}