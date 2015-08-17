/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.QueryTool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectToOneRelationTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.sql.ISpaceSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;

/**
 * @author pkupczyk
 */
@Component
public class PersonSpaceTranslator extends ObjectToOneRelationTranslator<Space, SpaceFetchOptions>
{

    @Autowired
    private ISpaceSqlTranslator spaceTranslator;

    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet objectIds)
    {
        PersonQuery query = QueryTool.getManagedQuery(PersonQuery.class);
        return query.getSpaces(objectIds);
    }

    @Override
    protected Map<Long, Space> translateRelated(TranslationContext context, Collection<Long> relatedIds, SpaceFetchOptions relatedFetchOptions)
    {
        return spaceTranslator.translate(context, relatedIds, relatedFetchOptions);
    }

}
