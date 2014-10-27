/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Jakub Straszewski
 */
public class MultiDatasetArchiverDBTransaction
{

    private static DataSource dataSource = ServiceProvider.getDataSourceProvider().getDataSource("multi-dataset-archiver-db");

    private static SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");

    private static AtomicInteger containerCounter = new AtomicInteger(1);

    private IMultiDataSetArchiverQueryDAO transaction;

    public MultiDatasetArchiverDBTransaction()
    {
        this.transaction = getTransactionalQuery();
    }

    private static IMultiDataSetArchiverQueryDAO getTransactionalQuery()
    {
        return QueryTool.getQuery(dataSource, IMultiDataSetArchiverQueryDAO.class);
    }

    public List<MultiDataSetArchiverDataSetDTO> getDataSetsForContainer(MultiDataSetArchiverContainerDTO container)
    {
        return transaction.listDataSetsForContainerId(container.getId());
    }

    /**
     * Creates a new container
     */
    public MultiDataSetArchiverContainerDTO createContainer()
    {
        String path = String.format("%s/%s/%s", "code", simpleDateformat.format(new Date()), containerCounter.incrementAndGet());
        MultiDataSetArchiverContainerDTO container =
                new MultiDataSetArchiverContainerDTO(0, "code", path, MultiDataSetArchiverContainerDTO.LOCATION_STAGE, false);

        long id = transaction.addContainer(container);
        container.setId(id);

        return container;
    }

    public MultiDataSetArchiverDataSetDTO insertDataset(DatasetDescription dataSet,
            MultiDataSetArchiverContainerDTO container)
    {
        String code = dataSet.getDataSetCode();

        MultiDataSetArchiverDataSetDTO mads = transaction.getDataSetForCode(code);

        if (mads != null)
        {
            throw new IllegalStateException("Trying to add dataset that has already been added.");
        }

        mads = new MultiDataSetArchiverDataSetDTO(0, code, container.getId(), dataSet.getDataSetSize());

        long id = transaction.addDataSet(mads);
        mads.setId(id);

        return mads;
    }

    /**
     * @see net.lemnik.eodsql.TransactionQuery#commit()
     */
    public void commit()
    {
        transaction.commit();
    }

    /**
     * @see net.lemnik.eodsql.TransactionQuery#rollback()
     */
    public void rollback()
    {
        transaction.rollback();
    }

    /**
     * @see net.lemnik.eodsql.BaseQuery#close()
     */
    public void close()
    {
        transaction.close();
    }

}
