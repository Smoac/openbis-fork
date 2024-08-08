package ch.ethz.sis.rdf.main.model.xlsx;

import ch.ethz.sis.rdf.main.Utils;

public class VocabularyTypeOption {
    public String code;
    public String label;
    public String description;

    public VocabularyTypeOption(String uri, String label)
    {
        this.code = Utils.extractLabel(uri);
        this.label = label;
        this.description = uri;
    }

    public VocabularyTypeOption(String code, String label, String description)
    {
        this.code = code;
        this.label = label;
        this.description = description;
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
}
