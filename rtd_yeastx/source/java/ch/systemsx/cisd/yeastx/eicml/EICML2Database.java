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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.lemnik.eodsql.QueryTool;

import org.xml.sax.SAXException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.yeastx.db.DBFactory;
import ch.systemsx.cisd.yeastx.eicml.EICMLParser.IChromatogramObserver;
import ch.systemsx.cisd.yeastx.eicml.EICMLParser.IMSRunObserver;

/**
 * Tool for uploading <code>eicML</code> files to the database.
 * 
 * @author Bernd Rinn
 */
public class EICML2Database
{

    private final static int CHROMATOGRAM_BATCH_SIZE = 100;

    public static IEICMSRunDAO getDAO(Connection conn)
    {
        return QueryTool.getQuery(conn, IEICMSRunDAO.class);
    }

    private static void addChromatograms(IEICMSRunDAO dao, long eicMLId,
            List<ChromatogramDTO> chromatograms, int threshold)
    {
        if (chromatograms.size() >= threshold)
        {
            dao.addChromatograms(eicMLId, chromatograms);
            chromatograms.clear();
        }
    }

    /**
     * Method for uploading an <var>eicMLFile</var> to the database.
     */
    public static void uploadEicMLFile(final Connection conn, final File eicMLFile, String permId)
            throws SQLException
    {
        final long[] eicMLId = new long[1];
        final List<ChromatogramDTO> chromatograms =
                new ArrayList<ChromatogramDTO>(CHROMATOGRAM_BATCH_SIZE);
        try
        {
            final IEICMSRunDAO dao = getDAO(conn);
            new EICMLParser(eicMLFile.getPath(), permId, new IMSRunObserver()
                {
                    public void observe(EICMSRunDTO run)
                    {
                        // add chromatograms from the last run to the database before setting the new run id
                        addChromatograms(dao, eicMLId[0], chromatograms, 1);
                        eicMLId[0] = dao.addMSRun(run);
                    }
                }, new IChromatogramObserver()
                {
                    public void observe(ChromatogramDTO chromatogram)
                    {
                        chromatograms.add(chromatogram);
                        addChromatograms(dao, eicMLId[0], chromatograms, CHROMATOGRAM_BATCH_SIZE);
                    }
                });
            addChromatograms(dao, eicMLId[0], chromatograms, 1);
            conn.commit();
        } catch (Throwable th)
        {
            conn.rollback();
            if (th instanceof SQLException)
            {
                throw (SQLException) th;
            } else
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(th);
            }
        }
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException,
            IOException, SQLException
    {
        final long start = System.currentTimeMillis();
        final Connection conn = new DBFactory(DBFactory.createDefaultDBContext()).getConnection();
        try
        {
            final String dir = args[0];
            int permId = 0;
            for (String f : new File(dir).list(new EICMLFilenameFilter()))
            {
                uploadEicMLFile(conn, new File(dir, f), Integer.toString(++permId));
            }
        } finally
        {
            conn.close();
        }
        System.out.println((System.currentTimeMillis() - start) / 1000.0);
    }

}
