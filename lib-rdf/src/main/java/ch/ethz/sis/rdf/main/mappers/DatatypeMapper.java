package ch.ethz.sis.rdf.main.mappers;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.rdf.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for mapping datatype properties to openBIS data types in an ontology model.
 */
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
        datatypeMappings.put(XSDDatatype.XSDstring.getURI(), DataType.VARCHAR.name());
        datatypeMappings.put(XSDDatatype.XSDdouble.getURI(), "REAL");
        datatypeMappings.put(XSDDatatype.XSDdateTime.getURI(), "TIMESTAMP");
        datatypeMappings.put(XSDDatatype.XSDgDay.getURI(), "INTEGER");
        datatypeMappings.put(XSDDatatype.XSDgMonth.getURI(), "INTEGER");
        datatypeMappings.put(XSDDatatype.XSDgYear.getURI(), "INTEGER");
        datatypeMappings.put(XSDDatatype.XSDtime.getURI(), "TIMESTAMP");
        datatypeMappings.put(XSDDatatype.XSDanyURI.getURI(), "VARCHAR");
    }
//
//    public Map<String, List<String>> mappedDataTypes = new HashMap<>();
//
//    public DatatypeMapper(OntModel model) {
//        this.mappedDataTypes = toOpenBISDataTypes(model);
//    }
//
//    public Map<String, List<String>> getMappedDataTypes() {
//        return mappedDataTypes;
//    }

    /**
     * Maps datatype properties to their respective openBIS data types in the given ontology model.
     *
     * @param model the ontology model to process
     * @return a map where keys are URIs of datatype properties and values are lists of strings representing the openBIS data types
     *
     * Example:
     *      https://biomedit.ch/rdf/sphn-schema/sphn#hasCollectionDateTime --> [TIMESTAMP]
     *      https://biomedit.ch/rdf/sphn-schema/sphn#hasTemplateIdentifier --> [VARCHAR]
     *      https://biomedit.ch/rdf/sphn-schema/sphn#hasMonth --> [INTEGER]
     */
    public static Map<String, List<String>> getRDFtoOpenBISDataTypeMap(OntModel model) {
        Map<String, List<String>> mappedDataTypes = new HashMap<>();

        model.listDatatypeProperties().forEachRemaining(property -> {
            if (property.isURIResource()) {
                Resource range = property.getRange();
                if (range != null) {
                    List<String> openBISDataTypeRange = new ArrayList<>();

                    if (range.canAs(UnionClass.class)) {
                        // If the range is a union class, process each operand
                        // rdfs:range [ a rdfs:Datatype ;
                        //            owl:unionOf ( xsd:double xsd:string ) ] ;
                        UnionClass unionRange = range.as(UnionClass.class);
                        for(int i=0; i<unionRange.getOperands().size(); i++) {
                            RDFNode item = unionRange.getOperands().get(i);
                            if (item.isURIResource()) {
                                openBISDataTypeRange.add(datatypeMappings.getOrDefault(item.asResource().getURI(), "UNKNOWN"));
                            }
                        }
                        /*unionRange.listOperands().forEachRemaining(operand -> {
                            if (operand.isURIResource()) {
                                openBISDataTypeRange.add(datatypeMappings.getOrDefault(operand.getURI(), "UNKNOWN"));
                            }
                        });*/
                    } else if (range.isURIResource()) {
                        // If the range is a single URI resource
                        openBISDataTypeRange.add(datatypeMappings.getOrDefault(range.getURI(), "UNKNOWN"));
                    }

                    if (!openBISDataTypeRange.isEmpty()) {
                        mappedDataTypes.put(property.getURI(), openBISDataTypeRange);
                    }
                }
            }
        });
        return mappedDataTypes;
    }
}

