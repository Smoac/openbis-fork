/*
 * Copyright 2010 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.ENTITY_KIND;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.ENTITY_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.MATCHING_FIELD;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.MATCHING_TEXT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.REGISTRATOR;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class MatchingEntitiesProvider extends AbstractCommonTableModelProvider<MatchingEntity>
{
    private final SearchableEntity[] matchingEntities;
    private final String queryText;
    private final boolean useWildcardSearchMode;

    public MatchingEntitiesProvider(ICommonServer commonServer, String sessionToken,
            SearchableEntity[] matchingEntities, String queryText, boolean useWildcardSearchMode)
    {
        super(commonServer, sessionToken);
        this.matchingEntities = matchingEntities;
        this.queryText = queryText;
        this.useWildcardSearchMode = useWildcardSearchMode;
    }

    @Override
    protected TypedTableModel<MatchingEntity> createTableModel(int maxSize)
    {
        List<MatchingEntity> entities =
            commonServer.listMatchingEntities(sessionToken, matchingEntities, queryText,
                    useWildcardSearchMode);
        TypedTableModelBuilder<MatchingEntity> builder = new TypedTableModelBuilder<MatchingEntity>();
        builder.addColumn(ENTITY_KIND);
        builder.addColumn(ENTITY_TYPE);
        builder.addColumn(IDENTIFIER).withDefaultWidth(140);
        builder.addColumn(REGISTRATOR);
        builder.addColumn(MATCHING_FIELD).withDefaultWidth(140);
        builder.addColumn(MATCHING_TEXT).withDefaultWidth(200);
        for (MatchingEntity matchingEntity : entities)
        {
            builder.addRow(matchingEntity);
            builder.column(ENTITY_KIND).addString(matchingEntity.getEntityKind().getDescription());
            builder.column(ENTITY_TYPE).addString(matchingEntity.getEntityType().getCode());
            builder.column(IDENTIFIER).addString(matchingEntity.getIdentifier());
            builder.column(REGISTRATOR).addPerson(matchingEntity.getRegistrator());
            builder.column(MATCHING_FIELD).addString(matchingEntity.getFieldDescription());
            builder.column(MATCHING_TEXT).addString(matchingEntity.getTextFragment());
        }
        return builder.getModel();
    }

}
