package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.List;

import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.DtoConverters;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;

/**
 * A {@link IOriginalDataProvider} implementation for listing matching entities.
 * 
 * @author Christian Ribeaud
 */
final class ListMatchingEntitiesOriginalDataProvider extends
        AbstractOriginalDataProvider<MatchingEntity>
{

    private final ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity[] matchingEntities;

    private final String queryText;

    ListMatchingEntitiesOriginalDataProvider(ICommonServer commonServer, String sessionToken,
            ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity[] matchingEntities,
            String queryText)
    {
        super(commonServer, sessionToken);
        this.matchingEntities = matchingEntities;
        this.queryText = queryText;
    }

    //
    // AbstractOriginalDataProvider
    //

    public final List<MatchingEntity> getOriginalData()
    {
        return BeanUtils.createBeanList(MatchingEntity.class, commonServer.listMatchingEntities(
                sessionToken, matchingEntities, queryText), DtoConverters
                .getMatchingEntityConverter());
    }
}