package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.openbis.generic.client.web.server.NamedInputStream;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.MaterialLoader;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.SampleUploadSectionsParser;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.SampleUploadSectionsParser.BatchSamplesOperation;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.server.library_tools.ScreeningLibraryTransformer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LibraryRegistrationInfo.RegistrationScope;

/**
 * Extracts new plates, oligos and genes from given input stream.
 * 
 * @author Izabela Adamczyk
 */
class LibraryExtractor
{
    private List<NewMaterial> newGenes;

    private List<NewMaterial> newOligos;

    private List<NewSamplesWithTypes> newSamplesWithType;

    private final InputStream inputStream;

    private final String experiment;

    private final String space;

    private final String plateGeometry;

    private final RegistrationScope registrationScope;

    public LibraryExtractor(InputStream inputStream, String experiment, String space,
            String plateGeometry, RegistrationScope registrationScope)
    {
        this.inputStream = inputStream;
        this.experiment = experiment;
        this.space = space;
        this.plateGeometry = plateGeometry;
        this.registrationScope = registrationScope;
    }

    public List<NewMaterial> getNewGenes()
    {
        return newGenes;
    }

    public List<NewMaterial> getNewOligos()
    {
        return newOligos;
    }

    public List<NewSamplesWithTypes> getNewSamplesWithType()
    {
        return newSamplesWithType;
    }

    public void extract()
    {
        File genesFile = createTempFile();
        File oligosFile = createTempFile();
        File platesFile = createTempFile();
        try
        {
            Status status =
                    ScreeningLibraryTransformer.readLibrary(inputStream, experiment, plateGeometry,
                            space, genesFile.getAbsolutePath(), oligosFile.getAbsolutePath(),
                            platesFile.getAbsolutePath());
            if (status.isError())
            {
                throw new UserFailureException(status.tryGetErrorMessage());
            }
            newGenes = registrationScope.isGenes() ? extractMaterials(genesFile) : null;
            newOligos = registrationScope.isSiRNAs() ? extractMaterials(oligosFile) : null;
            newSamplesWithType = extractSamples(platesFile);
        } catch (FileNotFoundException ex)
        {
            new UserFailureException(ex.getMessage());
        } finally
        {
            genesFile.delete();
            oligosFile.delete();
            platesFile.delete();
        }
    }

    private static List<NewSamplesWithTypes> extractSamples(File platesFile)
            throws FileNotFoundException
    {
        SampleType typeInFile = new SampleType();
        typeInFile.setCode(SampleType.DEFINED_IN_FILE);
        BatchSamplesOperation prepared =
                SampleUploadSectionsParser.prepareSamples(typeInFile, Arrays
                        .asList(new NamedInputStream(new FileInputStream(platesFile), platesFile
                                .getName(), null)), null, null, true,
                        BatchOperationKind.REGISTRATION);
        List<NewSamplesWithTypes> samples = prepared.getSamples();
        setUpdatableTypes(samples);
        return samples;
    }

    private static void setUpdatableTypes(List<NewSamplesWithTypes> samples)
    {
        for (NewSamplesWithTypes s : samples)
        {
            s.setAllowUpdateIfExist(true);
        }
    }

    private static List<NewMaterial> extractMaterials(File genesFile) throws FileNotFoundException
    {
        MaterialLoader loader = new MaterialLoader();
        loader.load(Arrays.asList(new NamedInputStream(new FileInputStream(genesFile), genesFile
                .getName(), null)));
        return loader.getNewMaterials();
    }

    private File createTempFile()
    {
        File file =
                FileOperations.getInstance().createTempFile(
                        ScreeningClientService.class.getSimpleName(), null);
        file.deleteOnExit();
        return file;
    }

}