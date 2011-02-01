package ch.systemsx.cisd.openbis.dss.etl.jython;

import java.io.File;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.registrator.api.v1.impl.ImageDataSet;

/**
 * Jython dropbox for HCS and Microscopy image datasets.
 * 
 * @author Tomasz Pylak
 */
public class JythonPlateDataSetHandler extends JythonTopLevelDataSetHandler
{
    public JythonPlateDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
    }

    /**
     * Create a screening specific factory available to the python script.
     */
    @Override
    protected IDataSetRegistrationDetailsFactory<ImageDataSetInformation> createObjectFactory(PythonInterpreter interpreter)
    {
        return new JythonPlateDatasetFactory(getRegistratorState());
    }

    public static class JythonPlateDatasetFactory extends JythonObjectFactory<ImageDataSetInformation>
    {
        public JythonPlateDatasetFactory(OmniscientTopLevelDataSetRegistratorState registratorState)
        {
            super(registratorState);
        }

        @Override
        public DataSet<ImageDataSetInformation> createDataSet(
                DataSetRegistrationDetails<ImageDataSetInformation> registrationDetails,
                File stagingFile)
        {
            return new ImageDataSet(registrationDetails, stagingFile);
        }

        @Override
        protected ImageDataSetInformation createDataSetInformation()
        {
            return new ImageDataSetInformation();
        }
    }
}
