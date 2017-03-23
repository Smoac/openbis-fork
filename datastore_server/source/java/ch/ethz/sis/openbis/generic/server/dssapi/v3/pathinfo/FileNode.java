package ch.ethz.sis.openbis.generic.server.dssapi.v3.pathinfo;

class FileNode implements DataSetContentNode
{
    private final String fullPath;

    private final long length;

    private final Integer checksum;

    public FileNode(String parentPath, String name, long length, Integer checksum)
    {
        if (parentPath == null)
        {
            this.fullPath = name;
        } else
        {
            this.fullPath = parentPath + "/" + name;
        }
        this.length = length;
        this.checksum = checksum;
    }

    @Override
    public long getLength()
    {
        return length;
    }

    @Override
    public Integer getChecksum()
    {
        return checksum;
    }

    @Override
    public String getFullPath()
    {
        return this.fullPath;
    }

    @Override
    public boolean isDirectory()
    {
        return false;
    }

}
