package ch.systemsx.cisd.etlserver.plugins.grouping;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

public interface TreeNode
{
    public long getCumulatedSize();

    public void addSize(long addon);

    public List<AbstractExternalData> collectSubTree();
}