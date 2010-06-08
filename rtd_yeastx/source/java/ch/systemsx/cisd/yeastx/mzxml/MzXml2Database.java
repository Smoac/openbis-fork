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

package ch.systemsx.cisd.yeastx.mzxml;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.yeastx.db.AbstractDatasetLoader;
import ch.systemsx.cisd.yeastx.db.generic.DMDataSetDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzPrecursorDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzRunDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzScanDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzXmlDTO;
import ch.systemsx.cisd.yeastx.utils.JaxbXmlParser;

/**
 * Tool for uploading <code>mzXML</code> files to the database.
 * 
 * @author Tomasz Pylak
 */
public class MzXml2Database extends AbstractDatasetLoader<IMzXmlDAO>
{
    public MzXml2Database(DataSource datasource)
    {
        super(datasource, IMzXmlDAO.class);
    }

    @Private
    // for tests only
    MzXml2Database(DataSource dataSource, IMzXmlDAO dao)
    {
        super(dataSource, IMzXmlDAO.class, dao);
    }

    /**
     * Method for uploading an <var>mzXML</var> to the database.
     */
    public void upload(final File file, final DMDataSetDTO dataSet) throws SQLException
    {
        MzXmlDTO mzXml = JaxbXmlParser.parse(MzXmlDTO.class, file, false);
        uploadFile(mzXml, dataSet);
    }

    private void uploadFile(final MzXmlDTO mzXml, final DMDataSetDTO dataSet) throws SQLException
    {
        try
        {
            createDataSet(dataSet);
            uploadRun(mzXml.getRun(), dataSet);
        } catch (Throwable th)
        {
            rollbackAndRethrow(th);
        }
    }

    @Private
    void uploadRun(MzRunDTO run, DMDataSetDTO dataSet)
    {
        long runId = getDao().addRun(dataSet, run.getInstrument());
        List<MzScanDTO> scans = run.getScans();
        for (MzScanDTO scan : scans)
        {
            uploadScan(runId, scan);
        }
    }

    private void uploadScan(long runId, MzScanDTO scan)
    {
        List<MzPrecursorDTO> precursors = scan.getPrecursors();
        MzPrecursorDTO precursor1 = new MzPrecursorDTO();
        MzPrecursorDTO precursor2 = new MzPrecursorDTO();
        if (precursors != null)
        {
            if (precursors.size() > 0)
            {
                precursor1 = precursors.get(0);
            }
            if (precursors.size() > 1)
            {
                precursor2 = precursors.get(1);
            }
            if (precursors.size() > 2)
            {
                throw UserFailureException
                        .fromTemplate(
                                "Scan number '%d' has %d precursors and at most 2 are supported by the data model",
                                scan.getNumber(), precursors.size());
            }
        }
        getDao().addScan(runId, scan, precursor1, precursor2);
    }

}
