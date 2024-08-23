package ch.ethz.sis.afsserver.server.shuffling;

import java.io.IOException;

public class SimpleChecksumProvider implements IChecksumProvider
{
    @Override public long getChecksum(final String dataSetCode, final String relativePath) throws IOException
    {
        // TODO
        return 0;
    }
}
