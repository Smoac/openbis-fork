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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.etlserver.BaseDirectoryHolder;
import ch.systemsx.cisd.etlserver.FileRenamer;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.TransferredDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * A class that implements the rollback of a data set storage.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetStorageRollbacker
{
    private final OmniscientTopLevelDataSetRegistratorState registratorContext;

    private final Logger operationLog;

    private final UnstoreDataAction unstoreAction;

    private final File incomingDataSetFile;

    private final String dataSetTypeCodeOrNull;

    private final Throwable errorOrNull;

    private final ErrorType errorTypeOrNull;

    private BaseDirectoryHolder baseDirectoryHolder;

    /**
     * Ctor
     * 
     * @param registratorContext
     * @param operationLog
     * @param unstoreAction
     * @param incomingDataSetFile
     * @param dataSetTypeCodeOrNull
     * @param errorOrNull
     */
    public DataSetStorageRollbacker(OmniscientTopLevelDataSetRegistratorState registratorContext,
            Logger operationLog, UnstoreDataAction unstoreAction, File incomingDataSetFile,
            String dataSetTypeCodeOrNull, Throwable errorOrNull)
    {
        this(registratorContext, operationLog, unstoreAction, incomingDataSetFile,
                dataSetTypeCodeOrNull, errorOrNull, null);
    }

    /**
     * Ctor
     * 
     * @param registratorContext
     * @param operationLog
     * @param unstoreAction
     * @param incomingDataSetFile
     * @param dataSetTypeCodeOrNull
     * @param errorOrNull
     */
    public DataSetStorageRollbacker(OmniscientTopLevelDataSetRegistratorState registratorContext,
            Logger operationLog, UnstoreDataAction unstoreAction, File incomingDataSetFile,
            String dataSetTypeCodeOrNull, Throwable errorOrNull, ErrorType errorTypeOrNull)
    {
        super();
        this.registratorContext = registratorContext;
        this.operationLog = operationLog;
        this.unstoreAction = unstoreAction;
        this.incomingDataSetFile = incomingDataSetFile;
        this.dataSetTypeCodeOrNull = dataSetTypeCodeOrNull;
        this.errorOrNull = errorOrNull;
        this.errorTypeOrNull = errorTypeOrNull;
    }

    public String getErrorMessageForLog()
    {
        if (errorTypeOrNull != null)
        {
            return "Responding to error [" + errorTypeOrNull + "] by performing action "
                    + unstoreAction + " on " + incomingDataSetFile;
        } else
        {
            return "Performing action " + unstoreAction + " on " + incomingDataSetFile;
        }
    }

    /**
     * Do the specified rollback actions and return the new location of the incomingDataSetFile or
     * null if it was deleted.
     */
    public File doRollback()
    {
        if (unstoreAction == UnstoreDataAction.MOVE_TO_ERROR)
        {
            File newLocation = moveIncomingToError();
            if (null != errorOrNull)
            {
                writeThrowable();
            }
            return newLocation;
        } else if (unstoreAction == UnstoreDataAction.DELETE)
        {
            FileUtilities.deleteRecursively(incomingDataSetFile,
                    new Log4jSimpleLogger(operationLog));
            return null;
        }
        return incomingDataSetFile;
    }

    public File moveIncomingToError()
    {
        // Make sure the data set information is valid
        DataSetInformation dataSetInfo = new DataSetInformation();
        dataSetInfo.setShareId(registratorContext.getGlobalState().getShareId());
        if (null == dataSetTypeCodeOrNull)
        {
            dataSetInfo.setDataSetType(new DataSetType(DataSetTypeCode.UNKNOWN.getCode()));
        } else
        {
            dataSetInfo.setDataSetType(new DataSetType(dataSetTypeCodeOrNull));
        }

        // Create the error directory
        File baseDirectory =
                DataSetStorageAlgorithm.createBaseDirectory(
                        TransferredDataSetHandler.ERROR_DATA_STRATEGY, registratorContext
                                .getStorageProcessor().getStoreRootDirectory(), registratorContext
                                .getFileOperations(), dataSetInfo, dataSetInfo.getDataSetType(),
                        incomingDataSetFile);
        baseDirectoryHolder =
                new BaseDirectoryHolder(TransferredDataSetHandler.ERROR_DATA_STRATEGY,
                        baseDirectory, incomingDataSetFile);

        // Move the incoming there
        FileRenamer.renameAndLog(incomingDataSetFile, baseDirectoryHolder.getTargetFile());
        return baseDirectoryHolder.getTargetFile();
    }

    /**
     * Assumes that the errorOrNull is not null
     */
    private void writeThrowable()
    {
        assert errorOrNull != null;
        final String fileName = incomingDataSetFile.getName() + ".exception";
        final File file = new File(baseDirectoryHolder.getTargetFile().getParentFile(), fileName);
        FileWriter writer = null;
        try
        {
            writer = new FileWriter(file);
            errorOrNull.printStackTrace(new PrintWriter(writer));
        } catch (final IOException e)
        {
            operationLog.warn(String.format("Could not write out the exception '%s' in file '%s'.",
                    fileName, file.getAbsolutePath()), e);
        } finally
        {
            IOUtils.closeQuietly(writer);
        }
    }

}
