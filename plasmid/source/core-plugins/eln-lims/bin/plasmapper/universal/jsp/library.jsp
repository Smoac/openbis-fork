<%@ page 
import="ca.ualberta.xdong.plasMapper.annotate.*, java.util.*" %>
<%
String vendor = request.getParameter("vendor");

VectorSeqDatabase dbVector = new VectorSeqDatabase();
dbVector.setVendor(vendor);
Vector libraries = dbVector.getAllNameID();

if (dbVector.getConnection() != null)
	System.out.println("Connection is not closed");
dbVector.destroy();
if ((dbVector.getConnection()).isClosed())
	System.out.println("Connection is closed");
%>

<html>
<head>
<title>Plasmid Library</title>
</head>
<body>

<br>
<%

for (int i = 0; i < libraries.size(); i++) {

	LibrarySeq libSeq = ((LibrarySeq) libraries.elementAt(i));
	String id = libSeq.getID();

	out.println(
		"<br> <a href=\"/PlasMapper/jsp/librarySeq.jsp?id="
			+ id
			+ "\">"
			+ libSeq.getName()
			+ "</a>");
	out.println("<br>");
}
%>


</body>
</html>
