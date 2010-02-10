package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleImageHtmlRenderer;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class GeneratedImageTableCellTest extends AssertJUnit
{

    @Test
    public void testAgainstSimpleImageHTMLRenderer()
    {
        GeneratedImageTableCell cell = new GeneratedImageTableCell("servletName", 600, 300, 60, 60);
        cell.addParameter("code", "CODE_8472");
        String cellHTML = cell.getHTMLString("http://my.server.ch", "sessionToken");
        String rendererHTML =
                SimpleImageHtmlRenderer
                        .createEmbededImageHtml(
                                "http://my.server.ch/servletName?sessionID=sessionToken&code=CODE_8472&w=60&h=60",
                                "http://my.server.ch/servletName?sessionID=sessionToken&code=CODE_8472&w=600&h=300");
        assertEquals(cellHTML, rendererHTML);
    }
}
