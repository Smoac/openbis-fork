package ch.systemsx.cisd.etlserver.plugins;

import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * An base for archiving policies respecting desired target min-max size of the archive
 * 
 * @author Sascha Fedorenko
 */
public class BaseGroupingPolicy
{

    public static final String MINIMAL_ARCHIVE_SIZE = "minimal-archive-size";

    public static final String MAXIMAL_ARCHIVE_SIZE = "maximal-archive-size";

    protected static final long DEFAULT_MINIMAL_ARCHIVE_SIZE = 0;

    protected static final long DEFAULT_MAXIMAL_ARCHIVE_SIZE = Long.MAX_VALUE;

    protected final long minArchiveSize;

    protected final long maxArchiveSize;

    private IDataSetPathInfoProvider pathInfoProvider;

    public BaseGroupingPolicy(ExtendedProperties properties)
    {
        minArchiveSize =
                PropertyUtils.getLong(properties, MINIMAL_ARCHIVE_SIZE, DEFAULT_MINIMAL_ARCHIVE_SIZE);

        maxArchiveSize =
                PropertyUtils.getLong(properties, MAXIMAL_ARCHIVE_SIZE, DEFAULT_MAXIMAL_ARCHIVE_SIZE);
    }

    protected Long patchDatasetSize(AbstractExternalData ds)
    {
        ISingleDataSetPathInfoProvider dsInfoProvider = getDatasetPathInfoProvider().tryGetSingleDataSetPathInfoProvider(ds.getCode());
        Long size = null;
        if (dsInfoProvider != null)
        {
            size = dsInfoProvider.getRootPathInfo().getSizeInBytes();
            ds.setSize(size);
        }
        return size;
    }

    private IDataSetPathInfoProvider getDatasetPathInfoProvider()
    {
        if (pathInfoProvider == null)
        {
            pathInfoProvider = ServiceProvider.getDataSetPathInfoProvider();
        }
        return pathInfoProvider;
    }

}