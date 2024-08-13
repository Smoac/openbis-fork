package ch.ethz.sis.rdf.main.model.rdf;

import ch.ethz.sis.rdf.main.model.xlsx.VocabularyType;

import java.util.List;
import java.util.Map;

public class ModelRDF
{
    public String ontNamespace;
    public String ontVersion;
    public Map<String, String> ontMetadata;
    public Map<String, String> nsPrefixes;
    public Map<String, OntClassExtension> stringOntClassExtensionMap;
    public Map<String, List<String>> RDFtoOpenBISDataType;
    public Map<String, List<String>> objectPropertyMap;
    public Map<String, List<ResourceRDF>> resourcesGroupedByType;
    public List<VocabularyType> vocabularyTypeList;
    public Map<String, List<VocabularyType>> vocabularyTypeListGroupedByType;
    public Map<String, List<String>> subClassChanisMap;

    public boolean isSubClass(String uri)
    {
        return subClassChanisMap.containsKey(uri);
    }

    //TODO implement a non hardcoded way to check this: check if its type in the model is one of the Classes
    public boolean isResource(String uri)
    {
        return resourcesGroupedByType.containsKey(uri);
    }

    public boolean isPresentInVocType(String uri)
    {
        return vocabularyTypeListGroupedByType.containsKey(uri);
    }
}
