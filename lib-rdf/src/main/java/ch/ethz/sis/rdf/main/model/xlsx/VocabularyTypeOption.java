package ch.ethz.sis.rdf.main.model.xlsx;

import java.util.Objects;

public class VocabularyTypeOption {
    public String code;
    public String label;
    public String description;

    public VocabularyTypeOption(String code, String label, String description)
    {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public String toString()
    {
        return "VocabularyTypeOption{" +
                "code='" + code + '\'' +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        VocabularyTypeOption that = (VocabularyTypeOption) o;
        return Objects.equals(code, that.code) && Objects.equals(label, that.label) && Objects.equals(description,
                that.description);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(code, label, description);
    }
}
