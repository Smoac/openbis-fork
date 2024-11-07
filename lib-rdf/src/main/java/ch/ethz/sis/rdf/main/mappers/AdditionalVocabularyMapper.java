package ch.ethz.sis.rdf.main.mappers;

import ch.ethz.sis.rdf.main.model.xlsx.VocabularyType;
import ch.ethz.sis.rdf.main.model.xlsx.VocabularyTypeOption;
import ch.ethz.sis.rdf.main.parser.ResourceParsingResult;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

import java.util.*;

public class AdditionalVocabularyMapper
{

    public static List<VocabularyType> findVocabularyTypes(ResourceParsingResult resourceParsingResult, OntModel additionalOntModel, Set<String> baseVocabClassUris){
        Set<String> importedClasses = resourceParsingResult.getClassesImported();
        List<VocabularyType> res = new ArrayList<>();
        Map<String, List<VocabularyTypeOption>> temp = new HashMap<>();
        for (String classUri : importedClasses){
            OntClass ontClass = additionalOntModel.getOntClass(classUri);
            String baseClassUri = getBaseVocabClass(ontClass, baseVocabClassUris);
            if (baseClassUri == null)
            {
                continue;
            }
            if (temp.get(baseClassUri) == null){
                temp.put(baseClassUri, new ArrayList<>());
            }

            temp.get(baseClassUri).add(getTypeOption(ontClass));
        }
        for (Map.Entry<String, List<VocabularyTypeOption>> entry : temp.entrySet()){
            OntClass ontClass = additionalOntModel.getOntClass(entry.getKey());
            String code = getCode(ontClass);
            String description = ontClass.getProperty(RDFS.label).getObject().toString();;
            String label = ontClass.getProperty(SKOS.prefLabel).getObject().toString();

            VocabularyType vocabularyType = new VocabularyType(code, description, ontClass.getURI(), entry.getValue());
            res.add(vocabularyType);


        }



        return res;

    }

    private static VocabularyTypeOption getTypeOption(OntClass ontClass){
        String code = getCode(ontClass);
        String description = ontClass.getProperty(RDFS.label).getObject().toString();;
        String label = ontClass.getProperty(SKOS.prefLabel).getObject().toString();
        return new VocabularyTypeOption(code, label, description);
    }

    private static String getCode(OntClass ontClass){
        if (ontClass.getURI().contains("snomed")){
            return "SNOMED-" + ontClass.getURI().replace("http://snomed.info/id/", "");
        }

        return "";



    }


    private static String getBaseVocabClass(OntClass cls, Set<String> baseVocabClassUris){
        if (baseVocabClassUris.contains(cls.getURI())){
            return cls.getURI();
        }
        if (cls.getSuperClass() == null || cls.getSuperClass().getURI() == null){
            return null;
        }
        return getBaseVocabClass(cls.getSuperClass(), baseVocabClassUris);

    }





}
