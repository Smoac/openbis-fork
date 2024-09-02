package ch.ethz.sis.rdf.main.parser;

import ch.ethz.sis.rdf.main.ClassCollector;
import ch.ethz.sis.rdf.main.mappers.DatatypeMapper;
import ch.ethz.sis.rdf.main.mappers.NamedIndividualMapper;
import ch.ethz.sis.rdf.main.mappers.ObjectPropertyMapper;
import ch.ethz.sis.rdf.main.model.rdf.ModelRDF;
import ch.ethz.sis.rdf.main.model.rdf.OntClassExtension;
import ch.ethz.sis.rdf.main.model.xlsx.SamplePropertyType;
import ch.ethz.sis.rdf.main.model.xlsx.SampleType;
import org.apache.jena.ontology.*;
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

        modelRDF.vocabularyTypeList = NamedIndividualMapper.getVocabularyTypeList(model);
        modelRDF.vocabularyTypeListGroupedByType = NamedIndividualMapper.getVocabularyTypeListGroupedByType(model);

        if(canCreateOntModel(model))
        {
            OntModel ontModel = loadRDFOntModel(inputFileName, inputFormatValue);

            Map<String, List<String>> RDFtoOpenBISDataTypeMap = DatatypeMapper.getRDFtoOpenBISDataTypeMap(ontModel);
            //modelRDF.RDFtoOpenBISDataType = RDFtoOpenBISDataTypeMap;
            Map<String, List<String>> objectPropToOntClassMap = ObjectPropertyMapper.getObjectPropToOntClassMap(ontModel);
            //modelRDF.objectPropertyMap = objectPropToOntClassMap;
            Map<String, OntClassExtension> ontClass2OntClassExtensionMap = ClassCollector.getOntClass2OntClassExtensionMap(ontModel);
            modelRDF.stringOntClassExtensionMap = ClassCollector.getOntClass2OntClassExtensionMap(ontModel);

            List<SampleType> sampleTypeList = ClassCollector.getSampleTypeList(ontModel);

            restrictionsToSampleMetadata(sampleTypeList, ontClass2OntClassExtensionMap);

            //TODO: there is no direct connection from hasComparator to Comparator, from prop to vocabulary type
            for(SampleType sampleType: sampleTypeList)
            {
                for(SamplePropertyType samplePropertyType: sampleType.properties)
                {
                    if (!Objects.equals(samplePropertyType.dataType, "SAMPLE"))
                    {
                        if (RDFtoOpenBISDataTypeMap.get(samplePropertyType.ontologyAnnotationId) != null)
                        {
                            samplePropertyType.dataType = RDFtoOpenBISDataTypeMap.get(samplePropertyType.ontologyAnnotationId).get(0);
                            //System.out.println("    DATATYPE: "+ samplePropertyType.dataType + " -> " + samplePropertyType.ontologyAnnotationId + " -> " + RDFtoOpenBISDataTypeMap.get(samplePropertyType.ontologyAnnotationId).get(0));

                        }
                        if (objectPropToOntClassMap.get(samplePropertyType.ontologyAnnotationId) != null)
                        {
                            samplePropertyType.dataType = "SAMPLE"+ ":" + objectPropToOntClassMap.get(samplePropertyType.ontologyAnnotationId).get(0);
                            //System.out.println(" OBJECT_PROP: "+ samplePropertyType.dataType + " -> " + samplePropertyType.ontologyAnnotationId + " -> " + objectPropToOntClassMap.get(samplePropertyType.ontologyAnnotationId).get(0));
                        }
                        //System.out.println("  VACAB_TYPE: "+ samplePropertyType.dataType + " -> " + samplePropertyType.ontologyAnnotationId + " -> " + modelRDF.vocabularyTypeListGroupedByType.get(samplePropertyType.ontologyAnnotationId));
                    }
                   }
            }
            modelRDF.sampleTypeList = sampleTypeList; //ClassCollector.getSampleTypeList(ontModel);
        }

        boolean modelContainsResources = parserUtils.containsResources(model);
        //System.out.println("Model contains Resources: " + (modelContainsResources ? "YES" : "NO"));

        modelRDF.resourcesGroupedByType = modelContainsResources ? parserUtils.getResourceMap(model) : new HashMap<>();
        /*modelRDF.resourcesGroupedByType.keySet().forEach(key -> {
            System.out.println(key + " -> " + modelRDF.resourcesGroupedByType.get(key));
        });*/

        getSubclassChainsEndingWithClass(model, model.listStatements(null, RDFS.subClassOf, (RDFNode) null));
        modelRDF.subClassChanisMap = chainsMap;

        if (verbose) {
            parserUtils.extractGeneralInfo(model, model.getNsPrefixURI(""));
        }

        return modelRDF;
    }

    private void restrictionsToSampleMetadata(List<SampleType> sampleTypeList, Map<String, OntClassExtension> ontClass2OntClassExtensionMap)
    {
        for(SampleType sampleType: sampleTypeList)
        {
            Map<String, Map<String, String>> sampleMetadata = new HashMap<>();
            OntClassExtension ontClassExtension = ontClass2OntClassExtensionMap.get(sampleType.ontologyAnnotationId);
            Map<String, List<Restriction>> restrictionsMap = ontClassExtension.restrictions;
            for(SamplePropertyType samplePropertyType: sampleType.properties)
            {
                Map<String, String> propertyMetadata = new HashMap<>();
                List<Restriction> propertyTypeRestrictionList = restrictionsMap.get(samplePropertyType.ontologyAnnotationId);
                if (propertyTypeRestrictionList != null)
                {
                    for(Restriction restriction: propertyTypeRestrictionList){
                        if (restriction.isCardinalityRestriction())
                        {
                            propertyMetadata.put("CardinalityRestriction", String.valueOf(restriction.asCardinalityRestriction().getCardinality()));
                        } else if (restriction.isMinCardinalityRestriction())
                        {
                            propertyMetadata.put("MinCardinalityRestriction", String.valueOf(restriction.asMinCardinalityRestriction().getMinCardinality()));
                        } else if (restriction.isMaxCardinalityRestriction())
                        {
                            propertyMetadata.put("MaxCardinalityRestriction", String.valueOf(restriction.asMaxCardinalityRestriction().getMaxCardinality()));
                        } else if (restriction.isSomeValuesFromRestriction())
                        {
                            RDFNode someValuesFrom = restriction.asSomeValuesFromRestriction().getSomeValuesFrom();
                            if (someValuesFrom.isURIResource()) {
                                propertyMetadata.put("SomeValuesFromRestriction", someValuesFrom.asResource().getURI());
                            } else if (someValuesFrom.isAnon() && someValuesFrom.canAs(OntClass.class)){
                                OntClass anonClass = someValuesFrom.as(OntClass.class);
                                // Recursively handle the anonymous class, be it union, intersection, etc.
                                if (anonClass.isUnionClass()) {
                                    UnionClass unionClass = anonClass.asUnionClass();
                                    propertyMetadata.put("SomeValuesFromRestriction", ontClassExtension.unions.get(unionClass).toString());
                                }
                            }
                        } else
                        {
                            propertyMetadata.put("UNHANDLED_Restriction", restriction.toString());
                        }
                    }
                    samplePropertyType.metadata.putAll(propertyMetadata);
                    samplePropertyType.setMultiValue(checkMultiValue(propertyMetadata));
                    samplePropertyType.setMandatory(checkMandatory(propertyMetadata));
                }
                sampleMetadata.put(samplePropertyType.code, propertyMetadata);
            }
            sampleType.metadata = sampleMetadata;
        }
    }

    private static boolean checkMultiValue(Map<String, String> propertyMetadata)
    {
        return propertyMetadata.containsKey("MinCardinalityRestriction") && !propertyMetadata.containsKey("MaxCardinalityRestriction");
    }

    private static int checkMandatory(Map<String, String> propertyMetadata)
    {
        return (Objects.equals(propertyMetadata.get("MinCardinalityRestriction"), "1") &&
                Objects.equals(propertyMetadata.get("MaxCardinalityRestriction"), "1")) ? 1 : 0;
    }

    private void checkFileExists(String inputFileName)
    {
        InputStream in = FileManager.getInternal().open(inputFileName);
        if (in == null)
        {
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
