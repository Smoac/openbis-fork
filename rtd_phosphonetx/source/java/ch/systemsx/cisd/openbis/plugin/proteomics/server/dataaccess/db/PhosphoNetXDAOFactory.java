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

package ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.IProteinQueryDAO;

/**
 * @author Franz-Josef Elmer
 */
public class PhosphoNetXDAOFactory implements IPhosphoNetXDAOFactory
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PhosphoNetXDAOFactory.class);
    
    private final Map<DataSource, IProteinQueryDAO> daos = new HashMap<DataSource, IProteinQueryDAO>();

    private final IDAOFactory daoFactory;

    private final IDataSourceProvider dataSourceProvider;

    public PhosphoNetXDAOFactory(IDataSourceProvider dataSourceProvider, IDAOFactory daoFactory)
    {
        this.dataSourceProvider = dataSourceProvider;
        this.daoFactory = daoFactory;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("DAO factory for proteomics created.");
        }
    }
    
    @Override
    public IProteinQueryDAO getProteinQueryDAO(String experimentPermID)
    {
        ExperimentPE experiment = daoFactory.getExperimentDAO().tryGetByPermID(experimentPermID);
        if (experiment == null)
        {
            throw new UserFailureException("No experiment with following perm ID found: "
                    + experimentPermID);
        }
        return getProteinQueryDAO(experiment);
    }

    @Override
    public IProteinQueryDAO getProteinQueryDAO(TechId experimentID)
    {
        ExperimentPE experiment = daoFactory.getExperimentDAO().tryGetByTechId(experimentID);
        if (experiment == null)
        {
            throw new UserFailureException("No experiment with following technical ID found: "
                    + experimentID);
        }
        return getProteinQueryDAO(experiment);
    }

    public IProteinQueryDAO getProteinQueryDAO(ExperimentPE experiment)
    {
        DataSource dataSource = getDataSource(experiment);
        IProteinQueryDAO dao = daos.get(dataSource);
        if (dao == null)
        {
            dao = QueryTool.getQuery(dataSource, IProteinQueryDAO.class);
            daos.put(dataSource, dao);
        }
        return dao;
    }
    
    private DataSource getDataSource(ExperimentPE experiment)
    {
        IDataDAO dataDAO = daoFactory.getDataDAO();
        
        List<DataPE> dataSets = dataDAO.listDataSets(experiment);
        Set<String> dataStores = new HashSet<String>();
        for (DataPE data : dataSets)
        {
            dataStores.add(data.getDataStore().getCode());
        }
        if (dataStores.isEmpty())
        {
            throw new UserFailureException("Experiment with " + experiment.getIdentifier()
                    + " has no data sets.");
        }
        if (dataStores.size() > 1)
        {
            throw new UserFailureException("Experiment with " + experiment.getIdentifier()
                    + " has data sets from more than one store. The stores are the following: "
                    + dataStores);
        }
        DataSource dataSource =
                dataSourceProvider.getDataSourceByDataStoreServerCode(dataStores.iterator()
                        .next(), "proteomics");
        return dataSource;
    }

}
