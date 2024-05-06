package ch.ethz.sis.rdf.main.mappers;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

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

public class OntClassObject
{
    public OntClass ontClass;
    public OntClass superClass;
    public String label;
    public String comment;
    public String skosDefinition;
    public String skosNote;

    public Map<OntProperty, List<Restriction>> restrictions;
    public Map<UnionClass, List<String>> unions;

    public OntClassObject(OntClass ontClass) {
        this.ontClass = ontClass;
        this.restrictions = new HashMap<>();
        this.unions = new HashMap<>();
        // Parsing standard annotations
        this.label = getAnnotation(ontClass, RDFS.label);
        this.comment = getAnnotation(ontClass, RDFS.comment);
        this.skosDefinition = getAnnotation(ontClass, SKOS.definition);
        this.skosNote = getAnnotation(ontClass, SKOS.note);
    }

    public void addRestriction(OntProperty property, Restriction restriction) {
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
                .append("\nComment: ").append(comment != null ? comment : "No comment")
                .append("\nSKOS Definition: ").append(skosDefinition != null ? skosDefinition : "-")
                .append("\nSKOS Note: ").append(skosNote != null ? skosNote : "-")
                .append("\nRestrictions:\n");
        restrictions.forEach((k, v) ->
                sb.append(" - on ").append(k).append(": ").append(printRestriction(v)).append("\n"));
        return sb.toString();
    }

    private String printRestriction(List<Restriction> restrictions) {
        StringBuilder sb = new StringBuilder();
        for (Restriction restriction : restrictions) {
            switch(RestrictionClassEnum.valueOf(restriction.getClass().getSimpleName())){
                case CARDINALITY:
                    //System.out.println("   - Cardinality Restriction on " + restriction.getOnProperty().getURI() + " value: " + restriction.asCardinalityRestriction().getCardinality());
                    sb.append(restriction.asCardinalityRestriction().getCardinality());
                    break;
                case MIN_CARDINALITY:
                    sb.append(" MinCardinalityRestriction [").append(restriction.asMinCardinalityRestriction().getMinCardinality()).append("]");
                    break;
                case MAX_CARDINALITY:
                    sb.append(" MaxCardinalityRestriction [").append(restriction.asMaxCardinalityRestriction().getMaxCardinality()).append("]");
                    break;
                case SOME_VALUES_FROM:
                    StringBuilder values = new StringBuilder();
                    Resource resource = restriction.asSomeValuesFromRestriction().getSomeValuesFrom();
                    assert resource != null;
                    if (this.unions.get(resource) != null) {
                        values.append(resource)
                                .append(": ")
                                .append(String.join(", ", this.unions.get(resource)));
                    } else {
                        values.append(resource);
                    }
                    sb.append(" SomeValuesFromRestriction [").append(values).append("]");
                    break;
                default:
                    //System.out.println(" x ");
                    sb.append("NONE");
                    break;
            }
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
}
