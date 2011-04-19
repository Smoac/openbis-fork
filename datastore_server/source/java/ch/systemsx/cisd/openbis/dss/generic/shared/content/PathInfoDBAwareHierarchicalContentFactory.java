package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.io.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentFactory;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.IDelegatedAction;
import ch.systemsx.cisd.openbis.dss.generic.server.DatabaseBasedDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;

/**
 * The implementation of {@link IHierarchicalContentFactory} that aware of Path Info DB.
 * 
 * @author Piotr Buczek
 */
public class PathInfoDBAwareHierarchicalContentFactory extends
        DefaultFileBasedHierarchicalContentFactory
{

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PathInfoDBAwareHierarchicalContentFactory.class);

    public static IHierarchicalContentFactory create()
    {
        if (DatabaseBasedDataSetPathInfoProvider.isDataSourceDefined())
        {
            operationLog.debug("Path Info DB is properly configured");
            return new PathInfoDBAwareHierarchicalContentFactory(
                    new DatabaseBasedDataSetPathInfoProvider());
        } else
        {
            operationLog.info("Path Info DB was NOT configured. "
                    + "File system based implementation will be used.");
            return new DefaultFileBasedHierarchicalContentFactory();
        }
    }

    private final IDataSetPathInfoProvider pathInfoProvider;

    @Private
    PathInfoDBAwareHierarchicalContentFactory(IDataSetPathInfoProvider pathInfoProvider)
    {
        this.pathInfoProvider = pathInfoProvider;
    }

    @Override
    public IHierarchicalContent asHierarchicalContent(File file, IDelegatedAction onCloseAction)
    {
        final String dataSetCode = file.getName();
        DataSetPathInfo rootPathInfo = pathInfoProvider.tryGetDataSetRootPathInfo(dataSetCode);
        if (rootPathInfo != null) // exists in DB
        {
            operationLog.info("Data set " + dataSetCode + " was found in Path Info DB.");
            return new SimplePathInfoBasedHierarchicalContent(rootPathInfo, file, onCloseAction);
        } else
        {
            operationLog.info("Data set " + dataSetCode + " was NOT found in Path Info DB. "
                    + "Falling back to file system based implementation.");
            return super.asHierarchicalContent(file, onCloseAction);
        }
    }

}