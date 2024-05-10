package ch.ethz.sis.rdf.main.mappers;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.rdf.model.Resource;

import java.util.*;

public class DatatypeMapper {
    private static final Map<String, String> datatypeMappings = new HashMap<>();

    static {
        //datatypeMappings.put(XSDDatatype.XSDstring.getURI(), "STRING");
        //datatypeMappings.put(XSDDatatype.XSDdouble.getURI(), "DOUBLE");
        //datatypeMappings.put(XSDDatatype.XSDdateTime.getURI(), "DATETIME");
        //datatypeMappings.put(XSDDatatype.XSDgDay.getURI(), "DAY");
        //datatypeMappings.put(XSDDatatype.XSDgMonth.getURI(), "MONTH");
        //datatypeMappings.put(XSDDatatype.XSDgYear.getURI(), "YEAR");
        //datatypeMappings.put(XSDDatatype.XSDtime.getURI(), "TIME");
        datatypeMappings.put(XSDDatatype.XSDdateTime.getURI(), "TIMESTAMP");
        datatypeMappings.put(XSDDatatype.XSDstring.getURI(), "VARCHAR");
        datatypeMappings.put(XSDDatatype.XSDdouble.getURI(), "REAL");
        datatypeMappings.put(XSDDatatype.XSDgDay.getURI(), "INTEGER");
        datatypeMappings.put(XSDDatatype.XSDgMonth.getURI(), "INTEGER");
        datatypeMappings.put(XSDDatatype.XSDgYear.getURI(), "INTEGER");
        datatypeMappings.put(XSDDatatype.XSDtime.getURI(), "TIMESTAMP");
    }

    public static Map<String, List<String>> toOpenBISDataTypes(OntModel model){
        Map<String, List<String>> mappedDataTypes = new HashMap<>();
        model.listDatatypeProperties().forEachRemaining(property -> {
            if (property.isURIResource()) {
                Resource range = property.getRange();
                if (range != null) {
                    if (range.canAs(UnionClass.class)) {
                        UnionClass unionRange = range.as(UnionClass.class);
                        List<String> openBISDataTypeRange = new ArrayList<>();
                        // Process each member of the union eg.
                        // rdfs:range [ a rdfs:Datatype ;
                        //            owl:unionOf ( xsd:double xsd:string ) ] ;
                        unionRange.listOperands().forEachRemaining(operand -> {
                            if (operand.isURIResource()) {
                                openBISDataTypeRange.add(datatypeMappings.getOrDefault(operand.getURI(), "UNKNOWN"));
                            }
                        });
                        mappedDataTypes.put(property.getURI(), openBISDataTypeRange);
                        //System.out.println(property.getURI() + " range includes: " + openBISDataTypeRange);
                    } else {
                        // Map the range URI to a openBIS type, if available
                        String openBISDataType = datatypeMappings.getOrDefault(range.getURI(), "UNKNOWN");
                        //System.out.println(property.getURI() + " range mapped to: " + openBISDataType);
                        mappedDataTypes.put(property.getURI(), Collections.singletonList(openBISDataType));
                    }
                }
            }
        });
        return mappedDataTypes;
    }
}
