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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.IDataSetPathsInfoFeeder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DatabaseBasedDataSetPathsInfoFeederTest extends AbstractFileSystemTestCase
{
    private Mockery context;
    private IPathsInfoDAO dao;
    private IDataSetPathsInfoFeeder feeder;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        dao = context.mock(IPathsInfoDAO.class);
        feeder = new DatabaseBasedDataSetPathsInfoFeeder(dao);
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
        File dir = new File(workingDirectory, "dir");
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
                    
                    one(dao).createDataSetFile(42L, null, "dir", 1, 26, true);
                    will(returnValue(101L));
                    
                    one(dao).createDataSetFile(42L, 101L, "dir/hello.txt", 2, 11, false);
                    will(returnValue(102L));
                    
                    one(dao).createDataSetFile(42L, 101L, "dir/read.me", 2, 15, false);
                    will(returnValue(103L));
                    
                    one(dao).createDataSetFile(42L, 101L, "dir/dir", 2, 0, true);
                    will(returnValue(104L));
                    
                    one(dao).createDataSetFile(42L, null, "read.me", 1, 12, false);
                    will(returnValue(105L));
                }
            });
        
        feeder.addPaths("ds-1", "a/b/c/", workingDirectory);
    }
}
