package ch.ethz.sis.hcscld.examples;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

import ch.ethz.sis.hcscld.CellLevelClassificationsEnum;
import ch.ethz.sis.hcscld.CellLevelDataFactory;
import ch.ethz.sis.hcscld.ICellLevelClassificationDataset;
import ch.ethz.sis.hcscld.ICellLevelClassificationWritableDataset;
import ch.ethz.sis.hcscld.ICellLevelDataReader;
import ch.ethz.sis.hcscld.ICellLevelDataWriter;
import ch.ethz.sis.hcscld.ImageId;
import ch.ethz.sis.hcscld.ImageQuantityStructure;
import ch.ethz.sis.hcscld.ObjectNamespace;

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
        File f = new File("classification.cld");
        f.delete();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelClassificationWritableDataset wds =
                writer.addClassificationDataset("456", new ImageQuantityStructure(16, 24, 9),
                        CellState.class);
        ObjectNamespace ns = wds.addObjectNamespace("main");
        Random rng = new Random();
        long start = System.currentTimeMillis();
        CellState[] state = new CellState[160];
        for (ImageId id : wds.getImageQuantityStructure())
        {
            for (int i = 0; i < state.length; ++i)
            {
                state[i] = CellState.values()[rng.nextInt(CellState.values().length)];
            }
            wds.writeClassification(id, ns, state);
        }
        writer.close();
        System.out.println(((System.currentTimeMillis() - start) / 1000.0) + " s");
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelClassificationDataset ds =
                reader.getDataSet("456").toClassificationDataset();
        for (CellLevelClassificationsEnum<CellState> cls : ds.getClassifications(CellState.class, ns))
        {
            System.out.println(cls.getId() + ":" + Arrays.toString(cls.getData()));
        }
        System.out.println(ds.getClassification(new ImageId(1, 0, 0), ns, 2));
        reader.close();
    }
}
