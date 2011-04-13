/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.dss.generic.server.DatabaseBasedDataSetPathInfoProvider.DataSetFileRecord;
import ch.systemsx.cisd.openbis.dss.generic.server.DatabaseBasedDataSetPathInfoProvider.IPathInfoDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=DatabaseBasedDataSetPathInfoProvider.class)
public class DatabaseBasedDataSetPathInfoProviderTest extends AssertJUnit
{
    private static final Long DATA_SET_ID = 41L;
    
    private Mockery context;
    private IPathInfoDAO dao;
    private IDataSetPathInfoProvider pathInfoProvider;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        dao = context.mock(IPathInfoDAO.class);
        pathInfoProvider = new DatabaseBasedDataSetPathInfoProvider(dao);
    }
    
    @AfterMethod
    public void tearDown(Method method)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(method.getName() + "() : ", t);
        }
    }
    
    @Test
    public void testListDataSetRootPathInfoForUnknownDataSet()
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).tryToGetDataSetId("ds-1");
                    will(returnValue(null));
                }
            });

        List<DataSetPathInfo> infos = pathInfoProvider.listDataSetRootPathInfos("ds-1");

        assertEquals(0, infos.size());
    }

    @Test
    public void testListDataSetRootPathInfoForEmptyResult()
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).tryToGetDataSetId("ds-1");
                    will(returnValue(DATA_SET_ID));

                    one(dao).listDataSetFiles(DATA_SET_ID);
                    will(returnValue(Arrays.asList()));
                }
            });

        List<DataSetPathInfo> infos = pathInfoProvider.listDataSetRootPathInfos("ds-1");

        assertEquals(0, infos.size());
    }
    
    @Test
    public void testListDataSetRootPathInfoWithTwoResults()
    {
        final DataSetFileRecord r1 = record(1, 2L, "dir/text.txt", 23, false);
        final DataSetFileRecord r2 = record(2, null, "dir", 53, true);
        final DataSetFileRecord r3 = record(3, 2L, "dir/dir", 30, true);
        final DataSetFileRecord r4 = record(4, 3L, "dir/dir/hello", 3, false);
        final DataSetFileRecord r5 = record(5, 3L, "dir/dir/hi", 27, false);
        final DataSetFileRecord r6 = record(6, null, "dir2", 0, true);
        final DataSetFileRecord r7 = record(7, null, "read.me", 23, false);
        context.checking(new Expectations()
            {
                {
                    one(dao).tryToGetDataSetId("ds-1");
                    will(returnValue(DATA_SET_ID));

                    one(dao).listDataSetFiles(DATA_SET_ID);
                    will(returnValue(Arrays.asList(r1, r2, r3, r4, r5, r6, r7)));
                }
            });
        
        List<DataSetPathInfo> infos = pathInfoProvider.listDataSetRootPathInfos("ds-1");
        
        check("dir", true, 53, infos.get(0));
        check("dir/text.txt", false, 23, infos.get(0).getChildren().get(0));
        check("dir/dir", true, 30, infos.get(0).getChildren().get(1));
        check("dir/dir/hello", false, 3, infos.get(0).getChildren().get(1).getChildren().get(0));
        check("dir/dir/hi", false, 27, infos.get(0).getChildren().get(1).getChildren().get(01));
        check("dir2", true, 0, infos.get(1));
        check("read.me", false, 23, infos.get(2));
        assertEquals(3, infos.size());
    }
    
    private void check(String expectedRelativePath, boolean expectingDirectory, long expectedSize,
            DataSetPathInfo info)
    {
        assertEquals(expectedRelativePath, info.getRelativePath());
        assertEquals(expectingDirectory, info.isDirectory());
        assertEquals(expectedSize, info.getSizeInBytes());
        List<DataSetPathInfo> children = info.getChildren();
        for (DataSetPathInfo child : children)
        {
            assertSame(info, child.getParent());
        }
    }

    private DatabaseBasedDataSetPathInfoProvider.DataSetFileRecord record(long id, Long parentId,
            String relativePath, long size, boolean directory)
    {
        DatabaseBasedDataSetPathInfoProvider.DataSetFileRecord record =
                new DatabaseBasedDataSetPathInfoProvider.DataSetFileRecord();
        record.id = id;
        record.parent_id = parentId;
        record.relative_path = relativePath;
        record.size_in_bytes = size;
        record.is_directory = directory;
       return record;
    }
}
