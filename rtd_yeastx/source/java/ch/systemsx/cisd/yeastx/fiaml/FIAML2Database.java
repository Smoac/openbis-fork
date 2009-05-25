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

package ch.systemsx.cisd.yeastx.fiaml;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.yeastx.db.DBFactory;
import ch.systemsx.cisd.yeastx.fiaml.FIAMLParser.IMSRunObserver;

/**
 * Tool for uploading <code>fiaML</code> files to the database.
 * 
 * @author Bernd Rinn
 */
public class FIAML2Database
{

    private final static int PROFILE_CHUNK_SIZE = 250;

    public static IFIAMSRunDAO getDAO(Connection conn)
    {
        return QueryTool.getQuery(conn, IFIAMSRunDAO.class);
    }

    private static Iterable<ProfileDTO> profileChunk(final FIAMSRunDataDTO runData)
    {
        return new Iterable<ProfileDTO>()
            {
                public Iterator<ProfileDTO> iterator()
                {
                    return new Iterator<ProfileDTO>()
                        {
                            int i = 0;

                            public boolean hasNext()
                            {
                                return i < runData.getProfileMz().length;
                            }

                            public ProfileDTO next()
                            {
                                final int imax =
                                        Math.min(runData.getProfileMz().length, i
                                                + PROFILE_CHUNK_SIZE);
                                final int imin = i;
                                i = imax;
                                return ProfileDTO.split(runData.getProfileMz(), runData
                                        .getProfileIntensities(), imin, imax);
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    /**
     * Method for uploading an <var>fiaMLFile</var> to the database.
     */
    public static void uploadFiaMLFile(final Connection conn, final File fiaMLFile,
            final String permId) throws SQLException
    {
        try
        {
            final IFIAMSRunDAO dao = getDAO(conn);
            new FIAMLParser(fiaMLFile.getPath(), permId, new IMSRunObserver()
                {
                    public void observe(FIAMSRunDTO run, FIAMSRunDataDTO runData)
                    {
                        final long fiaMsRunId = dao.addMSRun(run);
                        getDAO(conn).addProfiles(fiaMsRunId, profileChunk(runData));
                        getDAO(conn)
                                .addCentroids(fiaMsRunId, runData.getCentroidMz(),
                                        runData.getCentroidIntensities(),
                                        runData.getCentroidCorrelations());
                    }
                });
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

    public static void main(String[] args) throws SQLException
    {
        final long start = System.currentTimeMillis();
        final Connection conn = new DBFactory(DBFactory.createDefaultDBContext()).getConnection();
        try
        {
            final String dir = args[0];
            int permId = 0;
            for (String f : new File(dir).list(new FIAMLFilenameFilter()))
            {
                System.out.println(f);
                uploadFiaMLFile(conn, new File(dir, f), Integer.toString(++permId));
            }
        } finally
        {
            conn.close();
        }
        System.out.println((System.currentTimeMillis() - start) / 1000.0);
    }

}
