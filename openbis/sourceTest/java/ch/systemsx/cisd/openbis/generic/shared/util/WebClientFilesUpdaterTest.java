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

package ch.systemsx.cisd.openbis.generic.shared.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.base.utilities.OSUtilities;

/**
 * Test cases for corresponding {@link WebClientFilesUpdater} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = WebClientFilesUpdater.class)
public final class WebClientFilesUpdaterTest extends AbstractFileSystemTestCase
{

    private static final String OPENBIS_GWT_XML_FILE_CONTENT =
            WebClientFilesUpdater.XML_MARKER_START + OSUtilities.LINE_SEPARATOR
                    + WebClientFilesUpdater.XML_MARKER_END;

    private static final String CLIENT_PLUGIN_PROVIDER_JAVA_FILE_CONTENT =
            WebClientFilesUpdater.JAVA_MARKER_START + OSUtilities.LINE_SEPARATOR
                    + WebClientFilesUpdater.JAVA_MARKER_END;

    private File openBISGwtXmlFile;

    private File clientPluginProviderJavaFile;

    private WebClientFilesUpdater filesUpdater;

    //
    // AbstractFileSystemTestCase
    //

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        openBISGwtXmlFile =
                new File(workingDirectory, WebClientFilesUpdater.OPENBIS_PACKAGE_NAME + "/"
                        + WebClientFilesUpdater.OPENBIS_GWT_XML_FILE_NAME);
        FileUtils.writeStringToFile(openBISGwtXmlFile, OPENBIS_GWT_XML_FILE_CONTENT);
        assertTrue(openBISGwtXmlFile.exists());
        clientPluginProviderJavaFile =
                new File(workingDirectory, WebClientFilesUpdater.CLIENT_PLUGIN_PROVIDER_CLASS
                        .replace(".", "/")
                        + ".java");
        FileUtils.writeStringToFile(clientPluginProviderJavaFile,
                CLIENT_PLUGIN_PROVIDER_JAVA_FILE_CONTENT);
        assertTrue(clientPluginProviderJavaFile.exists());
        final File pluginDir =
                new File(workingDirectory, WebClientFilesUpdater.OPENBIS_PACKAGE_NAME + "/"
                        + WebClientFilesUpdater.PLUGIN_PACKAGE_NAME);
        assertTrue(pluginDir.mkdir());
        assertTrue(new File(pluginDir, "screening").mkdir());
        filesUpdater = new WebClientFilesUpdater(workingDirectory.getPath());
    }

    @Test
    public final void testConstructor()
    {
        boolean fail = true;
        try
        {
            new WebClientFilesUpdater(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        new WebClientFilesUpdater(workingDirectory.getPath());
        try
        {
            new WebClientFilesUpdater(workingDirectory.getPath(), "dummy");
            fail("IllegalArgumentException expected.");
        } catch (final IllegalArgumentException ex)
        {
            assertEquals("Technology 'dummy' must be one of '[screening]'.", ex.getMessage());
        }
    }

    @Test
    public final void testUpdateOpenBISGwtXmlFile() throws IOException
    {
        assertEquals(OPENBIS_GWT_XML_FILE_CONTENT, FileUtils.readFileToString(openBISGwtXmlFile));
        filesUpdater.updateOpenBISGwtXmlFile();
        assertEquals("<!-- Automatically generated part - START -->" + OSUtilities.LINE_SEPARATOR
                + "    <!-- Screening plugin -->" + OSUtilities.LINE_SEPARATOR
                + "    <script src=\"screening-dictionary.js\"/>" + OSUtilities.LINE_SEPARATOR
                + "    <public path=\"plugin/screening/client/web/public\"/>"
                + OSUtilities.LINE_SEPARATOR
                + "    <source path=\"plugin/screening/client/web/client\"/>"
                + OSUtilities.LINE_SEPARATOR + "    <!-- Automatically generated part - END -->"
                + OSUtilities.LINE_SEPARATOR, FileUtils.readFileToString(openBISGwtXmlFile));
    }

    @Test
    public final void testUpdateClientPluginProvider() throws IOException
    {
        assertEquals(CLIENT_PLUGIN_PROVIDER_JAVA_FILE_CONTENT, FileUtils
                .readFileToString(clientPluginProviderJavaFile));
        filesUpdater.updateClientPluginProvider();
        assertEquals(
                "// Automatically generated part - START"
                        + OSUtilities.LINE_SEPARATOR
                        + "        registerPluginFactory(new ch.systemsx.cisd.openbis.plugin."
                        + "screening.client.web.client.application.ClientPluginFactory(originalViewContext));"
                        + OSUtilities.LINE_SEPARATOR
                        + "        // Automatically generated part - END"
                        + OSUtilities.LINE_SEPARATOR, FileUtils
                        .readFileToString(clientPluginProviderJavaFile));
    }
}