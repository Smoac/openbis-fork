function defineAction(str){


    if(str=='Format'){
         window.document.plasmidMap.target = "";
	 		window.document.plasmidMap.action="/PlasMapper/jsp/format.jsp";
      }
      else if(str=="TextOutput"){
	result = window.open("/PlasMapper/html/dummy.html", "result","toolbar=1,width=900,height=600,top=0,left=0, status=1,location=1,menubar=1,scrollbars=1,resizable=1" );
        window.document.plasmidMap.target = "result";
        result.focus();

	window.document.plasmidMap.action="/PlasMapper/servlet/TextMap";

	}
	 else if(str=="Genbank"){
	result = window.open("/PlasMapper/html/dummy.html", "result","toolbar=1,width=900,height=600,top=0,left=0, status=1,location=1,menubar=1,scrollbars=1,resizable=1" );
        window.document.plasmidMap.target = "result";
        result.focus();

	window.document.plasmidMap.action="/PlasMapper/servlet/GenbankOutput";

	}
	else{
	result = window.open("/PlasMapper/html/dummy.html", "result","toolbar=1,width=900,height=600,top=0,left=0, status=1,location=1,menubar=1,scrollbars=1,resizable=1" );
            window.document.plasmidMap.target = "result";
            result.focus();

                window.document.plasmidMap.action="/PlasMapper/servlet/DrawVectorMap";


	   }


}

function openLibrary(){

 a_vendor = this.document.plasmidMap.vendor.options[this.document.plasmidMap.vendor.selectedIndex].value;

  window.location.href='/PlasMapper/jsp/library.jsp?vendor=' + a_vendor;

}

function displayHelp() {
 help = window.open("/PlasMapper/html/help.html", "help","toolbar=1,width=900,height=600,top=0,left=0, status=1,location=1,menubar=1,scrollbars=1,resizable=1");
  help.focus();


}

function validateFormat(){

  var seqValue = window.document.plasmidMap.sequence.value;

  if(seqValue == null || seqValue.length == 0){
	alert("You should paste a DNA sequence into the text window before you format");
	return false;
  }
    return true;

}

function validateSubmit(){

  var fileValue = window.document.plasmidMap.fastaFile.value;
  var seqValue = window.document.plasmidMap.sequence.value;

  if(fileValue == null || fileValue.length == 0){

       if(seqValue == null || seqValue.length == 0){

	       alert("Please select a desired DNA sequence file or paste a DNA sequence into the DNA text window");
	       return false;
       }
   }
   else{

       if(seqValue == null || seqValue.length == 0)
		    //return true;
	  valiedateUserFeature();

       else{
	       alert("You cannot process a sequence file and a sequence in the text window at the same time");
	       return false;
       }
  }

  //return true;
	return valiedateUserFeature();
}


function valiedateUserFeature() {

	if(window.document.plasmidMap.featureName1.value != "" && window.document.plasmidMap.featureName1.value.length != 0){

		if(window.document.plasmidMap.start1.value == "" || window.document.plasmidMap.start1.value.length == 0){
	                 alert("Please fill in the start position of the user defined feature1");
	                 return false;
	         }
	        else if(checkNumber(window.document.plasmidMap.start1.value) == false){
	                 alert("The start position of the user defined feature1 is not a valid number");
	                 return false;
	        }
		if(window.document.plasmidMap.stop1.value == "" || window.document.plasmidMap.stop1.value.length == 0){
	                 alert("Please fill in the stop position of the user defined feature1");
	                 return false;
	         }
	        else if(checkNumber(window.document.plasmidMap.stop1.value) == false){
	                 alert("The stop position of the user defined feature1 is not a valid number");
	                 return false;
	        }
	}
	if(window.document.plasmidMap.featureName2.value != "" && window.document.plasmidMap.featureName2.value.length != 0){

		if(window.document.plasmidMap.start2.value == "" || window.document.plasmidMap.start2.value.length == 0){
	                 alert("Please fill in the start position of the user defined feature2");
	                 return false;
	         }
	        else if(checkNumber(window.document.plasmidMap.start2.value) == false){
	                 alert("The start position of the user defined feature2 is not a valid number");
	                 return false;
	        }
		if(window.document.plasmidMap.stop2.value == "" || window.document.plasmidMap.stop2.value.length == 0){
	                 alert("Please fill in the stop position of the user defined feature2");
	                 return false;
	         }
	        else if(checkNumber(window.document.plasmidMap.stop2.value) == false){
	                 alert("The stop position of the user defined feature2 is not a valid number");
	                 return false;
	        }
	}
	if(window.document.plasmidMap.featureName3.value != "" && window.document.plasmidMap.featureName3.value.length != 0){

		if(window.document.plasmidMap.start3.value == "" || window.document.plasmidMap.start3.value.length == 0){
	                 alert("Please fill in the start position of the user defined feature3");
	                 return false;
	         }
	        else if(checkNumber(window.document.plasmidMap.start3.value) == false){
	                 alert("The start position of the user defined feature3 is not a valid number");
	                 return false;
	        }
		if(window.document.plasmidMap.stop3.value == "" || window.document.plasmidMap.stop3.value.length == 0){
	                 alert("Please fill in the stop position of the user defined feature3");
	                 return false;
	         }
	        else if(checkNumber(window.document.plasmidMap.stop3.value) == false){
	                 alert("The stop position of the user defined feature3 is not a valid number");
	                 return false;
	        }
	}
		if(window.document.plasmidMap.featureName4.value != "" && window.document.plasmidMap.featureName4.value.length != 0){

		if(window.document.plasmidMap.start4.value == "" || window.document.plasmidMap.start4.value.length == 0){
	                 alert("Please fill in the start position of the user defined feature4");
	                 return false;
	         }
	        else if(checkNumber(window.document.plasmidMap.start4.value) == false){
	                 alert("The start position of the user defined feature4 is not a valid number");
	                 return false;
	        }
		if(window.document.plasmidMap.stop4.value == "" || window.document.plasmidMap.stop4.value.length == 0){
	                 alert("Please fill in the stop position of the user defined feature4");
	                 return false;
	         }
	        else if(checkNumber(window.document.plasmidMap.stop4.value) == false){
	                 alert("The stop position of the user defined feature4 is not a valid number");
	                 return false;
	        }
	}
	return true;


}

function checkNumber(TheNumber) {

	var GoodChars = "0123456789"
	var i = 0
	if (TheNumber=="") {
		// Return false if number is empty
		return false;
	}
	for (i =0; i <= TheNumber.length -1; i++) {
		if (GoodChars.indexOf(TheNumber.charAt(i)) == -1) {
			return false;
		}
	}
	return true;
}
