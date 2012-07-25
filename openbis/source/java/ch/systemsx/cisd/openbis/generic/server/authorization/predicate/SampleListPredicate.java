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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.Select;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.LongArrayMapper;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.StringArrayMapper;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractSpacePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleOwnerIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * A predicate for lists of entities of {@link Sample}s. This
 * predicate authorizes for read-only access, i.e. it will allow access to shared samples for all
 * users.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Bernd Rinn
 */
@ShouldFlattenCollections(value = false)
public class SampleListPredicate extends AbstractSpacePredicate<List<Sample>>
{
    private final SampleOwnerIdentifierPredicate idOwnerPredicate;

    public SampleListPredicate()
    {
        idOwnerPredicate = new SampleOwnerIdentifierPredicate();
    }

    //
    // AbstractPredicate
    //

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        idOwnerPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "sample";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<Sample> samples)
    {
        // All fields relevant for authorization are expected to be filled:
        // - technical id
        // - permanent id
        // - space code
        // - space identifier
        final List<Long> ids = new ArrayList<Long>(samples.size());
        final List<String> permIds = new ArrayList<String>(samples.size());
        for (Sample sample : samples)
        {
            if (sample.getId() == null)
            {
                throw new AuthorizationFailureException("id is undefined.");
            }
            ids.add(sample.getId());
            if (sample.getPermId() == null)
            {
                throw new AuthorizationFailureException("permId is undefined.");
            }
            permIds.add(sample.getPermId());

            if (sample.getSpaceCode() != null) // == null represents a shared sample
            {
                final Status status =
                        evaluate(person, allowedRoles, authorizationDataProvider
                                .getHomeDatabaseInstance(), sample.getSpaceCode());
                if (Status.OK.equals(status) == false)
                {
                    return status;
                }
            }

            final SampleOwnerIdentifier idOwner =
                    SampleIdentifierFactory.parse(sample.getIdentifier());
            final Status status = idOwnerPredicate.evaluate(person, allowedRoles, idOwner);
            if (Status.OK.equals(status) == false)
            {
                return status;
            }
        }
        for (Long spaceId : getSampleSpaceIds(ids, permIds))
        {
            if (spaceId == null || spaceId == 0)
            {
                continue; // Shared samples will return a spaceId of null (or 0 in EoDSQL).
            }
            final Status status =
                    evaluate(person, allowedRoles, spaceId);
            if (Status.OK.equals(status) == false)
            {
                return status;
            }
        }
        return Status.OK;
    }

    private final static int ARRAY_SIZE_LIMIT = 999;

    interface ISampleToSpaceQuery extends BaseQuery
    {
        @Select(sql = "select distinct space_id from samples where id = any(?{1}) "
                + "union select distinct space_id from samples where perm_id = any(?{2})", parameterBindings =
            { LongArrayMapper.class, StringArrayMapper.class })
        public List<Long> getSampleSpaceIds(long[] sampleIds, String[] samplePermIds);
    }

    private Collection<Long> getSampleSpaceIds(final List<Long> ids, final List<String> permIds)
    {
        if (ids.size() != permIds.size())
        {
            throw new IllegalArgumentException("Expect to get the same number of ids and permIds.");
        }
        final int size = ids.size();
        if (size == 0)
        {
            return Collections.emptyList();
        }
        final ISampleToSpaceQuery query =
                QueryTool.getQuery(authorizationDataProvider.getConnection(),
                        ISampleToSpaceQuery.class);
        if (size > ARRAY_SIZE_LIMIT)
        {
            final Set<Long> spaceIds = new HashSet<Long>(size);
            for (int startIdx = 0; startIdx < size; startIdx += ARRAY_SIZE_LIMIT)
            {
                final List<Long> idSubList = ids.subList(startIdx,
                        Math.min(size, startIdx + ARRAY_SIZE_LIMIT));
                final List<String> permIdSubList = permIds.subList(startIdx,
                        Math.min(size, startIdx + ARRAY_SIZE_LIMIT));
                spaceIds.addAll(query.getSampleSpaceIds(toArray(idSubList),
                        permIdSubList.toArray(new String[permIdSubList.size()])));
            }
            return spaceIds;
        } else
        {
            return query.getSampleSpaceIds(toArray(ids), permIds.toArray(new String[size]));
        }
    }

    private long[] toArray(List<Long> list)
    {
        final long[] result = new long[list.size()];
        for (int i = 0; i < result.length; ++i)
        {
            result[i] = list.get(i);
        }
        return result;
    }

}
