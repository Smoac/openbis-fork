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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperiment;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperimentImmutable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISampleImmutable;

/**
 * The implementation of a transaction. This class is designed to be used in one thread.
 * <p>
 * A transaction tracks commands that are invoked on it so they can be reverted (rolledback) if
 * necessary.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationTransaction implements IDataSetRegistrationTransaction
{
    // Keeps track of steps that have been executed and may need to be reverted. Elements are kept
    // in the order they need to be reverted.
    private final RollbackStack rollbackStack = new RollbackStack();

    public IDataSet createNewDataSet()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ISampleImmutable getSample(String sampleIdentifierString)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ISample getSampleForUpdate(String sampleIdentifierString)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ISample createNewSample(String sampleIdentifierString)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public IExperimentImmutable getExperiment(String experimentIdentifierString)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public IExperiment getExperimentForUpdate(String experimentIdentifierString)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public IExperiment createNewExperiment(String experimentIdentifierString)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String moveFile(String src, IDataSet dst)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String moveFile(String src, IDataSet dst, String dstInDataset)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String createNewFile(IDataSet dst, String fileName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String createNewFile(IDataSet dst, String dstInDataset, String fileName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void deleteFile(String src)
    {
        // TODO Auto-generated method stub

    }

    /**
     * Rollback any commands that have been executed. Rollback is done in the reverse order of
     * execution.
     */
    public void rollback()
    {
        rollbackStack.rollbackAll();
    }

    /**
     * Execute the command and add it to the list of commands that have been executed.
     * <p>
     * Made package visible for testing purposes.
     */
    void executeCommand(ITransactionalCommand cmd)
    {
        rollbackStack.pushAndExecuteCommand(cmd);
    }

}
