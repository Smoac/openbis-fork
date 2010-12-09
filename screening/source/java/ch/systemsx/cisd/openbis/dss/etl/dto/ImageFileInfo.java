package ch.systemsx.cisd.openbis.dss.etl.dto;

import ch.systemsx.cisd.bds.hcs.Location;

/**
 * DTO with information about one image file
 * 
 * @author Tomasz Pylak
 */
public final class ImageFileInfo
{
    private final Location wellLocationOrNull;

    private final Location tileLocation;

    private String channelCode;

    private final String imageRelativePath;

    private final Float timepointOrNull;

    private final Float depthOrNull;

    public ImageFileInfo(Location wellLocationOrNull, String channelCode, Location tileLocation,
            String imageRelativePath, Float timepointOrNull, Float depthOrNull)
    {
        assert channelCode != null;
        assert tileLocation != null;
        assert imageRelativePath != null;

        this.wellLocationOrNull = wellLocationOrNull;
        this.channelCode = channelCode;
        this.tileLocation = tileLocation;
        this.imageRelativePath = imageRelativePath;
        this.timepointOrNull = timepointOrNull;
        this.depthOrNull = depthOrNull;
    }

    public Location tryGetWellLocation()
    {
        return wellLocationOrNull;
    }

    public Location getTileLocation()
    {
        return tileLocation;
    }

    public String getChannelCode()
    {
        return channelCode;
    }

    public String getImageRelativePath()
    {
        return imageRelativePath;
    }

    public Float tryGetTimepoint()
    {
        return timepointOrNull;
    }

    public Float tryGetDepth()
    {
        return depthOrNull;
    }

    public void setChannelCode(String channelCode)
    {
        this.channelCode = channelCode;
    }

    @Override
    public String toString()
    {
        return "ImageFileInfo [well=" + wellLocationOrNull + ", tile=" + tileLocation
                + ", channel=" + channelCode + ", path=" + imageRelativePath + ", timepoint="
                + timepointOrNull + ", depth=" + depthOrNull + "]";
    }

}