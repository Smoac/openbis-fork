/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import org.apache.commons.lang.time.StopWatch;
import org.testng.annotations.Test;

import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "slow")
public class PathInfoDatabaseTest extends SystemTestCase
{
    @Test
    public void testPathInfoDatabase() throws Exception
    {
        DataSource dataSource = PathInfoDataSourceProvider.getDataSource();
        cleanUpDatabase(dataSource);
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int n = feedDataBase(dataSource);
        System.out.println(stopWatch.getTime() + " msec for creating " + n + " entries");
        
        stopWatch.reset();
        List<String> paths = new ArrayList<String>();
        Connection connection = null;
        String regex = ".*file-[2-3].*81.*(25|49).*36.*";
        try
        {
            connection = dataSource.getConnection();
            stopWatch.start();
            PreparedStatement s = connection.prepareStatement("select * from data_set_files " +
            		"where relative_path ~ ?");
            System.out.println(stopWatch.getTime() + " msec for preparing SQL statement "+stopWatch);
            s.setString(1, regex);
            ResultSet rs = s.executeQuery();
            while (rs.next())
            {
                paths.add(rs.getString("relative_path"));
            }
            System.out.println(stopWatch.getTime()+" msec for PostgreSQL regex search");
            Collections.sort(paths);
            assertEquals("[file-2-4/file-9-81/file-5-25/file-6-36-xyz.xml, "
                    + "file-2-4/file-9-81/file-7-49/file-6-36-xyz.xml, "
                    + "file-3-9/file-9-81/file-5-25/file-6-36-xyz.xml, "
                    + "file-3-9/file-9-81/file-7-49/file-6-36-xyz.xml]", paths.toString());
        } finally
        {
            close(connection);
        }
        
        stopWatch.reset();
        stopWatch.start();
        List<DataSetPathInfo> results =
                ServiceProvider.getDataSetPathInfoProvider().listPathInfosByRegularExpression("ds-1", regex);
        System.out.println(stopWatch.getTime() + " msec for reading db with regex");
        paths.clear();
        for (DataSetPathInfo info : results)
        {
            paths.add(info.getRelativePath());
        }
        Collections.sort(paths);
        assertEquals("[file-2-4/file-9-81/file-5-25/file-6-36-xyz.xml, "
                + "file-2-4/file-9-81/file-7-49/file-6-36-xyz.xml, "
                + "file-3-9/file-9-81/file-5-25/file-6-36-xyz.xml, "
                + "file-3-9/file-9-81/file-7-49/file-6-36-xyz.xml]", paths.toString());
        
        stopWatch.reset();
        stopWatch.start();
        final Pattern pattern = Pattern.compile(regex);
        DataSetPathInfo root = ServiceProvider.getDataSetPathInfoProvider().tryGetDataSetRootPathInfo("ds-1");
        System.out.println(stopWatch.getTime() + " msec for reading db");
        results.clear();
        search(results, root, pattern);
        System.out.println(stopWatch.getTime() + " msec for reading db and searching");
        paths.clear();
        for (DataSetPathInfo info : results)
        {
            paths.add(info.getRelativePath());
        }
        Collections.sort(paths);
        assertEquals("[file-2-4/file-9-81/file-5-25/file-6-36-xyz.xml, "
                + "file-2-4/file-9-81/file-7-49/file-6-36-xyz.xml, "
                + "file-3-9/file-9-81/file-5-25/file-6-36-xyz.xml, "
                + "file-3-9/file-9-81/file-7-49/file-6-36-xyz.xml]", paths.toString());
    }

    private int feedDataBase(DataSource dataSource)
    {
        int numberOfEntries = 0;
        IPathsInfoDAO dao = QueryTool.getQuery(dataSource, IPathsInfoDAO.class);
        try
        {
            long id = dao.createDataSet("ds-1", "a/b/c/");
            long parentId = dao.createDataSetFile(id, null, "", "ds-1", 0, true);
            numberOfEntries += feedDataBase(dao, id, parentId, 3, "");
            dao.commit();
        } catch (Exception ex)
        {
            dao.rollback();
            ex.printStackTrace();
        }
        dao.close();
        return numberOfEntries;
    }

    private int feedDataBase(IPathsInfoDAO dao, long dataSetId, Long parentId, int level,
            String prefix)
    {
        int numberOfEntries = 0;
        for (int i = 0; i < 10; i++)
        {
            boolean directory = level > 0;
            String fileName = "file-" + i + "-" + (i * i) + (directory ? "" : "-xyz.xml");
            long id =
                    dao.createDataSetFile(dataSetId, parentId, prefix + fileName, fileName, level
                            * 100 + i, directory);
            numberOfEntries++;
            if (directory)
            {
                numberOfEntries +=
                        feedDataBase(dao, dataSetId, id, level - 1, prefix + fileName + "/");
            }
        }
        return numberOfEntries;
    }

    private void search(List<DataSetPathInfo> results, DataSetPathInfo info, Pattern pattern)
    {
        if (pattern.matcher(info.getRelativePath()).matches())
        {
            results.add(info);
        }
        List<DataSetPathInfo> children = info.getChildren();
        for (DataSetPathInfo child : children)
        {
            search(results, child, pattern);
        }
    }
    
    private void cleanUpDatabase(DataSource dataSource) throws SQLException
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
            connection.createStatement().execute("delete from data_sets where code like 'ds-1'");
        } finally
        {
            close(connection);
        }
    }

    private void close(Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
}
