package ch.ethz.sis.rdf.main.model.xlsx;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class SamplePropertyType
{
    public String code;
    public String propertyLabel;
    public String dataType;
    public String vocabularyCode;
    public String description;
    public HashMap<String, String> metadata;
    public String ontologyAnnotationId;
    public boolean isMultiValue;
    public int isMandatory;

    public SamplePropertyType(String propertyLabel, String ontologyAnnotationId)
    {
        this.code = propertyLabel.toUpperCase(Locale.ROOT);
        this.propertyLabel = propertyLabel;
        this.ontologyAnnotationId = ontologyAnnotationId;
        this.metadata = new HashMap<>();
    }

    public void setMultiValue(boolean isMultiValue)
    {
        this.isMultiValue = isMultiValue;
    }

    public void setMandatory(int isMandatory)
    {
        this.isMandatory = isMandatory;
    }

    @Override
    public String toString()
    {
        return "PropertyType{" +
                "code='" + code + '\'' +
                ", propertyLabel='" + propertyLabel + '\'' +
                ", dataType='" + dataType + '\'' +
                ", vocabularyCode='" + vocabularyCode + '\'' +
                ", description='" + description + '\'' +
                ", metadata='" + metadata + '\'' +
                ", ontologyAnnotationId='" + ontologyAnnotationId + '\'' +
                ", multiValued=" + isMultiValue +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SamplePropertyType that = (SamplePropertyType) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(code);
    }
}
