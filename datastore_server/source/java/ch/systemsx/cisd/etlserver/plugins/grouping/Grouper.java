package ch.systemsx.cisd.etlserver.plugins.grouping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.etlserver.plugins.grouping.TreeNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

public class Grouper<TKey, TVal extends TreeNode> extends HashMap<TKey, TVal> implements TreeNode
{
    private static final long serialVersionUID = 7296635809776410306L;

    private long size = 0;

    private Class<?> clz;

    public Grouper(Class<?> clz)
    {
        this.clz = clz;
    }

    @SuppressWarnings("unchecked")
    public TVal sureGet(TKey key)
    {
        TVal result = get(key);

        if (result == null)
        {
            try
            {
                put(key, result = (TVal) clz.newInstance());
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    @Override
    public long getCumulatedSize()
    {
        return size;
    }

    @Override
    public void addSize(long addon)
    {
        size += addon;
    }

    @Override
    public List<AbstractExternalData> collectSubTree()
    {
        ArrayList<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
        for (TVal sub : values())
        {
            result.addAll(sub.collectSubTree());
        }

        return result;
    }
}