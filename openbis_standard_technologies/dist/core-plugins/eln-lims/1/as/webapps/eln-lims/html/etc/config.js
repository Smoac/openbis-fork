var loadJSResorce = function(pathToResource, onLoad) {
		var head = document.getElementsByTagName('head')[0];
		var script= document.createElement('script');
		script.type= 'text/javascript';
		var src = pathToResource;
		script.src= src;
		script.onreadystatechange= function () {
			if (this.readyState == 'complete') onLoad();
		}
		script.onload = onLoad;
		
		head.appendChild(script);
}

//<PROFILE_PLACEHOLDER>
loadJSResorce("./js/config/StandardProfile.js", function() { profile = new StandardProfile(); });
//</PROFILE_PLACEHOLDER>