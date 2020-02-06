/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;

public class TranslationVo
{

    private Long userId;

    private TableMapper tableMapper;

    private AbstractCompositeSearchCriteria parentCriterion;

    private Collection<ISearchCriteria> criteria;

    private SearchOperator operator;

    private Map<Object, Map<String, JoinInformation>> aliases = new HashMap<>();

    private List<Object> args = new ArrayList<>();

    private Map<String, String> dataTypeByPropertyName;

    private Set<Long> ids;

    private SortOptions<?> sortOptions;

    private String[] typesToFilter;

    private String idColumnName;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(final Long userId)
    {
        this.userId = userId;
    }

    public TableMapper getTableMapper()
    {
        return tableMapper;
    }

    public void setTableMapper(final TableMapper tableMapper)
    {
        this.tableMapper = tableMapper;
    }

    public AbstractCompositeSearchCriteria getParentCriterion()
    {
        return parentCriterion;
    }

    public void setParentCriterion(final AbstractCompositeSearchCriteria parentCriterion)
    {
        this.parentCriterion = parentCriterion;
    }

    public Collection<ISearchCriteria> getCriteria()
    {
        return criteria;
    }

    public void setCriteria(final Collection<ISearchCriteria> criteria)
    {
        this.criteria = criteria;
    }

    public SearchOperator getOperator()
    {
        return operator;
    }

    public void setOperator(final SearchOperator operator)
    {
        this.operator = operator;
    }

    public Map<Object, Map<String, JoinInformation>> getAliases()
    {
        return aliases;
    }

    public void setAliases(
            final Map<Object, Map<String, JoinInformation>> aliases)
    {
        this.aliases = aliases;
    }

    public List<Object> getArgs()
    {
        return args;
    }

    public void setArgs(final List<Object> args)
    {
        this.args = args;
    }

    public Map<String, String> getDataTypeByPropertyName()
    {
        return dataTypeByPropertyName;
    }

    public void setDataTypeByPropertyName(final Map<String, String> dataTypeByPropertyName)
    {
        this.dataTypeByPropertyName = dataTypeByPropertyName;
    }

    public Set<Long> getIds()
    {
        return ids;
    }

    public void setIds(final Set<Long> ids)
    {
        this.ids = ids;
    }

    public SortOptions<?> getSortOptions()
    {
        return sortOptions;
    }

    public void setSortOptions(final SortOptions<?> sortOptions)
    {
        this.sortOptions = sortOptions;
    }

    public String[] getTypesToFilter()
    {
        return typesToFilter;
    }

    public void setTypesToFilter(final String[] typesToFilter)
    {
        this.typesToFilter = typesToFilter;
    }

    public String getIdColumnName()
    {
        return idColumnName;
    }

    public void setIdColumnName(final String idColumnName)
    {
        this.idColumnName = idColumnName;
    }

}
