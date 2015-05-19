
<html>
<head><title>PlasMapper - Graphic Map</title></head>
<body>
<br>

<br>
<%
  String fileFormat = request.getParameter("fileFormat");
  String imageFile = "../tmp/" + request.getParameter("fileName");
%>

<% if(fileFormat.equals("svg") || fileFormat.equals("svgz")){ %>

<embed src= <%= imageFile %> type="image/svg+xml" pluginspage="http://www.adobe.com/svg/viewer/install/" height="1000" width="1000" id ="Panel">

<% }else{ %>

<img src= <%=imageFile%>>

<% } %>

</body>
</html>
