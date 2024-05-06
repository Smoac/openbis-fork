package ch.ethz.sis.rdf.main.mappers;

public enum RestrictionClassEnum{
    CARDINALITY("CardinalityRestrictionImpl"),
    MIN_CARDINALITY("MinCardinalityRestrictionImpl"),
    MAX_CARDINALITY("MaxCardinalityRestrictionImpl"),
    SOME_VALUES_FROM("SomeValuesFromRestrictionImpl");

    private final String simpleName;

    RestrictionClassEnum(String simpleName) {this.simpleName = simpleName;}

    public String getSimpleName() { return simpleName;}
}
