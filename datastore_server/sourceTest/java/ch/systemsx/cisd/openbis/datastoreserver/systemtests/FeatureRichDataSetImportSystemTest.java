/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author Jakub Straszewski
 */
public class FeatureRichDataSetImportSystemTest extends SystemTestCase
{
    // for jython script go to sourceTest/core-plugins/generic-test/1/dss/drop-boxes/rich-test

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-rich-test");
    }

    @Test
    public void testRichImport() throws Exception
    {

        File exampleDataSet = new File(workingDirectory, "my-data");
        createExampleDataSet(exampleDataSet);
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported();

        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        Thread.sleep(1000);

        assertSpaceProjectExperiment(openBISService);

        assertLinkedDataSetImported(openBISService);

        assert100MaterialsCreated(openBISService);

        assertEmailHasBeenSentFromHook();

        assertMaterialUpdated(openBISService);
    }

    private void assertEmailHasBeenSentFromHook()
    {
        File emailDirectory = new File(new File(workingDirectory, "dss-root"), "email");

        for (File f : FileUtilities.listFiles(emailDirectory))
        {
            String content = FileUtilities.loadExactToString(f);
            if (content.contains("post_metadata_registration rich"))
            {
                assertTrue(content, content.contains("rich_email_text")); // check the content
                                                                          // introduced
                // with the persistent map
                return; // assert ok
            }
        }
        fail("No email found!");
    }

    private void assertSpaceProjectExperiment(IEncapsulatedOpenBISService openBISService)
    {
        List<Project> projects = openBISService.listProjects();
        boolean notFound = true;
        for (Project p : projects)
        {
            if (p.getIdentifier().equals("/RICH_SPACE/RICH_PROJECT"))
            {
                notFound = false;
                assertEquals("RICH_SPACE", p.getSpace().getCode());
            }
        }
        if (notFound)
        {
            fail("No registered project found!");
        }

        List<Experiment> experiments =
                openBISService.listExperiments(new ProjectIdentifier("RICH_SPACE", "RICH_PROJECT"));

        assertEquals(1, experiments.size());
        Experiment experiment = experiments.get(0);

        assertEquals("RICH_EXPERIMENT", experiment.getCode());
    }

    private void assert100MaterialsCreated(IEncapsulatedOpenBISService openBISService)
    {
        LinkedList<MaterialIdentifier> ids = new LinkedList<MaterialIdentifier>();

        for (int i = 0; i < 100; i++)
        {
            MaterialIdentifier ident = MaterialIdentifier.tryParseIdentifier("RM_" + i + " (GENE)");
            ids.add(ident);
        }

        ListMaterialCriteria criteria = ListMaterialCriteria.createFromMaterialIdentifiers(ids);
        List<Material> materials = openBISService.listMaterials(criteria, true);

        assertEquals(100, materials.size());

        for (Material m : materials)
        {
            String code = m.getCode();
            String expectedGeneSymbol = code + "_S";
            IEntityProperty property = m.getProperties().get(0);
            assertEquals("GENE_SYMBOL", property.getPropertyType().getCode());
            assertEquals(expectedGeneSymbol, property.getValue());
        }

    }

    private void assertMaterialUpdated(IEncapsulatedOpenBISService openBISService)
    {
        LinkedList<MaterialIdentifier> ids = new LinkedList<MaterialIdentifier>();
        MaterialIdentifier ident = MaterialIdentifier.tryParseIdentifier("AD3 (VIRUS)");
        ids.add(ident);

        ListMaterialCriteria criteria = ListMaterialCriteria.createFromMaterialIdentifiers(ids);

        List<Material> materials = openBISService.listMaterials(criteria, true);

        assertEquals(1, materials.size());

        for (Material m : materials)
        {
            IEntityProperty property = m.getProperties().get(0);
            assertEquals("DESCRIPTION", property.getPropertyType().getCode());
            assertEquals("modified description", property.getValue());
        }
    }

    private void assertLinkedDataSetImported(IEncapsulatedOpenBISService openBISService)
    {
        ExternalData a = listOneDataSet(openBISService, "FR_LINK_CODE");

        assertTrue("The imported dataset should be isLinkData", a.isLinkData());
        assertTrue("The imported dataset should be LinkDataSet", a instanceof LinkDataSet);

        LinkDataSet link = (LinkDataSet) a;

        assertEquals("External code", "EX_CODE", link.getExternalCode());
        assertEquals("External data management system", "DMS_1", link
                .getExternalDataManagementSystem().getCode());
    }

    /**
     * List exactly one dataset. assert that it exists.
     */
    private ExternalData listOneDataSet(IEncapsulatedOpenBISService openBISService, String code)
    {
        List<String> codes = new LinkedList<String>();
        codes.add(code);
        List<ExternalData> x = openBISService.listDataSetsByCode(codes);

        assertEquals("Exactly one data set expected.", 1, x.size());

        return x.get(0);
    }

    private void createExampleDataSet(File exampleDataSet)
    {
        exampleDataSet.mkdirs();
        FileUtilities.writeToFile(new File(exampleDataSet, "set1.txt"), "hello world");
        FileUtilities.writeToFile(new File(exampleDataSet, "set1.txt"), "hello world");
    }

}
