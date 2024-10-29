package ch.ethz.sis.rdf.main.model.rdf;

public class AdditionalProperty
{

    private String code;
    private int mandatory;
    private int shownInEditsViews;
    private String property;
    private String dataType;
    private String vocubalary;
    private String ontologyId;
    private String ontologyVersion;
    private String ontologyAnnotationId;
    private int multiValued;
    int min;
    int max;
    String uri;
    String description;

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getMin()
    {
        return min;
    }

    public void setMin(int min)
    {
        this.min = min;
    }

    public int getMax()
    {
        return max;
    }

    public void setMax(int max)
    {
        this.max = max;
    }

    public AdditionalProperty(String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public int getMandatory()
    {
        return mandatory;
    }

    public void setMandatory(int mandatory)
    {
        this.mandatory = mandatory;
    }

    public int getShownInEditsViews()
    {
        return shownInEditsViews;
    }

    public void setShownInEditsViews(int shownInEditsViews)
    {
        this.shownInEditsViews = shownInEditsViews;
    }

    public String getProperty()
    {
        return property;
    }

    public void setProperty(String property)
    {
        this.property = property;
    }

    public String getDataType()
    {
        return dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public String getVocubalary()
    {
        return vocubalary;
    }

    public void setVocubalary(String vocubalary)
    {
        this.vocubalary = vocubalary;
    }

    public String getOntologyId()
    {
        return ontologyId;
    }

    public void setOntologyId(String ontologyId)
    {
        this.ontologyId = ontologyId;
    }

    public String getOntologyVersion()
    {
        return ontologyVersion;
    }

    public void setOntologyVersion(String ontologyVersion)
    {
        this.ontologyVersion = ontologyVersion;
    }

    public String getOntologyAnnotationId()
    {
        return ontologyAnnotationId;
    }

    public void setOntologyAnnotationId(String ontologyAnnotationId)
    {
        this.ontologyAnnotationId = ontologyAnnotationId;
    }

    public int getMultiValued()
    {
        return multiValued;
    }

    public void setMultiValued(int multiValued)
    {
        this.multiValued = multiValued;
    }

    public String getUri()
    {
        return uri;
    }

    public void setUri(String uri)
    {
        this.uri = uri;
    }
}
