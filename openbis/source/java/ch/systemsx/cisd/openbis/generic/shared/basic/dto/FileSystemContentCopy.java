package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

public class FileSystemContentCopy implements IContentCopy
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String label;

    private String host;

    private String directory;

    private String path;

    private String hash;

    public FileSystemContentCopy()
    {
    }

    public FileSystemContentCopy(String code, String label, String host, String directory, String path, String hash)
    {
        this.code = code;
        this.label = label;
        this.host = host;
        this.directory = directory;
        this.path = path;
        this.hash = hash;
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

        if (hash == null)
        {
            return "External DMS: " + labelString + "</br>Host: " + host + "<br/>Directory: " + directory + path;
        } else
        {
            return "External DMS: " + labelString + "</br>Host: " + host + "<br/>Directory: " + directory + path + "<br>Commit hash:" + hash;
        }
    }
}
