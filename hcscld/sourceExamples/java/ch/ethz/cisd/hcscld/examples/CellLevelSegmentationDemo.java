package ch.ethz.cisd.hcscld.examples;

import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.sun.media.jai.widget.DisplayJAI;

import ch.ethz.cisd.hcscld.CellLevelDataFactory;
import ch.ethz.cisd.hcscld.ICellLevelDataReader;
import ch.ethz.cisd.hcscld.ICellLevelDataWriter;
import ch.ethz.cisd.hcscld.ICellLevelSegmentationDataset;
import ch.ethz.cisd.hcscld.ICellLevelSegmentationWritableDataset;
import ch.ethz.cisd.hcscld.ImageGeometry;
import ch.ethz.cisd.hcscld.SegmentationImageUtilities;
import ch.ethz.cisd.hcscld.SegmentedObject;
import ch.ethz.cisd.hcscld.ImageQuantityStructure;
import ch.ethz.cisd.hcscld.ImageId;

/**
 * A demo program for writing and reading cell-level segmentation data.
 * 
 * @author Bernd Rinn
 */
public class CellLevelSegmentationDemo
{

    private static void showImage(final RenderedImage image, final String name)
    {
        // Requires the Java Advanced Imaging Library (JAI)
         final Frame frame = new Frame("Image '" + name + "'");
         final DisplayJAI panel = new DisplayJAI(image);
         frame.add(panel);
         frame.setLocationByPlatform(true);
        
         frame.pack();
         frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException
    {
        final BufferedImage image =
                ImageIO.read(new File("bDZ21-1K_wP24_s9_z1_t1_cCy5_u001_pSC_Cells.png"));
        SegmentedObject[] cells = SegmentationImageUtilities.getSegmentedObjects(image);
        File f = new File("segmentation.cld");
        f.delete();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        long start = System.currentTimeMillis();
        ICellLevelSegmentationWritableDataset wds =
                writer.addSegmentationDataset("789", "cell", new ImageQuantityStructure(16, 24, 9),
                        new ImageGeometry(image.getWidth(), image.getHeight()), true);
        wds.writeImageSegmentation(new ImageId(15, 23, 8), Arrays.asList(cells));
        writer.close();
        System.out.println(((System.currentTimeMillis() - start) / 1000.0) + " s");
        start = System.currentTimeMillis();
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelSegmentationDataset ds =
                reader.getDataSet("789").toSegmentationDataset();
        ImageGeometry imageGeometry = ds.getImageGeometry();
        System.out.println("Image Geometry: " + imageGeometry);
        String segmentedObjectTypeName = ds.getSegmentedObjectTypeName();
        System.out.println("Segmented Object Type: " + segmentedObjectTypeName);
        SegmentedObject[] objects = ds.getObjects(new ImageId(15, 23, 8), true);
        reader.close();
        final BufferedImage image2 =
                SegmentationImageUtilities.createBinarySegmentationEdgesImage(imageGeometry,
                        objects);
        System.out.println(((System.currentTimeMillis() - start) / 1000.0) + " s");
        showImage(image2, "All " + segmentedObjectTypeName + "s");
    }
}
