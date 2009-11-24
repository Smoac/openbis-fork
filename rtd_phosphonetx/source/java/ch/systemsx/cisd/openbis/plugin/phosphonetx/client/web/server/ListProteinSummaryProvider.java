/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSummary;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ListProteinSummaryProvider implements IOriginalDataProvider<ProteinSummary>
{
    private final IPhosphoNetXServer server;
    private final String sessionToken;
    private final TechId experimentID;

    ListProteinSummaryProvider(IPhosphoNetXServer server, String sessionToken,
            TechId experimentID)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.experimentID = experimentID;
    }

    public List<ProteinSummary> getOriginalData() throws UserFailureException
    {
        return server.listProteinSummariesByExperiment(sessionToken, experimentID);
    }

}
