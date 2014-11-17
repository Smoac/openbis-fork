package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * An archiving policy that selects a subset from archiving candidates by grouping them on space and "packing" them into a min-max size batches
 * 
 * @author Sascha Fedorenko
 */
public class BySpacePolicy extends BaseGroupingPolicy implements IAutoArchiverPolicy
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, BySpacePolicy.class);

    public BySpacePolicy(ExtendedProperties properties)
    {
        super(properties);
    }

    @Override
    public List<AbstractExternalData> filter(List<AbstractExternalData> dataSets)
    {
        return new ArrayList<AbstractExternalData>();
    }
}
