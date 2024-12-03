package ch.ethz.sis.openbis.generic.imagingapi.v3.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonObject("imaging.dto.ImagingDataSetExportConfig")
public class ImagingDataSetExportConfig implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String archiveFormat;

    @JsonProperty
    private String imageFormat;

    @JsonProperty
    private String resolution;

    @JsonProperty
    private List<ImagingExportIncludeOptions> include = new ArrayList<>();


    @JsonIgnore
    public String getArchiveFormat()
    {
        return archiveFormat;
    }

    public void setArchiveFormat(String archiveFormat)
    {
        this.archiveFormat = archiveFormat;
    }

    @JsonIgnore
    public String getImageFormat()
    {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat)
    {
        this.imageFormat = imageFormat;
    }

    @JsonIgnore
    public String getResolution()
    {
        return resolution;
    }

    public void setResolution(String resolution)
    {
        this.resolution = resolution;
    }

    @JsonIgnore
    public List<ImagingExportIncludeOptions> getInclude()
    {
        return include;
    }

    public void setInclude(List<ImagingExportIncludeOptions> include)
    {
        this.include = include;
    }

}
