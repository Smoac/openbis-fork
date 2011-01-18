package ch.systemsx.cisd.openbis.dss.etl.jython;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler;
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
     * Set the factory available to the python script.
     */
    @Override
    protected void setObjectFactory(PythonInterpreter interpreter)
    {
        interpreter.set("factory", new JythonPlateDatasetFactory(getRegistratorState()));
    }

    public static class JythonPlateDatasetFactory extends JythonObjectFactory
    {
        public JythonPlateDatasetFactory(OmniscientTopLevelDataSetRegistratorState registratorState)
        {
            super(registratorState);
        }

        /**
         * Factory method that creates a new data set information object.
         */
        @Override
        public ImageDataSetInformation createDataSetInformation()
        {
            ImageDataSetInformation dataSetInfo = new ImageDataSetInformation();
            dataSetInfo.setInstanceCode(registratorState.getHomeDatabaseInstance().getCode());
            dataSetInfo.setInstanceUUID(registratorState.getHomeDatabaseInstance().getUuid());

            return dataSetInfo;
        }
    }
}
