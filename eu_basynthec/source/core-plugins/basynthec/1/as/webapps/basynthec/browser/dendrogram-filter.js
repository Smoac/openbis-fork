

/**
 * @param inputBox the CSS selector of a text field, where users can enter restrictions 
 *        for the displayed tree nodes
 * @param renderTreeAction a function to be used to render the tree
 * @param 
 */
function addFilteringToDendrogram(inputBox, renderAction, maxLeafNodes) 
{
	$(inputBox).change(function() {
		
		var filterText = $(inputBox).text();
		var delayRenderActionInMs = 300;

		// execute filtering async and give user time to 
		// continue typing
		setTimeout(function() {
			var filterText2 = $(inputBox).text()
			
			if (filterText != filterText2) {
				// user has changed filter in the meantime
				return;
			}
			
			var filteredTreeRoot = { name:root.name, children: [] };
			filterNode(root, filteredTreeRoot, filterText, maxLeafNodes);
			renderAction(filteredTreeRoot);
			
		}, delayRenderActionInMs)
		
	});
}

function filterTree(root, filterText, maxLeafNodes) {
	
}

function filterNode(sourceNode, resultNode, filterText, maxLeafNodes) 
{
	
}

function reduceNames(groups, name) 
{ 
	if (groups.length < 1) return groups.push([name]); 
	
	var last = groups[groups.length - 1]; 
	if (last[last.length - 1].charAt(0) == name.charAt(0)) 
		last.push(name); 
	else 
		groups.push([name]);

	return groups;
}
