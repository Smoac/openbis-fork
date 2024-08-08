package ch.ethz.sis.rdf.main.model.xlsx;

import java.util.ArrayList;
import java.util.List;

public class VocabularyType {
    public String code;
    public String description;
    public List<VocabularyTypeOption> options;

    public VocabularyType(String code, String description, List<VocabularyTypeOption> options)
    {
        this.code = code;
        this.description = description;
        this.options = options;
    }

    public VocabularyType() {
        this.code = "";
        this.description = "";
        this.options = new ArrayList<>();
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

    public void setOptions(List<VocabularyTypeOption> options)
    {
        this.options = options;
    }

    @Override
    public String toString()
    {
        return "VocabularyType{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", options=" + options +
                '}';
    }
}
