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

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.ResourceNames;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component(value = ResourceNames.PHOSPHONETX_PLUGIN_SERVICE)
public class PhosphoNetXClientService extends AbstractClientService implements IPhosphoNetXClientService
{
    @Resource(name = ResourceNames.PHOSPHONETX_PLUGIN_SERVER)
    private IPhosphoNetXServer server;
    
    public PhosphoNetXClientService()
    {
        super();
    }

    public PhosphoNetXClientService(IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
    }

    @Override
    protected IServer getServer()
    {
        return server;
    }

    public ResultSet<ProteinInfo> listProteinsByExperiment(ListProteinByExperimentCriteria criteria) throws UserFailureException
    {
        final String sessionToken = getSessionToken();
        return listEntities(criteria, new ListProteinOriginalDataProvider(server,
                sessionToken, criteria.getExperimentID()));
    }

    public String prepareExportProteins(TableExportCriteria<ProteinInfo> exportCriteria)
            throws UserFailureException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
