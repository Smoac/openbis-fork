<%@ page 
import="ca.ualberta.xdong.plasMapper.annotate.*" %>

<%String id = request.getParameter("id");
VectorSeqDatabase dbVector = new VectorSeqDatabase();
//dbVector.setVendor(dbVector.getVendor(id));
StringBuffer seqBuffer = dbVector.getSeq(id);
dbVector.destroy();
%>

<html>
<head>
<meta HTTP-EQUIV=" Content-Type"
	CONTENT=" text/html; charset=iso-8859-1">

<meta NAME=" Description"
	CONTENT=" Wishart Pharmaceutical Research Group -
Vadar Program">

<meta NAME=" Keywords"
	CONTENT="University of Alberta,Computing Sciences,
Biology Science, Bioinformatics,Plasmid,Vector,drawing,annotation,web server,
Vector Feature,Xiaoli Dong,Paul Stothard,Ian J. Forsythe, David S. Wishart">

<link rel=stylesheet type="text/css"
	href="/PlasMapper/style/PlasMapper.css"
	title="default PlasMapper styles" />

<title>PlasMapper - Library Sequence</title>

<SCRIPT LANGUAGE="javascript" TYPE="text/javascript"
	SRC="/PlasMapper/javaScript/PlasMapper.js"></SCRIPT>


</head>

<body bgcolor="#ffffff">

<table align="center" border=0>
	<tr>
		<td align="center">


		<form name="plasmidMap" METHOD="post" ENCTYPE="multipart/form-data">

		<table width="650" border="0" align="center" cellspacing="0"
			cellpadding="0" style="border-collapse: collapse">
			<tr valign="top">
				<td align="center"><img SRC="/PlasMapper/image/plasMapper.jpg"></td>
			</tr>

			<tr>
				<td height=10 align="center">&nbsp;</td>
			</tr>

			<tr>
				<td align="center">
				<h1>PlasMapper Version 2.0</h1>
				</td>
			</tr>

			<tr>
				<td><img SRC="/PlasMapper/image/blueline.gif" ALT="blue line"
					width="644" height="7"></td>
			</tr>
			<tr>
				<td width=645>The PlasMapper server automatically generates and
				annotates plasmid maps using only the plasmid DNA sequence as input.
				Plasmid sequences up to 20,000 bp may be annotated and displayed.
				Plasmid figures may be rendered in PNG, JPG, SVG or SVGZ format.
				PlasMapper supports an extensive array of display options. <br>
				<br>
				Please cite the following: <a
					href="http://www.pubmedcentral.gov/articlerender.fcgi?tool=pubmed&pubmedid=15215471">Xiaoli
				Dong, Paul Stothard, Ian J. Forsythe, and David S. Wishart
				"PlasMapper: a web server for drawing and auto-annotating plasmid
				maps" Nucleic Acids Res. 2004 Jul 1;32(Web Server issue):W660-4.</a>
				<br>

				</td>
			</tr>
			<tr>
				<td><img SRC="/PlasMapper/image/blueline.gif" ALT="blue line"
					width="645" height="7"></td>
			</tr>



			<tr valign="top">
				<td height="40" colspan=2 width="645">For additional information on
				how to run PlasMapper, click <input TYPE="button" name="HELP"
					value="HELP" onClick="displayHelp();"></td>
			</tr>
			<tr>
				<td height=52 colspan=2 width="645">Select desired DNA sequence file
				&nbsp;(FASTA format only) <input TYPE="file" NAME="fastaFile"
					VALUE="" SIZE=50></td>
			</tr>



			<tr>
				<td height="55" colspan=2 width="645"><br>
				<strong><font color=green>OR</font></strong> Select a plasmid based
				on supplier <select name="vendor">
					<option value="Amersham%20Pharmacia">Amersham Pharmacia</option>
					<option value="Clontech">Clontech</option>
					<option value="Epicentre">Epicentre</option>
					<option value="IBI">IBI</option>
					<option value="Invitrogen">Invitrogen</option>
					<option value="NEB">NEB</option>
					<option value="Promega">Promega</option>
					<option value="Public">Public</option>
					<option value="Stratagene">Stratagene</option>
				</select> <input type="button" name="library"
					value="Plasmid Library" onClick="openLibrary();"> <br>
				<br>
				<strong><font color=green>OR</font></strong> paste the DNA sequence
				into the text window below &nbsp;(FASTA fomat only) <br>
				<textarea NAME="sequence" ROWS="8" COLS="80"><%=Utility.formatToFasta(new String(seqBuffer))%></textarea>
				<br>

				</td>
			</tr>
			<tr>
				<td colspan=2 align=center><input TYPE="submit" NAME="Submit"
					VALUE="Graphic Map"
					onClick="if(validateSubmit() == false)return false;defineAction('Submit');">

				<input TYPE="submit" NAME="Submit" VALUE="Text Map"
					onClick="if(validateSubmit() == false)return false;defineAction('TextOutput');">
				<input TYPE="submit" NAME="Submit" VALUE="Genbank Format"
					onClick="if(validateFormat() == false) return false;defineAction('Genbank')">
				<input TYPE="submit" NAME="Submit" VALUE="(Re)Format"
					onClick="if(validateFormat() == false) return false;defineAction('Format')">
				<input TYPE="button" NAME="Clear" VALUE="Clear"
					onClick="window.document.plasmidMap.sequence.value='';"></td>


			</tr>

			<tr>
				<td colspan=2 align="center" height=15><img
					SRC="/PlasMapper/image/blueline.gif" ALT="blue line" width="645"
					height="7"></td>
			</tr>

			<tr>
				<td><b>Feature Options:</b>

				<table border=1 width=100%>

					<tr>
						<td width=50%>

						<table border=0>
							<tr>

								<td><input type="checkbox" name="showOption" value=1 checked></td>
								<td>&nbsp; Replication Origin(s)</td>
							</tr>

							<tr>

								<td><input type="checkbox" name="showOption" value=2 checked></td>
								<td>&nbsp; Promoter(s)</td>
							</tr>

							<tr>

								<td><input type="checkbox" name="showOption" value=3 checked></td>
								<td>&nbsp; Terminator(s)</td>
							</tr>

							<tr>


								<td><input type="checkbox" name="showOption" value=4 checked></td>
								<td>&nbsp; Selectable Marker(s)</td>
							</tr>
						</table>
						</td>

						<!--td width = 35>&nbsp;</td-->

						<td width=50%>
						<table>

							<tr>

								<td><input type="checkbox" name="showOption" value=6 checked></td>
								<td>&nbsp; Regulatory Sequence(s)</td>
							</tr>

							<tr>

								<td><input type="checkbox" name="showOption" value=7 checked></td>
								<td>&nbsp; Affinity Tag(s)</td>
							</tr>

							<tr>

								<td><input type="checkbox" name="showOption" value=8 checked></td>
								<td>&nbsp; Miscellaneous Feature(s)</td>
							</tr>
							<tr>

								<td><input type="checkbox" name="showOption" value=5 checked></td>
								<td>&nbsp; Reporter Gene(s)</td>
							</tr>
						</table><tr>
						<td>

						<table>
							<tr>
								<td colspan=2><input type="checkbox" name="showOption" value=9
									checked>&nbsp; Restriction site(s)</td>
							</tr>
							<tr>
								<td colspan=2><font color=green size="2"><i>(Use common enzyme
								set to annotate sequence)</i></font></td>
							</tr>
							<tr>
								<td><input type="radio" name="restriction" value=1 checked>&nbsp;
								Unique sites</td>
							</tr>
							<tr>
								<td><input type="radio" name="restriction" value=2>&nbsp; Unique
								sites from &#62;&#61; 4 cutters</td>
							</tr>
							<tr>
								<td><input type="radio" name="restriction" value=3>&nbsp; Unique
								sites from &#62;&#61; 6 cutters</td>
							</tr>
							<tr>
								<td><input type="radio" name="restriction" value=4>&nbsp; All
								sites</td>
							</tr>
							<tr>
								<td><input type="radio" name="restriction" value=5>&nbsp; All
								sites from &#62;&#61; 4 cutters</td>
							</tr>
							<tr>
								<td><input type="radio" name="restriction" value=6>&nbsp; All
								sites from &#62;&#61; 6 cutters</td>
							</tr>
						</table>
						</td>

						<td>
						<table>
							<tr>
								<td colspan=2>&nbsp;</td>
							</tr>
							<tr>
								<td colspan=2><font color=green size="2"><i>(Use all enzyme set
								to annotate sequence)</i></font></td>
							</tr>
							<tr>
								<td><input type="radio" name="restriction" value=7>&nbsp; Unique
								sites</td>
							</tr>
							<tr>
								<td><input type="radio" name="restriction" value=8>&nbsp; Unique
								sites from &#62;&#61; 4 cutters</td>
							</tr>
							<tr>
								<td><input type="radio" name="restriction" value=9>&nbsp; Unique
								sites from &#62;&#61; 6 cutters</td>
							</tr>
							<tr>
								<td><input type="radio" name="restriction" value=10>&nbsp; All
								sites</td>
							</tr>
							<tr>
								<td><input type="radio" name="restriction" value=11>&nbsp; All
								sites from &#62;&#61; 4 cutters</td>
							</tr>
							<tr>
								<td><input type="radio" name="restriction" value=12>&nbsp; All
								sites from &#62;&#61; 6 cutters</td>
							</tr>
						</table>
						</td>

					</tr>


					<tr>
						<td colspan=2>Display ORFs that are at least &nbsp;<input
							type="text" name="orfLen" size=5 value=200>&nbsp;codons long on
						the <br>
						&nbsp;<input type="checkbox" name="strand" value=1 checked>&nbsp;
						Forward Strand <br>
						&nbsp;<input type="checkbox" name="strand" value=2 checked>&nbsp;
						Reverse Strand</td>
					</tr>
				</table>

				</td>
			</tr>

			<tr>
				<td><br>
				<b>User Defined Features:</b>
				<table border=1 width="100%" style="border-collapse: collapse">
					<tr>
						<td colspan=2>
						<table width="100%">
							<tr>
								<td>Feature1 Name</td>
								<td><input type="text" name="featureName1" size=20></td>
								<td>Start</td>
								<td><input type="text" name="start1" size=5></td>
								<td rowspan=2>&nbsp;Strand &nbsp;</td>
								<td rowspan=2><select name="dir1">
									<option value=1>Forward
									<option value=2>Reverse
								</select></td>
							</tr>
							<tr>
								<td>Category</td>
								<td><select name="category1">
									<option value="origin_of_replication">origin of replication
									<option value="promoter">promoter
									<option value="terminator">terminator
									<option value="selectable_marker">selectable marker
									<option value="reporter_gene">reporter gene
									<option value="regulatory_sequence">regulatory sequence
									<option value="tag">tag
									<option value="other_gene">other gene
									<option value="unique_restriction_site">unique restriction site
									
									<option value="restriction_site">restriction site
									<option value="open_reading_frame">open reading frame
								</select></td>
								<td>Stop &nbsp;</td>
								<td><input type="text" name="stop1" size=5></td>
							</tr>
						</table>
						</td>
					</tr>

					<tr>
						<td colspan=2>
						<table width="100%">
							<tr>
								<td>Feature2 Name</td>
								<td><input type="text" name="featureName2" size=20></td>
								<td>Start</td>
								<td><input type="text" name="start2" size=5></td>
								<td rowspan=2>&nbsp;Strand &nbsp;</td>
								<td rowspan=2><select name="dir2">
									<option value=1>Forward
									<option value=2>Reverse
								</select></td>
							</tr>
							<tr>
								<td>Category</td>
								<td><select name="category2">
									<option value="origin_of_replication">origin of replication
									<option value="promoter">promoter
									<option value="terminator">terminator
									<option value="selectable_marker">selectable marker
									<option value="reporter_gene">reporter gene
									<option value="regulatory_sequence">regulatory sequence
									<option value="tag">tag
									<option value="other_gene">other gene
									<option value="unique_restriction_site">unique restriction site
									
									<option value="restriction_site">restriction site
									<option value="open_reading_frame">open reading frame
								</select></td>
								<td>Stop &nbsp;</td>
								<td><input type="text" name="stop2" size=5></td>
							</tr>
						</table>
						</td>
					</tr>


					<tr>
						<td colspan=2>
						<table width="100%">
							<tr>
								<td>Feature3 Name</td>
								<td><input type="text" name="featureName3" size=20></td>
								<td>Start</td>
								<td><input type="text" name="start3" size=5></td>
								<td rowspan=2>&nbsp;Strand &nbsp;</td>
								<td rowspan=2><select name="dir3">
									<option value=1>Forward
									<option value=2>Reverse
								</select></td>
							</tr>
							<tr>
								<td>Category</td>
								<td><select name="category3">
									<option value="origin_of_replication">origin of replication
									<option value="promoter">promoter
									<option value="terminator">terminator
									<option value="selectable_marker">selectable marker
									<option value="reporter_gene">reporter gene
									<option value="regulatory_sequence">regulatory sequence
									<option value="tag">tag
									<option value="other_gene">other gene
									<option value="unique_restriction_site">unique restriction site
									
									<option value="restriction_site">restriction site
									<option value="open_reading_frame">open reading frame
								</select></td>
								<td>Stop &nbsp;</td>
								<td><input type="text" name="stop3" size=5></td>
							</tr>
						</table>
						</td>
					</tr>


					<tr>
						<td colspan=2>
						<table width="100%">
							<tr>
								<td>Feature4 Name</td>
								<td><input type="text" name="featureName4" size=20></td>
								<td>Start</td>
								<td><input type="text" name="start4" size=5></td>
								<td rowspan=2>&nbsp;Strand &nbsp;</td>
								<td rowspan=2><select name="dir4">
									<option value=1>Forward
									<option value=2>Reverse
								</select></td>
							</tr>
							<tr>
								<td>Category</td>
								<td><select name="category4">
									<option value="origin_of_replication">origin of replication
									<option value="promoter">promoter
									<option value="terminator">terminator
									<option value="selectable_marker">selectable marker
									<option value="reporter_gene">reporter gene
									<option value="regulatory_sequence">regulatory sequence
									<option value="tag">tag
									<option value="other_gene">other gene
									<option value="unique_restriction_site">unique restriction site
									
									<option value="restriction_site">restriction site
									<option value="open_reading_frame">open reading frame
								</select></td>
								<td>Stop &nbsp;</td>
								<td><input type="text" name="stop4" size=5></td>
							</tr>
						</table>
						</td>
					</tr>

					<tr>
						<td colspan=2>
						<table width="100%">
							<tr>
								<td>Feature5 Name</td>
								<td><input type="text" name="featureName5" size=20></td>
								<td>Start</td>
								<td><input type="text" name="start5" size=5></td>
								<td rowspan=2>&nbsp;Strand &nbsp;</td>
								<td rowspan=2><select name="dir5">
									<option value=1>Forward
									<option value=2>Reverse
								</select></td>
							</tr>
							<tr>
								<td>Category</td>
								<td><select name="category5">
									<option value="origin_of_replication">origin of replication
									<option value="promoter">promoter
									<option value="terminator">terminator
									<option value="selectable_marker">selectable marker
									<option value="reporter_gene">reporter gene
									<option value="regulatory_sequence">regulatory sequence
									<option value="tag">tag
									<option value="other_gene">other gene
									<option value="unique_restriction_site">unique restriction site
									
									<option value="restriction_site">restriction site
									<option value="open_reading_frame">open reading frame
								</select></td>
								<td>Stop &nbsp;</td>
								<td><input type="text" name="stop5" size=5></td>
							</tr>
						</table>
						</td>
					</tr>

					<tr>
						<td colspan=2>
						<table width="100%">
							<tr>
								<td>Feature6 Name</td>
								<td><input type="text" name="featureName6" size=20></td>
								<td>Start</td>
								<td><input type="text" name="start6" size=5></td>
								<td rowspan=2>&nbsp;Strand &nbsp;</td>
								<td rowspan=2><select name="dir6">
									<option value=1>Forward
									<option value=2>Reverse
								</select></td>
							</tr>
							<tr>
								<td>Category</td>
								<td><select name="category6">
									<option value="origin_of_replication">origin of replication
									<option value="promoter">promoter
									<option value="terminator">terminator
									<option value="selectable_marker">selectable marker
									<option value="reporter_gene">reporter gene
									<option value="regulatory_sequence">regulatory sequence
									<option value="tag">tag
									<option value="other_gene">other gene
									<option value="unique_restriction_site">unique restriction site
									
									<option value="restriction_site">restriction site
									<option value="open_reading_frame">open reading frame
								</select></td>
								<td>Stop &nbsp;</td>
								<td><input type="text" name="stop6" size=5></td>
							</tr>
						</table>
						</td>
					</tr>
				</table>
				</td>
			</tr>

			<tr>
				<td colspan=2 align="center" height=15><br>
				<img SRC="/PlasMapper/image/blueline.gif" ALT="blue line"
					width="645" height="7"></td>
			</tr>

			<tr>
				<td valign=top><b>Plasmid Map Graphic Display Options:</b>


				<table border=0>
					<tr>
						<td valign="top">

						<table border=0 width="100%">
							<tr>
								<td>Color Scheme</td>
								<td><input type=radio name="scheme" value=0 checked>Regular</td>
								<td><input type=radio name="scheme" value=1>Inverse</td>
							</tr>
							<tr>
								<td>Shading</td>
								<td><input type=radio name="shading" value=0 checked>On</td>
								<td><input type=radio name="shading" value=1>Off</td>
							</tr>
							<tr>
								<td>Label Colors</td>
								<td><input type=radio name="labColor" value=0 checked>On</td>
								<td><input type=radio name="labColor" value=1>Off</td>
							</tr>
							<tr>
								<td>Label Boxes</td>
								<td><input type=radio name="labelBox" value=0>On</td>
								<td><input type=radio name="labelBox" value=1 checked>Off</td>
							</tr>
							<tr>
								<td>Labels</td>
								<td><input type=radio name="labels" value=0 checked>On</td>
								<td><input type=radio name="labels" value=1>Off</td>
							</tr>
							<tr>
								<td>Inner Labels</td>
								<td><input type=radio name="innerLabels" value=0 checked>On</td>
								<td><input type=radio name="innerLabels" value=1>Off</td>
							</tr>
							<tr>
								<td>Legend</td>
								<td><input type=radio name="legend" value=0 checked>On</td>
								<td><input type=radio name="legend" value=1>Off</td>
							</tr>
							<tr>
								<td>Arrows</td>
								<td><input type=radio name="arrow" value=0 checked>On</td>
								<td><input type=radio name="arrow" value=1>Off</td>
							</tr>
							<tr>
								<td>Tick Marks</td>
								<td><input type=radio name="tickMark" value=0 checked>On</td>
								<td><input type=radio name="tickMark" value=1>Off</td>
							</tr>
						</table>

						</td>

						<td width=35>&nbsp;</td>

						<td>
						<table border=0>
							<tr>
								<td>Title</td>
								<td><input type="text" name="mapTitle" size=20></td>
							</tr>
							<tr>
								<td>Display comment</td>
								<td><input type="text" name="comment" size=20
									value="Created using PlasMapper"></td>
							</tr>
							<tr>
								<td>Image format</td>
								<td><select name="imageFormat">
									<option>PNG
									<option>JPG
									<option>SVG
									<option>SVGZ
								</select></td>
							</tr>
							<tr>
								<td>Image Size</td>
								<td><select name="imageSize">

									<option>500 x 200
									<option>550 x 200
									<option>550 x 450
									<option>650 x 550
									<option>750 x 650
									<option selected>850 x 750
									<option>1000 x 800
									<option>1028 x 800
									<option>1059 x 800
									<option>1200 x 1000
									<option>2400 x 2000
								</select></td>
							</tr>

							<tr>
								<td>Backbone Thickness</td>
								<td><select name="backbone">
									<option>xxx-small
									<option>xx-small
									<option>x-small
									<option>small
									<option selected>medium
									<option>large
									<option>x-large
									<option>xx-large
									<option>xxx-large
								</select></td>
							</tr>

							<tr>
								<td>Arc Thickness</td>
								<td><select name="arc">
									<option>xxx-small
									<option>xx-small
									<option>x-small
									<option>small
									<option selected>medium
									<option>large
									<option>x-large
									<option>xx-large
									<option>xxx-large
								</select></td>
							</tr>
						</table>

						</td>
					</tr>
				</table>


				</td>
			</tr>
		</table>
		</form>
	<tr>
		<td valign="top" align="center"><img
			SRC="/PlasMapper/image/blueline.gif" ALT="blue line" width="650"
			height="7"></td>
	</tr>
	<tr>
		<td align="center">Problems? Questions? Suggestions? Please contact <a
			href="mailto:xdong@redpoll.pharmacy.ualberta.ca">Xiaoli Dong</a>, <a
			href="mailto:stothard@ualberta.ca">Paul Stothard</a> or <a
			HREF="mailto:david.wishart@ualberta.ca">David Wishart</a></td>
	</tr>
	<tr>
		<td height=25 valign="middle">Funding for this project was provided by
		&nbsp; <a href="http://www.pence.ca/pence/index_noflash.html"><img
			SRC="/PlasMapper/image/pence.jpeg" border="0"></a> &nbsp;and <a
			href="http://www.genomeprairie.ca"><img
			SRC="/PlasMapper/image/genomePrairie.jpeg" border="0"></a></td>
	</tr>
</table>
<img align=middle
	src="http://www.ualberta.ca/htbin/Geo-counter.gif?ualberta-reg-dong1002-1.gif&font:Hidden">
</body>
</html>


