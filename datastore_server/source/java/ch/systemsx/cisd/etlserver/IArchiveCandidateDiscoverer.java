package ch.systemsx.cisd.etlserver;

import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;

/**
 * Finds data sets that are possible candidates for archiving
 * 
 * @author Sascha Fedorenko
 */
public interface IArchiveCandidateDiscoverer
{
    /**
     * Initialize the discoverer with specific properties
     * 
     * @param properties
     */
    void initialize(Properties properties);

    /**
     * Return a list of data sets that can be scheduled for archiving. This will be called periodically so there's no need to return everything in one
     * list. First best subset is sufficient, make sure though that the older data is returned first.
     * 
     * @param openbis an interface to search data sets with
     * @param criteria general time and type criteria to start with
     * @return list of data sets that the auto archiver can process
     */
    List<AbstractExternalData> findDatasetsForArchiving(IEncapsulatedOpenBISService openbis, ArchiverDataSetCriteria criteria);
}
