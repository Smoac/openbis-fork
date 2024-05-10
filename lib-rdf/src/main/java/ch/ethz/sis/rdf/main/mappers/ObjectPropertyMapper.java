package ch.ethz.sis.rdf.main.mappers;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import java.io.FileNotFoundException;
import java.util.*;

public class ObjectPropertyMapper
{
    public static Map<String, List<String>> toObjects(OntModel model){
        Map<String, List<String>> mappedObjectProperty = new HashMap<>();
        model.listObjectProperties().forEachRemaining(property -> {
            if (property.isURIResource()) {
                Resource range = property.getRange();
                if (range != null) {
                    if (range.canAs(UnionClass.class)) {
                        UnionClass unionRange = range.as(UnionClass.class);
                        List<String> objectPropertyRange = new ArrayList<>();
                        // Process each member of the union eg.
                        // rdfs:range [ a owl:Class ;
                        //            owl:unionOf ( sphn:Terminology sphn:Code ) ] ;
                        unionRange.listOperands().forEachRemaining(operand -> {
                            if (operand.isURIResource()) {
                                objectPropertyRange.add(operand.getURI());
                            }
                        });
                        mappedObjectProperty.put(property.getURI(), objectPropertyRange);
                        //System.out.println(property.getURI() + " range includes: " + objectPropertyRange);
                    } else {
                        mappedObjectProperty.put(property.getURI(), Collections.singletonList(range.getURI()));
                        //System.out.println(property.getURI() + " range mapped to: " + range.getURI());
                    }
                }
            }
        });
        return mappedObjectProperty;
    }
}
