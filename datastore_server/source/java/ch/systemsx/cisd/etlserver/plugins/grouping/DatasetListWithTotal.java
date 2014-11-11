package ch.systemsx.cisd.etlserver.plugins.grouping;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

public class DatasetListWithTotal extends ArrayList<AbstractExternalData> implements TreeNode
{
    private static final long serialVersionUID = 7158139354538463051L;

    private long dataSize = 0;

    @Override
    public boolean add(AbstractExternalData e)
    {
        addSize(e.getSize());
        return super.add(e);
    }

    @Override
    public long getCumulatedSize()
    {
        return dataSize;
    }

    @Override
    public void addSize(long addon)
    {
        this.dataSize += addon;
    }

    @Override
    public List<AbstractExternalData> collectSubTree()
    {
        return this;
    }
}