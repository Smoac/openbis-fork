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

import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.PathPrefixPrepender;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcessingInstructionDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * Factory for standard post processors. A standard post processor does the following:
 * <ol>
 * <li>Copies the data set file or folder to the processing directory. The path of the processing
 * directory is a combination of the processing code (i.e. {@link ProcessingInstructionDTO#getPath()})
 * and the mandatory property <code>root-directory</code>. This copy action is done during the
 * preparation step.
 * <li>Stores the processing parameters (i.e. {@link ProcessingInstructionDTO#getParameters()}) in a
 * file in the processing directory who's name is specified by the mandatory property
 * <code>paramaters-file</code>.
 * <li>Creates an empty file in the processing directory who's name is specified by the mandatory
 * property <code>finished-file</code>.
 * </ol>
 * 
 * @author Franz-Josef Elmer
 */
public class StandardProcessorFactory implements IProcessorFactory
{
    private static final String HARD_LINK_INSTEAD_OF_COPY_KEY = "hard-link-instead-of-copy";

    private static final String PATH_PREFIX_ABSOLUTE_KEY = "prefix-for-absolute-paths";

    private static final String PATH_PREFIX_RELATIVE_KEY = "prefix-for-relative-paths";

    private static final String PARAMETERS_FILE_KEY = "parameters-file";

    private static final String INPUT_STORAGE_FORMAT_KEY = "input-storage-format";

    private static final String DATA_SET_CODE_PREFIX_GLUE_KEY = "data-set-code-prefix-glue";

    private static final String FINISHED_FILE_TEMPLATE_KEY = "finished-file-template";

    private final String parametersFileName;

    private final String finishedFileNameTemplate;

    private final IFileFactory fileFactory;

    private final StorageFormat inputStorageFormat;

    final PathPrefixPrepender pathPrefixPrepender;

    private final String dataSetCodePrefixGlueCharacter;

    /**
     * Creates a new instances for the specified properties. Uses the default timing parameters.
     * 
     * @throws ConfigurationFailureException if one of the mandatory properties is missing or the
     *             property <code>root-directory</code> is not the path of an existing directory.
     */
    public static StandardProcessorFactory create(final Properties properties)
            throws ConfigurationFailureException
    {
        final FileBasedFileFactory fileBasedFileFactory = createFileFactory(properties);
        return new StandardProcessorFactory(properties, fileBasedFileFactory);
    }

    private static FileBasedFileFactory createFileFactory(final Properties properties)
            throws ConfigurationFailureException
    {
        final boolean useHardLinks =
                PropertyUtils.getBoolean(properties, HARD_LINK_INSTEAD_OF_COPY_KEY, true);
        final FileBasedFileFactory fileBasedFileFactory =
                new FileBasedFileFactory(useHardLinks);
        return fileBasedFileFactory;
    }

    private StandardProcessorFactory(final Properties properties, final IFileFactory fileFactory)
            throws ConfigurationFailureException
    {
        this.fileFactory = fileFactory;
        assert properties != null : "Undefined properties.";
        final String prefixForAbsolutePathsOrNull =
                properties.getProperty(PATH_PREFIX_ABSOLUTE_KEY);
        final String prefixForRelativePathsOrNull =
                properties.getProperty(PATH_PREFIX_RELATIVE_KEY);
        final String inputDataFormatCode =
                PropertyUtils.getMandatoryProperty(properties, INPUT_STORAGE_FORMAT_KEY);
        inputStorageFormat = StorageFormat.tryGetFromCode(inputDataFormatCode);
        if (inputStorageFormat == null)
        {
            throw ConfigurationFailureException.fromTemplate(INPUT_STORAGE_FORMAT_KEY
                    + " property has illegal value '%s'.", inputDataFormatCode);
        }
        pathPrefixPrepender =
                new PathPrefixPrepender(prefixForAbsolutePathsOrNull, prefixForRelativePathsOrNull);
        parametersFileName = PropertyUtils.getMandatoryProperty(properties, PARAMETERS_FILE_KEY);
        finishedFileNameTemplate =
                PropertyUtils.getMandatoryProperty(properties, FINISHED_FILE_TEMPLATE_KEY);
        dataSetCodePrefixGlueCharacter =
                PropertyUtils.getMandatoryProperty(properties, DATA_SET_CODE_PREFIX_GLUE_KEY);
    }

    //
    // IProcessorFactory
    //

    public final PathPrefixPrepender getPathPrefixPrepender()
    {
        return pathPrefixPrepender;
    }

    public final IProcessor createProcessor()
    {
        return new StandardProcessor(fileFactory, inputStorageFormat, pathPrefixPrepender,
                parametersFileName, finishedFileNameTemplate, dataSetCodePrefixGlueCharacter);
    }

}
