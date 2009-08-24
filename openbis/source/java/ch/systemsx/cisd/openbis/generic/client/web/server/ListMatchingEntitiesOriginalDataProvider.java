package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;

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
        final List<MatchingEntity> entities =
                commonServer.listMatchingEntities(sessionToken, matchingEntities, queryText);
        return entities;
    }
}