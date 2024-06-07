import java.util.List;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ProprietaryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.RelativeLocationLocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;

public class Test
{
    public static void main(String[] args)
    {
        String experimentPermId = null;
        String samplePermId = "20240605161202283-13";

        PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
        physicalCreation.setShareId("1");
        physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("PROPRIETARY"));
        physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
        physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());
        physicalCreation.setH5arFolders(false);
        physicalCreation.setH5Folders(false);

        DataSetCreation creation = new DataSetCreation();
        creation.setAfsData(true);
        creation.setDataStoreId(new DataStorePermId("AFS"));
        creation.setDataSetKind(DataSetKind.PHYSICAL);
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setPhysicalData(physicalCreation);

        if (experimentPermId != null)
        {
            creation.setCode(experimentPermId);
            creation.setExperimentId(new ExperimentPermId(experimentPermId));
            physicalCreation.setLocation("test2");
        } else if (samplePermId != null)
        {
            creation.setCode(samplePermId);
            creation.setSampleId(new SamplePermId(samplePermId));
            physicalCreation.setLocation("test2");
        }

        OpenBIS openBIS = new OpenBIS("http://localhost:8888");
        openBIS.login("admin", "password");

        final List<DataSetPermId> permIds = openBIS.createDataSetsAS(List.of(creation));

        System.out.println(permIds);
    }
}
