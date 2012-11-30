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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.etlserver.registrator.api.RollbackStack;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class AbstractTestWithRollbackStack extends AbstractFileSystemTestCase
{
    protected File queue1File;

    protected File queue2File;

    protected RollbackStack rollbackStack;

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();

        queue1File = new File(workingDirectory, "queue1.dat");
        queue2File = new File(workingDirectory, "queue2.dat");

        rollbackStack = new RollbackStack(queue1File, queue2File);
    }

}
