function BrowserWidget(containerId, mainController, serverFacade) {
	this.containerId = containerId;
	this.mainController = mainController;
	this.serverFacade = serverFacade;
	this.structure = {};
	
	this.init = function() {
		var _this = this;
		this.serverFacade.listSpacesWithProjectsAndRoleAssignments(null, function(dataWithSpacesAndProjects) {
			var spaces = dataWithSpacesAndProjects.result;
			var projects = [];
			var projectsAsMap = {};
			for(var i = 0; i < spaces.length; i++) {
				var space = spaces[i];
				for(var j = 0; j < space.projects.length; j++) {
					var project = space.projects[i];
					delete project["@id"];
					delete project["@type"];
					projects.push(project);
					projectsAsMap[project.code] = project;
				}
			}
			
			_this.serverFacade.listExperiments(projects, function(experiments) {
				for(var i = 0; i < experiments.result.length; i++) {
					var experiment = experiments.result[i];
					var identifier = experiment.identifier.split("/");
					var project = projectsAsMap[identifier[2]];
					if(!project.experiments) {
						project.experiments = [];
					}
					project.experiments.push(experiment);
				}
				_this._initStructure(spaces);
			}
			);
		});
	}
	
	this._initStructure = function(spaces) {
		for(var i = 0; i < spaces.length; i++) {
			var space = spaces[i];
			var projects = {};
			if(space.projects) {
				for(var j = 0; j < space.projects.length; j++) {
					var project = space.projects[j];
					var experiments = {};
					if(project.experiments) {
						for(var k = 0; k < project.experiments.length; k++) {
							var experiment = project.experiments[k];
							var experimentIdentifier = experiment.identifier.split("/");
							var sampleIdentifier = "/" + experimentIdentifier[1] + "/" + experimentIdentifier[3];
							experiments[experiment.code] = new BrowserExperiment("showViewExperiment", sampleIdentifier, experiment.code);
						}
					}
					projects[project.code] = new BrowserProject(project.code, project.code, experiments);
				}
			}
			this.structure[space.code] = new BrowserSpace(space.code, space.code, projects);
		}
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