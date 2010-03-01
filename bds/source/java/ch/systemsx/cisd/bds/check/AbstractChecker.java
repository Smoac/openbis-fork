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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.bds.StringUtils;
import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * Contains a bunch of tools useful in data structure or HCS_IMAGE format consistency checking.
 * 
 * @author Izabela Adamczyk
 */
public abstract class AbstractChecker
{

    protected final ProblemReport problemReport = new ProblemReport();

    protected final boolean verbose;

    protected static final String CONTAINS_ORIGINAL_DATA = "contains_original_data";

    protected static final String NUMBER_OF_CHANNELS = "number_of_channels";

    protected static final String PLATE_GEOMETRY = "plate_geometry";

    protected static final String COLUMNS = "columns";

    protected static final String ROWS = "rows";

    protected static final String WELL_GEOMETRY = "well_geometry";

    public static final String ANNOTATIONS = "annotations";

    public static final String CODE = "code";

    public static final String DATA = "data";

    public static final String DATA_SET = "data_set";

    public static final String EMAIL = "email";

    public static final String EXPERIMENT_CODE = "experiment_code";

    public static final String EXPERIMENT_IDENTIFIER = "experiment_identifier";

    public static final String EXPERIMENT_REGISTRATION_TIMESTAMP =
            "experiment_registration_timestamp";

    public static final String EXPERIMENT_REGISTRATOR = "experiment_registrator";

    public static final String WAVELENGTH = "wavelength";

    public static final String FALSE = ch.systemsx.cisd.bds.Utilities.Boolean.FALSE.toString();

    public static final String TRUE = ch.systemsx.cisd.bds.Utilities.Boolean.TRUE.toString();

    public static final String FIRST_NAME = "first_name";

    public static final String SPACE_CODE = "space_code";

    public static final String INSTANCE_CODE = "instance_code";

    public static final String INSTANCE_UUID = "instance_uuid";

    public static final String IS_COMPLETE = "is_complete";

    public static final String IS_MEASURED = "is_measured";

    public static final String LAST_NAME = "last_name";

    public static final String MAJOR = "major";

    public static final String MD5SUM = "md5sum";

    public static final String METADATA = "metadata";

    public static final String MINOR = "minor";

    public static final String OBSERVABLE_TYPE = "observable_type";

    public static final String ORIGINAL = "original";

    public static final String PARAMETERS = "parameters";

    public static final String PARENT_CODES = "parent_codes";

    public static final String PRODUCER_CODE = "producer_code";

    public static final String PRODUCTION_TIMESTAMP = "production_timestamp";

    public static final String PROJECT_CODE = "project_code";

    public static final String SAMPLE = "sample";

    public static final String STANDARD = "standard";

    public static final String STANDARD_ORIGINAL_MAPPING = "standard_original_mapping";

    public static final String TYPE_CODE = "type_code";

    public static final String TYPE_DESCRIPTION = "type_description";

    public static final String UNKNOWN = "UNKNOWN";

    public static final String VERSION = "version";

    public static final String CHANNEL = "channel";

    public static final String COLUMN = "column";

    public static final String FORMAT = "format";

    public static final String HCS_IMAGE = "HCS_IMAGE";

    public static final String ROW = "row";

    public static final String ROW_COLUMN_TIFF = "row%s_column%s.tiff";

    public static final String MSG_DOES_NOT_CONTAIN_TIMESTAMP = "'%s' does not contain timestamp.";

    public static final String MSG_EMPTY_FILE = "'%s' found in directory '%s' is empty.";

    public static final String FATAL_ERROR = "FATAL ERROR: ";

    public static final String MSG_CONTAINS_ORIGINAL_DATA_IS_SET_TO_FALSE_BUT_DATA_ORIGINAL_IS_NOT_EMPTY =
            "contains_original_data is set to FALSE but data/original is not empty";

    private static final String MSG_DIRECTORY_DOES_NOT_EXIST = "Directory does not exist.";

    public static final String MSG_EXPECTED_DIRECTORY = "Expected directory as argument.";

    public static final String MSG_INCORRECT_CONTAINS_ORIGINAL_DATA_VALUE_CANNOT_CHECK_MAPPING_AND_ORIGINAL_DATA =
            "Incorrect value of 'contains_original_data' - cannot check mapping of standard and original data.";

    public static final String MSG_NO_DIRECTORY_SPECIFIED = "No directory specified.";

    public static final String MSG_FILE_NOT_FOUND = "File '%s' missing in '%s'.";

    public static final String MSG_NOT_ENOUGH_INFORMATION_TO_CHECK_STANDARD_DATA_CONSISTENCY_PROBLEMS =
            "Not enough information to check standard data consistency. Problems: ";

    public static final String MSG_NUMBER_OF_CHANNELS_DOES_NOT_CONTAIN_A_NUMBER_SO_CHANNEL_ANNOTATIONS_COULD_NOT_BE_CHECKED =
            "'number_of_channels' does not contain a number - channel annotations could not be checked.";

    public static final String MSG_NUMBER_OF_CHANNELS_DOES_NOT_CONTAIN_NUMBER =
            "number_of_channels does not contain number";

    public static final String MSG_PLATE_GEOMETRY_COLUMNS_DOES_NOT_CONTAIN_NUMBER =
            "plate_geometry/columns does not contain number";

    public static final String MSG_PLATE_GEOMETRY_ROWS_DOES_NOT_CONTAIN_NUMBER =
            "plate_geometry/rows does not contain number";

    public static final String MSG_IS_NOT_FILE = "'%s' found in directory '%s' is not a file.";

    public static final String MSG_IS_NOT_EMPTY = "'%s' found in directory '%s' is not empty.";

    public static final String MSG_UNEXPECTED_VALUE =
            "Unexpected value loaded from '%s'. (Expected: %s, but was: %s)";

    public static final String MSG_VALUE_NOT_IN_ENUMERATION =
            "Value '%s' loaded from  file '%s' does not belong to %s";

    public static final String MSG_WELL_GEOMETRY_COLUMNS_DOES_NOT_CONTAIN_NUMBER =
            "well_geometry/columns does not contain number";

    public static final String MSG_WELL_GEOMETRY_ROWS_DOES_NOT_CONTAIN_NUMBER =
            "well_geometry/rows does not contain number";

    private static final String MSG_ERROR_IN_STANDARD_ORIGINAL_MAPPING_LINE =
            "Error in standard-original mapping line ";

    private static final String IO_EXCEPTION_WHILE_COMPARING_FILES =
            "I/O Exception while comparing file '%s' with file '%s': %s";

    public static final String MSG_WRONG_UUID = "Value '%s' found in file '%s' is not a UUID";

    protected static final String path(final String... args)
    {
        final String separator = "/";
        final StringBuilder path = new StringBuilder();
        boolean isFirst = true;
        for (final String s : args)
        {
            if (isFirst == false)
            {
                path.append(separator);
            }
            path.append(s);
            isFirst = false;
        }
        return path.toString();
    }

    public AbstractChecker(final boolean verbose)
    {
        this.verbose = verbose;
    }

    protected IDirectory checkAndTryGetDirectory(final IDirectory dataDir, final String name)
    {
        try
        {
            return Utilities.getSubDirectory(dataDir, name);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
            return null;
        }
    }

    protected Boolean checkFileContainsBoolean(final IDirectory dataDir, final String name)
    {
        try
        {
            checkTrimmed(problemReport, dataDir, name);
            return Utilities.getBoolean(dataDir, name).toBoolean();
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
            return null;
        }
    }

    protected void checkFileContainsEnumeration(final IDirectory dataSet, final String name,
            final String[] values)
    {
        try
        {
            checkTrimmed(problemReport, dataSet, name);
            final String loadedValue = Utilities.getTrimmedString(dataSet, name);
            boolean matches = false;
            for (final String value : values)
            {
                if (value.compareToIgnoreCase(loadedValue) == 0)
                {
                    matches = true;
                    break;
                }
            }
            if (matches == false)
            {
                throw new DataStructureException(String.format(MSG_VALUE_NOT_IN_ENUMERATION,
                        loadedValue, name, Arrays.asList(values)));
            }

        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }

    }

    protected Integer checkFileContainsNumber(final IDirectory dir, final String file)
    {
        try
        {
            checkTrimmed(problemReport, dir, file);
            return Utilities.getNumber(dir, file);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
            return null;
        }
    }

    protected void checkFileExists(final IDirectory dataDir, final String name)
    {
        try
        {
            getFileOrFail(dataDir, name);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    protected void checkFileIsEmpty(final IDirectory dataDir, final String name)
    {
        try
        {
            final IFile file = getFileOrFail(dataDir, name);
            if (StringUtils.isEmpty(file.getStringContent()) == false)
            {
                throw new DataStructureException(String.format(MSG_IS_NOT_EMPTY, name, dataDir));
            }

        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    /**
     * Checks if given argument is not null and is a directory.
     * 
     * @param directory to check
     * @throws IllegalArgumentException if argument is null or is not a directory
     */
    protected void checkIsDirectory(final File directory)
    {
        if (directory == null)
        {
            throw new IllegalArgumentException(MSG_NO_DIRECTORY_SPECIFIED);
        } else if (directory.exists() == false)
        {
            throw new IllegalArgumentException(MSG_DIRECTORY_DOES_NOT_EXIST);
        } else if (directory.isDirectory() == false)
        {
            throw new IllegalArgumentException(MSG_EXPECTED_DIRECTORY);
        }
    }

    protected void checkSpecificVersion(final IDirectory containerNode, final int major,
            final int minor)
    {

        try
        {
            final IDirectory versionNode = Utilities.getSubDirectory(containerNode, VERSION);
            checkLoadedValue(versionNode, MAJOR, major);
            checkLoadedValue(versionNode, MINOR, minor);
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    protected void checkStandardOriginalMapping(final IDirectory bdsRoot)
    {
        try
        {
            final List<String> mappingLines =
                    getFileOrFail(bdsRoot, path(METADATA, STANDARD_ORIGINAL_MAPPING))
                            .getStringContentList();

            for (int i = 0; i < mappingLines.size(); i++)
            {
                final String line = mappingLines.get(i);
                final String[] values = line.split("\t", 3);
                final int currentLineNumber = i + 1;
                if (values.length != 3)
                {
                    problemReport.error(MSG_ERROR_IN_STANDARD_ORIGINAL_MAPPING_LINE
                            + currentLineNumber + ": found " + values.length + " column"
                            + (values.length == 1 ? "" : "s") + " (should be 3)");
                } else
                {
                    final String fileName1 = values[0];
                    final String relationshipOperator = values[1];
                    final String fileName2 = values[2];
                    if ("I".equals(relationshipOperator))
                    {
                        checkFilesIdentical(bdsRoot, currentLineNumber, fileName1, fileName2);

                    } else if ("T".equals(relationshipOperator) == false)
                    {
                        problemReport.error(MSG_ERROR_IN_STANDARD_ORIGINAL_MAPPING_LINE
                                + currentLineNumber + ": unknown relationship operator '"
                                + relationshipOperator
                                + "' in second column (should be either 'I' or 'T')");
                    }
                }

            }
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    private void checkFilesIdentical(final IDirectory bdsRoot, final int currentLineNumber,
            final String fileName1, final String fileName2)
    {
        final IFile file1 = tryGetFile(bdsRoot, path(DATA, STANDARD, fileName1));
        final IFile file2 = tryGetFile(bdsRoot, path(DATA, ORIGINAL, fileName2));
        if (file1 == null || file2 == null)
        {
            final List<String> missing = new ArrayList<String>();
            if (file1 == null)
            {
                missing.add(fileName1);
            }
            if (file2 == null)
            {
                missing.add(fileName2);
            }
            problemReport.error(MSG_ERROR_IN_STANDARD_ORIGINAL_MAPPING_LINE + currentLineNumber
                    + ": missing files " + missing);
            return;
        }
        InputStream input1 = null;
        InputStream input2 = null;
        try
        {
            input1 = file1.getInputStream();
            input2 = file2.getInputStream();
            if (IOUtils.contentEquals(input1, input2) == false)
            {
                problemReport.error(MSG_ERROR_IN_STANDARD_ORIGINAL_MAPPING_LINE + currentLineNumber
                        + ": content of the files is supposed to be identical but is different ("
                        + fileName1 + "," + fileName2 + ")");
            }
        } catch (final IOException ex)
        {
            problemReport.error(String.format(IO_EXCEPTION_WHILE_COMPARING_FILES, fileName1,
                    fileName2, ex.getMessage()));
        } finally
        {
            IOUtils.closeQuietly(input1);
            IOUtils.closeQuietly(input2);
        }
    }

    /**
     * Creates a {@link File} extracting it's name from method parameters
     */
    protected static File getBdsDirectory(final String[] args, final String programName)
    {
        final File file = new Parameters(args, programName).getFile();
        return file;
    }

    protected static boolean isVerbose(final String[] args, final String programName)
    {
        return new Parameters(args, programName).isVerbose();
    }

    protected IFile getFileOrFail(final IDirectory dataDir, final String name)
    {
        final INode node = dataDir.tryGetNode(name);
        if (node == null)
        {
            throw new DataStructureException(String.format(MSG_FILE_NOT_FOUND, name, dataDir));
        }

        if (node instanceof IFile == false)
        {
            throw new DataStructureException(String.format(MSG_IS_NOT_FILE, name, dataDir));
        }
        final IFile file = (IFile) node;
        return file;
    }

    protected IDirectory getFormatOrFail(final IDirectory metadata)
    {
        final IDirectory format = Utilities.getSubDirectory(metadata, FORMAT);
        return format;
    }

    protected IDirectory getMetadataOrFail(final IDirectory containerNode)
    {
        final IDirectory metadata = Utilities.getSubDirectory(containerNode, METADATA);
        return metadata;
    }

    private void checkLoadedValue(final IDirectory dir, final String file, final int value)
    {
        try
        {
            checkTrimmed(problemReport, dir, file);

            final int loaded = Utilities.getNumber(dir, file);
            if (loaded != value)
            {
                problemReport.error(String.format(MSG_UNEXPECTED_VALUE, file, value, loaded));
            }
        } catch (final Exception e)
        {
            problemReport.error(e.getMessage());
        }
    }

    public static void checkTrimmed(final ProblemReport problemReport, final IDirectory dir,
            final String file)
    {
        final String loaded = Utilities.getExactString(dir, file);
        final String loadedTrimmed = Utilities.getTrimmedString(dir, file);
        if (loaded.equals(loadedTrimmed) == false)
        {
            problemReport.error(String.format(
                    "Found not trimmed value in file '%s' (directory '%s').", file, dir));
        }
    }

    private IFile tryGetFile(final IDirectory bdsRoot, final String name)
    {
        IFile file1;
        try
        {
            file1 = getFileOrFail(bdsRoot, name);
        } catch (final Exception e)
        {
            file1 = null;
        }
        return file1;
    }

    protected static void printReportAndExit(final ProblemReport report)
    {
        if (report.noProblemsFound())
        {
            System.exit(0);
        } else
        {
            System.err.print(report);
            System.err.print(String.format("Consistency checking finished (%s problem%s found).\n",
                    report.numberOfProblems(), report.numberOfProblems() != 1 ? "s" : ""));
            System.exit(1);
        }
    }

    protected static void exitWithFatalError(final Exception e)
    {
        System.err.print(FATAL_ERROR + e.getMessage());
        System.exit(1);
    }

}
