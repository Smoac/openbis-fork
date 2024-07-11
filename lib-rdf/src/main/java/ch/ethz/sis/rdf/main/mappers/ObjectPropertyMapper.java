package ch.ethz.sis.rdf.main.mappers;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for mapping object properties to their ranges in an ontology model.
 */
public class ObjectPropertyMapper {

    public Map<String, List<String>> mappedObjectProperty;

    public ObjectPropertyMapper(OntModel model){
        this.mappedObjectProperty = toObjects(model);
    }

    public Map<String, List<String>> getMappedObjectProperty() {
        return mappedObjectProperty;
    }

    /**
     * Maps object properties to their respective ranges in the given ontology model.
     *
     * @param model the ontology model to process
     * @return a map where keys are URIs of object properties and values are lists of URIs representing the ranges
     *
     * Example:
     *      https://biomedit.ch/rdf/sphn-schema/sphn#hasOriginLocation --> [https://biomedit.ch/rdf/sphn-schema/sphn#Location]
     *      https://biomedit.ch/rdf/sphn-schema/sphn#hasDrug --> [https://biomedit.ch/rdf/sphn-schema/sphn#Drug]
     */
    private Map<String, List<String>> toObjects(OntModel model) {
        Map<String, List<String>> mappedObjectProperty = new HashMap<>();

        model.listObjectProperties().forEachRemaining(property -> {
            if (property.isURIResource()) {
                Resource range = property.getRange();
                if (range != null) {
                    List<String> objectPropertyRange = new ArrayList<>();

                    if (range.canAs(UnionClass.class)) {
                        // If the range is a union class, process each operand
                        // rdfs:range [ a owl:Class ;
                        //            owl:unionOf ( sphn:Terminology sphn:Code ) ] ;
                        UnionClass unionRange = range.as(UnionClass.class);
                        unionRange.listOperands().forEachRemaining(operand -> {
                            if (operand.isURIResource()) {
                                objectPropertyRange.add(operand.getURI());
                            }
                        });
                    } else if (range.isURIResource()) {
                        // If the range is a single URI resource
                        objectPropertyRange.add(range.getURI());
                    }

                    if (!objectPropertyRange.isEmpty()) {
                        mappedObjectProperty.put(property.getURI(), objectPropertyRange);
                    }
                }
            }
        });

        return mappedObjectProperty;
    }
}
