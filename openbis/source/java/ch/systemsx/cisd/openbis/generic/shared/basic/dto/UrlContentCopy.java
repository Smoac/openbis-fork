package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

public class UrlContentCopy implements IContentCopy
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String label;

    private String url;

    public UrlContentCopy(String code, String label, String url)
    {
        this.code = code;
        this.label = label;
        this.url = url;
    }

    public UrlContentCopy()
    {
    }

    @Override
    public String getLocation()
    {
        String labelString;
        if (label == null || label.length() == 0)
        {
            labelString = code;
        } else
        {
            labelString = code + " (" + label + ")";
        }

        return "External DMS: " + labelString + "</br>Link: <a class=\"gwt-Anchor\" href=\"" + url + "\" target=\"_blank\" \">" + url + "</a><br>";
    }
}
