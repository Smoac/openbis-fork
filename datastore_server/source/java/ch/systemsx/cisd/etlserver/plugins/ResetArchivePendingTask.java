package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.dss.generic.server.CommandQueueLister;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.QueueingDataSetStatusUpdaterService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodesWithStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

public class ResetArchivePendingTask implements IMaintenanceTask
{

    @Override
    public void setUp(String pluginName, Properties properties)
    {
    }

    @Transactional
    @Override
    public void execute()
    {
        // 1. Find datasets with DataSetArchivingStatus.ARCHIVE_PENDING
        IEncapsulatedOpenBISService service = ServiceProvider.getOpenBISService();
        List<SimpleDataSetInformationDTO> inArchivePendings = service.listPhysicalDataSetsByArchivingStatus(DataSetArchivingStatus.ARCHIVE_PENDING);

        // 2. Filter out datasets that are not on the command queue
        Set<String> inQueue = CommandQueueLister.getDataSetCodesFromCommandQueue();
        List<String> toUpdate = new ArrayList<String>();
        for (SimpleDataSetInformationDTO inArchivePending : inArchivePendings)
        {
            if (!inQueue.contains(inArchivePending.getDataSetCode()))
            {
                toUpdate.add(inArchivePending.getDataSetCode());
            }
        }

        // 3. Update datasets status to AVAILABLE
        DataSetArchivingStatus status = DataSetArchivingStatus.AVAILABLE;
        boolean presentInArchive = false;

        DataSetCodesWithStatus codesWithStatus = new DataSetCodesWithStatus(toUpdate, status, presentInArchive);
        QueueingDataSetStatusUpdaterService.update(codesWithStatus);
    }

}
