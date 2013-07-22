var openbisUrl = 'https://localhost:8443/';
var dssUrl = 'https://localhost:8444/datastore_server';

function dataset(sample, data) {
   if(data.result){
      for (var i = 0; i < data.result.length; i++) {
        console.log(data.result)
        openbisServer.listFilesForDataSet(data.result[i].code, "/", true, filelist.curry(sample, data.result[i]));
      }
   }
}

function filelist(sample, dataset, files) {
	for (var i in files.result) {
		if (!files.result[i].isDirectory) {
			var inspector = inspectors.select("#"+sample.code+"_INSPECTOR");
			var pathInDataSet = files.result[i].pathInDataSet;
			
			if(pathInDataSet){
				var downloadUrl = dssUrl+"/"+dataset.code+"/"+pathInDataSet+"?sessionID=" + openbisServer.getSession();
				inspector.select("td.data_sets").append("a").attr("href", downloadUrl).text(pathInDataSet)
				inspector.select("td.data_sets").append("br");
				
				if (/\.svg$/.test(pathInDataSet)) {
						// Retrieve the svg file and inject it into the DOM
						d3.xml(downloadUrl, "image/svg+xml", function(xml) {
							var importedNode = document.importNode(xml.documentElement, true);
							d3.select(importedNode)
								.attr("width", inspectorsWidth - 20)
								.attr("height", inspectorsWidth - 20)
								.attr("viewBox", "200 200 650 650");
							inspector.node().appendChild(importedNode);
						});
				}
			}
		}
	}
}

//
// UI Related
//

function createTableFromProperties() {
	var tableTemplate = "<table id=\"yeast-table\" cellpadding=\"0\" cellspacing=\"0\" border=\"1\" bordercolor=\"white\" class=\"display\" width=\"100%\"><thead><tr class=\"yeast-table-header\"><th>Code</th>";
	for(var i=0; i<SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME.length;i++) {
		tableTemplate += "<th>" + SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME[i]+ "</th>";
	}
	tableTemplate += "</tr></thead><tbody></tbody></table>";
	$("#tableContainer").append(tableTemplate);
}

function createTableFilterFromProperties() {
	var PADDING = 40;
	var START = 660;
	
	
	var tableTemplate = "<table>";
	for(var i=0; i<SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME.length;i++) {
		START = START+PADDING;
		tableTemplate += "<tr style=\"position:absolute; top: "+START+"px; left: 200px\"><td style=\"font-weight: bold; width: 200px\">"+SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME[i]+"</td><td><input style=\"width: 200px\" id=\""+SAMPLE_TYPE_PROPERTIES[i]+"_filter\" type=\"text\"></td></tr>";
	}
	
	tableTemplate += "</table>";
	$("#filterContainer").append(tableTemplate);
}

function createVisualizeFiltersFromProperties() {
	for(var i=0;i<SAMPLE_TYPE_PROPERTIES.length;i++) {
		$('#'+SAMPLE_TYPE_PROPERTIES[i]+'_filter').keyup(function() {
			var filterResults = [];
			for(var i=0;i<SAMPLE_TYPE_PROPERTIES.length;i++) {
				filterResults[i] = $('#'+SAMPLE_TYPE_PROPERTIES[i]+'_filter').val();
			}
			
			visualize(
				filterResults
			);
		});	
	}
}

function populateMenu() {
	
	var menu = "";
		menu += "<div id='button-group'>	<button id='logout-button' style='position: absolute; right:10px; top:10px;'>Logout</button> </div>";
		menu += "<div id='search_browser' style='position: absolute; left:10px; top:100px'> <a href='search-browser.html'>Search</a> </div>";
		menu += "<div id='bacteria_browser'	style='position: absolute; left:10px; top:150px'> <a href='bacteria-browser.html'>Bacteria</a> </div>";
		menu += "<div id='chemical_browser'	style='position: absolute; left:10px; top:200px'> <a href='chemical-browser.html'>Chemicals</a> </div>";
		menu += "<div id='antibody_browser' style='position: absolute; left:10px; top:250px'> <a href='antibody-browser.html'>Antibodies</a> </div>";
		menu += "<div id='media_browser' style='position: absolute; left:10px; top:300px'> <a href='media-browser.html'>Media</a> </div>";
		menu += "<div id='buffer_browser' style='position: absolute; left:10px; top:350px'> <a href='solutions_buffers-browser.html'>Solutions Buffers</a> </div>";
		menu += "<div id='enzyme_browser' style='position: absolute; left:10px; top:400px'> <a href='enzyme-browser.html'>Enzymes</a> </div>";
		menu += "<div id='oligo_browser'	style='position: absolute; left:10px; top:450px'> <a href='oligo-browser.html'>Oligos</a> </div>";
		menu += "<div id='plasmid_browser'	style='position: absolute; left:10px; top:500px'> <a href='plasmid-browser.html'>Plasmids</a> </div>";
		menu += "<div id='yeast_browser' style='position: absolute; left:10px; top:550px'> <a href='yeast-browser.html'>Yeasts</a> </div>";
		menu += "<div id='protocol_browser' style='position: absolute; left:10px; top:600px'> <a href='general_protocol-browser.html'>General Protocols</a> </div>";
		menu += "<div id='pcr_browser' style='position: absolute; left:10px; top:650px'> <a href='pcr-browser.html'>PCR</a> </div>";
		menu += "<div id='western_blotting_browser' style='position: absolute; left:10px; top:700px'> <a href='western_blotting-browser.html'>Western Blotting</a> </div>";
		
	$("#sectionsContainer").append(menu);
	
	$('#logout-button').click(function() { 
		openbisServer.logout(function(data) { 
			$("#login-form-div").show();
			$("#main").hide();
			$("#username").focus();
		});
	});
}