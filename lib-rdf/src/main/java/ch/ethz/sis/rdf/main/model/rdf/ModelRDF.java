package ch.ethz.sis.rdf.main.model.rdf;

import ch.ethz.sis.rdf.main.model.xlsx.VocabularyType;

import java.util.List;
import java.util.Map;

public class ModelRDF {
    public String ontNamespace;
    public String ontVersion;
    public Map<String, String> ontMetadata;
    public Map<String, String> nsPrefixes;
    public Map<String, OntClassExtension> classDetailsMap;
    public Map<String, List<String>> RDFtoOpenBISDataType;
    public Map<String, List<String>> mappedObjectProperty;
    public Map<String, List<ResourceRDF>> resourcesGroupedByType;
    public List<VocabularyType> mappedNamedIndividualList;
    public Map<String, List<VocabularyType>> mappedNamedIndividual;
    public Map<String, List<String>> mappedSubClasses;


}
