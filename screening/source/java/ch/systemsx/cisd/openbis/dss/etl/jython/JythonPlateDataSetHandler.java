package ch.systemsx.cisd.openbis.dss.etl.jython;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

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
    protected IDataSetRegistrationDetailsFactory<DataSetInformation> createObjectFactory(PythonInterpreter interpreter)
    {
        return new JythonPlateDatasetFactory(getRegistratorState());
    }

    public static class JythonPlateDatasetFactory extends JythonObjectFactory
    {
        public JythonPlateDatasetFactory(OmniscientTopLevelDataSetRegistratorState registratorState)
        {
            super(registratorState);
        }

        /**
         * Factory method that creates a new registration details object.
         */
        public DataSetRegistrationDetails<ImageDataSetInformation> createImageRegistrationDetails()
        {
            DataSetRegistrationDetails<ImageDataSetInformation> registrationDetails =
                    new DataSetRegistrationDetails<ImageDataSetInformation>();
            registrationDetails.setDataSetInformation(createImageDataSetInformation());
            return registrationDetails;
        }

        /**
         * Factory method that creates a new data set information object for image dataset.
         */
        private ImageDataSetInformation createImageDataSetInformation()
        {
            ImageDataSetInformation dataSetInfo = new ImageDataSetInformation();
            dataSetInfo.setInstanceCode(registratorState.getHomeDatabaseInstance().getCode());
            dataSetInfo.setInstanceUUID(registratorState.getHomeDatabaseInstance().getUuid());

            return dataSetInfo;
        }
    }
}
