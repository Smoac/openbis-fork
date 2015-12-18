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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringContainsValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEndsWithValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringStartsWithValue;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

/**
 * @author pkupczyk
 */
public abstract class AbstractSearchObjectManuallyExecutor<CRITERIA extends AbstractObjectSearchCriteria<?>, OBJECT> implements
        ISearchObjectExecutor<CRITERIA, OBJECT>
{

    @Autowired
    protected IDAOFactory daoFactory;

    protected abstract List<OBJECT> listAll();

    protected abstract Matcher getMatcher(ISearchCriteria criteria);

    @Override
    public List<OBJECT> search(IOperationContext context, CRITERIA criteria)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (criteria == null)
        {
            throw new IllegalArgumentException("Criterion cannot be null");
        }

        return getMatching(context, listAll(), criteria);
    }

    private List<OBJECT> getMatching(IOperationContext context, List<OBJECT> objects, CRITERIA criteria)
    {
        if (criteria.getCriteria() == null || criteria.getCriteria().isEmpty())
        {
            return objects;
        } else
        {
            List<List<OBJECT>> partialMatches = new LinkedList<List<OBJECT>>();

            for (ISearchCriteria subCriteria : criteria.getCriteria())
            {
                Matcher matcher = getMatcher(subCriteria);

                List<OBJECT> partialMatch = matcher.getMatching(context, objects, subCriteria);
                if (partialMatch == null)
                {
                    partialMatch = Collections.emptyList();
                }
                partialMatches.add(partialMatch);
            }

            if (SearchOperator.AND.equals(criteria.getOperator()))
            {
                Set<OBJECT> matches = new HashSet<OBJECT>(partialMatches.get(0));
                for (List<OBJECT> partialMatch : partialMatches)
                {
                    matches.retainAll(partialMatch);
                }
                return new ArrayList<OBJECT>(matches);
            } else if (SearchOperator.OR.equals(criteria.getOperator()))
            {
                Set<OBJECT> matches = new HashSet<OBJECT>();
                for (List<OBJECT> partialMatch : partialMatches)
                {
                    matches.addAll(partialMatch);
                }
                return new ArrayList<OBJECT>(matches);
            } else
            {
                throw new IllegalArgumentException("Unknown search operator: " + criteria.getOperator());
            }
        }
    }

    protected abstract class Matcher
    {

        public abstract List<OBJECT> getMatching(IOperationContext context, List<OBJECT> objects, ISearchCriteria criteria);

    }

    protected abstract class SimpleFieldMatcher extends Matcher
    {

        @Override
        public List<OBJECT> getMatching(IOperationContext context, List<OBJECT> objects, ISearchCriteria criteria)
        {
            List<OBJECT> matches = new ArrayList<OBJECT>();

            for (OBJECT object : objects)
            {
                if (isMatching(context, object, criteria))
                {
                    matches.add(object);
                }
            }

            return matches;
        }

        protected abstract boolean isMatching(IOperationContext context, OBJECT object, ISearchCriteria criteria);

    }

    protected abstract class StringFieldMatcher extends SimpleFieldMatcher
    {

        @Override
        protected boolean isMatching(IOperationContext context, OBJECT object, ISearchCriteria criteria)
        {
            AbstractStringValue fieldValue = ((StringFieldSearchCriteria) criteria).getFieldValue();

            if (fieldValue == null || fieldValue.getValue() == null || fieldValue instanceof AnyStringValue)
            {
                return true;
            }

            String actualValue = getFieldValue(object);

            if (actualValue == null)
            {
                actualValue = "";
            } else
            {
                actualValue = actualValue.toLowerCase();
            }

            String searchedValue = fieldValue.getValue().toLowerCase();

            if (fieldValue instanceof StringEqualToValue)
            {
                return actualValue.equals(searchedValue);
            } else if (fieldValue instanceof StringContainsValue)
            {
                return actualValue.contains(searchedValue);
            } else if (fieldValue instanceof StringStartsWithValue)
            {
                return actualValue.startsWith(searchedValue);
            } else if (fieldValue instanceof StringEndsWithValue)
            {
                return actualValue.endsWith(searchedValue);
            } else
            {
                throw new IllegalArgumentException("Unknown string value: " + criteria.getClass());
            }
        }

        protected abstract String getFieldValue(OBJECT object);

    }

}
