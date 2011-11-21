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
 * 
 * @param propName a name of property
 * @returns {Function} a function that can be passed to 
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

