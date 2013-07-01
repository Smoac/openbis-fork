$(document).ready(function(){

$('.com-clcbio-client-thin-gui-PreviewTabPanelimpTab').hover(function() {
  $(this).addClass('com-clcbio-client-thin-gui-PreviewTabPaneleditTab');
}, function() {
  $(this).removeClass('com-clcbio-client-thin-gui-PreviewTabPaneleditTab');
});

$("div.com-clcbio-client-thin-gui-PreviewTabPanelimpTab").click(function() {
	alert("davs");
});

});