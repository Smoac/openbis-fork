package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

import ch.systemsx.cisd.bds.hcs.Location;

/**
 * DTO with information about one image file
 * 
 * @author Tomasz Pylak
 */
public final class ImageFileInfo
{
    private Location wellLocationOrNull;

    private Location tileLocation;

    private String channelCode;

    private final String imageRelativePath;

    private Float timepointOrNull;

    private Float depthOrNull;

    private Integer seriesNumber;

    public ImageFileInfo(String channelCode, int tileRow, int tileColumn, String imageRelativePath)
    {
        assert channelCode != null;
        assert imageRelativePath != null;

        this.channelCode = channelCode;
        this.imageRelativePath = imageRelativePath;
        setTile(tileRow, tileColumn);
    }

    public Integer tryGetWellRow()
    {
        return wellLocationOrNull == null ? null : wellLocationOrNull.getY();
    }

    public Integer tryGetWellColumn()
    {
        return wellLocationOrNull == null ? null : wellLocationOrNull.getX();
    }

    public boolean hasWellLocation()
    {
        return wellLocationOrNull != null;
    }

    public int getTileRow()
    {
        return tileLocation.getY();
    }

    public int getTileColumn()
    {
        return tileLocation.getX();
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

    public Integer tryGetSeriesNumber()
    {
        return seriesNumber;
    }

    public void setChannelCode(String channelCode)
    {
        this.channelCode = channelCode;
    }

    // --- setters

    public void setWell(int row, int column)
    {
        this.wellLocationOrNull = Location.createLocationFromRowAndColumn(row, column);
    }

    /** @return true if well row and column could be parsed */
    public boolean setWell(String wellText)
    {
        this.wellLocationOrNull =
                Location.tryCreateLocationFromTransposedMatrixCoordinate(wellText);
        return wellLocationOrNull != null;
    }

    public void setTile(int row, int column)
    {
        this.tileLocation = Location.createLocationFromRowAndColumn(row, column);
    }

    public void setTimepoint(Float value)
    {
        this.timepointOrNull = value;
    }

    public void setDepth(Float value)
    {
        this.depthOrNull = value;
    }

    public void setSeriesNumber(Integer value)
    {
        this.seriesNumber = value;
    }

    @Override
    public String toString()
    {
        return "ImageFileInfo [well=" + wellLocationOrNull + ", tile=" + tileLocation
                + ", channel=" + channelCode + ", path=" + imageRelativePath + ", timepoint="
                + timepointOrNull + ", depth=" + depthOrNull + ", seriesNumber=" + seriesNumber
                + "]";
    }

}