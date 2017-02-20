package ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.create;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.annotation.TechPreview;

@JsonObject("dss.dto.datasetfile.DataSetFileCreation")
@TechPreview
public class DataSetFileCreation implements ICreation
{

    private static final long serialVersionUID = 1L;

    private String path;

    private boolean isDirectory;

    private Long fileLength;

    private Integer checksumCRC32;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public boolean isDirectory()
    {
        return isDirectory;
    }

    public void setDirectory(boolean isDirectory)
    {
        this.isDirectory = isDirectory;
    }

    public Long getFileLength()
    {
        return fileLength;
    }

    public void setFileLength(long fileLength)
    {
        this.fileLength = fileLength;
    }

    public Integer getChecksumCRC32()
    {
        return checksumCRC32;
    }

    public void setChecksumCRC32(int checksumCRC32)
    {
        this.checksumCRC32 = checksumCRC32;
    }
}
