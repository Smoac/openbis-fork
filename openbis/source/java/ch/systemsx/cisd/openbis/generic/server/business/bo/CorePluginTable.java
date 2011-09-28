/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.CorePluginPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Kaloyan Enimanev
 */
public final class CorePluginTable extends AbstractBusinessObject implements ICorePluginTable
{
    public CorePluginTable(IDAOFactory daoFactory, Session session)
    {
        super(daoFactory, session);
    }

    public List<CorePluginPE> listCorePluginsByName(String name)
    {
        return getCorePluginDAO().listCorePluginsByName(name);
    }

    public void registerPlugins(List<CorePluginPE> pluginsToBeCreated)
    {
        assert pluginsToBeCreated != null : "Unspecified plugins.";

        try
        {
            getCorePluginDAO().createCorePlugins(pluginsToBeCreated);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("One of samples"));
        }
    }

}
