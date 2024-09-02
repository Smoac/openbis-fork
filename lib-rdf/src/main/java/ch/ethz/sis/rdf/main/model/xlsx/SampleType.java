package ch.ethz.sis.rdf.main.model.xlsx;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

import java.util.*;

public class SampleType {
    public String code;
    public String description;
    public String ontologyAnnotationId;
    public Map<String, Map<String, String>> metadata;
    public List<SamplePropertyType> properties;

    public SampleType(Resource cls){
        this.code = cls.getLocalName().toUpperCase(Locale.ROOT);
        this.description = processDescription(cls);
        this.metadata = new HashMap<>();
        this.ontologyAnnotationId = cls.getURI();
        this.properties = new ArrayList<>();
    }

    private String processDescription(Resource cls)
    {
        String label = getAnnotation(cls, RDFS.label);
        String comment = getAnnotation(cls, RDFS.comment);
        String skosDefinition = getAnnotation(cls, SKOS.definition);
        String skosNote = getAnnotation(cls, SKOS.note);

        assert label != null;
        StringBuilder description = new StringBuilder();
        description.append(label).append("\n");
        if (comment != null) description.append(comment).append("\n");
        if (skosDefinition != null) description.append(skosDefinition).append("\n");
        if (skosNote != null) description.append(skosNote).append("\n");

        return String.valueOf(description);
    }

    private String getAnnotation(Resource cls, Property property) {
        Statement annotation = cls.getProperty(property);
        if (annotation != null && annotation.getObject().isLiteral()) {
            return annotation.getObject().asLiteral().getString();
        }
        return null;
    }

    @Override
    public String toString()
    {
        return "SampleType{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", ontologyAnnotationId='" + ontologyAnnotationId + '\'' +
                ", metadata=" + metadata +
                ", properties=" + properties +
                '}';
    }
}
