package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.dss.generic.server.IDataSetCommandExecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.IDataSetCommandExecutorProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.QueueingDataSetStatusUpdaterService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodesWithStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

public class ResetArchivePendingTask implements IMaintenanceTask
{

    private static final String COMMAND_EXECUTOR_BEAN = "data-set-command-executor-provider";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ResetArchivePendingTask.class);

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        operationLog.info("Task " + pluginName + " initialized.");
    }

    @Transactional
    @Override
    public void execute()
    {
        operationLog.info(ResetArchivePendingTask.class.getSimpleName() + " Started");
        // 1. Find datasets with DataSetArchivingStatus.ARCHIVE_PENDING
        IEncapsulatedOpenBISService service = ServiceProvider.getOpenBISService();
        List<SimpleDataSetInformationDTO> inArchivePendings = service.listPhysicalDataSetsByArchivingStatus(DataSetArchivingStatus.ARCHIVE_PENDING);
        operationLog.info("Found " + inArchivePendings.size() + " datasets in " + DataSetArchivingStatus.ARCHIVE_PENDING.name() + " status.");

        // 2. Filter out datasets that are not on the command queue
        IDataSetCommandExecutorProvider commandExecutorProvider =
                (IDataSetCommandExecutorProvider) ServiceProvider
                        .getApplicationContext()
                        .getBean(COMMAND_EXECUTOR_BEAN);
        IDataSetCommandExecutor commandExecutor = commandExecutorProvider.getDefaultExecutor();

        Set<String> inQueue = commandExecutor.getDataSetCodesFromCommandQueue();
        operationLog.info("Found " + inQueue.size() + " datasets in the command queue.");

        List<String> toUpdate = new ArrayList<String>();
        for (SimpleDataSetInformationDTO inArchivePending : inArchivePendings)
        {
            if (!inQueue.contains(inArchivePending.getDataSetCode()))
            {
                toUpdate.add(inArchivePending.getDataSetCode());
                operationLog.info(inArchivePending.getDataSetCode() + " not found in command queue, scheduled to update.");
            }
        }

        // 3. Update datasets status to AVAILABLE
        DataSetArchivingStatus status = DataSetArchivingStatus.AVAILABLE;
        boolean presentInArchive = false;

        operationLog.info("Going to update " + toUpdate.size() + " datasets.");
        DataSetCodesWithStatus codesWithStatus = new DataSetCodesWithStatus(toUpdate, status, presentInArchive);
        QueueingDataSetStatusUpdaterService.update(codesWithStatus);
        operationLog.info(ResetArchivePendingTask.class.getSimpleName() + " Finished");
    }

}
