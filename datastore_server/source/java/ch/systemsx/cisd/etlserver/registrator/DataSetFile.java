package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;

/**
 * Wrapper for two copies of incoming dataset file. One is original file from the dropbox, second is
 * the prestaging copy;
 * 
 * @author jakubs
 */
public class DataSetFile
{
    private final File originalIncoming;

    private final File prestagingCopy;

    /**
     * Creates the dataset file with original incoming and prestaging copy
     */
    public DataSetFile(File originalIncoming, File prestagingCopy)
    {
        this.originalIncoming = originalIncoming;
        this.prestagingCopy = prestagingCopy;
    }

    /**
     * Creates the dataset file without prestaging copy.
     */
    public DataSetFile(File incoming)
    {
        this.originalIncoming = this.prestagingCopy = incoming;
    }
    
    /**
     * This file should be used only when it is really important to do something on the original
     * File.
     * 
     * @returns the original incoming dataset.
     */
    public File getOriginalIncoming()
    {
        return originalIncoming;
    }

    /**
     * This file should be used for most of the processing.
     * 
     * @returns The prestaging copy. If not available returns the original incoming file.
     */
    public File getPrestagingCopy()
    {
        return (prestagingCopy != null) ? prestagingCopy : originalIncoming;
    }

}
