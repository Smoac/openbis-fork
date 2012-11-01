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
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.etlserver.AbstractDelegatingStorageProcessor;
import ch.systemsx.cisd.etlserver.AbstractDelegatingStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.ITypeExtractor;

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
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PlasmidStorageProcessor.class);

    private static final Logger notifyLog = LogFactory.getLogger(LogCategory.NOTIFY,
            PlasmidStorageProcessor.class);

    private final static String HTML_FILE_TEMPLATE =
            "<html><head>\n"
                    + "<meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"><title>PlasMapper - Graphic Map</title></head>\n"
                    + "<body>\n"
                    + "<embed src=\"%%FILE_NAME%%\" type=\"image/svg+xml\" pluginspage=\"http://www.adobe.com/svg/viewer/install/\" id=\"Panel\" height=\"1010\" width=\"1010\">\n"
                    + "<br>\n" + "<a href=\"%%FILE_NAME%%\" target=\"_blank\">Download Link</a>"
                    + "</body></html>";

    private final static String PLASMAPPER_BASE_URL_KEY = "plasmapper-base-url";

    private final static String PLASMAPPER_ROOT_DIR_KEY = "plasmapper-root-dir";

    private static final String ORIGINAL_DIR = "original";

    private static final String GENERATED_DIR = "generated";

    private static final String GB_FILE_EXTENSION = ".gb";

    private static final String SVG_FILE_EXTENSION = ".svg";

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
    public IStorageProcessorTransaction createTransaction(
            StorageProcessorTransactionParameters parameters)
    {
        return new PlasmidStorageProcessorTransaction(parameters,
                super.createTransaction(parameters), this);
    }

    // WORKAROUND cannot move the file because it is on a different filesystem
    private void uploadAndCopyGeneratedFile(final File seqFile, final PlasMapperService service,
            final File destinationFile)
    {
        String outputFilePath = uploader.upload(seqFile, service);
        if (StringUtils.isBlank(outputFilePath))
        {
            notifyLog.error("Cannot upload file '" + seqFile.getName()
                    + "', see jetty.out for details.");
            throw new IllegalStateException("Cannot upload file '" + seqFile.getName()
                    + "', see jetty.out for details.");
        }
        File outputFile = new File(serverRootDir + outputFilePath);
        if (outputFile.isFile())
        {
            operationLog.info("Renaming and copying file '" + outputFile.getName() + "' from '"
                    + outputFile + "' to " + destinationFile);
            FileOperations.getInstance().copyFile(outputFile, destinationFile);

            if (destinationFile.getName().endsWith("svg"))
            {
                String htmlFileName = destinationFile.getName().replaceAll(".svg", ".html");
                File htmlFile = new File(destinationFile.getParentFile(), htmlFileName);
                operationLog.info("Generating html file '" + htmlFile + "'");
                FileUtilities.writeToFile(htmlFile,
                        HTML_FILE_TEMPLATE.replaceAll("%%FILE_NAME%%", destinationFile.getName()));
            }
        } else
        {
            throw new EnvironmentFailureException("'" + outputFile
                    + "' doesn't exist or is not a file.");
        }
    }

    static class PlasmidStorageProcessorTransaction extends
            AbstractDelegatingStorageProcessorTransaction
    {

        private static final long serialVersionUID = 1L;

        private final transient PlasmidStorageProcessor processor;

        public PlasmidStorageProcessorTransaction(StorageProcessorTransactionParameters parameters,
                IStorageProcessorTransaction superTransaction, PlasmidStorageProcessor processor)
        {
            super(parameters, superTransaction);
            this.processor = processor;
        }

        @Override
        protected File executeStoreData(ITypeExtractor typeExtractor, IMailClient mailClient)
        {
            nestedTransaction.storeData(typeExtractor, mailClient, incomingDataSetDirectory);
            File answer = nestedTransaction.getStoredDataDirectory();

            boolean isSeqType =
                    typeExtractor.getDataSetType(incomingDataSetDirectory).getCode()
                            .equals(DataSetTypeOracle.DataSetTypeInfo.SEQ_FILE.name());
            boolean isFaExtension =
                    "fa".equals(FilenameUtils.getExtension(incomingDataSetDirectory.getName()));

            if (isSeqType && !isFaExtension)
            {
                File originalDir = new File(answer, ORIGINAL_DIR);
                File[] files = originalDir.listFiles();
                assert files.length == 1;
                File seqFile = files[0];

                String baseFileName = FilenameUtils.getBaseName(seqFile.getName());
                String svgFileName = baseFileName + SVG_FILE_EXTENSION;
                String gbFileName = baseFileName + GB_FILE_EXTENSION;

                File generatedDir = new File(answer, GENERATED_DIR);
                if (generatedDir.mkdir())
                {
                    final File svgFileDest = new File(generatedDir, svgFileName);
                    final File gbFileDest = new File(generatedDir, gbFileName);

                    operationLog.info("Uploading '" + seqFile.getName() + "' to PlasMapper.");
                    processor.uploadAndCopyGeneratedFile(seqFile, PlasMapperService.GRAPHIC_MAP,
                            svgFileDest);
                    processor.uploadAndCopyGeneratedFile(seqFile,
                            PlasMapperService.GENEBANK_OUTPUT, gbFileDest);
                } else
                {
                    throw new EnvironmentFailureException("Couldn't create directory '"
                            + generatedDir + "'.");
                }
            }
            return answer;
        }

        @Override
        protected UnstoreDataAction executeRollback(Throwable ex)
        {
            return nestedTransaction.rollback(ex);
        }

        @Override
        protected void executeCommit()
        {
            nestedTransaction.commit();
        }
    }

}
