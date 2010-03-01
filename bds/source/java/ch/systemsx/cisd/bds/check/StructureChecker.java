/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.check;

import java.io.File;

import ch.systemsx.cisd.bds.Constants;
import ch.systemsx.cisd.bds.StringUtils;
import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;

/**
 * Allows to check if given BDS directory is consistent regarding definition of data structure
 * <code>V1.0</code>. Program will try to find all problems with the structure. If the path
 * provided as an argument is not a readable directory or version is incorrect (directory with
 * version does not exist, cannot be parsed, is not <code>1.0</code>, etc.) - program will stop
 * processing the path complaining only about this basic problem.
 * 
 * @author Izabela Adamczyk
 */
public final class StructureChecker extends AbstractChecker
{

    public StructureChecker(final boolean verbose)
    {
        super(verbose);
    }

    /**
     * Entry point. If given BDS structure is inconsistent prints a report containing all problems
     * found and exits with code <code>1</code>, otherwise exits with code <code>0</code>.
     * 
     * @param args - BDS directory
     */
    public final static void main(final String[] args)
    {
        try
        {
            final File bdsDirectory = getBdsDirectory(args, StructureChecker.class.getName());
            final ProblemReport report =
                    new StructureChecker(isVerbose(args, StructureChecker.class.getName()))
                            .getStructureConsistencyReport(bdsDirectory);
            printReportAndExit(report);
        } catch (final Exception e)
        {
            exitWithFatalError(e);
        }
    }

    /**
     * Returns a {@link ProblemReport} with information about problems with BDS structure
     * inconsistencies.
     */
    public final ProblemReport getStructureConsistencyReport(final File bdsDirectory)
    {
        checkIsDirectory(bdsDirectory);
        final IDirectory containerNode = NodeFactory.createDirectoryNode(bdsDirectory);
        checkSpecificVersion(containerNode, 1, 0);
        checkData(containerNode);
        checkMetadata(containerNode);
        checkAnnotations(containerNode);
        return problemReport;
    }

    private void checkAnnotations(final IDirectory containerNode)
    {
        try
        {
            Utilities.getSubDirectory(containerNode, AbstractChecker.ANNOTATIONS);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkMetadata(final IDirectory containerNode)
    {
        try
        {
            final IDirectory metadata = getMetadataOrFail(containerNode);
            checkDataSet(metadata);
            checkFormat(metadata);
            checkExperimentIdentifier(metadata);
            checkRegistrationTimestamp(metadata);
            checkExperimentRegistrator(metadata);
            checkSample(metadata);
            checkMd5Sum(metadata);
            checkParameters(metadata);
            checkStandardOriginalMappingExist(metadata);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }

    }

    private void checkStandardOriginalMappingExist(final IDirectory metadata)
    {
        try
        {
            checkFileExists(metadata, STANDARD_ORIGINAL_MAPPING);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkParameters(final IDirectory metadata)
    {
        try
        {
            Utilities.getSubDirectory(metadata, AbstractChecker.PARAMETERS);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkMd5Sum(final IDirectory metadata)
    {
        try
        {
            final IDirectory md5sum = Utilities.getSubDirectory(metadata, AbstractChecker.MD5SUM);
            checkFileNotEmpty(md5sum, AbstractChecker.ORIGINAL, false);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkSample(final IDirectory metadata)
    {
        try
        {
            final IDirectory sample = Utilities.getSubDirectory(metadata, AbstractChecker.SAMPLE);
            checkFileNotEmptyAndTrimmed(sample, AbstractChecker.SPACE_CODE);
            checkFileNotEmptyAndTrimmed(sample, AbstractChecker.INSTANCE_CODE);
            checkFileContainsUUID(sample, AbstractChecker.INSTANCE_UUID);
            checkFileNotEmptyAndTrimmed(sample, AbstractChecker.TYPE_DESCRIPTION);
            checkFileNotEmptyAndTrimmed(sample, AbstractChecker.TYPE_CODE);
            checkFileNotEmptyAndTrimmed(sample, AbstractChecker.CODE);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkExperimentRegistrator(final IDirectory metadata)
    {
        try
        {
            final IDirectory experimentRegistrator =
                    Utilities.getSubDirectory(metadata, AbstractChecker.EXPERIMENT_REGISTRATOR);
            checkFileNotEmptyAndTrimmed(experimentRegistrator, AbstractChecker.FIRST_NAME);
            checkFileNotEmptyAndTrimmed(experimentRegistrator, AbstractChecker.LAST_NAME);
            checkFileNotEmptyAndTrimmed(experimentRegistrator, AbstractChecker.EMAIL);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkRegistrationTimestamp(final IDirectory metadata)
    {
        checkFileContainsTimestamp(metadata, AbstractChecker.EXPERIMENT_REGISTRATION_TIMESTAMP);
    }

    private void checkExperimentIdentifier(final IDirectory metadata)
    {
        try
        {
            final IDirectory experimentIdentifier =
                    Utilities.getSubDirectory(metadata, AbstractChecker.EXPERIMENT_IDENTIFIER);
            checkFileNotEmptyAndTrimmed(experimentIdentifier, AbstractChecker.INSTANCE_CODE);
            checkFileContainsUUID(experimentIdentifier, AbstractChecker.INSTANCE_UUID);
            checkFileNotEmptyAndTrimmed(experimentIdentifier, AbstractChecker.SPACE_CODE);
            checkFileNotEmptyAndTrimmed(experimentIdentifier, AbstractChecker.PROJECT_CODE);
            checkFileNotEmptyAndTrimmed(experimentIdentifier, AbstractChecker.EXPERIMENT_CODE);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkFormat(final IDirectory metadata)
    {
        try
        {
            final IDirectory format = getFormatOrFail(metadata);
            checkVersion(format);
            checkFileNotEmptyAndTrimmed(format, AbstractChecker.CODE);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkDataSet(final IDirectory metadata)
    {
        try
        {
            final IDirectory dataSet =
                    Utilities.getSubDirectory(metadata, AbstractChecker.DATA_SET);
            checkFileNotEmptyAndTrimmed(dataSet, AbstractChecker.CODE);
            checkFileContainsTimestamp(dataSet, AbstractChecker.PRODUCTION_TIMESTAMP);
            checkFileNotEmptyAndTrimmed(dataSet, AbstractChecker.PRODUCER_CODE);
            checkFileNotEmptyAndTrimmed(dataSet, AbstractChecker.OBSERVABLE_TYPE);
            final Boolean isMeasured = checkFileContainsBoolean(dataSet, IS_MEASURED);
            checkFileContainsEnumeration(dataSet, IS_COMPLETE, new String[]
                { TRUE, FALSE, UNKNOWN });
            checkParentCodes(dataSet, isMeasured);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkParentCodes(final IDirectory dataSet, final Boolean isMeasured)
    {
        try
        {
            if (isMeasured == null || isMeasured.booleanValue())
            {
                checkFileExists(dataSet, PARENT_CODES);
            } else
            {
                checkFileNotEmptyAndTrimmed(dataSet, PARENT_CODES);
            }
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkData(final IDirectory containerNode)
    {
        try
        {
            final IDirectory dataDir =
                    Utilities.getSubDirectory(containerNode, AbstractChecker.DATA);
            checkAndTryGetDirectory(dataDir, ORIGINAL);
            checkAndTryGetDirectory(dataDir, STANDARD);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkFileContainsTimestamp(final IDirectory dataDir, final String name)
    {
        try
        {
            checkTrimmed(problemReport, dataDir, name);
            if (Utilities.tryGetDate(dataDir, name) == null)
            {
                throw new DataStructureException(String.format(
                        AbstractChecker.MSG_DOES_NOT_CONTAIN_TIMESTAMP, name));
            }
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private final void checkFileContainsUUID(final IDirectory directory, final String name)
    {
        try
        {
            checkTrimmed(problemReport, directory, name);
            final String value = Utilities.getTrimmedString(directory, name);
            if (Constants.UUID_PATTERN.matcher(value).matches() == false)
            {
                throw new DataStructureException(String.format(AbstractChecker.MSG_WRONG_UUID,
                        value, name));
            }
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private final void checkFileNotEmpty(final IDirectory dataDir, final String name,
            final boolean mustBeTrimmed)
    {
        try
        {
            if (mustBeTrimmed)
            {
                checkTrimmed(problemReport, dataDir, name);
            }
            final IFile file = getFileOrFail(dataDir, name);
            if (StringUtils.isEmpty(file.getStringContent()))
            {
                throw new DataStructureException(String.format(AbstractChecker.MSG_EMPTY_FILE,
                        name, dataDir));
            }

        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkFileNotEmptyAndTrimmed(final IDirectory dataDir, final String name)
    {
        checkFileNotEmpty(dataDir, name, true);
    }

    private void checkVersion(final IDirectory containerNode)
    {
        try
        {
            final IDirectory versionNode = Utilities.getSubDirectory(containerNode, VERSION);
            checkFileContainsNumber(versionNode, MAJOR);
            checkFileContainsNumber(versionNode, MINOR);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

}
