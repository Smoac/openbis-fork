package ch.systemsx.cisd.openbis.dss.etl.jython;

import java.io.File;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureVectorDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.BasicDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IFeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Jython dropbox for HCS and Microscopy image datasets.
 * 
 * @author Tomasz Pylak
 */
public class JythonPlateDataSetHandler extends JythonTopLevelDataSetHandler<DataSetInformation>
{
    public JythonPlateDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
    }

    /**
     * Create a screening specific factory available to the python script.
     */
    @Override
    protected IDataSetRegistrationDetailsFactory<DataSetInformation> createObjectFactory(
            PythonInterpreter interpreter)
    {
        return new JythonPlateDatasetFactory(getRegistratorState());
    }

    public static class JythonPlateDatasetFactory extends JythonObjectFactory<DataSetInformation>
    {
        private final IDataSetRegistrationDetailsFactory<ImageDataSetInformation> imageDatasetFactory;

        private final IDataSetRegistrationDetailsFactory<FeatureVectorDataSetInformation> featureVectorDatasetFactory;

        public JythonPlateDatasetFactory(OmniscientTopLevelDataSetRegistratorState registratorState)
        {
            super(registratorState);
            this.imageDatasetFactory =
                    new JythonObjectFactory<ImageDataSetInformation>(this.registratorState)
                        {
                            @Override
                            protected ImageDataSetInformation createDataSetInformation()
                            {
                                return new ImageDataSetInformation();
                            }
                        };
            this.featureVectorDatasetFactory =
                    new JythonObjectFactory<FeatureVectorDataSetInformation>(this.registratorState)
                        {
                            @Override
                            protected FeatureVectorDataSetInformation createDataSetInformation()
                            {
                                return new FeatureVectorDataSetInformation();
                            }
                        };
        }

        /** By default a starndard dataset is created. */
        @Override
        protected DataSetInformation createDataSetInformation()
        {
            return new DataSetInformation();
        }

        public DataSetRegistrationDetails<ImageDataSetInformation> createImageRegistrationDetails(
                SimpleImageDataConfig imageDataSet, File incomingDatasetFolder)
        {
            return SimpleImageDataSetRegistrator.createImageDatasetDetails(imageDataSet,
                    incomingDatasetFolder, imageDatasetFactory);
        }

        /** a simple method to register the described image dataset in a separate transaction */
        public void registerImageDataset(SimpleImageDataConfig imageDataSet,
                File incomingDatasetFolder,
                DataSetRegistrationService<ImageDataSetInformation> service)
        {
            DataSetRegistrationDetails<ImageDataSetInformation> imageDatasetDetails =
                    createImageRegistrationDetails(imageDataSet, incomingDatasetFolder);
            DataSetRegistrationTransaction<ImageDataSetInformation> transaction =
                    service.transaction(incomingDatasetFolder, imageDatasetFactory);
            IDataSet newDataset = transaction.createNewDataSet(imageDatasetDetails);
            transaction.moveFile(incomingDatasetFolder.getPath(), newDataset);
            transaction.commit();
        }

        // ----

        public IFeaturesBuilder createFeaturesBuilder()
        {
            return new FeaturesBuilder();
        }

        public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorRegistrationDetails(
                IFeaturesBuilder featureBuilder, File incomingDatasetFolder)
        {
            FeaturesBuilder myFeatureBuilder = (FeaturesBuilder) featureBuilder;
            DataSetRegistrationDetails<FeatureVectorDataSetInformation> registrationDetails =
                    featureVectorDatasetFactory.createDataSetRegistrationDetails();
            FeatureVectorDataSetInformation featureVectorDataSet =
                    registrationDetails.getDataSetInformation();
            featureVectorDataSet.setFeatures(myFeatureBuilder.getFeatureDefinitionValuesList());
            registrationDetails
                    .setDataSetType(ScreeningConstants.DEFAULT_ANALYSIS_WELL_DATASET_TYPE);
            registrationDetails.setMeasuredData(false);
            return registrationDetails;
        }

        /**
         * Factory method that creates a new registration details object for non-image datasets.
         * 
         * @deprecated used only in Matt's dropbox to register analysis datasets. Will be removed.
         */
        @Deprecated
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
