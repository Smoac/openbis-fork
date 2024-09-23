package ch.ethz.sis.rdf.main.model.xlsx;

import java.util.List;
import java.util.Objects;

public class VocabularyType {
    public String code;
    public String ontologyAnnotationId;
    public String description;
    public List<VocabularyTypeOption> options;

    public VocabularyType(String code, String description, String ontologyAnnotationId, List<VocabularyTypeOption> options)
    {
        this.code = code;
        this.description = description;
        this.ontologyAnnotationId = ontologyAnnotationId;
        this.options = options;
    }

    public VocabularyType(String code, String description, List<VocabularyTypeOption> options)
    {
        this.code = code;
        this.description = description;
        this.options = options;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public List<VocabularyTypeOption> getOptions()
    {
        return options;
    }

    @Override
    public String toString()
    {
        return "VocabularyType{" +
                "code='" + code + '\'' +
                ", ontologyAnnotationId='" + ontologyAnnotationId + '\'' +
                ", description='" + description + '\'' +
                ", options=" + options +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        VocabularyType that = (VocabularyType) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(code);
    }
}
