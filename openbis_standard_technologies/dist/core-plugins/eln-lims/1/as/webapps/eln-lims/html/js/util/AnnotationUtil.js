/*
 * Copyright 2015 ETH Zuerich, Scientific IT Services
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
var AnnotationUtil = new function() {
	var isFound = function(samples, identifier) {
		if (samples) {
			for (var i = 0; i < samples.length; i++) {
				if (samples[i].identifier === identifier) {
					return true;
				}
			}
		}
		return false;
	}
	
	var extractCode = function(identifier)
	{
		var terms = identifier.split("/");
		return terms[terms.length - 1];
	}
	
	/*
	  Builds annotations for the specified annotations and samples with the specified annotations builder. 
	  Only annotations related to the specified samples are used. The annotations builder should have two
	  methods: addKeyValue(key, value) and startRow().
	*/
	this.buildAnnotations = function(annotations, samples, annotationsBuilder) {
		for (var permId in annotations) {
			var annotation = annotations[permId];
			var identifier = annotation.identifier;
			if (isFound(samples, identifier)) {
				annotationsBuilder.startRow();
				annotationsBuilder.addKeyValue("CODE", extractCode(identifier));
				for (var propertyTypeCode in annotation) {
					if ($.inArray(propertyTypeCode, ["identifier", "sampleType"]) < 0) {
						annotationsBuilder.addKeyValue(propertyTypeCode, annotation[propertyTypeCode]);
					}
				}
			}
		}
	}
	
}