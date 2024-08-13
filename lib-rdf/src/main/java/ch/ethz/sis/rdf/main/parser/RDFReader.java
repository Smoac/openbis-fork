package ch.ethz.sis.rdf.main.parser;

import ch.ethz.sis.rdf.main.ClassCollector;
import ch.ethz.sis.rdf.main.mappers.DatatypeMapper;
import ch.ethz.sis.rdf.main.mappers.NamedIndividualMapper;
import ch.ethz.sis.rdf.main.mappers.ObjectPropertyMapper;
import ch.ethz.sis.rdf.main.model.rdf.ModelRDF;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.InputStream;
import java.util.*;

public class RDFReader
{
    private final ParserUtils parserUtils = new ParserUtils();
    private final Map<String, List<String>> chainsMap = new HashMap<>();
    
    public ModelRDF read(String inputFileName, String inputFormatValue){
        return read(inputFileName, inputFormatValue, false);
    }
     
    public ModelRDF read(String inputFileName, String inputFormatValue, boolean verbose)
    {
        ModelRDF modelRDF = new ModelRDF();

        Model model = loadRDFModel(inputFileName, inputFormatValue);

        modelRDF.ontNamespace = model.getNsPrefixURI("");
        modelRDF.ontVersion = parserUtils.getVersionIRI(model);
        modelRDF.ontMetadata = parserUtils.getOntologyMetadataMap(model);
        modelRDF.nsPrefixes = model.getNsPrefixMap();

        if(canCreateOntModel(model)){
            OntModel ontModel = loadRDFOntModel(inputFileName, inputFormatValue);

            modelRDF.RDFtoOpenBISDataType = DatatypeMapper.getRDFtoOpenBISDataTypeMap(ontModel);
            modelRDF.stringOntClassExtensionMap = ClassCollector.getOntClass2OntClassExtensionMap(ontModel);
            modelRDF.objectPropertyMap = ObjectPropertyMapper.getObjectPropToOntClassMap(ontModel);
        }

        boolean modelContainsResources = parserUtils.containsResources(model);
        //System.out.println("Model contains Resources: " + (modelContainsResources ? "YES" : "NO"));

        modelRDF.resourcesGroupedByType = modelContainsResources ? parserUtils.getResourceMap(model) : new HashMap<>();
        /*modelRDF.resourcesGroupedByType.keySet().forEach(key -> {
            System.out.println(key + " -> " + modelRDF.resourcesGroupedByType.get(key));
        });*/

        modelRDF.vocabularyTypeList = NamedIndividualMapper.getVocabularyTypeList(model);
        modelRDF.vocabularyTypeListGroupedByType = NamedIndividualMapper.getVocabularyTypeListGroupedByType(model);

        getSubclassChainsEndingWithClass(model, model.listStatements(null, RDFS.subClassOf, (RDFNode) null));
        modelRDF.subClassChanisMap = chainsMap;

        if (verbose) {
            parserUtils.extractGeneralInfo(model, model.getNsPrefixURI(""));
        }

        return modelRDF;
    }

    private void checkFileExists(String inputFileName)
    {
        InputStream in = FileManager.getInternal().open(inputFileName);
        if (in == null) {
            throw new IllegalArgumentException("File: " + inputFileName + " not found");
        }
    }

    private void loadRDFData(Model model, String inputFileName, String inputFormatValue)
    {
        switch (inputFormatValue) {
            case "TTL":
                RDFDataMgr.read(model, inputFileName, Lang.TTL);
                break;
            case "JSONLD":
            case "XML":
            default:
                throw new IllegalArgumentException("Input format file: " + inputFormatValue + " not supported");
        }
    }

    private Model loadRDFModel(String inputFileName, String inputFormatValue)
    {
        checkFileExists(inputFileName);
        Model model = ModelFactory.createDefaultModel();
        loadRDFData(model, inputFileName, inputFormatValue);
        return model;
    }

    private OntModel loadRDFOntModel(String inputFileName, String inputFormatValue)
    {
        checkFileExists(inputFileName);
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        loadRDFData(model, inputFileName, inputFormatValue);
        return model;
    }

    private boolean canCreateOntModel(Model model)
    {
        // Count RDFS Classes
        int rdfsClassCount = model.listSubjectsWithProperty(RDF.type, RDFS.Class).filterDrop(RDFNode::isAnon).toList().size();
        // Count OWL Classes
        int owlClassCount = model.listSubjectsWithProperty(RDF.type, OWL.Class).filterDrop(RDFNode::isAnon).toList().size();

        System.out.println("Total RDFS Classes: " + rdfsClassCount);
        System.out.println("Total OWL Classes: " + owlClassCount);
        //Create an OntModel only if there are no RDFS classes
        return (rdfsClassCount == 0 && owlClassCount > 0);
    }

    private void getSubclassChainsEndingWithClass(Model model, StmtIterator iter)
    {
        // Clear previous chains
        chainsMap.clear();

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
                    chainsMap.put(subclass, new ArrayList<>(chain));
                }
            } else {
                System.out.println("Skipping anonymous subclass: " + subclass);
            }
        }
    }

    private boolean findSubclassChain(Model model, Resource superclass, Set<String> visited, List<String> chain)
    {
        if (superclass == null || !superclass.isURIResource()) {
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
