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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.TabFileLoader;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * Default implementation which assumes that the information can be extracted from the file name.
 * Following information can be extracted:
 * <ul>
 * <li>Sample code
 * <li>Parent data set code
 * <li>Data producer code
 * <li>Data production date
 * <li>Data set properties file name
 * </ul>
 * The name is split into entities separated by the property {@link #ENTITY_SEPARATOR_PROPERTY_NAME}
 * . It is assumed that each of the above-mentioned pieces of information is one of these entities.
 * The extractor can be configured by the following optional properties:
 * <table border="1" * cellspacing="0" cellpadding="5">
 * <tr>
 * <th>Property</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><code>entity-separator</code></td>
 * <td><code>.</code></td>
 * <td>Character which separates entities in the file name. Whitespace characters are not allowed.</td>
 * </tr>
 * <tr>
 * <td><code>index-of-sample-code</code></td>
 * <td><code>-1</code></td>
 * <td>Index of the entity which is interpreted as the sample code.</td>
 * </tr>
 * <tr>
 * <td><code>index-of-parent-data-set-code</code></td>
 * <td>&nbsp;</td>
 * <td>Index of the entity which is interpreted as the parent data set code. If not specified no
 * parent data set code will be extracted.</td>
 * </tr>
 * <tr>
 * <td><code>index-of-data-producer-code</code></td>
 * <td>&nbsp;</td>
 * <td>Index of the entity which is interpreted as the data producer code. If not specified no data
 * producer code will be extracted.</td>
 * </tr>
 * <tr>
 * <td><code>index-of-data-production-date</code></td>
 * <td>&nbsp;</td>
 * <td>Index of the entity which is interpreted as the data production date. If not specified no
 * data production date will be extracted.</td>
 * </tr>
 * <tr>
 * <td><code>data-production-date-format</code></td>
 * <td><code>yyyyMMddHHmmss</code></td>
 * <td>Format of the data production date. For the correct syntax see <a
 * href="http://java.sun.com/j2se/1.5.0/docs/api/java/text/SimpleDateFormat.html"
 * >SimpleDateFormat</a>.</td>
 * </tr>
 * <tr>
 * <td><code>data-set-properties-file-name</code></td>
 * <td><code>&nbsp;</code></td>
 * <td>Path to a file inside a data set directory which contains data set properties.</td>
 * </tr>
 * </table>
 * The first entity has index 0, the second 1, etc. Using negative numbers one can specify entities
 * from the end. Thus, -1 means the last entity, -2 the second last entity, etc.
 * 
 * @author Franz-Josef Elmer
 */
public class DefaultDataSetInfoExtractor extends AbstractDataSetInfoExtractor
{

    /**
     * Name of the property specifying the index of the entity which should be interpreted as the
     * sample code.
     * <p>
     * Use a negative number to count from the end, e.g. <code>-1</code> to use the last entity as
     * the sample code.
     * </p>
     */
    @Private
    static final String INDEX_OF_SAMPLE_CODE = "index-of-sample-code";

    /**
     * Name of the property specifying the index of the entity which should be interpreted as the
     * parent data set code.
     * <p>
     * Use a negative number to count from the end, e.g. <code>-1</code> to use the last entity as
     * the sample code.
     * </p>
     */
    @Private
    static final String INDEX_OF_PARENT_DATA_SET_CODE = "index-of-parent-data-set-code";

    /** Default index of sample code. */
    private static final int DEFAULT_INDEX_OF_SAMPLE_CODE = -1;

    /**
     * Name of the property specifying the index of the entity which should be interpreted as the
     * data producer code.
     * <p>
     * Use a negative number to count from the end, e.g. <code>-1</code> to use the last entity as
     * the data producer code.
     * </p>
     */
    @Private
    static final String INDEX_OF_DATA_PRODUCER_CODE = "index-of-data-producer-code";

    /**
     * Name of the property specifying the index of the entity which should be interpreted as the
     * data production date.
     * <p>
     * Use a negative number to count from the end, e.g. <code>-1</code> to use the last entity as
     * the data production date.
     * </p>
     */
    @Private
    static final String INDEX_OF_DATA_PRODUCTION_DATE = "index-of-data-production-date";

    /**
     * Name of the property specifying the format of the data production date.
     */
    @Private
    static final String DATA_PRODUCTION_DATE_FORMAT = "data-production-date-format";

    /** Default data production date format. */
    private static final String DEFAULT_DATA_PRODUCTION_DATE_FORMAT = "yyyyMMddHHmmss";

    @Private static final String DATA_SET_PROPERTIES_FILE_NAME_KEY = "data-set-properties-file-name";

    private final int indexOfSampleCode;

    private final boolean noParentDataSetCode;

    private final int indexOfParentDataSetCode;

    private final boolean noDataProducerCode;

    private final int indexOfDataProducerCode;

    private final boolean noDataProductionDate;

    private final int indexOfDataProductionDate;

    private final SimpleDateFormat dateFormat;

    private final String dataSetPropertiesFileName;

    /**
     * The <var>properties</var> are not used by this constructor but present to fulfill the
     * contract.
     */
    public DefaultDataSetInfoExtractor(final Properties globalProperties)
    {
        super(globalProperties);
        indexOfSampleCode =
                PropertyUtils
                        .getInt(properties, INDEX_OF_SAMPLE_CODE, DEFAULT_INDEX_OF_SAMPLE_CODE);
        String indexAsString = properties.getProperty(INDEX_OF_PARENT_DATA_SET_CODE);
        noParentDataSetCode = indexAsString == null;
        indexOfParentDataSetCode =
                PropertyUtils.getInt(properties, INDEX_OF_PARENT_DATA_SET_CODE, 0);
        indexAsString = properties.getProperty(INDEX_OF_DATA_PRODUCER_CODE);
        noDataProducerCode = indexAsString == null;
        indexOfDataProducerCode = PropertyUtils.getInt(properties, INDEX_OF_DATA_PRODUCER_CODE, 0);
        indexAsString = properties.getProperty(INDEX_OF_DATA_PRODUCTION_DATE);
        noDataProductionDate = indexAsString == null;
        indexOfDataProductionDate =
                PropertyUtils.getInt(properties, INDEX_OF_DATA_PRODUCTION_DATE, 0);
        dateFormat =
                new SimpleDateFormat(properties.getProperty(DATA_PRODUCTION_DATE_FORMAT,
                        DEFAULT_DATA_PRODUCTION_DATE_FORMAT));
        dataSetPropertiesFileName = properties.getProperty(DATA_SET_PROPERTIES_FILE_NAME_KEY);
    }

    //
    // ICodeExtractor
    //

    public DataSetInformation getDataSetInformation(final File incomingDataSetPath)
            throws EnvironmentFailureException, UserFailureException
    {
        assert incomingDataSetPath != null : "Incoming data set path can not be null.";
        final DataSetNameEntitiesProvider entitiesProvider =
                new DataSetNameEntitiesProvider(incomingDataSetPath, entitySeparator,
                        stripExtension);
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setSampleCode(entitiesProvider.getEntity(indexOfSampleCode));
        dataSetInformation.setParentDataSetCode(tryGetParentDataSetCode(entitiesProvider));
        dataSetInformation.setProducerCode(tryGetDataProducerCode(entitiesProvider));
        dataSetInformation.setProductionDate(tryGetDataProductionDate(entitiesProvider));
        dataSetInformation.setDataSetProperties(extractDataSetProperties(incomingDataSetPath,
                dataSetPropertiesFileName));
        return dataSetInformation;
    }

    private String tryGetParentDataSetCode(
            final DataSetNameEntitiesProvider dataSetNameEntitiesProvider)
    {
        if (noParentDataSetCode)
        {
            return null;
        }
        return dataSetNameEntitiesProvider.getEntity(indexOfParentDataSetCode);
    }

    private String tryGetDataProducerCode(
            final DataSetNameEntitiesProvider dataSetNameEntitiesProvider)
    {
        if (noDataProducerCode)
        {
            return null;
        }
        return dataSetNameEntitiesProvider.getEntity(indexOfDataProducerCode);
    }

    private Date tryGetDataProductionDate(
            final DataSetNameEntitiesProvider dataSetNameEntitiesProvider)
    {
        if (noDataProductionDate)
        {
            return null;
        }
        final String dateString = dataSetNameEntitiesProvider.getEntity(indexOfDataProductionDate);
        try
        {
            return dateFormat.parse(dateString);
        } catch (final ParseException e)
        {
            throw new UserFailureException("Could not parse data production date '" + dateString
                    + "' because it violates the following format: " + dateFormat.toPattern());
        }
    }

    private List<NewProperty> extractDataSetProperties(File incomingDataSetPath, String fileName)
    {
        List<NewProperty> result = new ArrayList<NewProperty>();
        if (fileName != null && incomingDataSetPath.isDirectory())
        {
            File propertiesFile = new File(incomingDataSetPath, fileName);
            if (propertiesFile.isFile())
            {
                TabFileLoader<NewProperty> tabFileLoader =
                        new TabFileLoader<NewProperty>(
                                new IParserObjectFactoryFactory<NewProperty>()
                                    {

                                        public IParserObjectFactory<NewProperty> createFactory(
                                                IPropertyMapper propertyMapper)
                                                throws ParserException
                                        {
                                            return new AbstractParserObjectFactory<NewProperty>(
                                                    NewProperty.class, propertyMapper)
                                                {
                                                };
                                        }
                                    });
                result.addAll(tabFileLoader.load(propertiesFile));

            } else
            {
                throw new UserFailureException("Data set properties file '" + propertiesFile
                        + "' does not exist or is not a 'normal' file.");
            }
        }
        return result;

    }

}