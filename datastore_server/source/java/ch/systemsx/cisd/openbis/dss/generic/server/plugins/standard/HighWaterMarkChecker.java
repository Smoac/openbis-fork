package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.Serializable;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.highwatermark.HostAwareFile;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher.HighwaterMarkState;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil;

/**
 * Checks if the space available is larger than specified value.
 * 
 * @author Izabela Adamczyk
 */
public class HighWaterMarkChecker implements IStatusChecker, Serializable
{
    private static final long serialVersionUID = 1L;

    private final long highWaterMark;

    private final File highWaterMarkPath;

    /**
     * Loads the high water mark value from global properties -
     * {@link HostAwareFileWithHighwaterMark#HIGHWATER_MARK_PROPERTY_KEY}.
     */
    public HighWaterMarkChecker(File path)
    {
        this(PropertyUtils.getLong(PropertyParametersUtil.loadServiceProperties(),
                HostAwareFileWithHighwaterMark.HIGHWATER_MARK_PROPERTY_KEY, -1L), path);
    }

    public HighWaterMarkChecker(long archiveHighWaterMark, File archiveHighWaterMarkPath)
    {
        this.highWaterMark = archiveHighWaterMark;
        this.highWaterMarkPath = archiveHighWaterMarkPath;
    }

    public Status check()
    {
        HighwaterMarkWatcher w = new HighwaterMarkWatcher(highWaterMark);
        HighwaterMarkState state = w.getHighwaterMarkState(new HostAwareFile(highWaterMarkPath));
        if (HighwaterMarkWatcher.isBelow(state))
        {
            String canonicalPath = highWaterMarkPath.getPath();
            String mark = HighwaterMarkWatcher.displayKilobyteValue(state.getHighwaterMark());
            String space = HighwaterMarkWatcher.displayKilobyteValue(state.getFreeSpace());
            String message =
                    String.format("Free space on '%s': %s, highwater mark: %s.", canonicalPath,
                            space, mark);
            return Status.createError(message);
        } else
        {
            return Status.OK;
        }

    }
}