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

function SampleHierarchyTableModel(sample) {
	this.title = "Sample Hierarchy Table for " + sample.identifier;
	this.sample = sample;
	
	this.getData = function(dataList) {
		var dataList = [];
		this._addRow(dataList, this.sample, 0, "");
		this._addAncestorData(dataList, this.sample, 0, "");
		this._addDescendentData(dataList, this.sample, 0, "");
		dataList.sort(function (e1, e2) {
			var l1 = e1.level;
			var p1 = e1.identifier + e1.path;
			var l2 = e2.level;
			var p2 = e2.identifier + e2.path;
			if (l1 !== l2) {
				return l1 - l2;
			}
			return p1 < p2 ? -1 : (p1 > p2 ? 1 : 0);
		});
		return dataList;
	}
	
	this._addAncestorData = function(dataList, sample, level, path) {
		if (sample.parents) {
			for (var i = 0; i < sample.parents.length; i++) {
				var parent = sample.parents[i];
				var newPath = " → " + sample.code + path;
				var newLevel = level - 1;
				this._addRow(dataList, parent, newLevel, newPath);
				this._addAncestorData(dataList, parent, newLevel, newPath);
			}
		}
	}
	
	this._addDescendentData = function(dataList, sample, level, path) {
		if (sample.children) {
			for (var i = 0; i < sample.children.length; i++) {
				var child = sample.children[i];
				var newPath = " ← " + sample.code + path;
				var newLevel = level + 1;
				this._addRow(dataList, child, newLevel, newPath);
				this._addDescendentData(dataList, child, newLevel, newPath);
			}
		}
	}
	
	this._addRow = function(dataList, sample, level, path) {
		var annotations = FormUtil.getAnnotationsFromSample(sample);
		dataList.push({
			level : level,
			sampleType : sample.sampleTypeCode,
			identifier : sample.identifier,
			permId : sample.permId,
			path: path,
			name : sample.properties["NAME"],
			parentAnnotations : this._createAnnotations(annotations, sample.parents),
			childrenAnnotations : this._createAnnotations(annotations, sample.children),
			sample : sample
		});
		
	}
	
	this._createAnnotations = function(annotations, relatedSamples) {
		var content = "";
		var rowStarted = false;
		AnnotationUtil.buildAnnotations(annotations, relatedSamples, {
			startRow : function() {
				if (content !== "") {
					content += "\n";
				}
				rowStarted = true;
			},
			addKeyValue : function(key, value) {
				if (rowStarted === false) {
					content += ", ";
				}
				content += key + ":" + value;
				rowStarted = false;
			}
		})
		return content;
	}
	

}