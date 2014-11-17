package ch.systemsx.cisd.etlserver.plugins.grouping;

import java.util.ArrayList;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * Storage for grouped archival candidates
 * 
 * @author Sascha Fedorenko
 */
public class DatasetListWithTotal extends ArrayList<AbstractExternalData> implements Comparable<DatasetListWithTotal>
{
    private static final long serialVersionUID = 7158139354538463051L;

    private long dataSize = 0;

    @Override
    public boolean add(AbstractExternalData e)
    {
        addSize(e.getSize());
        return super.add(e);
    }

    public long getCumulatedSize()
    {
        return dataSize;
    }

    public void addSize(long addon)
    {
        this.dataSize += addon;
    }

    @Override
    public int compareTo(DatasetListWithTotal arg0)
    {
        return Long.signum(dataSize - arg0.dataSize);
    }
}