package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.common.io.FileBasedContentNode;
import ch.systemsx.cisd.openbis.dss.etl.Utils;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IImageGenerationAlgorithm;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

public class MaximumIntensityProjectionGenerationAlgorithm implements IImageGenerationAlgorithm, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient BufferedImage result = null;

    private String dataSetTypeCode;

    private String filename;

    private int width;

    private int height;

    public MaximumIntensityProjectionGenerationAlgorithm(String dataSetTypeCode) {
        this(dataSetTypeCode, 0, 0);
    }
    public MaximumIntensityProjectionGenerationAlgorithm(String dataSetTypeCode, String filename) {
        this(dataSetTypeCode, 0, 0, filename);
    }
    
    public MaximumIntensityProjectionGenerationAlgorithm(String dataSetTypeCode, int width, int height) {
        this(dataSetTypeCode, width, height, "maximum_intensity_projection");
    }

    public MaximumIntensityProjectionGenerationAlgorithm(String dataSetTypeCode,  int width, int height, String filename) {
        this.dataSetTypeCode = dataSetTypeCode;
        this.width = width;
        this.height = height;
        this.filename = filename;
    }

    @Override
    public String getDataSetTypeCode()
    {
        return dataSetTypeCode;
    }

    @Override
    public List<BufferedImage> generateImages(ImageDataSetInformation information, ImageDataSetStructure structure)
    {
        ImageLibraryInfo library = structure.getImageStorageConfiguraton().tryGetImageLibrary();
        List<ImageFileInfo> images = structure.getImages();
        int maxIntensity = 0;
        for (ImageFileInfo image: images) {
            String imagePath = image.getImageRelativePath();
            if (image.tryGetTimepoint() == null || image.tryGetTimepoint() != 0) {
                continue;
            }
            BufferedImage imageData = Utils.loadUnchangedImage(new FileBasedContentNode(new File(
                    information.getIncomingDirectory(), imagePath)), image.tryGetUniqueStringIdentifier(), library);
            maxIntensity = addImage(imageData);
        }
        
        if (result == null) {
            return Collections.emptyList();
        } else {
            for (int y=0; y<result.getHeight(); y++) {
                for (int x=0; x<result.getWidth(); x++) {
                    result.setRGB(x, y, adjust(result.getRGB(x, y), maxIntensity));
                }
            }
            if (width > 0 && height > 0) {
                BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                AffineTransform at = new AffineTransform();
                at.scale((double)width / (double)result.getWidth(), (double)height / (double)result.getHeight());
                AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                result = scaleOp.filter(result, scaled);                
            }
            
            return Collections.singletonList(result);
        }

    }

    private int adjust(int rgb, int maxIntensity)
    {
        if (maxIntensity > 255) {
            maxIntensity = 255;
        }
        int r = new Double( ((double)getRed(rgb)) / ((double)maxIntensity) * 255).intValue();
        int g = new Double( ((double)getGreen(rgb)) / ((double)maxIntensity) * 255).intValue();
        int b = new Double( ((double)getBlue(rgb)) / ((double)maxIntensity) * 255).intValue();
        
        return (r << 16) + (g << 8) + b;
        
    }


    private int addImage(BufferedImage image)
    {
        if (result == null) {
            result = new BufferedImage(image.getWidth(),image.getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int y=0; y<image.getHeight(); y++) {
                for (int x=0; x<image.getWidth(); x++) {
                    image.setRGB(x,y, 0);
                }
            }
        }
        int maxIntensity = 0;
        for (int y=0; y<image.getHeight(); y++) {
            for (int x=0; x<image.getWidth(); x++) {
                int rgb1 = result.getRGB(x,y);
                int rgb2 = image.getRGB(x,y);
                
                int intensity1 = intensity(rgb1);
                int intensity2 = intensity(rgb2);

                if (intensity1 > maxIntensity) {
                    maxIntensity = intensity1;
                }

                if (intensity2 > maxIntensity) {
                    maxIntensity = intensity2;
                }
                
                result.setRGB(x,y, intensity1 > intensity2 ? rgb1 : rgb2);
            }
        }
        return maxIntensity;
    }

    private int intensity(int rgb)
    {
        double r = getRed(rgb);
        double g = getGreen(rgb);
        double b = getBlue(rgb);
        return new Double(Math.sqrt(r*r + g*g + b*b)).intValue();
    }


    private int getBlue(int rgb)
    {
        return rgb & 0xff;
    }


    private int getGreen(int rgb)
    {
        return (rgb >> 8) & 0xff;
    }


    private int getRed(int rgb)
    {
        return (rgb >> 16) & 0xff;
    }

    @Override
    public String getImageFileName(int index)
    {
        return filename;
    }

}
