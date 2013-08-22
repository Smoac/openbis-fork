/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.archiveverifier.batch;

import java.io.File;
import java.util.List;

/**
 * Verifies the correctness of an archive file of single dataset.
 * 
 * @author anttil
 */
public class DataSetArchiveVerifier implements IDataSetArchiveVerifier
{
    private final IArchiveFileVerifier verifier;

    private final IArchiveFileRepository fileRepository;

    public DataSetArchiveVerifier(IArchiveFileRepository fileRepository, IArchiveFileVerifier verifier)
    {
        this.fileRepository = fileRepository;
        this.verifier = verifier;
    }

    @Override
    public IResult run(String dataSetCode)
    {
        File file = fileRepository.getArchiveFileOf(dataSetCode);
        if (file.exists())
        {
            List<String> errors = verifier.verify(file);
            return errors.isEmpty() ? new SuccessResult(file) : new FailedResult(file, errors);
        } else
        {
            return new FailedResult();
        }
    }
}
