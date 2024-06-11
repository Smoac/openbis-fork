package ch.ethz.sis.rdf.main.entity;

public class PropertyTupleRDF
{
    public String predicate;
    public String object;

    public PropertyTupleRDF(String predicate, String object) {
        this.predicate = predicate;
        this.object = object;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObject() {
        return object;
    }

    public String getPredicateLabel() {
        return predicate.substring(predicate.lastIndexOf("#") + 1);//.replace("has", "");
    }

    @Override
    public String toString() {
        return "(" + predicate + ", " + object + ")";
    }
}
