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
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    private static void addProfilesForRun(final Connection conn, final long fiaMsRunId,
            FIAMSRunDataDTO runData) throws SQLException
    {
        try
        {
            final PreparedStatement prepProfile =
                    conn.prepareStatement("INSERT INTO profiles "
                            + "(fiaMsRunId, lowMz, highMz, mz, "
                            + "intensities) values (?, ?, ?, ?, ?)");
            final float[] mz = runData.getProfileMz();
            final float[] intensities = runData.getProfileIntensities();
            for (int i = 0; i < mz.length; i += PROFILE_CHUNK_SIZE)
            {
                final int imax = Math.min(mz.length, i + PROFILE_CHUNK_SIZE);
                prepProfile.setLong(1, fiaMsRunId);
                prepProfile.setFloat(2, mz[i]);
                prepProfile.setFloat(3, mz[imax - 1]);
                prepProfile.setString(4, toString(mz, i, imax));
                prepProfile.setString(5, toString(intensities, i, imax));
                prepProfile.addBatch();
            }
            prepProfile.executeBatch();
        } catch (BatchUpdateException ex)
        {
            throw ex.getNextException();
        }
    }

    private static String toString(float[] array, int start, int end)
    {
        StringBuilder b = new StringBuilder();
        for (int i = start; i < end; ++i)
        {
            b.append(array[i]);
            b.append(',');
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

    private static void addCentroidsForRun(final Connection conn, final long fiaMsRunId,
            FIAMSRunDataDTO runData) throws SQLException
    {
        try
        {
            final PreparedStatement prepCentroid =
                    conn.prepareStatement("INSERT INTO centroids (fiaMsRunId, mz, "
                            + "intensity, correlation) values (?, ?, ?, ?)");
            for (int i = 0; i < runData.getCentroidMz().length; ++i)
            {
                prepCentroid.setLong(1, fiaMsRunId);
                prepCentroid.setFloat(2, runData.getCentroidMz()[i]);
                prepCentroid.setFloat(3, runData.getCentroidIntensities()[i]);
                prepCentroid.setFloat(4, runData.getCentroidCorrelations()[i]);
                prepCentroid.addBatch();
            }
            prepCentroid.executeBatch();
        } catch (BatchUpdateException ex)
        {
            throw ex.getNextException();
        }
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
                        try
                        {
                            addProfilesForRun(conn, fiaMsRunId, runData);
                            addCentroidsForRun(conn, fiaMsRunId, runData);
                        } catch (SQLException ex)
                        {
                            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                        }
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
        final Connection conn = DBFactory.getConnection();
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
