/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.importer.helper;

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.xls.importer.utils.IAttribute;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.ImportUtils;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.VersionUtils;
import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importer.XLSImport;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

public abstract class BasicImportHelper extends AbstractImportHelper
{
    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, XLSImport.class);

    protected final ImportModes mode;

    protected final ImportOptions options;

    public BasicImportHelper(ImportModes mode, ImportOptions options)
    {
        this.mode = mode;
        this.options = options;
    }

    protected abstract ImportTypes getTypeName();

    protected boolean isNewVersion(Map<String, Integer> header, List<String> values)
    {
        return true;
    }

    protected static boolean isNewVersionWithInternalNamespace(Map<String, Integer> header, List<String> values, Map<String, Integer> versions, boolean isSystem, String type, IAttribute versionAttribute, IAttribute codeAttribute, IAttribute internalAttribute)
    {
        String version = getValueByColumnName(header, values, versionAttribute);
        String code = getValueByColumnName(header, values, codeAttribute);

        if (code == null)
        {
            throw new UserFailureException("Mandatory field is missing or empty: " + codeAttribute);
        }

        String internal = getValueByColumnName(header, values, internalAttribute);
        boolean isInternalNamespace = ImportUtils.isTrue(internal);
        boolean canUpdate = (isInternalNamespace == false) || isSystem;

        if (canUpdate == false) {
            return false;
        } else if (canUpdate && (version == null || version.isEmpty())) {
            return true;
        } else {
            return VersionUtils.isNewVersion(version, VersionUtils.getStoredVersion(versions, type, code));
        }
    }

    protected void updateVersion(Map<String, Integer> header, List<String> values)
    {
        // do nothing
    }

    protected abstract boolean isObjectExist(Map<String, Integer> header, List<String> values);

    protected abstract void createObject(Map<String, Integer> header, List<String> values, int page, int line);

    protected abstract void updateObject(Map<String, Integer> header, List<String> values, int page, int line);

    public void importBlock(List<List<String>> page, int pageIndex, int start, int end)
    {
        int lineIndex = start;

        try
        {
            Map<String, Integer> header = parseHeader(page.get(lineIndex), true);
            lineIndex++;

            while (lineIndex < end)
            {
                validateLine(header, page.get(lineIndex));
                if (isNewVersion(header, page.get(lineIndex)))
                {
                    if (!isObjectExist(header, page.get(lineIndex)))
                    {
                        if (options.getDisallowEntityCreations() && getTypeName().isMetadata())
                        {
                            throw new UserFailureException("Entity creations disallowed but found with block type: " + getTypeName());
                        }
                        createObject(header, page.get(lineIndex), pageIndex, lineIndex);
                        updateVersion(header, page.get(lineIndex));
                    } else
                    {
                        switch (mode)
                        {
                            case FAIL_IF_EXISTS:
                                if (!(this instanceof SemanticAnnotationImportHelper))
                                {
                                    throw new UserFailureException("Mode FAIL_IF_EXISTS - Found existing " + getTypeName());
                                }
                            case UPDATE_IF_EXISTS:
                                updateObject(header, page.get(lineIndex), pageIndex, lineIndex);
                                updateVersion(header, page.get(lineIndex));
                                break;
                            case IGNORE_EXISTING:
                                // do nothing
                                break;
                            default:
                                throw new UserFailureException("Unknown mode");
                        }
                    }
                }

                lineIndex++;
            }

        } catch (Exception e)
        {
            UserFailureException userFailureException = new UserFailureException(
                    "sheet: " + (pageIndex + 1) + " line: " + (lineIndex + 1) + " message: " + e.getMessage());
            userFailureException.setStackTrace(e.getStackTrace());
            throw userFailureException;
        }
    }
}
