package ch.ethz.sis.rdf.main.parser;

import ch.ethz.sis.rdf.main.model.rdf.PropertyTupleRDF;
import ch.ethz.sis.rdf.main.model.rdf.ResourceRDF;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParserUtils {

    public boolean isNamedIndividual(Model model, RDFNode cls) {
        return model.listSubjectsWithProperty(RDF.type, cls)
                .filterKeep(subject -> model.contains(subject, RDF.type, OWL2.NamedIndividual))
                .hasNext();
    }

    //---------- RESOURCE PREFIX EXTRACTION ------------------

    public Map<String, List<ResourceRDF>> extractResource(Model model){
        // Namespace prefix
        //String prefix = model.getNsPrefixURI(RESOURCE_URI_NS);
        List<String> resourcePrefixes = extractResourcesPrefixList(model);

        // Map to store the resources grouped by type
        Map<String, List<ResourceRDF>> groupedResources = new HashMap<>();

        if(!resourcePrefixes.isEmpty()){
            for (String prefix: resourcePrefixes){
                // Iterate through all statements with rdf:type predicate
                model.listStatements(null, RDF.type, (Resource) null).forEachRemaining(statement -> {
                    Resource subject = statement.getSubject();
                    if (subject.isURIResource() && subject.getURI().startsWith(prefix)) {

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
        }
        return groupedResources;
    }

    public List<String> extractResourcesPrefixList(Model model){
        Set<String> resourcePossibleNSs = new HashSet<>();
        resourcePossibleNSs.addAll(extractClassResources(model, RDFS.Class));
        resourcePossibleNSs.addAll(extractClassResources(model, OWL.Class));
        resourcePossibleNSs.addAll(extractSubClassOfResources(model));
        return resourcePossibleNSs.stream().toList();
    }

    public boolean containsResources(Model model) {
        // need to check for RDFS and OWL classes
        return containsResources(model, RDFS.Class) || containsResources(model, OWL.Class);
    }

    private boolean containsResources(Model model, RDFNode rdfNode) {
        AtomicBoolean containsResources = new AtomicBoolean(false);
        // usually data are noted as triplet [resource, type, some class]
        // iterate over all Classes
        model.listSubjectsWithProperty(RDF.type, rdfNode).forEachRemaining(cls -> {
            // exclude anonymous classes
            if(!cls.isAnon()) {
                //check if the model contains resources that are usually noted as triplet [resource, type, some class]
                if(model.contains(null, RDF.type, cls)){
                    // exclude NamedIndividual cases that are noted like resources
                    if(isNamedIndividual(model, cls)) {
                        System.out.println(cls + " -> is a OWL NamedIndividual");
                    } else {
                        containsResources.set(true);
                    }
                }
            }
        });
        return containsResources.get();
    }

    //Can't avoid overlapping with the extractClassResources because all classes are subClassOf SPHNConcept
    private Set<String> extractSubClassOfResources(Model model) {
        Set<String> resourcePossibleNSs = new HashSet<>();
        model.listSubjectsWithProperty(RDF.type, OWL.Class).forEachRemaining(cls -> {
            if (!cls.isAnon()) {
                model.listSubjectsWithProperty(RDFS.subClassOf, cls).forEachRemaining(subClass -> {
                    //System.out.println(subClass + " -> subClassOf -> " + cls);
                    if (model.contains(null, RDF.type, subClass) && !isNamedIndividual(model, subClass)) {
                        //System.out.println(model.listSubjectsWithProperty(RDF.type, subClass).toList());
                        model.listSubjectsWithProperty(RDF.type, subClass).forEachRemaining(subject -> {
                            //System.out.println(subject2.getNameSpace());
                            resourcePossibleNSs.add(subject.getNameSpace());
                        });
                    }
                });
            }
        });
        return resourcePossibleNSs;
    }

    private Set<String> extractClassResources(Model model, RDFNode rdfNode) {
        Set<String> resourcePossibleNSs = new HashSet<>();
        model.listSubjectsWithProperty(RDF.type, rdfNode).forEachRemaining(cls -> {
            if(!cls.isAnon()) {
                if(!isNamedIndividual(model, cls)){
                    //System.out.println(model.listSubjectsWithProperty(RDF.type, cls).toList());
                    model.listSubjectsWithProperty(RDF.type, cls).forEachRemaining(subject -> {
                        //System.out.println(subject.getNameSpace());
                        resourcePossibleNSs.add(subject.getNameSpace());
                    });
                }
            }
        });
        return resourcePossibleNSs;
    }

    //---------- GENERAL INFO ------------------

    public Map<String, String> extractOntologyMetadata(Model model) {
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
                ontMetadata.put(DCTerms.bibliographicCitation.getLocalName(), ontology.getProperty(DCTerms.bibliographicCitation).getObject().toString());
                ontMetadata.put(DCTerms.license.getLocalName(), ontology.getProperty(DCTerms.license).getObject().toString());
                ontMetadata.put(OWL.priorVersion.getLocalName(), ontology.getProperty(OWL.priorVersion).getObject().toString());
                ontMetadata.put(OWL2.versionIRI.getLocalName(), ontology.getProperty(OWL2.versionIRI).getObject().toString());
            }
        } else {
            System.out.println("No specific ontology metadata found.");
        }
        return ontMetadata;
    }

    public String extractVersionIRI(Model model){
        StmtIterator iter = model.listStatements(null, OWL2.versionIRI, (RDFNode) null);
        if (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            return stmt.getObject().toString();
        }
        return null;
    }

    public void extractGeneralInfo(Model model, String ontNamespace) {
        System.out.println("General Information: ");
        // Count schema resources
        countSchemaResources(model);

        // Print all RDF types and their counts
        Map<Resource, Integer> rdfTypeCounts = getAllRdfTypeCounts(model);

        // Count resources with types in the specific namespace
        int countNamespaceTypes = countResourcesWithNamespaceType(model, ontNamespace);
        System.out.println("\tTotal Objects with types starting with default namespace <" + ontNamespace + ">: " + countNamespaceTypes);

        // Count subjects with a specific prefix
        List<String> resourcePrefixes = extractResourcesPrefixList(model);
        for (String prefix: resourcePrefixes){
            int countSubjectsWithPrefix = 0;
            if (prefix != null) {
                countSubjectsWithPrefix = countSubjectsWithPrefix(model, prefix);
            }
            System.out.println("\tTotal resource Subjects with prefix <" + prefix + ">: " + countSubjectsWithPrefix);
        }

        rdfTypeCounts.forEach((type, count) -> System.out.println("\t"+type + ": " + count));
    }

    private int countResourcesWithNamespaceType(Model model, String namespace) {
        return (int) model.listStatements(null, RDF.type, (Resource) null).filterKeep(statement -> {
            Resource type = statement.getObject().asResource();
            return type.getURI().startsWith(namespace);
        }).toList().size();
    }

    private int countSubjectsWithPrefix(Model model, String prefix) {
        List<Statement> statements = model.listStatements(null, RDF.type, (Resource) null).filterKeep(statement -> {
            Resource subject = statement.getSubject();
            return subject.isURIResource() && subject.getURI().startsWith(prefix);
        }).toList();
        //statements.forEach(System.out::println);
        return statements.size();
    }

    private void countSchemaResources(Model model) {
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

    private Map<Resource, Integer> getAllRdfTypeCounts(Model model) {
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

    private boolean isValueSetOrSubset(Resource resource) {
        // Logic to determine if a resource is a value set or subset based on specific properties or classes
        return resource.hasProperty(RDF.type, OWL.Restriction) || resource.hasProperty(RDF.type, OWL.Class)
                && (resource.hasProperty(OWL.unionOf) || resource.hasProperty(OWL.intersectionOf) || resource.hasProperty(OWL.allValuesFrom));
    }
}
