package ch.systemsx.cisd.openbis.dss.etl.jython;

import java.io.File;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.BasicDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageDataSetInformation;

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
    protected IDataSetRegistrationDetailsFactory<BasicDataSetInformation> createObjectFactory(
            PythonInterpreter interpreter)
    {
        return new JythonPlateDatasetFactory(getRegistratorState());
    }

    public static class JythonPlateDatasetFactory extends
            JythonObjectFactory<BasicDataSetInformation>
    {
        public JythonPlateDatasetFactory(OmniscientTopLevelDataSetRegistratorState registratorState)
        {
            super(registratorState);
        }

        @Override
        public DataSet<BasicDataSetInformation> createDataSet(
                DataSetRegistrationDetails<BasicDataSetInformation> registrationDetails,
                File stagingFile)
        {
            return new DataSet<BasicDataSetInformation>(registrationDetails, stagingFile);
        }

        @Override
        protected BasicDataSetInformation createDataSetInformation()
        {
            return new BasicDataSetInformation();
        }

        /**
         * Factory method that creates a new registration details object for image datasets.
         */
        public DataSetRegistrationDetails<ImageDataSetInformation> createImageRegistrationDetails()
        {
            DataSetRegistrationDetails<ImageDataSetInformation> registrationDetails =
                    new DataSetRegistrationDetails<ImageDataSetInformation>();
            ImageDataSetInformation dataSetInfo = new ImageDataSetInformation();
            setDatabaseInstance(dataSetInfo);
            registrationDetails.setDataSetInformation(dataSetInfo);
            return registrationDetails;
        }

        /**
         * Factory method that creates a new registration details object for non-image datasets.
         */
        public DataSetRegistrationDetails<BasicDataSetInformation> createBasicRegistrationDetails()
        {
            DataSetRegistrationDetails<BasicDataSetInformation> registrationDetails =
                    new DataSetRegistrationDetails<BasicDataSetInformation>();
            BasicDataSetInformation dataSetInfo = new BasicDataSetInformation();
            setDatabaseInstance(dataSetInfo);
            registrationDetails.setDataSetInformation(dataSetInfo);
            return registrationDetails;
        }
    }
}
