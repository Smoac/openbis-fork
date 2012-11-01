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

package ch.systemsx.cisd.openbis.plugin.screening.server.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.Select;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractSpacePredicate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.StringArrayMapper;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.exception.UndefinedSpaceException;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;

/**
 * A predicate for lists of entities which have {@link PlateIdentifier} as their super-class. This
 * predicate authorizes for read-only access, i.e. it will allow access to shared samples for all
 * users.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Bernd Rinn
 */
@ShouldFlattenCollections(value = false)
public class ScreeningPlateListReadOnlyPredicate extends
        AbstractSpacePredicate<List<? extends PlateIdentifier>>
{

    @Override
    public String getCandidateDescription()
    {
        return "plate";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<? extends PlateIdentifier> plates)
    {
        final List<String> permIds = new ArrayList<String>(plates.size());
        for (PlateIdentifier plate : plates)
        {
            boolean hasPermId = false;
            if (plate.getPermId() != null)
            {
                permIds.add(plate.getPermId());
                hasPermId = true;
            }

            final String spaceCodeOrNull =
                    SpaceCodeHelper.tryGetSpaceCode(person, plate.tryGetSpaceCode());
            if (spaceCodeOrNull == null && hasPermId == false)
            {
                throw new UndefinedSpaceException();
            }
            if (spaceCodeOrNull != null && plate.isSharedPlate() == false)
            {
                final Status status =
                        evaluate(person, allowedRoles, authorizationDataProvider
                                .getHomeDatabaseInstance(), spaceCodeOrNull);
                if (Status.OK.equals(status) == false)
                {
                    return status;
                }
            }
        }
        if (permIds.isEmpty() == false)
        {
            for (Long spaceId : getSampleSpaceIds(permIds))
            {
                if (spaceId == null)
                {
                    continue; // Shared samples will return a spaceId of null.
                }
                final Status status =
                        evaluate(person, allowedRoles, spaceId);
                if (Status.OK.equals(status) == false)
                {
                    return status;
                }
            }
        }
        return Status.OK;
    }

    private final static int ARRAY_SIZE_LIMIT = 999;

    interface ISampleToSpaceQuery extends BaseQuery
    {
        @Select(sql = "select distinct space_id from samples where perm_id = any(?{1})", parameterBindings =
            { StringArrayMapper.class })
        public List<Long> getSampleSpaceIds(String[] samplePermIds);
    }

    private Collection<Long> getSampleSpaceIds(final List<String> permIds)
    {
        final ISampleToSpaceQuery query =
                QueryTool.getQuery(authorizationDataProvider.getConnection(),
                        ISampleToSpaceQuery.class);
        if (permIds.size() > ARRAY_SIZE_LIMIT)
        {
            final Set<Long> spaceIds = new HashSet<Long>(permIds.size());
            for (int startIdx = 0; startIdx < permIds.size(); startIdx += ARRAY_SIZE_LIMIT)
            {
                final List<String> permIdSubList = permIds.subList(startIdx,
                        Math.min(permIds.size(), startIdx + ARRAY_SIZE_LIMIT));
                spaceIds.addAll(query.getSampleSpaceIds(permIdSubList.toArray(
                        new String[permIdSubList.size()])));
            }
            return spaceIds;
        } else
        {
            return query
                    .getSampleSpaceIds(permIds.toArray(new String[permIds.size()]));
        }
    }

}
