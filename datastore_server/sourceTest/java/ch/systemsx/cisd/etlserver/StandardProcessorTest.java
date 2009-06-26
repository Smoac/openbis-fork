/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.io.IOException;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.PathPrefixPrepender;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcessingInstructionDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * Test cases for the {@link StandardProcessor}.
 * 
 * @author Christian Ribeaud
 */
public final class StandardProcessorTest extends AbstractFileSystemTestCase
{
    private final static class AbsolutPathMatcher extends BaseMatcher<String>
    {
        private final String absolutePath;

        AbsolutPathMatcher(final String absolutePath)
        {
            this.absolutePath = absolutePath;
        }

        //
        // BaseMatcher
        //

        public final void describeTo(final Description description)
        {
            description.appendText(absolutePath);
        }

        public final boolean matches(final Object item)
        {
            if (item instanceof String == false)
            {
                return false;
            }
            final String path = (String) item;
            return path.replace('\\', '/').equals(absolutePath.replace('\\', '/'));
        }
    }

    private static final String PROCESSING_PATH = "processing";

    private static final String PREFIX_FOR_RELATIVE_PATH = "rel";

    private static final String PREFIX_FOR_ABSOLUTE_PATH = null;

    private static final String FINISHED_FILE_NAME_TEMPLATE = ".MARKER_is_finished_{0}";

    private static final String PARAMETERS_FILE_NAME = "parameters";

    private IProcessor processor;

    private Mockery context;

    private IFileFactory fileFactory;

    private PathPrefixPrepender pathPrefixPrepender;

    private IFile iFile;

    @Override
    @BeforeMethod
    public final void setUp() throws IOException
    {
        super.setUp();
        context = new Mockery();
        fileFactory = context.mock(IFileFactory.class);
        iFile = context.mock(IFile.class);
        pathPrefixPrepender = createPathPrefixPrepender();
        processor = createStandardProcessor();
    }

    @AfterMethod
    public void tearDown()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private final PathPrefixPrepender createPathPrefixPrepender()
    {
        final File file = new File(workingDirectory, PREFIX_FOR_RELATIVE_PATH);
        assertEquals(true, file.mkdir());
        assertTrue(file.exists());
        assertTrue(file.isDirectory());
        return new PathPrefixPrepender(PREFIX_FOR_ABSOLUTE_PATH, file.getAbsolutePath());
    }

    private final IProcessor createStandardProcessor()
    {
        return new StandardProcessor(fileFactory, StorageFormat.PROPRIETARY, pathPrefixPrepender,
                PARAMETERS_FILE_NAME, FINISHED_FILE_NAME_TEMPLATE, "_");
    }

    private final ProcessingInstructionDTO createProcessingInstruction()
    {
        final ProcessingInstructionDTO processingInstruction = new ProcessingInstructionDTO();
        processingInstruction.setPath(PROCESSING_PATH);
        return processingInstruction;
    }

    @Test
    public final void testInitiateProcessingWithNullParameters()
    {
        try
        {
            processor.initiateProcessing(null, null, null);
            fail("Null parameters not allowed here.");
        } catch (final AssertionError ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testInitiateProcessingWithNotSuitableDirectory()
    {
        final File dataSet = new File("dataSet");
        final String absolutePath =
                new File(new File(workingDirectory, PREFIX_FOR_RELATIVE_PATH), PROCESSING_PATH)
                        .getAbsolutePath();
        context.checking(new Expectations()
            {
                {
                    one(fileFactory).create(with(new AbsolutPathMatcher(absolutePath)));
                    will(returnValue(iFile));

                    one(iFile).check();
                    will(throwException(new ConfigurationFailureException("")));
                }
            });
        try
        {
            processor.initiateProcessing(createProcessingInstruction(), new DataSetInformation(),
                    dataSet);
            fail("Configuration failure exception.");
        } catch (final ConfigurationFailureException ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testInitiateProcessing()
    {
        final String dataSetName = "dataSet";
        final File dataSet = new File(dataSetName);
        final String absolutePath =
                new File(new File(workingDirectory, PREFIX_FOR_RELATIVE_PATH), PROCESSING_PATH)
                        .getAbsolutePath();
        final DataSetInformation dataSetInformation = new DataSetInformation();
        final String dataSetCode = "data-set-code";
        dataSetInformation.setDataSetCode(dataSetCode);
        final String dataSetFullName = dataSetCode + "_" + dataSetName;
        context.checking(new Expectations()
            {
                {
                    one(fileFactory).create(with(new AbsolutPathMatcher(absolutePath)));
                    will(returnValue(iFile));

                    one(iFile).check();

                    one(fileFactory).create(iFile, dataSetFullName);
                    one(fileFactory).create(iFile, ".MARKER_is_finished_" + dataSetFullName);
                }
            });
        processor.initiateProcessing(createProcessingInstruction(), dataSetInformation, dataSet);
        context.assertIsSatisfied();
    }

}