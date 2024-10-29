package ch.ethz.sis.rdf.main.model.rdf;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;

import java.util.*;

/*
 *  COMMENTS
 *
 *      In OWL Direct (DL-Based) Semantics, this excludes classes and properties.
 *
 *      In OWL RDF Based semantics, this includes everything, where classes and properties are also viewed as individuals
 *      (and thus owl:Thing has the same extension as rdfs:Resource).
 *
 *      rdfs:Class is the (RDFS) class of classes. Any member is itself a class.
 *      check for a good explanation https://answers.knowledgegraph.tech/t/rdfs-class-vs-owl-thing/4698
 */

public class OntClassExtension
{
    public OntClass ontClass;
    public OntClass superClass;
    public String label;

    public Map<String, List<Restriction>> restrictions;
    public Map<UnionClass, List<String>> unions;
    public List<PropertyTupleRDF> propertyTuples;
    public Map<String, AdditionalProperty> hackyProperties;

    public OntClassExtension(OntClass ontClass) {
        this.ontClass = ontClass;
        this.restrictions = new HashMap<>();
        this.unions = new HashMap<>();
        this.propertyTuples = new ArrayList<>();
        // Parsing standard annotations
        this.label = getAnnotation(ontClass, RDFS.label);
        this.hackyProperties = new HashMap<>();
    }

    public void addRestriction(String property, Restriction restriction) {
        this.restrictions.compute(property, (p, existingRestrictions) -> {
            if (existingRestrictions == null) {
                return new ArrayList<>(Collections.singletonList(restriction));
            } else {
                existingRestrictions.add(restriction);
                return existingRestrictions;
            }
        });
    }

    public void addUnion(UnionClass unionClass, List<String> operands) {
        this.unions.put(unionClass, operands);
    }

    public void setSuperClass(OntClass superClass) {this.superClass = superClass;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Class: ").append(ontClass.getURI() != null ? ontClass.getURI() : "Anonymous Class")
                .append("\nSuperClass: ").append(superClass != null ? superClass : "No superClass")
                .append("\nLabel: ").append(label != null ? label : "No label")
                .append("\nProperties: \n");

        propertyTuples.forEach(tupleRDF ->
                sb.append(" - ").append(tupleRDF.getPredicate()).append(" [").append(tupleRDF.getObject()).append("]").append("\n"));

        sb.append("\nRestrictions:\n");
        restrictions.forEach((k, v) ->
                sb.append(" - on ").append(k).append(": ").append(printRestriction(v)).append("\n"));
        return sb.toString();
    }

    private String printRestriction(List<Restriction> restrictions) {
        StringBuilder sb = new StringBuilder();
        for (Restriction restriction : restrictions) {
            if(restriction.isCardinalityRestriction()) sb.append(restriction.asCardinalityRestriction().getCardinality());
            else if(restriction.isMinCardinalityRestriction()) sb.append(" MinCardinalityRestriction [").append(restriction.asMinCardinalityRestriction().getMinCardinality()).append("]");
            else if(restriction.isMaxCardinalityRestriction()) sb.append(" MaxCardinalityRestriction [").append(restriction.asMaxCardinalityRestriction().getMaxCardinality()).append("]");
            else if(restriction.isSomeValuesFromRestriction()) {
                StringBuilder values = new StringBuilder();
                Resource resource = restriction.asSomeValuesFromRestriction().getSomeValuesFrom();
                assert resource != null;
                if (this.unions.get(resource) != null)
                {
                    values.append(resource)
                            .append(": ")
                            .append(String.join(", ", this.unions.get(resource)));
                } else
                {
                    values.append(resource);
                }
                sb.append(" SomeValuesFromRestriction [").append(values).append("]");
            }
            else sb.append("NONE");
        }
        return sb.toString();
    }

    private String getAnnotation(OntClass ontClass, Property property) {
        Statement annotation = ontClass.getProperty(property);
        if (annotation != null && annotation.getObject().isLiteral()) {
            return annotation.getObject().asLiteral().getString();
        }
        return null;
    }

    public Map<String, AdditionalProperty> getHackyProperties()
    {
        return hackyProperties;
    }
}
