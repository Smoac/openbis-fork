package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.etl.dto.RelativeImageFile;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ToStringUtil;

/**
 * Stores the mapping between image and their thumbnails paths. Thread-safe class.
 * 
 * @author Tomasz Pylak
 */
public class ThumbnailFilePaths
{
    private final Map<RelativeImageFile, String> imageToThumbnailPathMap;

    private final String thumbnailPhysicalDatasetPermId;

    public ThumbnailFilePaths(String thumbnailPhysicalDatasetPermId)
    {
        assert thumbnailPhysicalDatasetPermId != null : "thumbnailPhysicalDatasetPermId is null";

        this.thumbnailPhysicalDatasetPermId = thumbnailPhysicalDatasetPermId;
        this.imageToThumbnailPathMap = new HashMap<RelativeImageFile, String>();
    }

    /**
     * Saves the path to the thumbnail for the specified image. The thumbnail path is relative and
     * starts with the name of the thumbnail file.
     */
    public synchronized void saveThumbnailPath(RelativeImageFile image, String thumbnailRelativePath)
    {
        imageToThumbnailPathMap.put(image, thumbnailRelativePath);
    }

    public synchronized String getThumbnailPath(RelativeImageFile image)
    {
        return imageToThumbnailPathMap.get(image);
    }

    public String getThumbnailPhysicalDatasetPermId()
    {
        return thumbnailPhysicalDatasetPermId;
    }

    @Override
    public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        ToStringUtil.appendNameAndObject(buffer, "number of thumbnails",
                imageToThumbnailPathMap.size());
        ToStringUtil.appendNameAndObject(buffer, "dataset perm id", thumbnailPhysicalDatasetPermId);
        return buffer.toString();
    }
}