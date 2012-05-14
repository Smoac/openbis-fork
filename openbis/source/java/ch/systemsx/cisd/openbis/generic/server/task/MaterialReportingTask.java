/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * Task which feeds a reporting database with recently added/changed Materials.
 * 
 * @author Franz-Josef Elmer
 */
public class MaterialReportingTask implements IMaintenanceTask
{
    @Private
    static final String MAPPING_FILE_KEY = "mapping-file";

    @Private
    static final class MappingInfo
    {
        private static final class NameAndIndex
        {
            private final String name;

            private final int index;

            NameAndIndex(String name, int index)
            {
                this.name = name;
                this.index = index;
            }
        }

        private final String materialTypeCode;

        private final String tableName;

        private final String codeColumnName;

        private final Map<String, NameAndIndex> propertyMapping =
                new TreeMap<String, NameAndIndex>();

        MappingInfo(String materialTypeCode, String tableName, String codeColumnName)
        {
            this.materialTypeCode = materialTypeCode;
            this.tableName = tableName;
            this.codeColumnName = codeColumnName;
        }

        void addPropertyMapping(String propertyTypeCode, String propertyColumnName)
        {
            propertyMapping.put(propertyTypeCode, new NameAndIndex(propertyColumnName,
                    propertyMapping.size()));
        }

        String createInsertStatement()
        {
            StringBuilder builder = new StringBuilder("insert into ").append(tableName);
            builder.append(" (").append(codeColumnName);
            for (NameAndIndex nameAndIndex : propertyMapping.values())
            {
                builder.append(", ").append(nameAndIndex.name);
            }
            builder.append(") values(?");
            for (int i = 0; i < propertyMapping.size(); i++)
            {
                builder.append(", ?");
            }
            builder.append(")");
            return builder.toString();
        }

        BatchPreparedStatementSetter createSetter(final List<Material> materials)
        {
            return new BatchPreparedStatementSetter()
                {
                    public void setValues(PreparedStatement ps, int index) throws SQLException
                    {
                        Material material = materials.get(index);
                        List<IEntityProperty> properties = material.getProperties();
                        ps.setObject(1, material.getCode());
                        for (int i = 0; i < propertyMapping.size(); i++)
                        {
                            ps.setObject(i + 2, null);
                        }
                        for (IEntityProperty property : properties)
                        {
                            String code = property.getPropertyType().getCode();
                            NameAndIndex nameAndIndex = propertyMapping.get(code);
                            if (nameAndIndex != null)
                            {
                                ps.setObject(nameAndIndex.index + 2, property.tryGetAsString());
                            }
                        }
                    }

                    public int getBatchSize()
                    {
                        return materials.size();
                    }
                };
        }
    }

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MaterialReportingTask.class);

    private final ICommonServerForInternalUse server;

    private Map<String, MappingInfo> mapping;

    private SimpleDatabaseConfigurationContext dbConfigurationContext;

    public MaterialReportingTask()
    {
        this(CommonServiceProvider.getCommonServer());
    }

    public MaterialReportingTask(ICommonServerForInternalUse server)
    {
        this.server = server;
    }

    public void setUp(String pluginName, Properties properties)
    {
        dbConfigurationContext = new SimpleDatabaseConfigurationContext(properties);
        // String readTimestampSql = PropertyUtils.getMandatoryProperty(properties,
        // "read-timestamp-sql");
        // String writeTimestampSql = PropertyUtils.getMandatoryProperty(properties,
        // "write-timestamp-sql");
        String mappingFileName = PropertyUtils.getMandatoryProperty(properties, MAPPING_FILE_KEY);
        mapping = readMappingFile(mappingFileName);
    }

    @Private
    void closeDatabaseConnections()
    {
        if (dbConfigurationContext != null)
        {
            dbConfigurationContext.closeConnections();
        }
    }

    public void execute()
    {
        SessionContextDTO contextOrNull = server.tryToAuthenticateAsSystem();
        if (contextOrNull == null)
        {
            return;
        }
        String sessionToken = contextOrNull.getSessionToken();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dbConfigurationContext.getDataSource());
        Map<String, List<Material>> materialsByType =
                getRecentlyAddedOrChangedMaterials(sessionToken);
        for (Entry<String, List<Material>> entry : materialsByType.entrySet())
        {
            String materialTypeCode = entry.getKey();
            final List<Material> materials = entry.getValue();
            MappingInfo mappingInfo = mapping.get(materialTypeCode);
            if (mappingInfo != null)
            {
                String insertStatement = mappingInfo.createInsertStatement();
                jdbcTemplate.batchUpdate(insertStatement, mappingInfo.createSetter(materials));
            }
        }
        operationLog.info(materialsByType.size() + " materials reported.");
    }

    private Map<String, List<Material>> getRecentlyAddedOrChangedMaterials(String sessionToken)
    {
        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        DetailedSearchCriterion criterion =
                new DetailedSearchCriterion(
                        DetailedSearchField
                                .createAttributeField(MaterialAttributeSearchFieldKind.MODIFICATION_DATE_UNTIL),
                        CompareType.MORE_THAN_OR_EQUAL, readTimestamp(), "0");
        criteria.setCriteria(Arrays.asList(criterion));
        criteria.setConnection(SearchCriteriaConnection.MATCH_ALL);
        List<Material> materials = server.searchForMaterials(sessionToken, criteria);
        Map<String, List<Material>> result = new TreeMap<String, List<Material>>();
        for (Material material : materials)
        {
            String typeCode = material.getMaterialType().getCode();
            List<Material> list = result.get(typeCode);
            if (list == null)
            {
                list = new ArrayList<Material>();
                result.put(typeCode, list);
            }
            list.add(material);
        }
        return result;
    }

    private String readTimestamp()
    {
        return "2012-02-22";
    }

    @Private
    static Map<String, MappingInfo> readMappingFile(String mappingFileName)
    {
        Map<String, MaterialReportingTask.MappingInfo> map =
                new HashMap<String, MaterialReportingTask.MappingInfo>();
        List<String> lines = FileUtilities.loadToStringList(new File(mappingFileName));
        MappingInfo currentMappingInfo = null;
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i).trim();
            ExecptionFactory factory = new ExecptionFactory(mappingFileName, line, i);
            if (line.startsWith("#") || line.trim().length() == 0)
            {
                continue;
            }
            if (line.startsWith("["))
            {
                if (line.endsWith("]") == false)
                {
                    throw factory.exception("Missing ']'");
                }
                String[] splittedLine =
                        splitAndCheck(line.substring(0, line.length() - 1).substring(1), ":", 2,
                                factory);
                String materialTypeCode = check(splittedLine[0], factory, "material type code");
                splittedLine = splitAndCheck(splittedLine[1], ",", 2, factory);
                String tableName = check(splittedLine[0].trim(), factory, "table name");
                String codeColumnName = check(splittedLine[1].trim(), factory, "code column name");
                currentMappingInfo = new MappingInfo(materialTypeCode, tableName, codeColumnName);
                map.put(materialTypeCode, currentMappingInfo);
            } else if (currentMappingInfo != null)
            {
                String[] splittedLine = splitAndCheck(line, ":", 2, factory);
                String propertyTypeCode = check(splittedLine[0], factory, "property type code");
                currentMappingInfo.addPropertyMapping(propertyTypeCode, splittedLine[1].trim());
            } else
            {
                throw factory.exception("Missing first material type table definition of form "
                        + "'[<material type tode>: <table name>, <code column name>]'");
            }
        }
        return map;
    }

    private static String[] splitAndCheck(String string, String delimiter, int numberOfItems,
            ExecptionFactory factory)
    {
        String[] splittedString = string.split(delimiter);
        if (splittedString.length < numberOfItems)
        {
            throw factory.exception(numberOfItems + " items separated by '" + delimiter
                    + "' expected.");
        }
        return splittedString;
    }

    private static String check(String string, ExecptionFactory factory, String name)
    {
        if (string.length() == 0)
        {
            throw factory.exception("Missing " + name);
        }
        return string;
    }

    private static final class ExecptionFactory
    {
        private final String mappingFileName;

        private final String line;

        private final int lineIndex;

        ExecptionFactory(String mappingFileName, String line, int lineIndex)
        {
            this.mappingFileName = mappingFileName;
            this.line = line;
            this.lineIndex = lineIndex;
        }

        ConfigurationFailureException exception(String message)
        {
            return new ConfigurationFailureException("Error in mapping file '" + mappingFileName
                    + "' at line " + (lineIndex + 1) + " '" + line + "']: " + message);
        }
    }

}
