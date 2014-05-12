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

/**
 * Creates an instance of the browser widget.
 *
 * @constructor
 * @this {BrowserWidget}
 * @param {string} containerId The Container where the Inspector DOM will be attached.
 * @param {Map<String, List<Projects>>} structure The menu structure.
 */
function BrowserWidget(mainController, containerId, structure) {
	this.containerId = containerId;
	this.mainController = mainController;
	this.structure = structure;
	
	this.init = function() {
		this.repaint();
	}
	
	this.repaint = function() {
		var _this = this;
		var container = $("#" + containerId);
		container.empty();
		
		var $browserSpacesWrapper = $("<div>", { style: "float:left; margin-right:10px;" });
		var $browserProjectsWrapper = $("<div>", { style: "float:left; margin-right:10px;" });
		var $browserExperimentsWrapper = $("<div>", { style: "float:left; margin-right:10px;" });
		
		var $browserSpaces = $("<ul>", { class: "nav nav-tabs nav-stacked span5" });
		$browserSpacesWrapper.append($browserSpaces);
		
		
		for (var spaceCode in this.structure) {
			var space = this.structure[spaceCode];
			
			var spaceOnHoverEvent = function(projects) {
				return function() {
					var $browserProjects = $("<ul>", { class: "nav nav-tabs nav-stacked span5" });
					for(var projectCode in projects) {
						var project = projects[projectCode];
						
						var projectOnHoverEvent = function(experiments) {
							return function() {
								var $browserExperiments = $("<ul>", { class: "nav nav-tabs nav-stacked span5" });
								for(var experimentCode in experiments) {
									var experiment = experiments[experimentCode];
									
									var experimentOnClickEvent = function(experiment) {
										return function() {
											_this.mainController.changeView(experiment.href, experiment.hrefArgs);
										}
									}
									
									var $experiments = $("<li>", {click: experimentOnClickEvent(experiment)})
									.append($("<a>").append(experiment.displayName));
									
									$browserExperiments.append($experiments);
								}
								$browserExperimentsWrapper.empty();
								$browserExperimentsWrapper.append($browserExperiments);
							}
						}
						var $project = $("<li>", {mouseenter: projectOnHoverEvent(project.experiments)})
						.append($("<a>").append(project.displayName + "<i class='icon-chevron-right'></i>"));
						
						$browserProjects.append($project);
					}
					$browserProjectsWrapper.empty();
					$browserExperimentsWrapper.empty();
					$browserProjectsWrapper.append($browserProjects);
				}
			}
			var $space = $("<li>", { mouseenter: spaceOnHoverEvent(space.projects)})
			.append($("<a>").append(space.displayName + "<i class='icon-chevron-right'></i>"));
		
			$browserSpaces.append($space);
		}
		
		container.append($browserSpacesWrapper);
		container.append($browserProjectsWrapper);
		container.append($browserExperimentsWrapper);
	}
}

function BrowserSpace(code, displayName, projects) {
	this.code = code;
	this.displayName = displayName;
	this.projects = projects;
}

function BrowserProject(code, displayName, experiments) {
	this.code = code;
	this.displayName = displayName;
	this.experiments = experiments;
}

function BrowserExperiment(href, hrefArgs, displayName) {
	this.href = href;
	this.hrefArgs = hrefArgs;
	this.displayName = displayName;
}