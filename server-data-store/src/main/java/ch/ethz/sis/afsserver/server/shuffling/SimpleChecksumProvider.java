package ch.ethz.sis.afsserver.server.shuffling;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class SimpleChecksumProvider implements IChecksumProvider
{
    @Override public long getChecksum(final String dataSetCode, final String relativePath) throws IOException
    {
        SimpleDataSetInformationDTO dataSet = ServiceProvider.getOpenBISService().tryGetDataSet(dataSetCode);

        if (dataSet != null)
        {
            return FileUtils.checksumCRC32(new File(dataSet.getDataSetLocation(), relativePath));
        } else
        {
            return -1;
        }
    }
}
