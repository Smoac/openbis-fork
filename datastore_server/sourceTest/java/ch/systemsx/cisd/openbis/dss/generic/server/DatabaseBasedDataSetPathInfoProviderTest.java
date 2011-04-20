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
import java.util.Collections;
import java.util.Comparator;
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
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DatabaseBasedDataSetPathInfoProvider.class)
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

        DataSetPathInfo info = pathInfoProvider.tryGetFullDataSetRootPathInfo("ds-1");

        assertEquals(null, info);
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

        DataSetPathInfo info = pathInfoProvider.tryGetFullDataSetRootPathInfo("ds-1");

        assertEquals(null, info);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testListDataSetRootPathInfoWithTwoResults()
    {
        final DataSetFileRecord r1 = record(1, 2L, "dir/text.txt", "text.txt", 23, false);
        final DataSetFileRecord r2 = record(2, null, "dir", "dir", 53, true);
        final DataSetFileRecord r3 = record(3, 2L, "dir/dir", "dir", 30, true);
        final DataSetFileRecord r4 = record(4, 3L, "dir/dir/hello", "hello", 3, false);
        final DataSetFileRecord r5 = record(5, 3L, "dir/dir/hi", "hi", 27, false);
        final DataSetFileRecord r6 = record(6, 2L, "dir/dir2", "dir2", 0, true);
        context.checking(new Expectations()
            {
                {
                    one(dao).tryToGetDataSetId("ds-1");
                    will(returnValue(DATA_SET_ID));

                    one(dao).listDataSetFiles(DATA_SET_ID);
                    will(returnValue(Arrays.asList(r1, r2, r3, r4, r5, r6)));
                }
            });

        DataSetPathInfo info = pathInfoProvider.tryGetFullDataSetRootPathInfo("ds-1");

        check("dir", "dir", true, 53, info);
        check("dir/text.txt", "text.txt", false, 23, info.getChildren().get(0));
        check("dir/dir", "dir", true, 30, info.getChildren().get(1));
        check("dir/dir/hello", "hello", false, 3, info.getChildren().get(1).getChildren().get(0));
        check("dir/dir/hi", "hi", false, 27, info.getChildren().get(1).getChildren().get(1));
        check("dir/dir2", "dir2", true, 0, info.getChildren().get(2));
    }

    @Test
    public void testPathInfosByRelativePathRegex()
    {
        final String regex = "blabla";
        final DataSetFileRecord r1 = record(1, 2L, "dir/text.txt", "text.txt", 23, false);
        final DataSetFileRecord r2 = record(2, null, "dir", "dir", 53, true);
        context.checking(new Expectations()
            {
                {
                    one(dao).tryToGetDataSetId("ds-1");
                    will(returnValue(DATA_SET_ID));

                    one(dao).listDataSetFilesByRelativePathRegex(DATA_SET_ID, "^" + regex + "$");
                    will(returnValue(Arrays.asList(r1, r2)));
                }
            });

        List<DataSetPathInfo> list =
                pathInfoProvider.listPathInfosByRegularExpression("ds-1", regex);
        Collections.sort(list, new Comparator<DataSetPathInfo>()
            {
                public int compare(DataSetPathInfo i1, DataSetPathInfo i2)
                {
                    return i1.getRelativePath().compareTo(i2.getRelativePath());
                }
            });
        check("dir", "dir", true, 53, list.get(0));
        check("dir/text.txt", "text.txt", false, 23, list.get(1));
        assertEquals(2, list.size());
    }

    @SuppressWarnings("deprecation")
    private void check(String expectedRelativePath, String expectedFileName,
            boolean expectingDirectory, long expectedSize, DataSetPathInfo info)
    {
        assertEquals(expectedRelativePath, info.getRelativePath());
        assertEquals(expectedFileName, info.getFileName());
        assertEquals(expectingDirectory, info.isDirectory());
        assertEquals(expectedSize, info.getSizeInBytes());
        List<DataSetPathInfo> children = info.getChildren();
        for (DataSetPathInfo child : children)
        {
            assertSame(info, child.getParent());
        }
    }

    private DatabaseBasedDataSetPathInfoProvider.DataSetFileRecord record(long id, Long parentId,
            String relativePath, String fileName, long size, boolean directory)
    {
        DatabaseBasedDataSetPathInfoProvider.DataSetFileRecord record =
                new DatabaseBasedDataSetPathInfoProvider.DataSetFileRecord();
        record.id = id;
        record.parent_id = parentId;
        record.file_name = fileName;
        record.relative_path = relativePath;
        record.size_in_bytes = size;
        record.is_directory = directory;
        return record;
    }
}
