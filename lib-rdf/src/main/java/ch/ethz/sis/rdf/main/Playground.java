package ch.ethz.sis.rdf.main;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Playground {

    private static final ConcurrentHashMap<String, Model> schemaCache = new ConcurrentHashMap<>();

    public static Model fetchSchema(String uri) {
        return schemaCache.computeIfAbsent(uri, Playground::retrieveSchemaFromUri);
    }

    private static Model retrieveSchemaFromUri(String uri) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(uri);
        try {
            var response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                String rdfContent = EntityUtils.toString(response.getEntity());
                Model model = ModelFactory.createDefaultModel();
                RDFDataMgr.read(model, new StringReader(rdfContent), null, RDFDataMgr.determineLang(uri, null, Lang.RDFXML));
                return model;
            } else {
                throw new RuntimeException("Failed to fetch RDF schema from URI: " + uri);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching RDF schema from URI: " + uri, e);
        }
    }

    public static Model loadRDFFromURL(String url) {
        Model model = ModelFactory.createDefaultModel();
        try {
            // RDFDataMgr automatically handles content negotiation and parser selection based on the content type of the response.
            RDFDataMgr.read(model, url, null, null);
        } catch (Exception e) {
            System.err.println("Error loading RDF data: " + e.getMessage());
            e.printStackTrace();
        }
        return model;
    }

    public static Map<String, String> mapSchemaFields(Model model) {
        Map<String, String> fieldMap = new HashMap<>();

        ResIterator subjects = model.listSubjects();
        while (subjects.hasNext()) {
            Resource subject = subjects.nextResource();
            StmtIterator properties = subject.listProperties();
            while (properties.hasNext()) {
                Statement stmt = properties.nextStatement();
                Property predicate = stmt.getPredicate();
                RDFNode object = stmt.getObject();

                String field = predicate.getLocalName();
                String value = object.isResource() ? object.asResource().getURI() : object.asLiteral().getString();

                fieldMap.put(field, value);
            }
        }
        return fieldMap;
    }

    public static void main(String[] args) throws IOException
    {
        //String filePath = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/schema.json";
        String filePath = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/json_ld_using_schema_org.json";
        //Model model = ModelFactory.createDefaultModel();
        //model.read(filePath, "JSON-LD");

        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, filePath, Lang.JSONLD);

        Map<String, String> schemaFields = mapSchemaFields(model);
        schemaFields.forEach((key, value) -> System.out.println(key + ": " + value));

        /*model.listStatements().forEachRemaining(cls -> {
            System.out.println(cls);
        });

        model.listNameSpaces().forEachRemaining(cls -> {
            System.out.println(cls);
        });*/

        /*InputStream inputStream = new FileInputStream(filePath);
        Object jsonObject = JsonUtils.fromInputStream(inputStream);
        // Create a context JSON map containing prefixes and definitions
        Map context = new HashMap();
        JsonLdOptions options = new JsonLdOptions();
        Object compact = JsonLdProcessor.compact(jsonObject, context, options);
        System.out.println(JsonUtils.toPrettyString(compact));*/


        // String rdfUrl = "http://purl.obolibrary.org/obo/NCIT_C42781"; // Author
        // String rdfUrl = "http://schema.org/comment";
        // String rdfUrl = "http://purl.obolibrary.org/obo/NCIT_C41206"; // Institution
        String rdfUrl = "https://schema.org/Person"; //Person  Error loading RDF data: Failed to determine the content type: (URI=https://schema.org/Person : stream=text/html)
        //Model modelEnt = loadRDFFromURL(rdfUrl);

        Model schemaModel = fetchSchema(rdfUrl);
        System.out.println("--------------------------------------------------------");

        System.out.println(schemaModel.listStatements());

        System.out.println("--------------------------------------------------------");

        schemaModel.write(System.out, "TURTLE");

        // Print out the model in Turtle format for verification
        //modelEnt.write(System.out, "JSON-LD");

        /*String schemaUri = "http://purl.obolibrary.org/obo/NCIT_C98193";
        // Fetch and possibly cache the schema
        Model schemaModel = fetchSchema(schemaUri);
        System.out.println("--------------------------------------------------------");

        System.out.println(schemaModel.listStatements());

        System.out.println("--------------------------------------------------------");

        schemaModel.write(System.out, "TURTLE");*/
        // Example processing: Print out data model
        //model.write(System.out, "TURTLE");

    }
}
