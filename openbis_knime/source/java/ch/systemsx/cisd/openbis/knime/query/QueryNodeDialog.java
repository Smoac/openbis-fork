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

package ch.systemsx.cisd.openbis.knime.query;

import static ch.systemsx.cisd.openbis.knime.query.QueryNodeModel.QUERY_DESCRIPTION_KEY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.systemsx.cisd.openbis.knime.common.AbstractParameterDescriptionBasedNodeDialog;
import ch.systemsx.cisd.openbis.knime.common.FieldDescription;
import ch.systemsx.cisd.openbis.knime.server.FieldType;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;

/**
 * Node dialog for an openBIS SQL query.
 * 
 * @author Franz-Josef Elmer
 */
class QueryNodeDialog extends AbstractParameterDescriptionBasedNodeDialog<QueryDescription>
{
    QueryNodeDialog()
    {
        super("Query Settings");
    }

    @Override
    protected List<FieldDescription> getFieldDescriptions(QueryDescription description)
    {
        List<FieldDescription> fieldDescriptions = new ArrayList<FieldDescription>();
        List<String> parameters = description.getParameters();
        for (String parameter : parameters)
        {
            fieldDescriptions.add(new FieldDescription(parameter, FieldType.VARCHAR, ""));
        }
        return fieldDescriptions;
    }

    @Override
    protected List<QueryDescription> getSortedDescriptions(IQueryApiFacade facade)
    {
        List<QueryDescription> queries = facade.listQueries();
        Collections.sort(queries, new Comparator<QueryDescription>()
            {
                @Override
                public int compare(QueryDescription d1, QueryDescription d2)
                {
                    return d1.getName().compareTo(d2.getName());
                }
            });
        return queries;
    }

    @Override
    protected String getDescriptionKey()
    {
        return QUERY_DESCRIPTION_KEY;
    }

    @Override
    protected String getDescriptionComboBoxLabel()
    {
        return "Query";
    }

}
