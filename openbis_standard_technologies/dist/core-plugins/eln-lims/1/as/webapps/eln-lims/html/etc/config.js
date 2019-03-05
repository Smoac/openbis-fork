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

var onLoadInstanceProfileResorceFunc = function() {
	profile = new InstanceProfile();
	//
	// Updating title and logo
	//
	$("#mainLogo").attr("src", profile.mainLogo);
	$("#mainLogoTitle").append(profile.mainLogoTitle);
	if(profile.mainLogoTitle.length < 10) {
		$("#mainLogoTitle").css("font-weight", "bold");
	}
	$("login-form-div").attr("visibility", "visible");
}

//<PROFILE_PLACEHOLDER>
loadJSResorce("./etc/InstanceProfile.js", onLoadInstanceProfileResorceFunc);
//</PROFILE_PLACEHOLDER>