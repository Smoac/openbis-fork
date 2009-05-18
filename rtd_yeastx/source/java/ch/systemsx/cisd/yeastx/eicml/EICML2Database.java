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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ch.systemsx.cisd.yeastx.eicml.EICMLParser.IChromatogramObserver;
import ch.systemsx.cisd.yeastx.eicml.EICMLParser.IMSRunObserver;

/**
 * Tool for uploading eicML files to the database.
 * 
 * @author Bernd Rinn
 */
public class EICML2Database
{

    public static void main(String[] args) throws ParserConfigurationException, SAXException,
            IOException, SQLException
    {
        final Connection conn = DBFactory.getConnection();
        try
        {
            final String dir = args[0];
            int permId = 0;
            for (String f : new File(dir).list(new EICMLFilenameFilter()))
            {
                final long[] id = new long[1];
                try
                {
                    final IMSRunDAO dao = DBFactory.getDAO(conn);
                    new EICMLParser(dir + "/" + f, Integer.toString(++permId), new IMSRunObserver()
                        {
                            public void observe(MSRunDTO run)
                            {
                                id[0] = dao.addMSRun(run);
                            }
                        }, new IChromatogramObserver()
                        {
                            public void observe(ChromatogramDTO chromatogram)
                            {
                                chromatogram.msRunId = id[0];
                                dao.addChromatogram(chromatogram);
                            }
                        });
                    conn.commit();
                } catch (Throwable th)
                {
                    conn.rollback();
                    th.printStackTrace();
                }
            }
        } finally
        {
            conn.close();
        }
    }
}
