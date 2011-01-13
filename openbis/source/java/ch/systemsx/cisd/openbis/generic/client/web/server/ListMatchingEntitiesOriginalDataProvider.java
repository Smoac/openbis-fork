package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;

/**
 * A {@link IOriginalDataProvider} implementation for listing matching entities.
 * 
 * @author Christian Ribeaud
 */
final class ListMatchingEntitiesOriginalDataProvider extends
        AbstractOriginalDataProvider<MatchingEntity>
{

    private final SearchableEntity[] matchingEntities;

    private final String queryText;

    private final boolean useWildcardSearchMode;

    ListMatchingEntitiesOriginalDataProvider(ICommonServer commonServer, String sessionToken,
            SearchableEntity[] matchingEntities, String queryText,
            final boolean useWildcardSearchMode)
    {
        super(commonServer, sessionToken);
        this.matchingEntities = matchingEntities;
        this.queryText = queryText;
        this.useWildcardSearchMode = useWildcardSearchMode;
    }

    //
    // AbstractOriginalDataProvider
    //

    @Override
    public final List<MatchingEntity> getFullOriginalData()
    {
        final List<MatchingEntity> entities =
                commonServer.listMatchingEntities(sessionToken, matchingEntities, queryText,
                        useWildcardSearchMode);
        return entities;
    }
}
