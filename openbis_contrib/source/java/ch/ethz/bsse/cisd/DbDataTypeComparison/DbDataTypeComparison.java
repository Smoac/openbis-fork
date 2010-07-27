/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.DbDataTypeComparison;

/**
 * @author Manuel Kohler
 */

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.lemnik.eodsql.QueryTool;

public class DbDataTypeComparison
{
    private static final String DRY_RUN_FLAG = "--dry-run";

    private static Connection connect(String jdbcURL) throws ClassNotFoundException, SQLException
    {
        Class.forName("org.postgresql.Driver");
        final Driver driver = DriverManager.getDriver(jdbcURL);
        final Properties props = new Properties();
        props.put("user", System.getProperty("user.name"));
        props.put("password", "");
        return driver.connect(jdbcURL, props);
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length < 3)
        {
            String programName = getProgramName();
            System.err.println("Usage: java -jar " + programName
                    + " <source_db_jdbc_string> <target_db_jdbc_string> <user_ID> [" + DRY_RUN_FLAG
                    + "]");
            System.exit(1);
        }

        String source_db_jdbc_string = args[0];
        String target_db_jdbc_string = args[1];

        System.out.println("Used Source: " + source_db_jdbc_string);
        System.out.println("Used Target: " + target_db_jdbc_string);

        final Connection sourceconn = connect(source_db_jdbc_string);
        final Connection targetconn = connect(target_db_jdbc_string);

        DbDataTypeComparisonDAO dbSourceDao =
                QueryTool.getQuery(sourceconn, DbDataTypeComparisonDAO.class);
        DbDataTypeComparisonDAO dbTargetDao =
                QueryTool.getQuery(targetconn, DbDataTypeComparisonDAO.class);

        Set<String> dataSetTypeCodes =
                queryStringLists(Arrays.asList(dbSourceDao.listDataSetTypeCodes()),
                        Arrays.asList(dbTargetDao.listDataSetTypeCodes()));

        Set<String> experimentTypeCodes =
                queryStringLists(Arrays.asList(dbSourceDao.listExperimentTypeCodes()),
                        Arrays.asList(dbTargetDao.listExperimentTypeCodes()));

        Set<String> materialTypeCodes =
                queryStringLists(Arrays.asList(dbSourceDao.listMaterialTypeCodes()),
                        Arrays.asList(dbTargetDao.listMaterialTypeCodes()));

        Set<String> propertyTypeCodes =
                queryStringLists(Arrays.asList(dbSourceDao.listPropertyTypeCodes()),
                        Arrays.asList(dbTargetDao.listPropertyTypeCodes()));

        Set<String> sampleTypeCodes =
                queryStringLists(Arrays.asList(dbSourceDao.listSampleTypeCodes()),
                        Arrays.asList(dbTargetDao.listSampleTypeCodes()));

        Set<PropertyTypeAndDataTypeDTO> PropertyTypeAndDataType =
                queryStringLists(dbSourceDao.getPropertyTypeAndDataType(),
                        dbTargetDao.getPropertyTypeAndDataType());

        // Assignments
        Set<SamplePropertyAssignmentsDTO> SamplePropertyAssignments =
                queryStringLists(dbSourceDao.getSamplePropertyAssignments(),
                        dbTargetDao.getSamplePropertyAssignments());

        Set<DataSetPropertyAssignmentsDTO> DataSetPropertyAssignments =
                queryStringLists(dbSourceDao.getDataSetPropertyAssignments(),
                        dbTargetDao.getDataSetPropertyAssignments());

        Set<ExperimentPropertyAssignmentsDTO> ExperimentPropertyAssignments =
                queryStringLists(dbSourceDao.getExperimentPropertyAssignments(),
                        dbTargetDao.getExperimentPropertyAssignments());

        Set<MaterialPropertyAssignmentDTO> MaterialPropertyAssignment =
                queryStringLists(dbSourceDao.getMaterialPropertyAssignment(),
                        dbTargetDao.getMaterialPropertyAssignment());

        Set<ControlledVocabulariesDTO> ControlledVocabularies =
                queryStringLists(dbSourceDao.getControlledVocabularies(),
                        dbTargetDao.getControlledVocabularies());

        Set<TermsOfControlledVocabulariesDTO> TermsOfControlledVocabularies =
                queryStringLists(dbSourceDao.getTermsOfControlledVocabularies(),
                        dbTargetDao.getTermsOfControlledVocabularies());

        System.out.println("Differences:");
        System.out.println("dataSetTypeCodes: " + dataSetTypeCodes);
        System.out.println("experimentTypeCodes: " + experimentTypeCodes);
        System.out.println("materialTypeCodes: " + materialTypeCodes);
        System.out.println("propertyTypeCodes: " + propertyTypeCodes);
        System.out.println("sampleTypeCodes: " + sampleTypeCodes);

        System.out.println("====================");

        System.out.println("PropertyTypeAndDataType: " + PropertyTypeAndDataType);
        System.out.println("SamplePropertyAssignments: " + SamplePropertyAssignments);
        System.out.println("DataSetPropertyAssignments: " + DataSetPropertyAssignments);
        System.out.println("ExperimentPropertyAssignments: " + ExperimentPropertyAssignments);
        System.out.println("MaterialPropertyAssignment: " + MaterialPropertyAssignment);
        System.out.println("ControlledVocabularies: " + ControlledVocabularies);
        System.out.println("TermsOfControlledVocabularies: " + TermsOfControlledVocabularies);

        sourceconn.close();
        targetconn.close();
    }

    private static <T> Set<T> queryStringLists(List<T> sourceList, List<T> targetList)
    {
        Set<T> intersect = new HashSet<T>(targetList);
        intersect.retainAll(sourceList);
        System.out.println("Number of common objects: " + intersect.size());
        // System.out.println(intersect);

        Set<T> diff = new HashSet<T>(targetList);
        diff.removeAll(sourceList);
        System.out.println("Number of differential objects: " + diff.size());
        // System.out.println(diff);

        return diff;
    }

    private static String getProgramName()
    {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement main = stack[stack.length - 1];
        String mainClass = main.getClassName();

        // workaround, because the split does not work with a '.'
        mainClass = mainClass.replace(".", " ");
        String[] tokens = mainClass.split(" ");

        String jarName = tokens[tokens.length - 1] + ".jar";
        return jarName;
    }
}