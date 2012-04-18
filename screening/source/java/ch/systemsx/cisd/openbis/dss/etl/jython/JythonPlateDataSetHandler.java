package ch.systemsx.cisd.openbis.dss.etl.jython;

import java.io.File;
import java.util.Properties;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.PythonUtils;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Jython dropbox for HCS and Microscopy image datasets.
 * 
 * @author Tomasz Pylak
 */
public class JythonPlateDataSetHandler extends JythonTopLevelDataSetHandler<DataSetInformation>
{

    private final String ORIGINAL_DIRNAME_KEY = "image-datasets-original-dir-name";

    private final String originalDirName;

    public JythonPlateDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
        originalDirName = parseOriginalDir(globalState.getThreadParameters().getThreadProperties());
    }

    private String parseOriginalDir(Properties threadProperties)
    {
        String originalDir =
                PropertyUtils.getProperty(threadProperties, ORIGINAL_DIRNAME_KEY,
                        ScreeningConstants.ORIGINAL_DATA_DIR);
        if (false == FileUtilities.isValidFileName(originalDir))
        {
            throw ConfigurationFailureException.fromTemplate(
                    "Invalid folder name specified in '%s': '%s'.", ORIGINAL_DIRNAME_KEY,
                    originalDir);
        }
        return originalDir;
    }

    /**
     * Create a screening specific factory available to the python script.
     */
    @Override
    protected IDataSetRegistrationDetailsFactory<DataSetInformation> createObjectFactory(
            PythonInterpreter interpreter, DataSetInformation userProvidedDataSetInformationOrNull)
    {
        return new JythonPlateDatasetFactory(getRegistratorState(),
                userProvidedDataSetInformationOrNull);
    }

    @Override
    protected DataSetRegistrationService<DataSetInformation> createDataSetRegistrationService(
            DataSetFile incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return new JythonDataSetRegistrationService<DataSetInformation>(this, incomingDataSetFile,
                callerDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                PythonUtils.createIsolatedPythonInterpreter(), getGlobalState())
            {
                @SuppressWarnings("unchecked")
                @Override
                protected DataSetRegistrationTransaction<DataSetInformation> createTransaction(
                        File rollBackStackParentFolder,
                        File workingDirectory,
                        File stagingDirectory,
                        IDataSetRegistrationDetailsFactory<DataSetInformation> registrationDetailsFactory)
                {
                    return new ImagingDataSetRegistrationTransaction(rollBackStackParentFolder,
                            workingDirectory, stagingDirectory, this, registrationDetailsFactory,
                            originalDirName);
                }
            };
    }

}
