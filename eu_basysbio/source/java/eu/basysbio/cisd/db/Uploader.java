/*
 * Copyright 2010 ETH Zuerich, CISD
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

package eu.basysbio.cisd.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;

import eu.basysbio.cisd.db.IOpenBISDataSetQuery.DataSetContainer;

/**
 * Uploads the datasets to the BaSysBio data mart. For now this is a standalone program.
 * 
 * @author Bernd Rinn
 */
public class Uploader
{
    private final static String DATA_STORE_ROOT_DIR = "/scratch/basysbio/store";

    public static void main(String[] args) throws Exception
    {
        final IOpenBISDataSetQuery query = DBUtils.createDataSetQuery();
        int count = 0;
        final StopWatch sw = new StopWatch();
        sw.start();
        for (DataSetContainer ds : query.listTimeSeriesDataSets())
        {
            try
            {
                processDataSet(ds);
                ++count;
            } catch (ParsingException ex)
            {
                System.err.println("Warning: " + ex.toString());
            } catch (NumberFormatException ex)
            {
                System.err.println("Warning: " + ex.toString());
            }
        }
        System.out.println();
        System.out.println(count + " TIME_SERIES Data Sets found (" + sw + ").");
    }

    private static void processDataSet(DataSetContainer ds) throws ParsingException,
            NumberFormatException, IOException, SQLException
    {
        final IBaSysBioUpdater updater = DBUtils.createBaSysBioUpdater();
        try
        {
            final long dataSetId = updater.insertDataSet(ds);
            System.out.printf("[%d] %s::(%s,%s) <%s> -> %s\n", dataSetId, ds.ds_code, ds.exp_code,
                    ds.exp_perm_id, ds.uploader_email, ds.ds_location);
            final File tabFile = findFile(ds.ds_location);
            final StopWatch sw = new StopWatch();
            System.out.printf("Starting upload file with length %d.\n", tabFile.length());
            sw.start();
            final BufferedReader r = new BufferedReader(new FileReader(tabFile));
            final String headerLine = getHeaderLine(r);
            final TimeSeriesHeaderDescriptor descriptor =
                    new TimeSeriesHeaderDescriptor(tabFile, updater, headerLine);
            if (isLcaMic(ds))
            {
                descriptor.identifierColumn = "BBA_ID";
            }
            printHeader(descriptor);
            final List<String> identifiers = new ArrayList<String>();
            final List<String> identifiersHumanReadable = new ArrayList<String>();
            final List<TimeSeriesColumnDescriptor> columnDescriptors =
                    new ArrayList<TimeSeriesColumnDescriptor>();
            final List<Long> rowIds = new ArrayList<Long>();
            final List<Long> valueGroupIds = new ArrayList<Long>();
            final List<Double> values = new ArrayList<Double>();
            String line;
            while ((line = getHeaderLine(r)) != null)
            {
                try
                {
                    if (isLcaMic(ds))
                    {
                        processLineLcaMic(tabFile, updater, descriptor, line, identifiers,
                                identifiersHumanReadable, columnDescriptors, values, rowIds,
                                valueGroupIds);
                    } else
                    {
                        processLineRegular(tabFile, updater, descriptor, line, identifiers,
                                identifiersHumanReadable, columnDescriptors, values, rowIds,
                                valueGroupIds);
                    }
                } catch (ParsingException ex)
                {
                    System.err.println("Warning: " + ex.toString());
                } catch (NumberFormatException ex)
                {
                    System.err.println("Warning: " + ex.toString());
                }
            }
            r.close();
            updater.insertTimeSeriesEntry(dataSetId, descriptor.identifierColumn,
                    columnDescriptors, identifiers, identifiersHumanReadable, values, rowIds,
                    valueGroupIds);
            updater.close(true);
            System.out.printf("Upload took %s\n", sw);
        } catch (RuntimeException ex)
        {
            updater.rollback();
            updater.close();
            throw ex;
        } catch (IOException ex)
        {
            updater.rollback();
            updater.close();
            throw ex;
        }
    }

    private static String getHeaderLine(final BufferedReader r) throws IOException
    {
        String line = "#";
        while (line != null && line.startsWith("#"))
        {
            line = r.readLine();
        }
        return line;
    }

    private static boolean isLcaMic(DataSetContainer ds)
    {
        return "LCA_MIC".equals(ds.dst_code);
    }

    private static void processLineRegular(File tabFile, IBaSysBioUpdater updater,
            TimeSeriesHeaderDescriptor descriptor, String line, List<String> identifiers,
            List<String> identifiersHumanReadable,
            List<TimeSeriesColumnDescriptor> columnDescriptors, List<Double> values,
            List<Long> rowIds, List<Long> valueGroupIds) throws ParsingException,
            NumberFormatException
    {
        if (line.startsWith("#"))
        {
            return;
        }
        final long rowId = updater.nextRowId();
        final String[] splittedLine = StringUtils.split(line, '\t');
        if (splittedLine.length != descriptor.firstValueColumn
                + descriptor.columnDescriptors.length)
        {
            throw new ParsingException(tabFile, "Line has wrong number of headers, expected: "
                    + (descriptor.firstValueColumn + descriptor.columnDescriptors.length)
                    + ", found: " + splittedLine.length);
        }

        for (int i = 0; i < descriptor.columnDescriptors.length; ++i)
        {
            final TimeSeriesColumnDescriptor colDesc = descriptor.columnDescriptors[i];
            identifiers.add(splittedLine[0]);
            if (descriptor.humanReadableColumnIdx > 0)
            {
                identifiersHumanReadable.add(splittedLine[descriptor.humanReadableColumnIdx]);
            } else
            {
                identifiersHumanReadable.add(null);
            }
            columnDescriptors.add(colDesc);
            final double value = Double.parseDouble(splittedLine[descriptor.firstValueColumn + i]);
            values.add(Double.isNaN(value) ? null : value);
            rowIds.add(rowId);
            valueGroupIds.add(descriptor.valueGroupMap.get(colDesc.getValueGroupDescriptor()));
        }
    }

    private static void processLineLcaMic(File tabFile, IBaSysBioUpdater updater,
            TimeSeriesHeaderDescriptor descriptor, String line, List<String> identifiers,
            List<String> identifiersHumanReadable,
            List<TimeSeriesColumnDescriptor> columnDescriptors, List<Double> values,
            List<Long> rowIds, List<Long> valueGroupIds) throws ParsingException,
            NumberFormatException
    {
        if (line.startsWith("#"))
        {
            return;
        }
        final long rowId = updater.nextRowId();
        final String[] splittedLine = StringUtils.split(line, '\t');
        if (splittedLine.length != descriptor.firstValueColumn
                + descriptor.columnDescriptors.length)
        {
            throw new ParsingException(tabFile, "Line has wrong number of headers, expected: "
                    + (descriptor.firstValueColumn + descriptor.columnDescriptors.length)
                    + ", found: " + splittedLine.length);
        }

        final int timePoint = Integer.parseInt(splittedLine[0]);
        for (int i = 0; i < descriptor.columnDescriptors.length; ++i)
        {
            final String biId = descriptor.columnDescriptors[i].getBiId();
            final TimeSeriesColumnDescriptor colDesc =
                    new TimeSeriesColumnDescriptor(descriptor.columnDescriptors[i], "NB", timePoint);
            identifiers.add(biId);
            if (descriptor.humanReadableColumnIdx > 0)
            {
                identifiersHumanReadable.add(splittedLine[descriptor.humanReadableColumnIdx]);
            } else
            {
                identifiersHumanReadable.add(null);
            }
            columnDescriptors.add(colDesc);
            final double value = Double.parseDouble(splittedLine[descriptor.firstValueColumn + i]);
            values.add(Double.isNaN(value) ? null : value);
            rowIds.add(rowId);
            Long valueGroupId = descriptor.valueGroupMap.get(colDesc.getValueGroupDescriptor());
            if (valueGroupId == null)
            {
                valueGroupId = updater.nextValueGroupId();
                descriptor.valueGroupMap.put(colDesc.getValueGroupDescriptor(), valueGroupId);
            }
            valueGroupIds.add(valueGroupId);
        }
    }

    private static void printHeader(TimeSeriesHeaderDescriptor descriptor)
    {
        System.out.printf("  #%d\n", descriptor.columnDescriptors.length);
        for (TimeSeriesColumnDescriptor colDesc : descriptor.columnDescriptors)
        {
            if ("NC".equals(colDesc.getControlledGene()) == false
                    || colDesc.getDataSetType().toLowerCase().startsWith("lca"))
            {
                System.out.printf("    type:%s,time:%d,value:%s,unit:%s,gene:%s\n", colDesc
                        .getDataSetType(), colDesc.getTimePoint(), colDesc.getValueType(), colDesc
                        .getUnit(), colDesc.getControlledGene());
            }
        }
    }

    private static File findFile(String location)
    {
        final File dir = new File(DATA_STORE_ROOT_DIR, location + "/original");
        final File fileOrNull = tryFindFile(dir);
        if (fileOrNull == null)
        {
            throw new RuntimeException("No tab file found in " + location);
        } else
        {
            return fileOrNull;
        }
    }

    private static File tryFindFile(File dir)
    {
        for (File f : dir.listFiles(new FilenameFilter()
            {
                public boolean accept(File directory, String name)
                {
                    return name.endsWith(".txt");
                }
            }))
        {
            return f;
        }
        // Try sub-directories
        for (File f : dir.listFiles())
        {
            if (f.isDirectory())
            {
                final File fileOrNull = tryFindFile(f);
                if (fileOrNull != null)
                {
                    return fileOrNull;
                }
            }
        }
        return null;
    }
}
