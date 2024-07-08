package ch.ethz.sis.rdf.main.parser;

import ch.ethz.sis.rdf.main.entity.VocabularyType;
import ch.ethz.sis.rdf.main.entity.VocabularyTypeOption;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

import java.util.*;
import java.util.stream.Collectors;

public class NamedIndividualParser {
    private final Model model;

    public NamedIndividualParser(Model model) {
        this.model = model;
    }

    public List<VocabularyType> processGroupedNamedIndividuals() {
        Map<String, List<VocabularyType>> groupedByCode = processNamedIndividuals();

        List<VocabularyType> mergedList = groupedByCode.entrySet().stream().map(entry -> {
            String code = entry.getKey();
            List<VocabularyTypeOption> mergedOptions = new ArrayList<>();

            entry.getValue().forEach(vocabularyType -> mergedOptions.addAll(vocabularyType.getOptions()));

            String description = entry.getValue().get(0).getDescription();

            return new VocabularyType(code, description, mergedOptions);
        }).toList();

        //mergedList.forEach(System.out::println);

        return mergedList;
    }

    public Map<String, List<VocabularyType>> processNamedIndividuals() {
        List<VocabularyType> vocabularyTypeList = new ArrayList<>();
        model.listSubjectsWithProperty(RDF.type, OWL2.NamedIndividual).forEachRemaining(subject -> {
            processIndividual(subject, vocabularyTypeList);
        });
        return vocabularyTypeList.stream().collect(Collectors.groupingBy(VocabularyType::getCode));
    }

    private void processIndividual(Resource subject, List<VocabularyType> vocabularyTypeList) {
        try {
            String optionLabel = model.getProperty(subject, RDFS.label).getString();
            VocabularyTypeOption option = new VocabularyTypeOption(
                    subject.getLocalName().toUpperCase(Locale.ROOT),
                    optionLabel,
                    subject.getURI());

            model.listObjectsOfProperty(subject, RDF.type)
                    .filterKeep(rdfNode -> rdfNode.canAs(Resource.class))
                    .filterDrop(rdfNode -> rdfNode.asResource().equals(OWL2.NamedIndividual.asResource()))
                    .forEach(rdfNode -> {
                        VocabularyType vocabularyType = createVocabularyType((Resource) rdfNode, option);
                        vocabularyTypeList.add(vocabularyType);
                    });
        } catch (Exception e) {
            System.out.println("Error processing OWL2.NamedIndividual: " + subject + " | Error: " + e.getMessage());
        }
    }

    private VocabularyType createVocabularyType(Resource resource, VocabularyTypeOption option) {
        String description = getPropertySafely(resource, SKOS.definition, "");
        String subClassOf = getPropertySafely(resource, RDFS.subClassOf, resource.toString());

        return new VocabularyType(
                resource.getLocalName().toUpperCase(Locale.ROOT),
                description + "\n" + resource.getURI() + "\n" + subClassOf,
                new ArrayList<>(Collections.singletonList(option))
        );
    }

    private String getPropertySafely(Resource resource, Property property, String defaultValue) {
        Statement statement = model.getProperty(resource, property);
        return statement != null ? statement.getObject().toString() : defaultValue;
    }
}

