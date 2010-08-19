/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.plasmid.dss;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ch.ethz.bsse.cisd.plasmid.plasmapper.PlasMapperUploader;
import ch.ethz.bsse.cisd.plasmid.plasmapper.PlasMapperUploader.PlasMapperService;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.AbstractDelegatingStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Stores files containing plasmid data in the DSS store. Additionally for files holding a DNA
 * sequence it uses PlasMapper (see http://wishart.biology.ualberta.ca/PlasMapper/) to generate:
 * <ul>
 * <li>a GB file with the same sequence but in a standard Genbank format
 * <li>a PNG file with a graphical representation of the plasmid sequence (Graphic Map)
 * </ul>
 * The generated files will be stored in a separate directory ('generated') of the created Dataset.
 * They will have the same base name as the original file holding the DNA sequence which will be
 * kept in 'original' directory.
 * 
 * @author Piotr Buczek
 */
public class PlasmidStorageProcessor extends AbstractDelegatingStorageProcessor
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PlasmidStorageProcessor.class);

    private final static String PLASMAPPER_BASE_URL_KEY = "plasmapper-base-url";

    private final static String PLASMAPPER_ROOT_DIR_KEY = "plasmapper-root-dir";

    private static final String ORIGINAL_DIR = "original";

    private static final String GENERATED_DIR = "generated";

    private static final String GB_FILE_EXTENSION = ".gb";

    private static final String PNG_FILE_EXTENSION = ".png";

    private final PlasMapperUploader uploader;

    private final String serverRootDir;

    public PlasmidStorageProcessor(Properties properties)
    {
        super(properties);
        final String baseUrl =
                PropertyUtils.getMandatoryProperty(properties, PLASMAPPER_BASE_URL_KEY);
        this.uploader = new PlasMapperUploader(baseUrl);
        this.serverRootDir =
                PropertyUtils.getMandatoryProperty(properties, PLASMAPPER_ROOT_DIR_KEY);

        final File serverRootFile = new File(serverRootDir);
        if ((serverRootFile.isDirectory() && serverRootFile.canRead()) == false)
        {
            final String msg =
                    String.format("'%s' (value of '%s' property) is supposed to be a path "
                            + "to an existing readable directory but isn't.", serverRootDir,
                            PLASMAPPER_ROOT_DIR_KEY);
            throw new EnvironmentFailureException(msg);
        }
    }

    @Override
    public File storeData(final DataSetInformation dataSetInformation,
            final ITypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataSetDirectory, final File rootDir)
    {
        File answer =
                super.storeData(dataSetInformation, typeExtractor, mailClient,
                        incomingDataSetDirectory, rootDir);
        if (typeExtractor.getDataSetType(incomingDataSetDirectory).getCode().equals(
                DataSetTypeOracle.DataSetTypeInfo.SEQ_FILE.name()))
        {
            File originalDir = new File(answer, ORIGINAL_DIR);
            File[] files = originalDir.listFiles();
            assert files.length == 1;
            File seqFile = files[0];

            String baseFileName = FilenameUtils.getBaseName(seqFile.getName());
            String pngFileName = baseFileName + PNG_FILE_EXTENSION;
            String gbFileName = baseFileName + GB_FILE_EXTENSION;

            File generatedDir = new File(answer, GENERATED_DIR);
            if (generatedDir.mkdir())
            {
                final File pngFileDest = new File(generatedDir, pngFileName);
                final File gbFileDest = new File(generatedDir, gbFileName);

                operationLog.info("Uploading '" + seqFile.getName() + "' to PlasMapper.");
                uploadAndMoveGeneratedFile(seqFile, PlasMapperService.GRAPHIC_MAP, pngFileDest);
                uploadAndMoveGeneratedFile(seqFile, PlasMapperService.GENEBANK_OUTPUT, gbFileDest);
            } else
            {
                throw new EnvironmentFailureException("Couldn't create directory '" + generatedDir
                        + "'.");
            }
        }
        return answer;
    }

    private void uploadAndMoveGeneratedFile(final File seqFile, final PlasMapperService service,
            final File destinationFile)
    {
        String outputFilePath = uploader.upload(seqFile, service);
        File outputFile = new File(serverRootDir + outputFilePath);
        if (outputFile.isFile())
        {
            operationLog.info("Renaming and moving file '" + outputFile.getName() + "' from '"
                    + outputFile + "' to " + destinationFile);
            if (outputFile.renameTo(destinationFile) == false)
            {
                throw new EnvironmentFailureException("Couldn't rename file '" + outputFile
                        + "' to '" + destinationFile + "'.");
            }
        } else
        {
            throw new EnvironmentFailureException("'" + outputFile
                    + "' doesn't exist or is not a file.");
        }
    }

}
