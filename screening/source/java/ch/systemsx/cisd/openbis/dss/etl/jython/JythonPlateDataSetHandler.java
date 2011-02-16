package ch.systemsx.cisd.openbis.dss.etl.jython;

import java.io.File;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.BasicDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageDataSetInformation;

/**
 * Jython dropbox for HCS and Microscopy image datasets.
 * 
 * @author Tomasz Pylak
 */
public class JythonPlateDataSetHandler extends
        JythonTopLevelDataSetHandler<ImageDataSetInformation>
{
    public JythonPlateDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
    }

    /**
     * Create a screening specific factory available to the python script.
     */
    @Override
    protected IDataSetRegistrationDetailsFactory<ImageDataSetInformation> createObjectFactory(
            PythonInterpreter interpreter)
    {
        return new JythonPlateDatasetFactory(getRegistratorState());
    }

    public static class JythonPlateDatasetFactory extends
            JythonObjectFactory<ImageDataSetInformation>
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
            return new DataSet<ImageDataSetInformation>(registrationDetails, stagingFile);
        }

        @Override
        protected ImageDataSetInformation createDataSetInformation()
        {
            return new ImageDataSetInformation();
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

        public DataSetRegistrationDetails<ImageDataSetInformation> createImageRegistrationDetails(
                SimpleImageDataConfig imageDataSet, File incomingDatasetFolder)
        {
            return SimpleImageDataSetRegistrator.createImageDatasetDetails(imageDataSet,
                    incomingDatasetFolder, this);
        }

        /** a simple method to register the described image dataset */
        public void registerImageDataset(SimpleImageDataConfig imageDataSet,
                File incomingDatasetFolder,
                DataSetRegistrationService<ImageDataSetInformation> service)
        {
            DataSetRegistrationDetails<ImageDataSetInformation> imageDatasetDetails =
                    createImageRegistrationDetails(imageDataSet, incomingDatasetFolder);
            IDataSetRegistrationDetailsFactory<ImageDataSetInformation> myself = this;
            // TODO 2011-02-15, Tomasz Pylak: remove this casting
            @SuppressWarnings("unchecked")
            DataSetRegistrationTransaction<ImageDataSetInformation> transaction =
                    (DataSetRegistrationTransaction<ImageDataSetInformation>) service.transaction(
                            incomingDatasetFolder, myself);
            IDataSet newDataset = transaction.createNewDataSet(imageDatasetDetails);
            transaction.moveFile(incomingDatasetFolder.getPath(), newDataset);
            transaction.commit();
        }
    }
}
