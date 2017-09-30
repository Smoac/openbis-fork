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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.EntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.EntityTypeIdMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.ISearchEntityTypeExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class SearchPropertyAssignmentExecutor extends AbstractSearchObjectManuallyExecutor<PropertyAssignmentSearchCriteria, EntityTypePropertyTypePE>
        implements ISearchPropertyAssignmentExecutor
{

    @Autowired
    private ISearchEntityTypeExecutor searchEntityTypeExecutor;

    @Autowired
    private ISearchPropertyTypeExecutor searchPropertyTypeExecutor;

    @Override
    protected List<EntityTypePropertyTypePE> listAll()
    {
        List<EntityTypePropertyTypePE> propertyAssignments = new ArrayList<EntityTypePropertyTypePE>();
        propertyAssignments.addAll(daoFactory.getEntityPropertyTypeDAO(EntityKind.MATERIAL).listEntityPropertyTypes());
        propertyAssignments.addAll(daoFactory.getEntityPropertyTypeDAO(EntityKind.EXPERIMENT).listEntityPropertyTypes());
        propertyAssignments.addAll(daoFactory.getEntityPropertyTypeDAO(EntityKind.SAMPLE).listEntityPropertyTypes());
        propertyAssignments.addAll(daoFactory.getEntityPropertyTypeDAO(EntityKind.DATA_SET).listEntityPropertyTypes());
        return propertyAssignments;
    }

    @Override
    protected Matcher<EntityTypePropertyTypePE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof EntityTypeSearchCriteria)
        {
            return new EntityTypeMatcher();
        } else if (criteria instanceof PropertyTypeSearchCriteria)
        {
            return new PropertyTypeMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher<EntityTypePropertyTypePE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, EntityTypePropertyTypePE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof PropertyAssignmentPermId)
            {
                PropertyAssignmentPermId permId = (PropertyAssignmentPermId) id;

                if (permId.getEntityTypeId() == null)
                {
                    throw new UserFailureException("Property assignment entity type id cannot be null");
                }
                if (permId.getPropertyTypeId() == null)
                {
                    throw new UserFailureException("Property assignment property type id cannot be null");
                }

                return EntityTypeIdMatcher.isMatching(permId.getEntityTypeId(), object.getEntityType())
                        && PropertyTypeIdMatcher.isMatching(permId.getPropertyTypeId(), object.getPropertyType());
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }

    }

    private class EntityTypeMatcher extends Matcher<EntityTypePropertyTypePE>
    {

        @Override
        public List<EntityTypePropertyTypePE> getMatching(IOperationContext context, List<EntityTypePropertyTypePE> objects, ISearchCriteria criteria)
        {
            List<EntityTypePE> entityTypeList = searchEntityTypeExecutor.search(context, (EntityTypeSearchCriteria) criteria);
            Set<EntityTypePE> entityTypeSet = new HashSet<EntityTypePE>(entityTypeList);

            List<EntityTypePropertyTypePE> matching = new ArrayList<EntityTypePropertyTypePE>();
            for (EntityTypePropertyTypePE object : objects)
            {
                if (entityTypeSet.contains(object.getEntityType()))
                {
                    matching.add(object);
                }
            }

            return matching;
        }

    }

    private class PropertyTypeMatcher extends Matcher<EntityTypePropertyTypePE>
    {

        @Override
        public List<EntityTypePropertyTypePE> getMatching(IOperationContext context, List<EntityTypePropertyTypePE> objects, ISearchCriteria criteria)
        {
            List<PropertyTypePE> propertyTypeList = searchPropertyTypeExecutor.search(context, (PropertyTypeSearchCriteria) criteria);
            Set<PropertyTypePE> propertyTypeSet = new HashSet<PropertyTypePE>(propertyTypeList);

            List<EntityTypePropertyTypePE> matching = new ArrayList<EntityTypePropertyTypePE>();
            for (EntityTypePropertyTypePE object : objects)
            {
                if (propertyTypeSet.contains(object.getPropertyType()))
                {
                    matching.add(object);
                }
            }

            return matching;
        }

    }

}
