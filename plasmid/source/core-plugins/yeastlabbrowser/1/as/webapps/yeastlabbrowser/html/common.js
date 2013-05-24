var openbisUrl = 'http://localhost:20000/openbis/openbis';
var dssUrl = 'http://localhost:20001/datastore_server';

// var openbisUrl = 'https://openbis-csb.ethz.ch/openbis/openbis';
// var dssUrl = 'https://openbis-csb.ethz.ch:443/datastore_server';

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
