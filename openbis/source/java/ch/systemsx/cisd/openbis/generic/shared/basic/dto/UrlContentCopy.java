package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

public class UrlContentCopy implements IContentCopy
{
    private static final long serialVersionUID = 1L;

    private String label;

    private String url;

    public UrlContentCopy(String label, String url)
    {
        this.label = label;
        this.url = url;
    }

    public UrlContentCopy()
    {
    }

    @Override
    public boolean isHyperLinkable()
    {
        return true;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public String getLocation()
    {
        return url;
    }
}
