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
import java.sql.SQLException;
import java.util.Iterator;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.TransactionQuery;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.yeastx.db.DBUtils;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;
import ch.systemsx.cisd.yeastx.fiaml.FIAMLParser.IMSRunObserver;

/**
 * Tool for uploading <code>fiaML</code> files to the database.
 * 
 * @author Bernd Rinn
 */
public class FIAML2Database
{

    private final static int PROFILE_CHUNK_SIZE = 250;

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

    private final IFIAMSRunDAO dao;

    public FIAML2Database(DataSource datasource)
    {
        this.dao = QueryTool.getQuery(datasource, IFIAMSRunDAO.class);
    }

    /**
     * Method for uploading an <var>fiaMLFile</var> to the database.
     */
    public void uploadFiaMLFile(final File fiaMLFile, final DMDataSetDTO dataSet)
            throws SQLException
    {
        TransactionQuery transaction = null;
        try
        {
            transaction = dao;
            DBUtils.createDataSet(dao, dataSet);
            new FIAMLParser(fiaMLFile.getPath(), new IMSRunObserver()
                {
                    public void observe(FIAMSRunDTO run, FIAMSRunDataDTO runData)
                    {
                        run.setExperimentId(dataSet.getExperimentId());
                        run.setSampleId(dataSet.getSampleId());
                        run.setDataSetId(dataSet.getId());
                        final long fiaMsRunId = dao.addMSRun(run);
                        dao.addProfiles(fiaMsRunId, profileChunk(runData));
                        dao.addCentroids(fiaMsRunId, runData.getCentroidMz(), runData
                                .getCentroidIntensities(), runData.getCentroidCorrelations());
                    }
                });
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

    public static void main(String[] args) throws SQLException
    {
        final long start = System.currentTimeMillis();
        final FIAML2Database fiaML2Database =
                new FIAML2Database(DBUtils.createDefaultDBContext().getDataSource());
        final String dir = args[0];
        int permId = 0;
        for (String f : new File(dir).list(new FIAMLFilenameFilter()))
        {
            System.out.println(f);
            fiaML2Database.uploadFiaMLFile(new File(dir, f), new DMDataSetDTO(Integer
                    .toString(++permId), "sample perm id", "sample name", "experiment perm id",
                    "experiment name"));
        }
        System.out.println((System.currentTimeMillis() - start) / 1000.0);
    }

}
