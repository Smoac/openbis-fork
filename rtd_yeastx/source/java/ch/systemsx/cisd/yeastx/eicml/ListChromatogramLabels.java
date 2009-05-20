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

import java.sql.Connection;
import java.sql.SQLException;

import net.lemnik.eodsql.DataIterator;

/**
 * A method for listing all chromatogram labels of all runs.
 *
 * @author Bernd Rinn
 */
public class ListChromatogramLabels
{

    public static void main(String[] args) throws SQLException
    {
        final Connection conn = DBFactory.getConnection();
        try
        {
            final IEICMSRunDAO dao = DBFactory.getDAO(conn);
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
            conn.close();
        }
    }

    private static void listChromatogramsForRuns(final IEICMSRunDAO dao, DataIterator<MSRunDTO> runs)
    {
        for (MSRunDTO run : runs)
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
