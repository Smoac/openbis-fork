package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

public class FileSystemContentCopy implements IContentCopy
{
    private static final long serialVersionUID = 1L;

    private String label;

    private String host;

    private String directory;

    private String path;

    private String hash;

    public FileSystemContentCopy()
    {
    }

    public FileSystemContentCopy(String label, String host, String directory, String path, String hash)
    {
        this.label = label;
        this.host = host;
        this.directory = directory;
        this.path = path;
        this.hash = hash;
    }

    @Override
    public boolean isHyperLinkable()
    {
        return false;
    }

    @Override
    public String getLocation()
    {
        if (hash == null)
        {
            return "Host: " + host + "<br/>Directory: " + directory + path;
        } else
        {
            return "Host: " + host + "<br/>Directory: " + directory + path + "<br>Commit hash:" + hash;
        }
    }

    @Override
    public String getLabel()
    {
        return label;
    }
}
