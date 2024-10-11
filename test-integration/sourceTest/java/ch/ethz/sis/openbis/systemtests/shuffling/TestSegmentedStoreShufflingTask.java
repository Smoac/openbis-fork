package ch.ethz.sis.openbis.systemtests.shuffling;

import java.io.IOException;
import java.util.Properties;

import ch.ethz.sis.afsserver.server.maintenance.IMaintenanceTask;
import ch.ethz.sis.afsserver.server.shuffling.DataSetMover;
import ch.ethz.sis.afsserver.server.shuffling.IChecksumProvider;
import ch.ethz.sis.afsserver.server.shuffling.IncomingShareIdProvider;
import ch.ethz.sis.afsserver.server.shuffling.SegmentedStoreShufflingTask;
import ch.ethz.sis.afsserver.server.shuffling.ServiceProvider;
import ch.ethz.sis.afsserver.server.shuffling.SimpleChecksumProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;

public class TestSegmentedStoreShufflingTask implements IMaintenanceTask
{

    private static String pluginName;

    private static Properties properties;

    @Override public void setUp(final String pluginName, final Properties properties)
    {
        TestSegmentedStoreShufflingTask.pluginName = pluginName;
        TestSegmentedStoreShufflingTask.properties = properties;
    }

    @Override public void execute()
    {
        // do not run periodically - only on request with executeOnce
    }

    public static void executeOnce(IChecksumProvider checksumProvider)
    {
        SegmentedStoreShufflingTask segmentedStoreShufflingTask =
                new SegmentedStoreShufflingTask(IncomingShareIdProvider.getIdsOfIncomingShares(), ServiceProvider.getOpenBISService(),
                        new SimpleFreeSpaceProvider(), new DataSetMover(ServiceProvider.getOpenBISService(), ServiceProvider.getLockManager()),
                        checksumProvider, ServiceProvider.getConfigProvider());
        segmentedStoreShufflingTask.setUp(pluginName, properties);
        segmentedStoreShufflingTask.execute();
    }

    public static class TestChecksumProvider implements IChecksumProvider
    {

        private boolean returnIncorrectChecksum;

        private RuntimeException failWithException;

        private Long delayByMillis;

        private final IChecksumProvider simpleChecksumProvider = new SimpleChecksumProvider();

        @Override public long getChecksum(final String dataSetCode, final String relativePath) throws IOException
        {
            if (returnIncorrectChecksum)
            {
                return -1;
            }

            if (failWithException != null)
            {
                throw failWithException;
            }

            if (delayByMillis != null)
            {
                try
                {
                    Thread.sleep(delayByMillis);
                } catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }

            return this.simpleChecksumProvider.getChecksum(dataSetCode, relativePath);
        }

        public void setReturnIncorrectChecksum(final boolean returnIncorrectChecksum)
        {
            this.returnIncorrectChecksum = returnIncorrectChecksum;
        }

        public void setFailWithException(final RuntimeException failWithException)
        {
            this.failWithException = failWithException;
        }

        public void setDelayByMillis(final Long delayByMillis)
        {
            this.delayByMillis = delayByMillis;
        }

    }

}
