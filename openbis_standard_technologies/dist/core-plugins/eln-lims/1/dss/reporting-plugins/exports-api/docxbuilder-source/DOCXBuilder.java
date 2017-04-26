package ch.ethz.sis;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTAltChunk;

public class DOCXBuilder
{
    public static void main(String[] args) throws Exception
    {
        DOCXBuilder docx = new DOCXBuilder();
        docx.addTitle("TitleA");
        docx.addHeader("MetaA");
        docx.addProperty("PropertyA", "ValueA");
        docx.addProperty("PropertyB", "ValueB");
        docx.addProperty("PropertyC",
                "<p>I am normal</p><p style=\"color:red;\">I am red</p><p style=\"color:blue;\">I am blue</p><p style=\"font-size:36px;\">I am big</p>");

        FileOutputStream out = new FileOutputStream(new File("wordFromHTML.docx"));
        out.write(docx.getDocBytes());
        out.close();
    }

    private StringBuffer doc;

    public DOCXBuilder()
    {
        doc = new StringBuffer();
        startDoc();
    }

    private void startDoc()
    {
        doc.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        doc.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        doc.append("<head></head>");
        doc.append("<body>");
    }

    private void endDoc()
    {
        doc.append("</body>");
        doc.append("</html>");
    }

    public void addProperty(String key, String value)
    {
        doc.append("<p>").append("<b>").append(key).append(": ").append("</b>").append(value).append("</p>");
    }

    public void addParagraph(String value)
    {
        doc.append("<p>").append(value).append("</p>");
    }

    public void addTitle(String title)
    {
        doc.append("<h1>").append(title).append("</h1>");
    }

    public void addHeader(String header)
    {
        doc.append("<h2>").append(header).append("</h2>");
    }

    public byte[] getHTMLBytes() throws Exception
    {
        endDoc();
        return doc.toString().getBytes();
    }

    public byte[] getDocBytes() throws Exception
    {
        // .. Finish Document
        endDoc();
        // .. HTML Code
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(new PartName("/hw.html"));
        afiPart.setBinaryData(doc.toString().getBytes());
        afiPart.setContentType(new ContentType("text/html"));
        Relationship altChunkRel = wordMLPackage.getMainDocumentPart().addTargetPart(afiPart);

        // .. the bit in document body
        CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk();
        ac.setId(altChunkRel.getId());
        wordMLPackage.getMainDocumentPart().addObject(ac);

        // .. content type
        wordMLPackage.getContentTypeManager().addDefaultContentType("html", "text/html");
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        wordMLPackage.save(outStream);

        return outStream.toByteArray();
    }
}
