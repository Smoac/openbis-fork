package ch.systemsx.cisd.openbis.common.io.hierarchical_content.api;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalContentProxy implements IHierarchicalContent
{
    private final IHierarchicalContent hierarchicalContent;

    private final List<IHierarchicalContentExecuteOnAccess> executeOnAccessList = new ArrayList<IHierarchicalContentExecuteOnAccess>();

    public HierarchicalContentProxy(IHierarchicalContent hierarchicalContent)
    {
        super();
        this.hierarchicalContent = hierarchicalContent;
    }

    public void addExecuteOnAccessMethod(IHierarchicalContentExecuteOnAccess executeOnAccessMethod)
    {
        this.executeOnAccessList.add(executeOnAccessMethod);
    }

    private void executeOnAccessMethods()
    {
        for (IHierarchicalContentExecuteOnAccess executeOnAccess : executeOnAccessList)
        {
            executeOnAccess.execute();
        }
    }

    @Override
    public IHierarchicalContentNode getRootNode()
    {
        // executeOnAccessMethods();
        return hierarchicalContent.getRootNode();
    }

    @Override
    public IHierarchicalContentNode getNode(String relativePath) throws IllegalArgumentException
    {
        executeOnAccessMethods();
        return hierarchicalContent.getNode(relativePath);
    }

    @Override
    public IHierarchicalContentNode tryGetNode(String relativePath)
    {
        executeOnAccessMethods();
        return hierarchicalContent.tryGetNode(relativePath);
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String relativePathPattern)
    {
        executeOnAccessMethods();
        return hierarchicalContent.listMatchingNodes(relativePathPattern);
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String startingPath, String fileNamePattern)
    {
        executeOnAccessMethods();
        return hierarchicalContent.listMatchingNodes(startingPath, fileNamePattern);
    }

    @Override
    public void close()
    {
        // executeOnAccessMethods();
        hierarchicalContent.close();
    }
}