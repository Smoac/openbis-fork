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
	var tableTemplate = "<table style='width:100%' class='table table-striped table-bordered table-hover' id=\"yeast-table\"><thead>";
	
	tableTemplate += "<tr style='border:none; border-collapse:collapse;'><td></td>";
	for(var i=0; i<SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME.length;i++) {
		tableTemplate += "<td style='border:none; border-collapse:collapse;'><input placeholder='"+SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME[i]+" filter' style=\"width: 90%\" id=\""+SAMPLE_TYPE_PROPERTIES[i]+"_filter\" type=\"text\"></td>";
	}
	tableTemplate += "</tr>";
	
	tableTemplate += "<tr class=\"yeast-table-header\"><th>Code</th>";
	for(var i=0; i<SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME.length;i++) {
		tableTemplate += "<th>" + SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME[i]+ "</th>";
	}
	tableTemplate += "<th></th></tr></thead><tbody id='sample-data-holder'></tbody></table>";
	
	$("#tableContainer").append(tableTemplate);
	
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
		menu += "<div id='button-group'>	<button class='btn' id='logout-button' style='position: absolute; right:10px; top:10px;'>Logout</button> </div>";
		menu += "<div id='search_browser' style='position: absolute; left:10px; top:100px'> <a style='width:110px' class='btn' href='search-browser.html'>Search</a> </div>";
		menu += "<div id='bacteria_browser'	style='position: absolute; left:10px; top:150px'> <a style='width:110px' class='btn' href='bacteria-browser.html'>Bacteria</a> </div>";
		menu += "<div id='chemical_browser'	style='position: absolute; left:10px; top:200px'> <a style='width:110px' class='btn' href='chemical-browser.html'>Chemicals</a> </div>";
		menu += "<div id='antibody_browser' style='position: absolute; left:10px; top:250px'> <a style='width:110px' class='btn' href='antibody-browser.html'>Antibodies</a> </div>";
		menu += "<div id='media_browser' style='position: absolute; left:10px; top:300px'> <a style='width:110px' class='btn' href='media-browser.html'>Media</a> </div>";
		menu += "<div id='buffer_browser' style='position: absolute; left:10px; top:350px'> <a style='width:110px' class='btn' href='solutions_buffers-browser.html'>Solutions Buffers</a> </div>";
		menu += "<div id='enzyme_browser' style='position: absolute; left:10px; top:400px'> <a style='width:110px' class='btn' href='enzyme-browser.html'>Enzymes</a> </div>";
		menu += "<div id='oligo_browser'	style='position: absolute; left:10px; top:450px'> <a style='width:110px' class='btn' href='oligo-browser.html'>Oligos</a> </div>";
		menu += "<div id='plasmid_browser'	style='position: absolute; left:10px; top:500px'> <a style='width:110px' class='btn' href='plasmid-browser.html'>Plasmids</a> </div>";
		menu += "<div id='yeast_browser' style='position: absolute; left:10px; top:550px'> <a style='width:110px' class='btn' href='yeast-browser.html'>Yeasts</a> </div>";
		menu += "<div id='protocol_browser' style='position: absolute; left:10px; top:600px'> <a style='width:110px' class='btn' href='general_protocol-browser.html'>Protocols</a> </div>";
		menu += "<div id='pcr_browser' style='position: absolute; left:10px; top:650px'> <a style='width:110px' class='btn' href='pcr-browser.html'>PCR</a> </div>";
		menu += "<div id='western_blotting_browser' style='position: absolute; left:10px; top:700px'> <a style='width:110px' class='btn' href='western_blotting-browser.html'>Western Blotting</a> </div>";
		
	$("#sectionsContainer").append(menu);
	
	$('#logout-button').click(function() { 
		openbisServer.logout(function(data) { 
			$("#login-form-div").show();
			$("#main").hide();
			$("#username").focus();
		});
	});
}

function populateMenuNew() {
	
	var menu = "";
		menu += "<span id='button-group'>	<button class='btn' id='logout-button' style='position: absolute; right:10px; top:10px;'>Logout</button> </span>";
		menu += "<center>";
		menu += "<span id='search_browser'> <a style='width:110px; margin:5px;' class='btn' href='search-browser.html'>Search</a> </span>";
		menu += "<span id='bacteria_browser'> <a style='width:110px; margin:5px;' class='btn' href='bacteria-browser.html'>Bacteria</a> </span>";
		menu += "<span id='chemical_browser'> <a style='width:110px; margin:5px;' class='btn' href='chemical-browser.html'>Chemicals</a> </span>";
		menu += "<span id='antibody_browser'> <a style='width:110px; margin:5px;' class='btn' href='antibody-browser.html'>Antibodies</a> </span>";
		menu += "<span id='media_browser'> <a style='width:110px; margin:5px;' class='btn' href='media-browser.html'>Media</a> </span>";
		menu += "<span id='buffer_browser'> <a style='width:110px; margin:5px;' class='btn' href='solutions_buffers-browser.html'>Solutions Buffers</a> </span>";
		menu += "<span id='enzyme_browser'> <a style='width:110px; margin:5px;' class='btn' href='enzyme-browser.html'>Enzymes</a> </span>";
		menu += "<span id='oligo_browser'> <a style='width:110px; margin:5px;' class='btn' href='oligo-browser.html'>Oligos</a> </span>";
		menu += "<span id='plasmid_browser'> <a style='width:110px; margin:5px;' class='btn' href='plasmid-browser.html'>Plasmids</a> </span>";
		menu += "<span id='yeast_browser'> <a style='width:110px; margin:5px;' class='btn' href='yeast-browser.html'>Yeasts</a> </span>";
		menu += "<span id='protocol_browser'> <a style='width:110px; margin:5px;' class='btn' href='general_protocol-browser.html'>Protocols</a> </span>";
		menu += "<span id='pcr_browser'> <a style='width:110px; margin:5px;' class='btn' href='pcr-browser.html'>PCR</a> </span>";
		menu += "<span id='western_blotting_browser'> <a style='width:110px; margin:5px;' class='btn' href='western_blotting-browser.html'>Western Blotting</a> </span>";
		menu += "</center>";
		
	$("#sectionsContainer").append(menu);
	
	$('#logout-button').click(function() { 
		openbisServer.logout(function(data) { 
			$("#login-form-div").show();
			$("#main").hide();
			$("#username").focus();
		});
	});
}

function showEditWindowForSample(code, permId) {
	//Working Example: https://sprint-openbis.ethz.ch/openbis/?viewMode=embedded#action=EDITING&entity=SAMPLE&code=A123&permId=20091006093948112-162773
	var editURLTemplate = openbisUrl + "openbis/?viewMode=embedded#action=EDITING&entity=SAMPLE";
	var codeParam = "&code="+code;
	var permId = "&permId="+permId;
	//window.open(editURLTemplate+codeParam+permId);
	$('#editFrame').attr('src', editURLTemplate+codeParam+permId);
}