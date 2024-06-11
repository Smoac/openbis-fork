package ch.ethz.sis.rdf.main;

import ch.ethz.sis.rdf.main.entity.OntClassObject;
import ch.ethz.sis.rdf.main.entity.PropertyTupleRDF;
import ch.ethz.sis.rdf.main.entity.ResourceRDF;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.*;

import java.io.InputStream;
import java.util.*;

import static ch.ethz.sis.rdf.main.ClassCollector.collectClassDetails;
import static ch.ethz.sis.rdf.main.mappers.DatatypeMapper.toOpenBISDataTypes;
import static ch.ethz.sis.rdf.main.mappers.ObjectPropertyMapper.toObjects;

public class RDFParser {

    public String ontNamespace;
    public String ontVersion;
    public Map<String, String> ontMetadata;
    public Map<String, String> nsPrefixes;
    public Map<String, OntClassObject> classDetailsMap;
    public Map<String, List<String>> mappedDataTypes;
    public Map<String, List<String>> mappedObjectProperty;
    public Map<String, List<ResourceRDF>> resourcesGroupedByType;

    private final String RESOURCE_URI_NS = "resource";

    /*public static void main(String[] args) {
        //String filePath = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/sphn_rdf_schema_with_data.ttl";
        String filePath = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/sphn_rdf_schema.ttl";
        //String filePath = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/new_binder_comp_1.0.0.ttl";
        //String filePath = "/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-data-small/mockdata_allergy.ttl";

        RDFParser parser = new RDFParser(filePath, "TTL", true);
        //RDFParser parser = new RDFParser("/home/mdanaila/Projects/master/openbis/lib-rdf/test-data/sphn-model/rdf_schema_sphn_dataset_release_2024_2.ttl", "TTL");
    }*/

    public RDFParser(String inputFileName, String inputFormatValue){
        this(inputFileName, inputFormatValue, false);
    }

    public RDFParser(String inputFileName, String inputFormatValue, boolean verbose) {

        OntModel model = loadRDFModel(inputFileName, inputFormatValue);

        this.ontNamespace = model.getNsPrefixURI("");
        this.ontVersion = extractVersionIRI(model);
        this.ontMetadata = extractOntologyMetadata(model);
        this.nsPrefixes = model.getNsPrefixMap();
        this.ontMetadata.forEach((key, value) -> System.out.println("\t" + key + ": " + value));
        this.classDetailsMap = collectClassDetails(model);
        //classDetailsMap.forEach((cls, map)->System.out.println(cls.getURI() + " -> " + map));
        this.mappedObjectProperty = toObjects(model);
        this.mappedDataTypes = toOpenBISDataTypes(model);
        this.resourcesGroupedByType = extractResource(model);

        if (verbose) {
            extractGeneralInfo(model);
        }
    }

    public Map<String, List<ResourceRDF>> extractResource(Model model){
        // Namespace prefix
        String prefix = model.getNsPrefixURI(RESOURCE_URI_NS);

        // Map to store the resources grouped by type
        Map<String, List<ResourceRDF>> groupedResources = new HashMap<>();

        // Iterate through all statements with rdf:type predicate
        model.listStatements(null, RDF.type, (Resource) null).forEachRemaining(statement -> {
            Resource subject = statement.getSubject();
            if (subject.isURIResource() && subject.getURI().startsWith(prefix)) {
                String subjectURI = subject.getURI();

                // Create a new ResourceRDF object
                ResourceRDF resource = new ResourceRDF(subjectURI);

                // Set the type of the resource
                Resource type = statement.getObject().asResource();
                resource.setType(type.getURI());

                // Add the resource to the appropriate group based on its type
                groupedResources.putIfAbsent(type.getURI(), new ArrayList<>());
                groupedResources.get(type.getURI()).add(resource);

                // Iterate over all properties of the subject
                model.listStatements(subject, null, (Resource) null)
                        .filterDrop(statement1 -> statement1.getPredicate().equals(RDF.type))
                        .forEachRemaining(propStatement -> {
                            Property predicate = propStatement.getPredicate();
                            String predicateURI = predicate.getURI();
                            String objectValue;

                            // Get the object value
                            if (propStatement.getObject().isResource()) {
                                Resource object = propStatement.getObject().asResource();
                                objectValue = object.getURI();
                            } else {
                                objectValue = propStatement.getObject().toString();
                            }

                            // Add the predicate and object as a PropertyTuple to the ResourceRDF
                            resource.properties.add(new PropertyTupleRDF(predicateURI, objectValue));
                        });
            }
        });
        return groupedResources;
    }

    public OntModel loadRDFModel(String inputFileName, String inputFormatValue) {
        InputStream in = FileManager.getInternal().open(inputFileName);
        if (in == null) {
            throw new IllegalArgumentException("File: " + inputFileName + " not found");
        }

        // Create a default RDF model and read the Turtle file
        //Model model = ModelFactory.createDefaultModel();
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

        switch(inputFormatValue){
            case "TTL":
                RDFDataMgr.read(model, inputFileName, Lang.TTL);
                break;
            case "JSONLD":
            default:
                throw new IllegalArgumentException("Input format file: " + inputFormatValue + " not supported");
        }
        return model;
    }

    private void extractGeneralInfo(Model model) {
        System.out.println("General Information: ");
        // Count schema resources
        countSchemaResources(model);

        // Print all RDF types and their counts
        Map<Resource, Integer> rdfTypeCounts = getAllRdfTypeCounts(model);

        // Count resources with types in the specific namespace
        int countNamespaceTypes = countResourcesWithNamespaceType(model, ontNamespace);
        System.out.println("\tTotal Objects with types starting with default namespace <" + ontNamespace + ">: " + countNamespaceTypes);

        // Count subjects with a specific prefix
        String prefix = model.getNsPrefixURI(RESOURCE_URI_NS);
        int countSubjectsWithPrefix = 0;
        if (prefix != null) {
            countSubjectsWithPrefix = countSubjectsWithPrefix(model, prefix);
        }
        System.out.println("\tTotal resource Subjects with prefix <" + prefix + ">: " + countSubjectsWithPrefix);

        rdfTypeCounts.forEach((type, count) -> System.out.println("\t"+type + ": " + count));
    }

    private String extractVersionIRI(Model model){
        StmtIterator iter = model.listStatements(null, OWL2.versionIRI, (RDFNode) null);
        if (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            return stmt.getObject().toString();
        }
        return null;
    }

    private Map<String, String> extractOntologyMetadata(Model model) {
        Map<String, String> ontMetadata = new HashMap<>();
        // Extract ontology metadata by checking for owl:Ontology
        ResIterator ontologies = model.listResourcesWithProperty(RDF.type, OWL.Ontology);
        if (ontologies.hasNext()) {
            System.out.println("Ontology metadata found:");
            while (ontologies.hasNext()) {
                Resource ontology = ontologies.nextResource();
                ontMetadata.put(DC.description.getLocalName(), ontology.getProperty(DC.description).getObject().toString());
                ontMetadata.put(DC.rights.getLocalName(), ontology.getProperty(DC.rights).getObject().toString());
                ontMetadata.put(DC.title.getLocalName(), ontology.getProperty(DC.title).getObject().toString());
                ontMetadata.put(DCTerms.license.getLocalName(), ontology.getProperty(DCTerms.license).getObject().toString());
                ontMetadata.put(OWL.priorVersion.getLocalName(), ontology.getProperty(OWL.priorVersion).getObject().toString());
                ontMetadata.put(OWL2.versionIRI.getLocalName(), ontology.getProperty(OWL2.versionIRI).getObject().toString());
            }
        } else {
            System.out.println("No specific ontology metadata found.");
        }
        return ontMetadata;
    }

    private static int countResourcesWithNamespaceType(Model model, String namespace) {
        return (int) model.listStatements(null, RDF.type, (Resource) null).filterKeep(statement -> {
            Resource type = statement.getObject().asResource();
            return type.getURI().startsWith(namespace);
        }).toList().size();
    }

    private static int countSubjectsWithPrefix(Model model, String prefix) {
        List<Statement> statements = model.listStatements(null, RDF.type, (Resource) null).filterKeep(statement -> {
            Resource subject = statement.getSubject();
            return subject.isURIResource() && subject.getURI().startsWith(prefix);
        }).toList();
        //statements.forEach(System.out::println);
        return statements.size();
    }

    private static void countSchemaResources(Model model) {
        int rdfsClassCount = model.listResourcesWithProperty(RDF.type, RDFS.Class).filterDrop(RDFNode::isAnon).toList().size();
        int rdfsDatatypeCount = model.listResourcesWithProperty(RDF.type, RDFS.Datatype).toList().size();
        int propertyCount = model.listResourcesWithProperty(RDF.type, RDF.Property).toList().size();
        int owlClassCount = model.listResourcesWithProperty(RDF.type, OWL.Class).filterDrop(RDFNode::isAnon).toList().size();
        int objectPropertyCount = model.listResourcesWithProperty(RDF.type, OWL.ObjectProperty).toList().size();
        int restrictionCount = model.listResourcesWithProperty(RDF.type, OWL.Restriction).toList().size();
        int datatypePropertyCount = model.listResourcesWithProperty(RDF.type, OWL.DatatypeProperty).toList().size();
        int annotationPropertyCount = model.listResourcesWithProperty(RDF.type, OWL.AnnotationProperty).toList().size();
        int namedIndividualCount = model.listResourcesWithProperty(RDF.type, OWL2.NamedIndividual).toList().size();
        int ontologyCount = model.listResourcesWithProperty(RDF.type, OWL.Ontology).toList().size();

        System.out.println("\tTotal RDFS Classes (no anon): " + rdfsClassCount);
        System.out.println("\tTotal RDFS Datatype: " + rdfsDatatypeCount);
        System.out.println("\tTotal RDF Properties: " + propertyCount);
        System.out.println("\tTotal OWL Ontology Metadata: " + ontologyCount);
        System.out.println("\tTotal OWL Classes (no anon): " + owlClassCount);
        System.out.println("\tTotal OWL Object Properties: " + objectPropertyCount);
        System.out.println("\tTotal OWL Datatype Properties: " + datatypePropertyCount);
        System.out.println("\tTotal OWL Annotation Properties: " + annotationPropertyCount);
        System.out.println("\tTotal OWL Named IndividualCount: " + namedIndividualCount);
        System.out.println("\tTotal OWL Restriction: " + restrictionCount);
    }

    private static Map<Resource, Integer> getAllRdfTypeCounts(Model model) {
        // Use a TreeMap with a custom comparator to sort based on resource.toString()
        Map<Resource, Integer> rdfTypeCounts = new TreeMap<>(Comparator.comparing(Resource::getURI));

        // List all rdf:type statements
        model.listStatements(null, RDF.type, (Resource) null).forEachRemaining(statement -> {
            Resource type = statement.getObject().asResource();
            // Exclude anonymous classes and value sets or subsets
            if (!type.isAnon() && !isValueSetOrSubset(type)) {
                rdfTypeCounts.put(type, rdfTypeCounts.getOrDefault(type, 0) + 1);
            }
        });

        return rdfTypeCounts;
    }

    private static boolean isValueSetOrSubset(Resource resource) {
        // Logic to determine if a resource is a value set or subset based on specific properties or classes
        return resource.hasProperty(RDF.type, OWL.Restriction) || resource.hasProperty(RDF.type, OWL.Class)
                && (resource.hasProperty(OWL.unionOf) || resource.hasProperty(OWL.intersectionOf) || resource.hasProperty(OWL.allValuesFrom));
    }
}
