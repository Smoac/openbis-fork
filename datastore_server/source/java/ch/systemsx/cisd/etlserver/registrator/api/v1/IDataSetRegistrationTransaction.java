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

package ch.systemsx.cisd.etlserver.registrator.api.v1;

/**
 * Interface for a data set registration transaction. All actions that go through the transaction
 * are committed atomically or rolledback.
 * <p>
 * The working directory for a file operations is the incoming data set folder (or incoming
 * directory if the data set is a simple file). Non-absolute paths are resolved relative to the
 * working directory.
 * <p>
 * New data sets are expected to have exactly one file or folder at the top level. When registered,
 * it is this file or folder that is put into the store.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDataSetRegistrationTransaction
{
    // Entity Retrieval/Creation

    /**
     * Create a new data set for registration in openBIS.
     */
    IDataSet createNewDataSet();

    /**
     * Get a sample from the openBIS AS.
     */
    ISampleImmutable getSample(String sampleIdentifierString);

    /**
     * Get a sample from the openBIS AS for the purpose of modifying it.
     */
    ISample getSampleForUpdate(String sampleIdentifierString);

    /**
     * Create a new sample to register with the openBIS AS. The sample will have a permId.
     */
    ISample createNewSample(String sampleIdentifierString);

    /**
     * Get an experiment from the openBIS AS.
     */
    IExperimentImmutable getExperiment(String experimentIdentifierString);

    /**
     * Get an experiment from the openBIS AS for the purpose of modifying it.
     */
    IExperiment getExperimentForUpdate(String experimentIdentifierString);

    /**
     * Create a new experiment to register with the openBIS AS. The experiment will have a permId.
     */
    IExperiment createNewExperiment(String experimentIdentifierString);

    // File operations -- The source and destination paths are local to the incoming data set folder
    // or incoming directory if the data set is just one file

    /**
     * Move a file from into the root of a data set.
     * 
     * @param src The path of the file to move.
     * @param dst The data set to add the file to.
     * @return The absolute path after the move.
     */
    String moveFile(String src, IDataSet dst);

    /**
     * Move a file to a specified location in a data set. Any necessary intermediate folders are
     * automatically created.
     * 
     * @param src The path of the file to move.
     * @param dst The data set to add the file to.
     * @param dstInDataset The path of the file in the data set
     * @return The absolute path after the move.
     */
    String moveFile(String src, IDataSet dst, String dstInDataset);

    /**
     * Take note of a new file to create in a data set. Doesn't actually create the file, just
     * tracks it for transactional purposes.
     * 
     * @param dst The data set to add the file to.
     * @param fileName The name of the new file to create. (Can be a simple file or directory.)
     * @return The absolute path of the new file.
     */
    String createNewFile(IDataSet dst, String fileName);

    /**
     * Take note of a new file to create in a data set. Doesn't actually create the file, just
     * tracks it for transactional purposes.
     * 
     * @param dst The data set to add the file to.
     * @param dstInDataset The path of the file in the data set
     * @param fileName The name of the new file to create. (Can be a simple file or directory.)
     * @return The absolute path of the new file.
     */
    String createNewFile(IDataSet dst, String dstInDataset, String fileName);

    /**
     * Delete a file a the given path.
     */
    void deleteFile(String src);
}
