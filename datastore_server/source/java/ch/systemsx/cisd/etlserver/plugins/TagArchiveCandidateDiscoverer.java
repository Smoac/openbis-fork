package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.IArchiveCandidateDiscoverer;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectIdentifierId;

public class TagArchiveCandidateDiscoverer implements IArchiveCandidateDiscoverer
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, TagArchiveCandidateDiscoverer.class);

    private static final String TAG_LIST = "tags";

    private final List<String> tags;

    public TagArchiveCandidateDiscoverer(Properties properties)
    {
        tags = PropertyUtils.getList(properties, TAG_LIST);
        if (tags.size() == 0)
        {
            operationLog.error("TagArchiveCandidateDiscoverer is configured with no tags. Nothing will be found.");
        }
    }

    @Override
    public List<AbstractExternalData> findDatasetsForArchiving(IEncapsulatedOpenBISService openbis, ArchiverDataSetCriteria criteria)
    {
        if (tags.size() == 0)
        {
            return Collections.emptyList();
        }

        List<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
        for (String tag : tags)
        {
            result.addAll(openbis.listNotArchivedDatasetsWithMetaproject(new MetaprojectIdentifierId(tag)));
        }
        return result;
    }
}
