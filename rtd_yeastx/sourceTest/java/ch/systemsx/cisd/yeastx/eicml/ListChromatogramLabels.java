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

package ch.systemsx.cisd.yeastx.eicml;

import java.sql.SQLException;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.TransactionQuery;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.yeastx.db.AbstractDBTest;
import ch.systemsx.cisd.yeastx.db.DBUtils;

/**
 * A method for listing all chromatogram labels of all runs.
 * 
 * @author Bernd Rinn
 */
public class ListChromatogramLabels
{

    public static void main(String[] args) throws SQLException
    {
        final DatabaseConfigurationContext context = AbstractDBTest.createDefaultDBContext();
        DBUtils.init(context);
        TransactionQuery transaction = null;
        try
        {
            final IEICMSRunDAO dao =
                    QueryTool.getQuery(context.getDataSource(), IEICMSRunDAO.class);
            transaction = dao;
            if (args.length > 0)
            {
                for (String fn : args)
                {
                    String rawFile = fn;
                    if (rawFile.endsWith(".RAW") == false)
                    {
                        rawFile += ".RAW";
                    }
                    listChromatogramsForRuns(dao, dao.getMsRunsForRawDataFile(rawFile));
                }
            } else
            {
                listChromatogramsForRuns(dao, dao.getMsRuns());
            }
        } finally
        {
            DBUtils.close(transaction);
        }
    }

    private static void listChromatogramsForRuns(final IEICMSRunDAO dao,
            DataIterator<EICMSRunDTO> runs)
    {
        for (EICMSRunDTO run : runs)
        {
            String msRunName = run.getRawDataFileName();
            if (msRunName.endsWith(".RAW"))
            {
                msRunName = msRunName.substring(0, msRunName.length() - 4);
            }
            for (ChromatogramDTO chrom : dao.getChromatogramsForRunNoData(run))
            {
                System.out.println(msRunName + "\t" + chrom.getLabel());
            }
        }
    }

}
