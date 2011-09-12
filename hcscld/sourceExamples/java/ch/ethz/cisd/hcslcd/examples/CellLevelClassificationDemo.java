package ch.ethz.cisd.hcslcd.examples;

import java.io.File;
import java.util.Arrays;

import ch.ethz.cisd.hcscld.CellLevelClassificationsEnum;
import ch.ethz.cisd.hcscld.CellLevelDataFactory;
import ch.ethz.cisd.hcscld.ICellLevelClassificationDataset;
import ch.ethz.cisd.hcscld.ICellLevelClassificationWritableDataset;
import ch.ethz.cisd.hcscld.ICellLevelDataReader;
import ch.ethz.cisd.hcscld.ICellLevelDataWriter;
import ch.ethz.cisd.hcscld.WellFieldGeometry;
import ch.ethz.cisd.hcscld.WellFieldId;

/**
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
        long start = System.currentTimeMillis();
        for (WellFieldId id : wds.getGeometry())
        {
            wds.writeClassification(id, new CellState[]
                { CellState.STEADY, CellState.STEADY, CellState.MITOTIC, CellState.DEAD,
                        CellState.STEADY, CellState.APOPTOTIC, CellState.STEADY, CellState.DEAD,
                        CellState.STEADY });
        }
        writer.close();
        System.out.println(((System.currentTimeMillis() - start) / 1000.0) + " s");
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelClassificationDataset ds =
                reader.getDataSet("456").tryAsClassificationDataset();
        for (CellLevelClassificationsEnum<CellState> cls : ds.getClassifications(CellState.class))
        {
            System.out.println(cls.getId() + ":" + Arrays.toString(cls.getData()));
        }
        System.out.println(ds.getClassification(new WellFieldId(1, 0, 0), 2));
        reader.close();
    }
}
