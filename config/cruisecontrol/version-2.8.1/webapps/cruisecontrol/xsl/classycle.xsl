<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>

  <xsl:template match="/" mode="classycle">
    <xsl:apply-templates select="cruisecontrol/build/target[@name='check-dependencies']" mode="classycle"/>
  </xsl:template>

  <xsl:template match="build/target" mode="classycle">
    <table align="center" cellpadding="2" cellspacing="0" border="0" width="98%">
      <tr><td class="compile-sectionheader">Classycle</td></tr>
      <tr><td align="left"><pre class="compile-error-data"><xsl:apply-templates select="task"/></pre></td></tr>
    </table>
  </xsl:template>

  <xsl:template match="message" mode="classycle">
    <xsl:value-of select="text()"/>
  </xsl:template>

</xsl:stylesheet>
