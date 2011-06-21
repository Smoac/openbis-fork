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

package ch.systemsx.cisd.etlserver.path;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.io.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.etlserver.IDataSetPathsInfoFeeder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DatabaseBasedDataSetPathsInfoFeederTest extends AbstractFileSystemTestCase
{
    private static final String ROOT_PATH = DatabaseBasedDataSetPathsInfoFeederTest.class.getName();
    
    private Mockery context;
    private IPathsInfoDAO dao;
    private IDataSetPathsInfoFeeder feeder;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        dao = context.mock(IPathsInfoDAO.class);
        feeder = new DatabaseBasedDataSetPathsInfoFeeder(dao, new DefaultFileBasedHierarchicalContentFactory());
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
    public void test()
    {
        final File dir = new File(workingDirectory, "dir");
        dir.mkdirs();
        FileUtilities.writeToFile(new File(dir, "hello.txt"), "hello world");
        FileUtilities.writeToFile(new File(dir, "read.me"), "nothing to read");
        new File(dir, "dir").mkdirs();
        FileUtilities.writeToFile(new File(workingDirectory, "read.me"), "hello reader");
        context.checking(new Expectations()
            {
                {
                    one(dao).createDataSet("ds-1", "a/b/c/");
                    will(returnValue(42L));
                    
                    one(dao).createDataSetFile(42L, null, "", ROOT_PATH, 38, true, new Date(1));
                    will(returnValue(100L));
                    
                    one(dao).createDataSetFile(42L, 100L, "dir", "dir", 26, true, new Date(2));
                    will(returnValue(101L));
                    
                    one(dao).createDataSetFile(42L, 101L, "dir/hello.txt", "hello.txt", 11, false, new Date(3));
                    will(returnValue(102L));
                    
                    one(dao).createDataSetFile(42L, 101L, "dir/read.me", "read.me", 15, false, new Date(4));
                    will(returnValue(103L));
                    
                    one(dao).createDataSetFile(42L, 101L, "dir/dir", "dir", 0, true, new Date(5));
                    will(returnValue(104L));
                    
                    one(dao).createDataSetFile(42L, 100L, "read.me", "read.me", 12, false, new Date(6));
                    will(returnValue(105L));
                }
            });
        
        feeder.addPaths("ds-1", "a/b/c/", workingDirectory);
    }
}
