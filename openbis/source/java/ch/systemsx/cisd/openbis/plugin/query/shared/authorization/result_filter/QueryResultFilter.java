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

package ch.systemsx.cisd.openbis.plugin.query.shared.authorization.result_filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.plugin.query.shared.authorization.AuthorizationChecker;
import ch.systemsx.cisd.openbis.plugin.query.shared.authorization.IAuthorizationChecker;

/**
 * Filters the rows of {@link TableModel} on magic columns (experiment, sample or data set
 * referenced by 'experiment_key', 'sample_key', 'data_set_key').
 * 
 * @author Izabela Adamczyk
 */
public class QueryResultFilter
{
    private final IGroupLoaderFactory groupLoaderFactory;

    private final IAuthorizationChecker authorizationChecker;

    private static final EntityKind[] FILTRABLE_ENTITY_KINDS =
        { EntityKind.EXPERIMENT, EntityKind.SAMPLE, EntityKind.DATA_SET };

    public QueryResultFilter(IDAOFactory daoFactory)
    {
        this.groupLoaderFactory = new GroupLoaderFactory(daoFactory);
        this.authorizationChecker = new AuthorizationChecker();
    }

    @Private
    QueryResultFilter(IGroupLoaderFactory groupLoaderFactory,
            IAuthorizationChecker authorizationChecker)
    {
        this.groupLoaderFactory = groupLoaderFactory;
        this.authorizationChecker = authorizationChecker;
    }

    public TableModel filterResults(PersonPE person, TableModel table)
    {
        for (EntityKind kind : FILTRABLE_ENTITY_KINDS)
        {
            filterByKind(table, person, kind);
        }
        return table;
    }

    private void filterByKind(TableModel table, PersonPE person, EntityKind kind)
    {
        List<Integer> columnsToFilter = getColumnsToFilter(table, kind);
        Set<String> entityIdentifiers = getValues(table, columnsToFilter);
        Map<String, GroupPE> entitySpaces = loadGroups(entityIdentifiers, kind);
        Iterator<TableModelRow> rowIterator = table.getRows().iterator();
        rowLoop: while (rowIterator.hasNext())
        {
            TableModelRow row = rowIterator.next();
            for (int c : columnsToFilter)
            {
                ISerializableComparable value = row.getValues().get(c);
                if (value != null
                        && authorizationChecker.isAuthorized(person, entitySpaces.get(value
                                .toString()), RoleSet.OBSERVER) == false)
                {
                    rowIterator.remove();
                    continue rowLoop;
                }
            }
        }
    }

    private static Set<String> getValues(TableModel table, List<Integer> columns)
    {
        Set<String> values = new HashSet<String>();
        for (TableModelRow row : table.getRows())
        {
            for (int c : columns)
            {
                ISerializableComparable value = row.getValues().get(c);
                if (value != null)
                {
                    values.add(value.toString());
                }
            }
        }
        return values;
    }

    private static List<Integer> getColumnsToFilter(TableModel table, EntityKind kind)
    {
        List<Integer> columns = new ArrayList<Integer>();
        for (int i = 0; i < table.getHeader().size(); i++)
        {
            TableModelColumnHeader header = table.getHeader().get(i);
            EntityKind headerEntityKindOrNull = header.tryGetEntityKind();
            if (headerEntityKindOrNull != null && headerEntityKindOrNull.equals(kind))
            {
                columns.add(i);
            }
        }
        return columns;
    }

    private Map<String, GroupPE> loadGroups(Set<String> values, EntityKind kind)
    {
        return groupLoaderFactory.create(kind).loadGroups(values);
    }

}