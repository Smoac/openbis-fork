package ch.ethz.sis.openbis.systemtests.shuffling;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import ch.ethz.sis.afsserver.server.shuffling.EagerShufflingTask;
import ch.ethz.sis.afsserver.server.shuffling.IChecksumProvider;
import ch.ethz.sis.afsserver.server.shuffling.IDataSetMover;
import ch.ethz.sis.afsserver.server.shuffling.IEncapsulatedOpenBISService;
import ch.ethz.sis.afsserver.server.shuffling.ISegmentedStoreShuffling;
import ch.ethz.sis.afsserver.server.shuffling.SegmentedStoreUtils;
import ch.ethz.sis.afsserver.server.shuffling.ServiceProvider;
import ch.ethz.sis.afsserver.server.shuffling.Share;
import ch.ethz.sis.afsserver.server.shuffling.SimpleChecksumProvider;
import ch.ethz.sis.afsserver.server.shuffling.SimpleShuffling;
import ch.systemsx.cisd.common.logging.ISimpleLogger;

public class TestShuffling implements ISegmentedStoreShuffling
{

    private final static TestDataSetMover dataSetMover = new TestDataSetMover();

    private final SimpleShuffling simpleShuffling;

    public TestShuffling(Properties properties)
    {
        simpleShuffling = new SimpleShuffling(properties, new EagerShufflingTask(properties, ServiceProvider.getOpenBISService(),
                TestShuffling.dataSetMover));
    }

    @Override public void init(final ISimpleLogger logger)
    {
        simpleShuffling.init(logger);
    }

    @Override public void shuffleDataSets(final List<Share> sourceShares, final List<Share> targetShares,
            final IEncapsulatedOpenBISService service, final IDataSetMover dataSetMover, final ISimpleLogger logger)
    {
        simpleShuffling.shuffleDataSets(sourceShares, targetShares, service, TestShuffling.dataSetMover, logger);
    }

    public static TestDataSetMover getDataSetMover()
    {
        return dataSetMover;
    }

    public static class TestDataSetMover implements IDataSetMover
    {

        private final TestChecksumProvider checksumProvider = new TestChecksumProvider();

        @Override public void moveDataSetToAnotherShare(final File dataSetDirInStore, final File share, final IChecksumProvider checksumProvider,
                final ISimpleLogger logger)
        {
            SegmentedStoreUtils.moveDataSetToAnotherShare(dataSetDirInStore, share, ServiceProvider.getOpenBISService(),
                    ServiceProvider.getLockManager(), this.checksumProvider, logger);
        }

        public TestChecksumProvider getChecksumProvider()
        {
            return checksumProvider;
        }
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

        public void setDelayByMillis(final long delayByMillis)
        {
            this.delayByMillis = delayByMillis;
        }

    }

}

