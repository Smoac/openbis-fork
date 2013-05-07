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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeTableModel;
import ch.systemsx.cisd.openbis.knime.common.ParameterBindings;
import ch.systemsx.cisd.openbis.knime.common.Util;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * Node model for SQL parameterized queries.
 *
 * @author Franz-Josef Elmer
 */
public class QueryNodeModel extends AbstractOpenBisNodeTableModel
{
    static final String QUERY_DESCRIPTION_KEY = "query-description";
    
    private QueryDescription queryDescription;
    private ParameterBindings parameterBindings = new ParameterBindings();

    @Override
    protected void loadAdditionalValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException
    {
        queryDescription =
                Util.deserializeDescription(settings.getByteArray(QUERY_DESCRIPTION_KEY));
        parameterBindings.loadValidatedSettingsFrom(settings);
    }

    @Override
    protected void saveAdditionalSettingsTo(NodeSettingsWO settings)
    {
        settings.addByteArray(QUERY_DESCRIPTION_KEY, Util
                .serializeDescription(queryDescription));
        parameterBindings.saveSettingsTo(settings);
    }
    
    @Override
    protected QueryTableModel getData(IQueryApiFacade facade)
    {
        return facade.executeQuery(queryDescription.getId(), parameterBindings.getBindings());
    }
    
}
