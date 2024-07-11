package ch.ethz.sis.rdf.main.parser;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.vocabulary.OWL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//TODO: implement here how to import external ontologies from:
// external links
// additional files (first implement way to handle multiple files) [name should be the same as the prefix ??]
public class ExtOntologyParser {

    private final List<String> importLinks;
    private final List<Model> models;
    private final List<String> invalidLinks;

    public ExtOntologyParser(Model model){
        this.importLinks = extractImportLinks(model);

        List<Model> models = new ArrayList<>();
        List<String> invalidLinks = new ArrayList<>();

        for (String link : importLinks) {
            Model extModel = loadModelFromURL(link, invalidLinks);
            if (extModel != null) {
                models.add(extModel);
            }
        }
        this.models = models;
        this.invalidLinks = invalidLinks;
    }



    private List<String> extractImportLinks(Model model) {
        List<String> importLinks = new ArrayList<>();

        StmtIterator iter = model.listStatements(null, OWL.imports, (RDFNode) null);

        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            RDFNode object = stmt.getObject();
            if (object.isURIResource()) {
                importLinks.add(object.asResource().getURI());
            }
        }

        return importLinks;
    }

    private Model loadModelFromURL(String url, List<String> invalidLinks) {
        Model model = ModelFactory.createDefaultModel();
        try {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestProperty("Accept", "application/xhtml+xml");
            conn.connect();
            try (InputStream in = conn.getInputStream()) {
                // Attempt to read as RDF/XML directly
                RDFDataMgr.read(model, in, null, org.apache.jena.riot.Lang.RDFXML);
                return model;
            } catch (RiotException e) {
                // Handle case where content might be HTML with embedded RDF/XML
                System.err.println("RiotException, trying to parse as HTML: " + e.getMessage());
                try (InputStream htmlIn = conn.getInputStream()) {
                    Document doc = Jsoup.parse(htmlIn, "UTF-8", "");
                    Elements rdfElements = doc.select("rdf\\:RDF");
                    if (!rdfElements.isEmpty()) {
                        Element rdfElement = rdfElements.first();
                        String rdfContent = rdfElement.outerHtml();
                        RDFDataMgr.read(model, InputStream.nullInputStream(), null, org.apache.jena.riot.Lang.RDFXML);
                        return model;
                    } else {
                        if (invalidLinks != null) {
                            invalidLinks.add(url);
                        }
                        System.err.println("No RDF/XML content found in HTML for URL: " + url);
                    }
                }
            }
        } catch (Exception e) {
            if (invalidLinks != null) {
                invalidLinks.add(url);
            }
            System.err.println(e);
        }
        return null;
    }

    public List<String> getImportLinks()
    {
        return importLinks;
    }

    public List<String> getInvalidLinks()
    {
        return invalidLinks;
    }

    public List<Model> getModels()
    {
        return models;
    }
}
