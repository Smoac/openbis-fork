package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.dss.etl.dto.RelativeImageFile;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ThumbnailsStorageFormat.FileFormat;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ToStringUtil;

/**
 * Stores the mapping between image and their thumbnails paths. Thread-safe class.
 * 
 * @author Tomasz Pylak
 */
public class ThumbnailsInfo
{
    public static class PhysicalDatasetInfo
    {
        private final String rootPath;

        private int thumbnailsWidth;

        private int thumbnailsHeight;

        // TODO make use of color depth
        @SuppressWarnings("unused")
        private Integer colorDepth;

        private FileFormat fileType;

        private Map<String, String> transformations;

        public PhysicalDatasetInfo(String rootPath, FileFormat fileType,
                Map<String, String> transformations)
        {
            this.rootPath = rootPath;
            this.fileType = fileType;
            this.transformations = transformations;
        }
    }

    private final Map<RelativeImageFile, String> imageToThumbnailPathMap;

    private final Map<String, PhysicalDatasetInfo> datasetInfos;

    public ThumbnailsInfo()
    {
        this.imageToThumbnailPathMap = new HashMap<RelativeImageFile, String>();
        this.datasetInfos = new HashMap<String, ThumbnailsInfo.PhysicalDatasetInfo>();
    }

    public void putDataSet(String permId, String rootPath, FileFormat fileFormat,
            Map<String, String> transformations)
    {
        datasetInfos.put(permId, new PhysicalDatasetInfo(rootPath, fileFormat, transformations));
    }

    /**
     * Saves the path to the thumbnail for the specified image. The thumbnail path is relative and
     * starts with the name of the thumbnail file.
     */
    public synchronized void saveThumbnailPath(String permId, RelativeImageFile image,
            String thumbnailRelativePath, int width, int height)
    {
        imageToThumbnailPathMap.put(image, thumbnailRelativePath);

        PhysicalDatasetInfo datasetInfo = datasetInfos.get(permId);
        if (datasetInfo != null)
        {
            datasetInfo.thumbnailsWidth = Math.max(datasetInfo.thumbnailsWidth, width);
            datasetInfo.thumbnailsHeight = Math.max(datasetInfo.thumbnailsHeight, height);
        }
    }

    public synchronized String getThumbnailPath(RelativeImageFile image)
    {
        return imageToThumbnailPathMap.get(image);
    }

    public Set<String> getThumbnailPhysicalDatasetsPermIds()
    {
        return datasetInfos.keySet();
    }

    public Size tryGetDimension(String permId)
    {
        PhysicalDatasetInfo datasetInfo = datasetInfos.get(permId);
        if (datasetInfo != null)
        {
            if (datasetInfo.thumbnailsWidth > 0 && datasetInfo.thumbnailsHeight > 0)
            {
                return new Size(datasetInfo.thumbnailsWidth, datasetInfo.thumbnailsHeight);
            }
        }
        return null;
    }

    public FileFormat getFileType(String permId)
    {
        PhysicalDatasetInfo datasetInfo = datasetInfos.get(permId);
        if (datasetInfo != null)
        {
            return datasetInfo.fileType;
        }
        return null;
    }

    public Map<String, String> getTransformations(String permId)
    {
        PhysicalDatasetInfo datasetInfo = datasetInfos.get(permId);
        return datasetInfo != null ? datasetInfo.transformations : null;
    }

    @Override
    public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        ToStringUtil.appendNameAndObject(buffer, "number of thumbnails",
                imageToThumbnailPathMap.size());
        ToStringUtil.appendNameAndObject(buffer, "dataset perm ids: ", printPermIds());
        return buffer.toString();
    }

    private String printPermIds()
    {
        StringBuilder sb = new StringBuilder("[");
        boolean notFirst = false;
        for (String permId : datasetInfos.keySet())
        {
            if (notFirst)
            {
                sb.append(";");
                notFirst = true;
            }
            sb.append(" ").append(permId);
        }
        return sb.append(" ]").toString();
    }

    public String getRootPath(String permId)
    {
        return datasetInfos.get(permId).rootPath;
    }
}