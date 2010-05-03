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
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import ch.systemsx.cisd.yeastx.db.AbstractDatasetLoader;
import ch.systemsx.cisd.yeastx.db.DBUtils;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;
import ch.systemsx.cisd.yeastx.db.IGenericDAO;
import ch.systemsx.cisd.yeastx.eicml.EICMLParser.IChromatogramObserver;
import ch.systemsx.cisd.yeastx.eicml.EICMLParser.IMSRunObserver;

/**
 * Tool for uploading <code>eicML</code> files to the database.
 * 
 * @author Bernd Rinn
 */
public class EICML2Database extends AbstractDatasetLoader
{

    private final static int CHROMATOGRAM_BATCH_SIZE = 100;

    private static void addChromatograms(IEICMSRunDAO dao, long eicMLId,
            List<ChromatogramDTO> chromatograms, int threshold)
    {
        if (chromatograms.size() >= threshold)
        {
            dao.addChromatograms(eicMLId, chromatograms);
            chromatograms.clear();
        }
    }

    private final IEICMSRunDAO dao;

    public EICML2Database(DataSource datasource)
    {
        this.dao = DBUtils.getQuery(datasource, IEICMSRunDAO.class);
    }

    /**
     * Method for uploading an <var>eicMLFile</var> to the database.
     */
    public void upload(final File eicMLFile, final DMDataSetDTO dataSet)
    {
        final long[] eicMLId = new long[1];
        final List<ChromatogramDTO> chromatograms =
                new ArrayList<ChromatogramDTO>(CHROMATOGRAM_BATCH_SIZE);
        try
        {
            createDataSet(dataSet);
            new EICMLParser(eicMLFile.getPath(), new IMSRunObserver()
                {
                    public void observe(EICMSRunDTO run)
                    {
                        // add chromatograms from the last run to the database before setting the
                        // new run id
                        addChromatograms(dao, eicMLId[0], chromatograms, 1);
                        run.setExperimentId(dataSet.getExperimentId());
                        run.setSampleId(dataSet.getSampleId());
                        run.setDataSetId(dataSet.getId());
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
        } catch (Throwable th)
        {
            rollbackAndRethrow(th);
        }
    }

    @Override
    protected IGenericDAO getDao()
    {
        return dao;
    }
}
