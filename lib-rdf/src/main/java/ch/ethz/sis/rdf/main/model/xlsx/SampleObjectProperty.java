package ch.ethz.sis.rdf.main.model.xlsx;

public class SampleObjectProperty
{
    public String propertyURI;
    public String label;
    public String value;
    public String valueURI;

    public SampleObjectProperty(String propertyURI, String label, String value, String valueURI)
    {
        this.propertyURI = propertyURI;
        this.label = label;
        this.value = value;
        this.valueURI = valueURI;
    }

    public String getLabel()
    {
        return label;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return "SampleObjectProperty{" +
                "propertyURI='" + propertyURI + '\'' +
                ", label='" + label + '\'' +
                ", value='" + value + '\'' +
                ", valueURI='" + valueURI + '\'' +
                '}';
    }
}
