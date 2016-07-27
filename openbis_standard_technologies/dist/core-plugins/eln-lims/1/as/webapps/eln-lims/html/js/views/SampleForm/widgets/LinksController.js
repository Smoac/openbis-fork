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

function LinksController(title, sampleTypeHints, isDisabled, samplesToEdit, showAnnotableTypes, disableAddAnyType) {
	var linksModel = new LinksModel(title, sampleTypeHints, isDisabled, showAnnotableTypes, disableAddAnyType);
	var linksView = new LinksView(this, linksModel);
	
	this.init = function($container) {
		linksView.repaint($container);
		
		if(sampleTypeHints && showAnnotableTypes) {
			for(var sIdx = 0; sIdx < sampleTypeHints.length; sIdx++) {
				linksView.initContainerForType(sampleTypeHints[sIdx].TYPE);
			}
		}
		
		if(samplesToEdit) {
			for(var sIdx = 0; sIdx < samplesToEdit.length; sIdx++) {
				this.addSample(samplesToEdit[sIdx]);
			}
		}
	}
	
	//
	// API - Used by Sample Form
	//
	this.isValid = function() {
		if(sampleTypeHints) {
			for(var typeIdx = 0; typeIdx < sampleTypeHints.length; typeIdx++) {
				var sampleTypeHint = sampleTypeHints[typeIdx];
				var sampleTypeCode = sampleTypeHint["TYPE"];
				var sampleTypeMinCount = sampleTypeHint["MIN_COUNT"];
				var sampleTypeAnnotations = sampleTypeHint["ANNOTATION_PROPERTIES"];
				var sampleTypeCount = (linksModel.samplesByType[sampleTypeCode])?linksModel.samplesByType[sampleTypeCode].length:0;
				if(sampleTypeCount < sampleTypeMinCount) {
					Util.showError("Currently you only have " + sampleTypeCount + " of the " + sampleTypeMinCount + " required " + sampleTypeCode + ".");
					return false;
				}
				
				if(sampleTypeCount > 0) {
					for(var sampleIdx = 0; sampleIdx < linksModel.samplesByType[sampleTypeCode].length; sampleIdx++) {
						var sampleWithAnnotations = linksModel.samplesByType[sampleTypeCode][sampleIdx];
						for(var annotIdx = 0; annotIdx < sampleTypeAnnotations.length; annotIdx++) {
							//TO-DO, Not enough information to validate required annotations here
						}
					}
				}
			}
		}
		
		return true;
	}
	
	this.addSample = function(sample) {
		linksView.updateSample(sample, true);
	}
	
	this.getSamples = function() {
		var allSamples = [];
		for(var sampleTypeCode in linksModel.samplesByType) {
			allSamples = allSamples.concat(linksModel.samplesByType[sampleTypeCode]);
		}
		return allSamples;
	}
	
	this.getSampleByIdentifier = function(identifier) {
		for(var sampleTypeCode in linksModel.samplesByType) {
			var samplesByType = linksModel.samplesByType[sampleTypeCode];
			for(var sIdx = 0; sIdx < samplesByType.length; sIdx++) {
				if(samplesByType[sIdx].identifier === identifier) {
					return samplesByType[sIdx];
				}
			}
		}
		return null;
	}
	
	this.getSamplesIdentifiers = function() {
		var allSamples = this.getSamples();
		var allSamplesIdentifiers = [];
		for(var sIdx = 0; sIdx < allSamples.length; sIdx++) {
			var sample = allSamples[sIdx];
			allSamplesIdentifiers.push(sample.identifier);
		}
		return allSamplesIdentifiers;
	}
	
	this.getSamplesRemovedIdentifiers = function() {
		return linksModel.samplesRemoved;
	}
}