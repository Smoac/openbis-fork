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

package ch.systemsx.cisd.yeastx.quant;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.TransactionQuery;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.yeastx.db.DBUtils;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;
import ch.systemsx.cisd.yeastx.etl.ConstantsYeastX;
import ch.systemsx.cisd.yeastx.quant.dto.ConcentrationCompounds;
import ch.systemsx.cisd.yeastx.quant.dto.MSConcentrationDTO;
import ch.systemsx.cisd.yeastx.quant.dto.MSQuantificationDTO;
import ch.systemsx.cisd.yeastx.quant.dto.MSQuantificationsDTO;
import ch.systemsx.cisd.yeastx.utils.JaxbXmlParser;

/**
 * Tool for uploading <code>quantML</code> files to the database.
 * 
 * @author Tomasz Pylak
 */
public class QuantML2Database
{

    private final IQuantMSDAO dao;

    public QuantML2Database(DataSource datasource)
    {
        this.dao = QueryTool.getQuery(datasource, IQuantMSDAO.class);
    }

    /**
     * Method for uploading an <var>fiaMLFile</var> to the database.
     */
    public void uploadQuantMLFile(final File file, final DMDataSetDTO dataSet) throws SQLException
    {
        TransactionQuery transaction = null;
        try
        {
            transaction = dao;
            DBUtils.createDataSet(dao, dataSet);
            MSQuantificationsDTO quantifications =
                    JaxbXmlParser.parse(MSQuantificationsDTO.class, file, false);
            uploadQuantifications(quantifications, dataSet);
            transaction.close(true);
        } catch (Throwable th)
        {
            try
            {
                DBUtils.rollbackAndClose(transaction);
            } catch (DataAccessException ex)
            {
                // Avoid this exception shadowing the original exception.
            }
            throw CheckedExceptionTunnel.wrapIfNecessary(th);
        }
    }

    private void uploadQuantifications(MSQuantificationsDTO quantifications, DMDataSetDTO dataSet)
    {
        for (MSQuantificationDTO quantification : quantifications.getQuantifications())
        {
            long quantificationId =
                    dao.addQuantification(dataSet.getId(), dataSet.getExperimentId(),
                            quantification);
            uploadConcentrations(quantificationId, quantification.getConcentrations());
        }
    }

    private void uploadConcentrations(long quantificationId, List<MSConcentrationDTO> concentrations)
    {
        for (MSConcentrationDTO concentration : concentrations)
        {
            long concentrationId = dao.addConcentration(quantificationId, concentration);
            uploadCompoundIds(concentrationId, concentration.getCompounds());
        }
    }

    private void uploadCompoundIds(long concentrationId, ConcentrationCompounds compounds)
    {
        dao.addCompoundIds(concentrationId, compounds.getCompoundIds());
    }

    public static void main(String[] args) throws SQLException
    {
        final long start = System.currentTimeMillis();
        final QuantML2Database quantML2Database =
                new QuantML2Database(DBUtils.createDefaultDBContext().getDataSource());
        final String dir = args[0];
        int permId = 0;
        for (String f : new File(dir).list(createQuantFilter()))
        {
            System.out.println(f);
            quantML2Database.uploadQuantMLFile(new File(dir, f), new DMDataSetDTO(Integer
                    .toString(++permId), "sample perm id", "sample name", "experiment perm id",
                    "experiment name"));
        }
        System.out.println((System.currentTimeMillis() - start) / 1000.0);
    }

    private static FilenameFilter createQuantFilter()
    {
        return new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.endsWith("." + ConstantsYeastX.QUANTML_EXT);
                }
            };
    }

}
