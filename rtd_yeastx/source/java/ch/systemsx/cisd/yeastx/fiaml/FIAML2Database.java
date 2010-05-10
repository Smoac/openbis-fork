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

import ch.systemsx.cisd.yeastx.db.AbstractDatasetLoader;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;
import ch.systemsx.cisd.yeastx.fiaml.FIAMLParser.IMSRunObserver;

/**
 * Tool for uploading <code>fiaML</code> files to the database.
 * 
 * @author Bernd Rinn
 */
public class FIAML2Database extends AbstractDatasetLoader<IFIAMSRunDAO>
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

    public FIAML2Database(DataSource dataSource)
    {
        super(dataSource, IFIAMSRunDAO.class);
    }

    /**
     * Method for uploading an <var>fiaMLFile</var> to the database.
     */
    public void upload(final File fiaMLFile, final DMDataSetDTO dataSet) throws SQLException
    {
        try
        {
            createDataSet(dataSet);
            new FIAMLParser(fiaMLFile.getPath(), new IMSRunObserver()
                {
                    public void observe(FIAMSRunDTO run, FIAMSRunDataDTO runData)
                    {
                        run.setExperimentId(dataSet.getExperimentId());
                        run.setSampleId(dataSet.getSampleId());
                        run.setDataSetId(dataSet.getId());
                        final long fiaMsRunId = getDao().addMSRun(run);
                        getDao().addProfiles(fiaMsRunId, profileChunk(runData));
                        getDao()
                                .addCentroids(fiaMsRunId, runData.getCentroidMz(),
                                        runData.getCentroidIntensities(),
                                        runData.getCentroidCorrelations());
                    }
                });
        } catch (Throwable th)
        {
            rollbackAndRethrow(th);
        }
    }
}
