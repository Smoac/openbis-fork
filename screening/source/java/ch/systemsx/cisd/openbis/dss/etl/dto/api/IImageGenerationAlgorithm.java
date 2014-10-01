package ch.systemsx.cisd.openbis.dss.etl.dto.api;

import java.awt.image.BufferedImage;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;

public interface IImageGenerationAlgorithm
{
    public List<BufferedImage> generateImages(ImageDataSetInformation information, ImageDataSetStructure structure);
    public String getDataSetTypeCode();
    public String getImageFileName(int index);
}
