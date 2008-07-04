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
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;

/**
 * Allows to check consistency of <code>HCS_IMAGE V1.0</code> format. Program will try to find all
 * the problems. If the path provided as an argument is not a readable directory or version is
 * incorrect (directory with version does not exist, cannot be parsed, etc.) - program will stop
 * processing the path complaining only about this basic problem.
 * 
 * @author Izabela Adamczyk
 */
public class HCSImageChecker extends AbstractChecker
{

    public HCSImageChecker(final boolean verbose)
    {
        super(verbose);
    }

    /**
     * Entry point. If given BDS structure contains inconsistent HCS_IMAGE data, prints a report
     * containing all problems found and exits with code <code>1</code>, otherwise exits with
     * code <code>0</code>.
     * 
     * @param args - BDS directory
     */
    public static void main(final String[] args)
    {
        try
        {
            final File bdsDirectory = getBdsDirectory(args, HCSImageChecker.class.getName());

            final ProblemReport report =
                    new HCSImageChecker(isVerbose(args, HCSImageChecker.class.getName()))
                            .getHCSImageConsistencyReport(bdsDirectory);
            printReportAndExit(report);
        } catch (final Exception e)
        {
            exitWithFatalError(e);
        }

    }

    /**
     * Returns a {@link ProblemReport} with information about problems with HCS_IMAGE structure
     * inconsistencies.
     */
    public ProblemReport getHCSImageConsistencyReport(final File bdsDirectory)
    {
        checkIsDirectory(bdsDirectory);
        final IDirectory containerNode = NodeFactory.createDirectoryNode(bdsDirectory);
        checkMetadataObservableType(containerNode);
        checkMetadataFormatCode(containerNode);
        checkMetadataFormatVersion(containerNode);
        checkMetadataParametersAndAnnotations(containerNode);
        return problemReport;
    }

    private void checkAnnotations(final IDirectory containerNode, final Integer numberOfChannels)
    {
        try
        {
            if (numberOfChannels == null)
            {
                problemReport
                        .warning(MSG_NUMBER_OF_CHANNELS_DOES_NOT_CONTAIN_A_NUMBER_SO_CHANNEL_ANNOTATIONS_COULD_NOT_BE_CHECKED);
                return;
            }
            for (int i = 1; i <= numberOfChannels.intValue(); i++)
            {
                final IDirectory channel =
                        Utilities.getSubDirectory(containerNode, path(ANNOTATIONS, CHANNEL) + i);
                checkFileContainsNumber(channel, WAVELENGTH);
            }

        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }

    }

    private void checkData(final IDirectory containerNode, final Integer numberOfChannels,
            final Integer numberOfPlateRows, final Integer numberOfPlateColumns,
            final Integer numberOfWellRows, final Integer numberOfWellColumns)
    {
        try
        {
            final IDirectory dataStandard =
                    Utilities.getSubDirectory(containerNode, path(DATA, STANDARD));

            final String missingInformations =
                    checkAllNecesseryInformationPresent(numberOfChannels, numberOfPlateRows,
                            numberOfPlateColumns, numberOfWellRows, numberOfWellColumns);
            if (missingInformations != null)
            {
                problemReport.warning(missingInformations);
                return;
            }
            try
            {
                final String isCompleteStr =
                        Utilities.getTrimmedString(containerNode, path(METADATA, DATA_SET,
                                IS_COMPLETE));
                if (TRUE.equals(isCompleteStr))
                {
                    checkChannels(numberOfChannels, numberOfPlateRows, numberOfPlateColumns,
                            numberOfWellRows, numberOfWellColumns, dataStandard);
                }
            } catch (final Exception e)
            {
                problemReport.error(e.getMessage());
            }

        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }

    }

    private void checkChannels(final Integer numberOfChannels, final Integer numberOfPlateRows,
            final Integer numberOfPlateColumns, final Integer numberOfWellRows,
            final Integer numberOfWellColumns, final IDirectory dataStandard)
    {
        channel: for (int channelNr = 1; channelNr <= numberOfChannels.intValue(); channelNr++)
        {

            {
                final IDirectory channelDir =
                        checkAndTryGetDirectory(dataStandard, CHANNEL + channelNr);
                if (channelDir == null)
                {
                    continue channel;
                }
                checkRows(numberOfPlateRows, numberOfPlateColumns, numberOfWellRows,
                        numberOfWellColumns, dataStandard, channelDir);
            }

        }
    }

    private void checkRows(final Integer numberOfPlateRows, final Integer numberOfPlateColumns,
            final Integer numberOfWellRows, final Integer numberOfWellColumns,
            final IDirectory dataStandard, final IDirectory channelDir)
    {
        for (int plateRowNr = 1; plateRowNr <= numberOfPlateRows.intValue(); plateRowNr++)
        {
            final IDirectory plateRowDir = checkAndTryGetDirectory(channelDir, ROW + plateRowNr);
            if (plateRowDir == null)
            {
                continue;
            }
            checkColumns(numberOfPlateColumns, numberOfWellRows, numberOfWellColumns, dataStandard);
        }
    }

    private void checkColumns(final Integer numberOfPlateColumns, final Integer numberOfWellRows,
            final Integer numberOfWellColumns, final IDirectory dataStandard)
    {
        for (int plateColumnNr = 1; plateColumnNr <= numberOfPlateColumns.intValue(); plateColumnNr++)
        {
            final IDirectory plateColumnDir =
                    checkAndTryGetDirectory(dataStandard, COLUMN + plateColumnNr);
            if (plateColumnDir == null)
            {
                continue;
            }
            checkWells(numberOfWellRows, numberOfWellColumns, plateColumnDir);
        }
    }

    private void checkWells(final Integer numberOfWellRows, final Integer numberOfWellColumns,
            final IDirectory plateColumnDir)
    {
        for (int wellRowNr = 1; wellRowNr <= numberOfWellRows.intValue(); wellRowNr++)
        {
            for (int wellColumnNr = 1; wellColumnNr <= numberOfWellColumns.intValue(); wellColumnNr++)
            {
                final String filename = String.format(ROW_COLUMN_TIFF, wellRowNr, wellColumnNr);
                checkFileExists(plateColumnDir, filename);
            }
        }
    }

    private String checkAllNecesseryInformationPresent(final Integer numberOfChannels,
            final Integer numberOfPlateRows, final Integer numberOfPlateColumns,
            final Integer numberOfWellRows, final Integer numberOfWellColumns)
    {
        if (numberOfChannels == null || numberOfPlateColumns == null || numberOfPlateRows == null
                || numberOfWellColumns == null || numberOfWellRows == null)
        {
            final List<String> missing = new ArrayList<String>();
            if (numberOfChannels == null)
            {
                missing.add(AbstractChecker.MSG_NUMBER_OF_CHANNELS_DOES_NOT_CONTAIN_NUMBER);
            }
            if (numberOfPlateColumns == null)
            {
                missing.add(AbstractChecker.MSG_PLATE_GEOMETRY_COLUMNS_DOES_NOT_CONTAIN_NUMBER);
            }
            if (numberOfPlateRows == null)
            {
                missing.add(AbstractChecker.MSG_PLATE_GEOMETRY_ROWS_DOES_NOT_CONTAIN_NUMBER);
            }
            if (numberOfWellColumns == null)
            {
                missing.add(AbstractChecker.MSG_WELL_GEOMETRY_COLUMNS_DOES_NOT_CONTAIN_NUMBER);
            }
            if (numberOfWellRows == null)
            {
                missing.add(AbstractChecker.MSG_WELL_GEOMETRY_ROWS_DOES_NOT_CONTAIN_NUMBER);
            }
            return MSG_NOT_ENOUGH_INFORMATION_TO_CHECK_STANDARD_DATA_CONSISTENCY_PROBLEMS + missing;
        }
        return null;
    }

    private void checkMetadataParametersAndAnnotations(final IDirectory containerNode)
    {
        try
        {
            checkAndTryGetDirectory(containerNode, path(METADATA, PARAMETERS, PLATE_GEOMETRY));
            final Integer plateRows =
                    checkFileContainsNumber(containerNode, path(METADATA, PARAMETERS,
                            PLATE_GEOMETRY, ROWS));
            final Integer plateColumns =
                    checkFileContainsNumber(containerNode, path(METADATA, PARAMETERS,
                            PLATE_GEOMETRY, COLUMNS));
            checkAndTryGetDirectory(containerNode, path(METADATA, PARAMETERS, WELL_GEOMETRY));
            final Integer wellRows =
                    checkFileContainsNumber(containerNode, path(METADATA, PARAMETERS,
                            WELL_GEOMETRY, ROWS));
            final Integer wellColumns =
                    checkFileContainsNumber(containerNode, path(METADATA, PARAMETERS,
                            WELL_GEOMETRY, COLUMNS));
            final Integer numberOfChannels =
                    checkFileContainsNumber(containerNode, path(METADATA, PARAMETERS,
                            NUMBER_OF_CHANNELS));
            checkAnnotations(containerNode, numberOfChannels);
            checkData(containerNode, numberOfChannels, plateRows, plateColumns, wellRows,
                    wellColumns);
            checkOriginalDataConsistentWithMetadataParameters(containerNode);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkOriginalDataConsistentWithMetadataParameters(final IDirectory containerNode)
    {
        final Boolean containsOriginalData =
                checkFileContainsBoolean(containerNode, path(METADATA, PARAMETERS,
                        CONTAINS_ORIGINAL_DATA));
        if (containsOriginalData == null)
        {
            problemReport
                    .warning(MSG_INCORRECT_CONTAINS_ORIGINAL_DATA_VALUE_CANNOT_CHECK_MAPPING_AND_ORIGINAL_DATA);
        } else
        {
            if (containsOriginalData.booleanValue())
            {
                checkStandardOriginalMapping(containerNode);

            } else
            {

                checkFileIsEmpty(containerNode, path(METADATA, STANDARD_ORIGINAL_MAPPING));
                final IDirectory original =
                        checkAndTryGetDirectory(containerNode, path(DATA, ORIGINAL));
                if (original != null && original.iterator().hasNext())
                {
                    problemReport
                            .error(MSG_CONTAINS_ORIGINAL_DATA_IS_SET_TO_FALSE_BUT_DATA_ORIGINAL_IS_NOT_EMPTY);
                }
            }
        }
    }

    private void checkMetadataObservableType(final IDirectory containerNode)
    {
        try
        {
            checkFileContainsEnumeration(containerNode, path(METADATA, DATA_SET, OBSERVABLE_TYPE),
                    new String[]
                        { AbstractChecker.HCS_IMAGE });
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkMetadataFormatVersion(final IDirectory containerNode)
    {
        try
        {
            final IDirectory format =
                    Utilities.getSubDirectory(containerNode, path(METADATA, FORMAT));
            checkSpecificVersion(format, 1, 0);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }

    }

    private void checkMetadataFormatCode(final IDirectory containerNode)
    {
        try
        {
            checkFileContainsEnumeration(containerNode, path(METADATA, FORMAT, CODE), new String[]
                { AbstractChecker.HCS_IMAGE });
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }

    }
}
