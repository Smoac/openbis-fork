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

package ch.systemsx.cisd.openbis.knime.query;

import java.util.Arrays;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeTableModel;
import ch.systemsx.cisd.openbis.knime.common.Util;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * Node model for report reader node.
 * 
 * @author Franz-Josef Elmer
 */
public class ReportNodeModel extends AbstractOpenBisNodeTableModel
{
    static final String REPORT_DESCRIPTION_KEY = "report-description";

    static final String DATA_SET_CODES_KEY = "data-set-codes";

    private ReportDescription reportDescription;

    private String[] dataSetCodes;

    @Override
    protected void loadAdditionalValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException
    {
        reportDescription =
                Util.deserializeDescription(settings.getByteArray(REPORT_DESCRIPTION_KEY));
        dataSetCodes = settings.getStringArray(DATA_SET_CODES_KEY);
    }

    @Override
    protected void saveAdditionalSettingsTo(NodeSettingsWO settings)
    {
        settings.addByteArray(REPORT_DESCRIPTION_KEY, Util.serializeDescription(reportDescription));
        settings.addStringArray(DATA_SET_CODES_KEY, dataSetCodes);
    }

    @Override
    protected QueryTableModel getData(IQueryApiFacade facade)
    {
        return facade.createReportFromDataSets(reportDescription.getKey(), Arrays.asList(dataSetCodes));
    }

}
