/*!
 * Utility methods to ease Javascript development.
 * 
 * Copyright 2011 ETH Zuerich, CISD
 */

/**
 * Return true if str ends with suffix.
 */
function endsWith(str, suffix)
{
    var lastIndex = str.lastIndexOf(suffix);
    return (lastIndex != -1) && (lastIndex + suffix.length == str.length);
}

/**
 * A sort function that uses properties.
 *
 * @param propName a name of property
 * @returns {Function} a function that can be passed to sort
 */
function sortByProp(propName) 
{
	var sortFunction = function(a, b) { if (a[propName] == b[propName]) return 0; return (a[propName] < b[propName]) ? -1 : 1};
	return sortFunction;
}

/**
 * @param arr an array
 * @returns the unique elements of an array
 */
function uniqueElements(arr) 
{
	var reduceFunc = function(list, elt) {
		var size = list.length;
		if (size == 0 || list[size - 1] != elt) 
		{ 
			list.push(elt); 
		}
		return list;
	};
	return arr.reduce(reduceFunc, []);
}

/**
 * A function that groups a collection into collections of length numElts.
 * 
 * @param numElts Number of elements
 * @returns {Function} a function that can be passed to reduce
 */
function groupBy(numElts)
{
	var groupBy = function(groups, elt) {
		if (groups.length < 1) {
			groups.push([elt]);
			return groups;
		}
		
		var lastGrp = groups[groups.length - 1];
		if (lastGrp.length < numElts) {
			lastGrp.push(elt);
		} else {
			groups.push([elt]);
		}
	
		return groups;
	}
	return groupBy;
}

/**
 * Check if the current elt number = last elt number + 1
 */
function isRun(lastElt, currentElt) {
	var lastNumber = Number(lastElt.label);
	var currentNumber = Number(currentElt.label);
	// Assume that non numeric values are runs
	if (lastNumber == NaN || currentNumber == NaN) return true;
	
	return currentNumber == (lastNumber + 1);
}

/**
 * A function that groups a collection into sequential runs of length maxNumEltsPerGroup
 * 
 * @param maxNumEltsPerGroup Number of elements
 * @returns {Function} a function that can be passed to reduce
 */
function groupByRuns(maxNumEltsPerGroup)
{
  var lastSeen = "";
	var groupBy = function(groups, elt) {
		// Initialize the groups
		if (groups.length < 1) { groups.push([elt]); return groups; }

		// Check if we should append to the last group or create a new one
		var lastGrp = groups[groups.length - 1];
		var createNewGroup = false;
		if (lastGrp.length >= maxNumEltsPerGroup) {
			// We've reached the size limit of the group
			createNewGroup = true;
		} else {
			// See if this is a run, if not create a new group
			createNewGroup = !isRun(lastGrp[lastGrp.length - 1], elt);
		}

		(createNewGroup) ? groups.push([elt]) : lastGrp.push(elt);
	
		return groups;
	}
	return groupBy;
}

/**
 * Convert a map of properties to an array of pairs
 */
function props_to_pairs(entity)
{
	var pairs = [];
	
	var d = entity.properties;
	
	for (var prop in d) {
		var pair = [prop, d[prop]];
		pairs.push(pair);
	}
	pairs.sort(function(a, b) { 
		if (a[0] == b[0]) return 0;
		// Sort in reverse lexicographical
		return (a[0] < b[0]) ? -1 : 1;
	});
	return pairs;
}
