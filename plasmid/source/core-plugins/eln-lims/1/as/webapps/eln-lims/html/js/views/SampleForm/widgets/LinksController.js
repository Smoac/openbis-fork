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

function LinksController(title, sampleTypeHints, isDisabled, samplesToEdit, showAnnotableTypes) {
	var linksModel = new LinksModel(title, sampleTypeHints, isDisabled, samplesToEdit, showAnnotableTypes);
	var linksView = new LinksView(this, linksModel);
	
	this.init = function($container) {
		linksView.repaint($container);
	}
	
	//
	// API - Used by Sample Form
	//
	this.isValid = function() {
		return true;
	}
	
	this.addSample = function(sample) {
		linksView.addSample(sample);
	}
	
	this.getSamples = function() {
		return [];
	}
	
	this.getSamplesIdentifiers = function() {
		return [];
	}
	
	this.getSamplesRemovedIdentifiers = function() {
		return [];
	}
}