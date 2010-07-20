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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.etlserver.FileTypeExtractor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;

/**
 * Tests of {@link TypeExtractorYeastX}
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = TypeExtractorYeastX.class)
public class TypeExtractorYeastXTest extends AssertJUnit
{
    @Test
    public void testHappyCase()
    {
        ITypeExtractor extractor = createExtractor("MATLAB: zzz, XML: xxx");
        assertTypes(extractor, new File("file.xxx"), "XML", "XXX");
        assertTypes(extractor, new File("file.zzz"), "MATLAB", "ZZZ");
    }

    @Test
    public void testUnrecognizedExtension()
    {
        ITypeExtractor extractor = createExtractor("");
        assertTypes(extractor, new File("file.txt"), "UNKNOWN", "UNKNOWN");
    }

    private void assertTypes(ITypeExtractor extractor, File xxx, String expectedFileType,
            String expectedDatasetType)
    {
        FileFormatType fileFormatType = extractor.getFileFormatType(xxx);
        assertEquals(expectedFileType, fileFormatType.getCode());
        DataSetType dataSetType = extractor.getDataSetType(xxx);
        assertEquals(expectedDatasetType, dataSetType.getCode());
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testIncorrectSyntaxNoCommaFails()
    {
        createExtractor("MATLAB: zzz XML: xxx");
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testIncorrectSyntaxNoColonFails()
    {
        createExtractor("MATLAB zzz, XML xxx");
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testIncorrectSyntaxOneTokenFails()
    {
        createExtractor("zzz");
    }

    @Test
    public void testEmptyValueCorrect()
    {
        createExtractor("");
    }

    @Test
    public void testOneMappingCorrect()
    {
        createExtractor("MATLAB: mat");
    }

    private static ITypeExtractor createExtractor(String fileTypesMappings)
    {
        Properties properties = new Properties();
        properties.put(FileTypeExtractor.FILE_TYPES_NAME, fileTypesMappings);
        return new TypeExtractorYeastX(properties);
    }
}
