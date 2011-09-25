package ch.ethz.cisd.hcscld.examples;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

import ch.ethz.cisd.hcscld.CellLevelClassificationsEnum;
import ch.ethz.cisd.hcscld.CellLevelDataFactory;
import ch.ethz.cisd.hcscld.ICellLevelClassificationDataset;
import ch.ethz.cisd.hcscld.ICellLevelClassificationWritableDataset;
import ch.ethz.cisd.hcscld.ICellLevelDataReader;
import ch.ethz.cisd.hcscld.ICellLevelDataWriter;
import ch.ethz.cisd.hcscld.WellFieldGeometry;
import ch.ethz.cisd.hcscld.WellFieldId;

/**
 * A demo program for writing and reading cell-level classification data. 
 * 
 * @author Bernd Rinn
 */
public class CellLevelClassificationDemo
{

    enum CellState
    {
        MITOTIC, APOPTOTIC, STEADY, DEAD
    }

    public static void main(String[] args)
    {
        File f = new File("classification.h5");
        f.delete();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelClassificationWritableDataset wds =
                writer.addClassificationDataset("456", new WellFieldGeometry(16, 24, 9),
                        CellState.class);
        Random rng = new Random();
        long start = System.currentTimeMillis();
        CellState[] state = new CellState[160];
        for (WellFieldId id : wds.getGeometry())
        {
            for (int i = 0; i < state.length; ++i)
            {
                state[i] = CellState.values()[rng.nextInt(CellState.values().length)];
            }
            wds.writeClassification(id, state);
        }
        writer.close();
        System.out.println(((System.currentTimeMillis() - start) / 1000.0) + " s");
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelClassificationDataset ds =
                reader.getDataSet("456").toClassificationDataset();
        for (CellLevelClassificationsEnum<CellState> cls : ds.getClassifications(CellState.class))
        {
            System.out.println(cls.getId() + ":" + Arrays.toString(cls.getData()));
        }
        System.out.println(ds.getClassification(new WellFieldId(1, 0, 0), 2));
        reader.close();
    }
}
