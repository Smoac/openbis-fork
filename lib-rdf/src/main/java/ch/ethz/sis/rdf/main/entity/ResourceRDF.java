package ch.ethz.sis.rdf.main.entity;

import java.util.ArrayList;
import java.util.List;

public class ResourceRDF {
    public String resourceVal;
    public String type;
    public List<PropertyTupleRDF> properties;

    public ResourceRDF(String resourceVal) {
        this.resourceVal = resourceVal;
        this.properties = new ArrayList<>();
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getPropertyPredicates(){
        List<String> predicates = new ArrayList<>();
        for (PropertyTupleRDF prop : properties) {
            predicates.add(prop.getPredicateLabel());
        }
        return predicates;
    }

    @Override
    public String toString() {
        return "ResourceRDF [resourceVal=" + resourceVal + ", type=" + type + ", properties=" + properties + "]";
    }
}
