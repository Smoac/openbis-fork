package ch.ethz.sis.rdf.main.parser;

import ch.ethz.sis.rdf.main.model.rdf.OntClassObject;
import ch.ethz.sis.rdf.main.model.rdf.ResourceRDF;
import ch.ethz.sis.rdf.main.model.xlsx.VocabularyType;
import ch.ethz.sis.rdf.main.mappers.DatatypeMapper;
import ch.ethz.sis.rdf.main.mappers.ObjectPropertyMapper;
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

public class RDFParser {

    private final ParserUtils parserUtils = new ParserUtils();

    public final String ontNamespace;
    public final String ontVersion;
    public final Map<String, String> ontMetadata;
    public final Map<String, String> nsPrefixes;
    public Map<String, OntClassObject> classDetailsMap;
    public Map<String, List<String>> mappedDataTypes;
    public Map<String, List<String>> mappedObjectProperty;
    public final Map<String, List<ResourceRDF>> resourcesGroupedByType;
    public final List<VocabularyType> mappedNamedIndividualList;
    public final Map<String, List<VocabularyType>> mappedNamedIndividual;
    public final Map<String, List<String>> mappedSubClasses;

    private final Map<String, List<String>> chains = new HashMap<>();

    public RDFParser(String inputFileName, String inputFormatValue){
        this(inputFileName, inputFormatValue, false);
    }

    public RDFParser(String inputFileName, String inputFormatValue, boolean verbose) {

        Model model = loadRDFModel(inputFileName, inputFormatValue);

        //ExtOntologyParser extOntologyParser = new ExtOntologyParser(model);
        //System.out.println("External links to import: " + extOntologyParser.getImportLinks());
        //System.out.println("Invalid external links: " + extOntologyParser.getInvalidLinks());

        this.ontNamespace = model.getNsPrefixURI("");
        this.ontVersion = parserUtils.extractVersionIRI(model);
        this.ontMetadata = parserUtils.extractOntologyMetadata(model);
        this.nsPrefixes = model.getNsPrefixMap();

        //this.ontMetadata.forEach((key, value) -> System.out.println("\t" + key + ": " + value));

        if(canCreateOntModel(model)){
            OntModel ontModel = loadRDFOntModel(inputFileName, inputFormatValue);

            this.mappedDataTypes = new DatatypeMapper(ontModel).getMappedDataTypes();
            this.classDetailsMap = collectClassDetails(ontModel, this.mappedDataTypes);
            //classDetailsMap.forEach((cls, map)->System.out.println(cls.getURI() + " -> " + map));
            this.mappedObjectProperty = new ObjectPropertyMapper(ontModel).getMappedObjectProperty();
        }

        boolean modelContainsResources = parserUtils.containsResources(model);
        System.out.println("Model contains Resources: " + (modelContainsResources ? "YES" : "NO"));
        this.resourcesGroupedByType = modelContainsResources ? parserUtils.extractResource(model) : new HashMap<>();
        this.resourcesGroupedByType.keySet().forEach(key ->{
            System.out.println(key + " -> " + this.resourcesGroupedByType.get(key));
        });

        NamedIndividualParser namedIndividualParser = new NamedIndividualParser(model);
        this.mappedNamedIndividualList = namedIndividualParser.getVocabularyTypeList();
        this.mappedNamedIndividual = namedIndividualParser.getVocabularyTypeListGroupedByType();

        getSubclassChainsEndingWithClass(model, model.listStatements(null, RDFS.subClassOf, (RDFNode) null));
        this.mappedSubClasses = chains;

        if (verbose) {
            parserUtils.extractGeneralInfo(model, model.getNsPrefixURI(""));
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

    public Model loadRDFModel(String inputFileName, String inputFormatValue) {
        InputStream in = FileManager.getInternal().open(inputFileName);
        if (in == null) {
            throw new IllegalArgumentException("File: " + inputFileName + " not found");
        }

        // Create a default RDF model and read the Turtle file
        Model model = ModelFactory.createDefaultModel();

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

    public OntModel loadRDFOntModel(String inputFileName, String inputFormatValue) {
        InputStream in = FileManager.getInternal().open(inputFileName);
        if (in == null) {
            throw new IllegalArgumentException("File: " + inputFileName + " not found");
        }

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

    public boolean canCreateOntModel(Model model) {
        // Count RDFS Classes
        int rdfsClassCount = model.listSubjectsWithProperty(RDF.type, RDFS.Class).filterDrop(RDFNode::isAnon).toList().size();
        // Count OWL Classes
        int owlClassCount = model.listSubjectsWithProperty(RDF.type, OWL.Class).filterDrop(RDFNode::isAnon).toList().size();

        System.out.println("Total RDFS Classes: " + rdfsClassCount);
        System.out.println("Total OWL Classes: " + owlClassCount);
        //Create an OntModel only if there are no RDFS classes
        return (rdfsClassCount == 0 && owlClassCount > 0);
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
