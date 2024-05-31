package ch.ethz.sis.openbis.systemtests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.systemtests.common.AbstractIntegrationTest;

public class IntegrationSessionWorkspaceTest extends AbstractIntegrationTest
{

    @Test
    public void testUploadToSessionWorkspace() throws Exception
    {
        final OpenBIS openBIS = createOpenBIS();
        final String sessionToken = openBIS.login(USER, PASSWORD);

        final Path originalFilePath = Path.of("sourceTest/java/tests.xml");

        // Testing upload

        final String uploadId = openBIS.uploadToSessionWorkspace(originalFilePath);

        // Verifying upload ID

        assertTrue(uploadId.endsWith("tests.xml"));

        // Verifying file info

        final Path uploadedFilePath = Path.of(String.format("targets/sessionWorkspace/%s/tests.xml", sessionToken));
        final File originalFile = originalFilePath.toFile();
        final File uploadedFile = uploadedFilePath.toFile();

        assertTrue(uploadedFile.exists());
        assertEquals(uploadedFile.length(), originalFile.length());

        // Verifying file content

        final byte[] originalFileContent = Files.readAllBytes(originalFilePath);
        final byte[] uploadedFileContent = Files.readAllBytes(uploadedFilePath);

        assertEquals(uploadedFileContent, originalFileContent);

        openBIS.logout();
    }

    @Test
    public void testImport() throws Exception
    {
        final OpenBIS openBIS = createOpenBIS();
        final String fileName = "import-test.zip";
        final Path originalFilePath = Path.of("sourceTest/java/ch/ethz/sis/openbis/systemtests/" + fileName);

        openBIS.login(USER, PASSWORD);
        openBIS.uploadToSessionWorkspace(originalFilePath);

        // Executing import

        final List<IObjectId> objectIds = openBIS.executeImport(new ImportData(ImportFormat.EXCEL, fileName),
                new ImportOptions(ImportMode.UPDATE_IF_EXISTS)).getObjectIds();

        // Verifying imported sample

        final List<ISampleId> sampleIdentifiers = objectIds.stream().filter(objectId -> objectId instanceof ISampleId)
                .map(objectId -> (ISampleId) objectId).collect(Collectors.toList());

        System.out.println("objectIds: " + objectIds);
        assertEquals(sampleIdentifiers.size(), 1);

        final SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
        sampleFetchOptions.withProperties();
        final Sample sample = openBIS.getSamples(sampleIdentifiers, sampleFetchOptions).values().iterator().next();
        assertEquals(sample.getIdentifier().getIdentifier(), "/DEFAULT/DEFAULT/TEST");

        final String notes = sample.getStringProperty("NOTES");
        assertEquals(notes, "Test");

        // Verifying imported sample type

        final List<IEntityTypeId> sampleTypes = objectIds.stream()
                .filter(objectId -> (objectId instanceof EntityTypePermId) && ((EntityTypePermId) objectId).getEntityKind() == EntityKind.SAMPLE)
                .map(objectId -> (IEntityTypeId) objectId).collect(Collectors.toList());

        assertEquals(sampleTypes.size(), 1);

        final SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();
        sampleTypeFetchOptions.withValidationPlugin().withScript();
        final SampleType sampleType = openBIS.getSampleTypes(sampleTypes, sampleTypeFetchOptions).values().iterator().next();
        final Plugin validationPlugin = sampleType.getValidationPlugin();

        assertNotNull(validationPlugin);

        assertEquals(validationPlugin.getName(), "EXPERIMENTAL_STEP.EXPERIMENTAL_STEP.EXPERIMENTAL_STEP.EXPERIMENTAL_STEP.date_range_validation");
        assertTrue(validationPlugin.getScript().contains("\"End date cannot be before start date!\""));

        openBIS.logout();
    }

}
