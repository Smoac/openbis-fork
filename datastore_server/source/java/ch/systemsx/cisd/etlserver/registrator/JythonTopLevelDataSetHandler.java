/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetHandler extends AbstractOmniscientTopLevelDataSetRegistrator
{
    // The key in the properties file
    public static final String SCRIPT_PATH_KEY = "script-path";

    private final String scriptPath;

    /**
     * @param globalState
     */
    public JythonTopLevelDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);

        scriptPath =
                PropertyUtils.getMandatoryProperty(globalState.getThreadParameters()
                        .getThreadProperties(), SCRIPT_PATH_KEY);

    }

    @Override
    protected void handleDataSet(File dataSetFile, DataSetRegistrationService service)
    {
        // Load the script
        File scriptFile = new File(scriptPath);
        String scriptString = FileUtilities.loadToString(scriptFile);

        // Create an evaluator
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.set("service", service);
        interpreter.set("incoming", dataSetFile);
        interpreter.set("state", getGlobalState());
        setObjectFactory(interpreter);

        interpreter.exec(scriptString);
    }

    /**
     * Set the factory available to the python script. Subclasses may want to override.
     */
    protected void setObjectFactory(PythonInterpreter interpreter)
    {
        interpreter.set("factory", new JythonObjectFactory(getRegistratorState()));
    }

    public static class JythonObjectFactory
    {
        private final OmniscientTopLevelDataSetRegistratorState registratorState;

        public JythonObjectFactory(OmniscientTopLevelDataSetRegistratorState registratorState)
        {
            this.registratorState = registratorState;
        }

        /**
         * Factory method that creates a new registration details object.
         */
        public DataSetRegistrationDetails createRegistrationDetails()
        {
            DataSetRegistrationDetails registrationDetails = new DataSetRegistrationDetails();
            registrationDetails.setDataSetInformation(createDataSetInformation());
            return registrationDetails;
        }

        /**
         * Factory method that creates a new data set information object.
         */
        public DataSetInformation createDataSetInformation()
        {
            DataSetInformation dataSetInfo = new DataSetInformation();
            dataSetInfo.setInstanceCode(registratorState.getHomeDatabaseInstance().getCode());
            dataSetInfo.setInstanceUUID(registratorState.getHomeDatabaseInstance().getUuid());

            return dataSetInfo;
        }
    }
}
