package ch.ethz.sis.rdf.main;

import ch.ethz.sis.rdf.main.entity.OntClassObject;
import ch.ethz.sis.rdf.main.entity.PropertyTupleRDF;
import ch.ethz.sis.rdf.main.entity.ResourceRDF;
import ch.ethz.sis.rdf.main.entity.VocabularyType;
import ch.ethz.sis.rdf.main.parser.NamedIndividualParser;
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

    public final String ontNamespace;
    public final String ontVersion;
    public final Map<String, String> ontMetadata;
    public final Map<String, String> nsPrefixes;
    public final Map<String, OntClassObject> classDetailsMap;
    public final Map<String, List<String>> mappedDataTypes;
    public final Map<String, List<String>> mappedObjectProperty;
    public final Map<String, List<ResourceRDF>> resourcesGroupedByType;
    public final List<VocabularyType> mappedNamedIndividualList;
    public final Map<String, List<VocabularyType>> mappedNamedIndividual;
    public final Map<String, List<String>> mappedSubClasses;

    private final Map<String, List<String>> chains = new HashMap<>();
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
        this.mappedDataTypes = toOpenBISDataTypes(model);
        this.classDetailsMap = collectClassDetails(model, this.mappedDataTypes);
        //classDetailsMap.forEach((cls, map)->System.out.println(cls.getURI() + " -> " + map));
        this.mappedObjectProperty = toObjects(model);
        this.resourcesGroupedByType = extractResource(model);
        NamedIndividualParser namedIndividualParser = new NamedIndividualParser(model);
        this.mappedNamedIndividualList = namedIndividualParser.processGroupedNamedIndividuals();
        this.mappedNamedIndividual = namedIndividualParser.processNamedIndividuals();

        getSubclassChainsEndingWithClass(model, model.listStatements(null, RDFS.subClassOf, (RDFNode) null));
        this.mappedSubClasses = chains;

        if (verbose) {
            extractGeneralInfo(model);
        }
    }

    public boolean isSubClass(String uri){
        return mappedSubClasses.containsKey(uri);
    }

    //TODO implement a non hardcoded way to check this: check if its type in the model is one of the Classes
    public boolean isResource(String uri){
        return resourcesGroupedByType.containsKey(uri);
    }

    //TODO implement a non hardcoded way to check this: check if its type in the model is one of the Classes
    public boolean isAlias(String uri){
        return mappedNamedIndividual.containsKey(uri);
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
            case "XML":
            default:
                throw new IllegalArgumentException("Input format file: " + inputFormatValue + " not supported");
        }
        return model;
    }

    private Map<String, String> extractLabelNamedIndividual(Model model) {
        Map<String, String> mappedNamedIndividual = new HashMap<>();
        // List properties for individuals
        model.listSubjectsWithProperty(RDF.type, OWL2.NamedIndividual).forEachRemaining(individual -> {
            individual.listProperties(RDFS.label).forEachRemaining(statement -> {
                mappedNamedIndividual.put(individual.getURI(), statement.getObject().toString());
            });
        });

        model.listStatements(null, RDF.type, OWL2.NamedIndividual).forEachRemaining(triplet -> {
            Resource subject = triplet.getSubject();
            System.out.println("Sub: " + subject + " -> from triplet: " + triplet);

            String label = model.getProperty(subject, RDFS.label).getString();

            System.out.println("label: " + label );

            model.listObjectsOfProperty(subject, RDF.type)
                    .filterDrop(resource -> resource.asResource().equals(OWL2.NamedIndividual.asResource()))
                    .forEach(resource ->{
                        System.out.println("    res: " + resource);
                    });
        });

        return mappedNamedIndividual;
    }

    private Map<String, List<ResourceRDF>> extractResource(Model model){
        // Namespace prefix
        String prefix = model.getNsPrefixURI(RESOURCE_URI_NS);

        // Map to store the resources grouped by type
        Map<String, List<ResourceRDF>> groupedResources = new HashMap<>();
        if(prefix != null) {
            // Iterate through all statements with rdf:type predicate
            model.listStatements(null, RDF.type, (Resource) null).forEachRemaining(statement -> {
                Resource subject = statement.getSubject();
                if (subject.isURIResource() && subject.getURI().startsWith(prefix)) {
                    //String subjectURI = subject.getURI();

                    // Create a new ResourceRDF object
                    ResourceRDF resource = new ResourceRDF(subject.getLocalName());

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
                                resource.properties.add(new PropertyTupleRDF(predicate.getLocalName(), objectValue));
                            });
                }
            });
        }
        return groupedResources;
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

    public void getSubclassChainsEndingWithClass(Model model, StmtIterator iter) {
        // Clear previous chains
        chains.clear();

        // Process each statement
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            Resource subclassRes = stmt.getSubject();
            String subclass = subclassRes.isURIResource() ? subclassRes.getURI() : "Anonymous Node";

            if (subclassRes.isURIResource()) {
                Resource superclass = stmt.getObject().asResource();

                Set<String> visited = new HashSet<>();
                List<String> chain = new ArrayList<>();
                chain.add(subclass);

                if (findSubclassChain(model, superclass, visited, chain)) {
                    chains.put(subclass, new ArrayList<>(chain));
                }
            } else {
                System.out.println("Skipping anonymous subclass: " + subclass);
            }
        }
    }

    private boolean findSubclassChain(Model model, Resource superclass, Set<String> visited, List<String> chain) {
        if (superclass == null || !superclass.isURIResource()) {
            //System.out.println("Skipping anonymous superclass.");
            return false;
        }

        String superclassURI = superclass.getURI();
        if (!visited.add(superclassURI)) {
            // Already visited this class, no valid chain here
            return false;
        }

        // Add superclass to chain
        chain.add(superclass.getURI());

        // Check if the superclass is of type owl:Class or rdfs:Class or owl:DatatypeProperty or owl:DatatypeProperty
        if (model.contains(superclass, RDF.type, OWL.Class) || model.contains(superclass, RDF.type, RDFS.Class) || model.contains(superclass, RDF.type, OWL2.Class)
                || model.contains(superclass, RDF.type, OWL.DatatypeProperty)
                || model.contains(superclass, RDF.type, OWL.ObjectProperty)) {
            return true;
        }

        // Recur for each superclass
        StmtIterator superIter = model.listStatements(superclass, RDFS.subClassOf, (RDFNode) null);
        while (superIter.hasNext()) {
            Statement stmt = superIter.nextStatement();
            Resource nextSuper = stmt.getObject().asResource();
            if (findSubclassChain(model, nextSuper, visited, chain)) {
                return true;
            }
        }

        // Remove the last element added if no valid chain is found
        chain.remove(chain.size() - 1);
        return false;
    }
}
