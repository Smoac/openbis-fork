package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.grouping;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

public interface TreeNode
{
    public long getCumulatedSize();

    public void addSize(long addon);

    public List<AbstractExternalData> collectSubTree();
}