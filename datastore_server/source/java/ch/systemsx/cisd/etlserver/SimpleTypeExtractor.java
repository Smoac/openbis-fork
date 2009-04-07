/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.ProcedureTypeCode;

/**
 * Implementation of {@link IProcedureAndDataTypeExtractor} which gets the types from the properties
 * argument of the constructor.
 * 
 * @author Franz-Josef Elmer
 */
public class SimpleTypeExtractor implements IProcedureAndDataTypeExtractor
{
    public static final String FILE_FORMAT_TYPE_KEY = "file-format-type";

    public static final String LOCATOR_TYPE_KEY = "locator-type";

    public static final String DATA_SET_TYPE_KEY = "data-set-type";

    public static final String PROCEDURE_TYPE_KEY = "procedure-type";

    private FileFormatType fileFormatType;

    private LocatorType locatorType;

    private DataSetType dataSetType;

    private ProcedureType procedureType;

    public SimpleTypeExtractor(final Properties properties)
    {
        String code =
                properties.getProperty(FILE_FORMAT_TYPE_KEY,
                        FileFormatType.DEFAULT_FILE_FORMAT_TYPE_CODE);
        fileFormatType = new FileFormatType(code);
        code = properties.getProperty(LOCATOR_TYPE_KEY, LocatorType.DEFAULT_LOCATOR_TYPE_CODE);
        locatorType = new LocatorType(code);
        code = properties.getProperty(DATA_SET_TYPE_KEY, DataSetTypeCode.HCS_IMAGE.getCode());
        dataSetType = new DataSetType(code);
        code =
                properties.getProperty(PROCEDURE_TYPE_KEY, ProcedureTypeCode.DATA_ACQUISITION
                        .getCode());
        procedureType = new ProcedureType(code);

    }

    //
    // IProcedureAndDataTypeExtractor
    //

    public final FileFormatType getFileFormatType(final File incomingDataSetPath)
    {
        return fileFormatType;
    }

    public final LocatorType getLocatorType(final File incomingDataSetPath)
    {
        return locatorType;
    }

    public final DataSetType getDataSetType(final File incomingDataSetPath)
    {
        return dataSetType;
    }

    public final ProcedureType getProcedureType(final File incomingDataSetPath)
    {
        return procedureType;
    }

}
