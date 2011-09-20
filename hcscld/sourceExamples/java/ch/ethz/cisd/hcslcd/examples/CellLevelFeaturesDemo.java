package ch.ethz.cisd.hcslcd.examples;

import java.io.File;
import java.util.Arrays;

import ch.ethz.cisd.hcscld.CellLevelDataFactory;
import ch.ethz.cisd.hcscld.CellLevelFeatures;
import ch.ethz.cisd.hcscld.ICellLevelDataReader;
import ch.ethz.cisd.hcscld.ICellLevelDataWriter;
import ch.ethz.cisd.hcscld.ICellLevelFeatureDataset;
import ch.ethz.cisd.hcscld.ICellLevelFeatureWritableDataset;
import ch.ethz.cisd.hcscld.IFeatureGroup;
import ch.ethz.cisd.hcscld.WellFieldGeometry;
import ch.ethz.cisd.hcscld.WellFieldId;

/**
 * A demo program for writing and reading cell-level featuer data.
 * 
 * @author Bernd Rinn
 */
public class CellLevelFeaturesDemo
{

    public static void main(String[] args)
    {
        File f = new File("features.h5");
        f.delete();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelFeatureWritableDataset wds =
                writer.addFeatureDataset("123", new WellFieldGeometry(16, 24, 9));
        IFeatureGroup fg =
                wds.addFeatureGroup(
                        "stdfeatures",
                        wds.createFeatures()
                                .addInt32Feature("a")
                                .addFloatSinglePrecisionFeature("b")
                                .addEnumFeature("c", "CellState",
                                        Arrays.asList("INFECTED", "HEALTHY", "UNCLEAR")));
        long start = System.currentTimeMillis();
        for (WellFieldId id : wds.getGeometry())
        {
            wds.writeFeatureGroup(fg, id, new Object[][]
                {
                    { 1, 2, "HEALTHY" },
                    { 4, 5, "UNCLEAR" },
                    { 7, 8, "INFECTED" },
                    { 10, 11, "HEALTHY" },
                    { 13, 14, "HEALTHY" },
                    { 16, 17, "HEALTHY" },
                    { 19, 20, "HEALTHY" },
                    { 22, 23, "HEALTHY" },
                    { 25, 26, "HEALTHY" },
                    { 28, 29, "HEALTHY" } });
        }
        writer.close();
        System.out.println(((System.currentTimeMillis() - start) / 1000.0) + " s");
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").tryAsFeatureDataset();
        final IFeatureGroup fg2 = ds.getFeatureGroup("stdfeatures");
        for (CellLevelFeatures features : ds.getFeatures(fg2))
        {
            System.out.println(features.getFeatureGroup().getMemberNames() + ":"
                    + features.getWellFieldId() + ":" + Arrays.toString(features.getData()[0]));
        }
        System.out.println(Arrays.toString(ds.getFeatures(fg2, new WellFieldId(1, 0, 0), 2)));
        reader.close();
    }
}
