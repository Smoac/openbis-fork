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

import net.lemnik.eodsql.TransactionQuery;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.yeastx.db.AbstractDBTest;
import ch.systemsx.cisd.yeastx.db.DBUtils;
import ch.systemsx.cisd.yeastx.db.DBUtilsForTests;

/**
 * A performance test of reading all chromatograms from the database.
 * 
 * @author Bernd Rinn
 */
public class ReadChromatogramsPerformanceTest
{

    public static void main(String[] args) throws SQLException
    {
        final DatabaseConfigurationContext context = AbstractDBTest.createDefaultDBContext();
        DBUtilsForTests.init(context);
        TransactionQuery transaction = null;
        long start = System.currentTimeMillis();
        try
        {
            final IEICMSRunDAO dao =
                    DBUtils.getQuery(context.getDataSource(), IEICMSRunDAO.class);
            for (EICMSRunDTO run : dao.getMsRuns())
            {
                // We need to iterate over the chromatograms to make sure they are really read.
                for (@SuppressWarnings("unused")
                ChromatogramDTO chromatogram : dao.getChromatogramsForRun(run))
                {
                    // Nothing to do.
                }
            }
        } finally
        {
            DBUtils.close(transaction);
        }
        System.out.println((System.currentTimeMillis() - start) / 1000.0f);
    }

}
