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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.yeastx.db.AbstractDBTest;
import ch.systemsx.cisd.yeastx.db.DBUtils;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;

/**
 * @author Tomasz Pylak
 */
public class MzXmlDAOTest extends AbstractDBTest
{
    private IMzXmlDAO dao;

    @BeforeMethod(alwaysRun = true)
    public void setDAO() throws SQLException
    {
        dao = DBUtils.getQuery(datasource, IMzXmlDAO.class);
    }

    @AfterMethod(alwaysRun = true)
    public void close()
    {
        if (dao != null)
        {
            dao.close();
        }
    }

    @Test
    public void testUpload() throws SQLException
    {
        MzXml2Database uploader = new MzXml2Database(datasource);
        DMDataSetDTO dataSetDTO =
                new DMDataSetDTO("data set perm id mzXML", "sample perm id", "sample name",
                        "experiment perm id", "experiment name");
        uploader.upload(new File("resource/examples/example.mzXML"), dataSetDTO);
        uploader.commit();
        dataSetDTO.setPermId("data set perm id mzXML minimal");
        uploader.upload(new File("resource/examples/example-fake-minimal.mzXML"), dataSetDTO);
        uploader.commit();
        dataSetDTO.setPermId("data set perm id mzXML full");
        uploader.upload(new File("resource/examples/example-fake-full.mzXML"), dataSetDTO);
        uploader.commit();
    }

}
