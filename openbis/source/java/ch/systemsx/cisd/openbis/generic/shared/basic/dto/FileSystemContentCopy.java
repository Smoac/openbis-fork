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

	private String repositoryId;

    public FileSystemContentCopy()
    {
    }

    public FileSystemContentCopy(String code, String label, String host, String directory, String path, String hash, String repositoryId)
    {
        this.code = code;
        this.label = label;
        this.host = host;
        this.directory = directory;
        this.path = path;
        this.hash = hash;
        this.repositoryId = repositoryId;
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

        String location = "External DMS: " + labelString + "</br>Host: " + host + "<br/>Directory: " + directory + path;
        if (hash != null)
        {
            location += "<br>Commit hash: " + hash;
        }
        if (repositoryId != null)
        {
            location += "<br>Repository id: " + repositoryId;
        }
        return location;
    }

    @Override
    public String getExternalDMSCode()
    {
        return this.code;
    }

    @Override
    public String getExternalDMSLabel()
    {
        return this.label;
    }

    @Override
    public String getExternalDMSAddress()
    {
        return this.host;
    }

    @Override
    public String getPath()
    {
        return this.path;
    }

    @Override
    public String getCommitHash()
    {
        return this.hash;
    }

	@Override
	public String getRespitoryId() {
		return repositoryId;
	}

    @Override
    public String getExternalCode()
    {
        return null;
    }

}
