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

package ch.systemsx.cisd.common.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.logging.LogMonitoringAppender;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for the
 * 
 * @{link DBREstrictionParser}.
 * @author Bernd Rinn
 */
public class DBRestrictionParserTest
{

    private String sqlScript;

    @BeforeClass
    public void setup()
    {
        LogInitializer.init();
        sqlScript =
                FileUtilities
                        .loadToString(new File(
                                "../common/sourceTest/java/ch/systemsx/cisd/common/db/DBRestrictionsTest.sql"));
        assert sqlScript != null;
    }

    @Test
    public void testNormalize()
    {
        final List<String> normalizedList =
                DBRestrictionParser.normalize(" a  1 ;; B\t2;\n\nC; \n--D\n E ");
        assertEquals(Arrays.asList("a 1", "b 2", "c", "e"), normalizedList);
    }

    @Test
    public void testGetDomains()
    {
        String invalidDomainStatement = "create domain bla for varchar(0)";
        final List<String> domainScript =
                Arrays.asList("create table sometable", "create domain user_id as varchar(15)",
                        invalidDomainStatement, "create domain code as varchar(8)",
                        "create domain description_80 as varchar(81)");
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, "line \""
                        + invalidDomainStatement
                        + "\" starts like a domain definition, but key word 'AS' is missing.");
        try
        {
            final Map<String, Integer> domains = DBRestrictionParser.parseDomains(domainScript);
            appender.verifyLogHasHappened();
            final Map<String, Integer> expectedDomains = new HashMap<String, Integer>();
            expectedDomains.put("user_id", 15);
            expectedDomains.put("code", 8);
            expectedDomains.put("description_80", 81);
            assertEquals(expectedDomains, domains);
        } finally
        {
            LogMonitoringAppender.removeAppender(appender);
        }
    }

    @Test
    public void testDefaultKeywordInDomain()
    {
        final List<String> domainScript =
                Arrays.asList("create domain vc22 as varchar(22) default 'nothing special'");

        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, "ill-formed");
        try
        {
            final Map<String, Integer> domains = DBRestrictionParser.parseDomains(domainScript);
            appender.verifyLogHasNotHappened();
            assertNotNull(domains.get("vc22"));
            assertEquals(22, domains.get("vc22").intValue());
        } finally
        {
            LogMonitoringAppender.removeAppender(appender);
        }
    }

    @Test
    public void testDoublePrecisionInDomain()
    {
        final List<String> domainScript = Arrays.asList("create domain dp as double precision");

        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, "ill-formed");
        try
        {
            final Map<String, Integer> domains = DBRestrictionParser.parseDomains(domainScript);
            appender.verifyLogHasNotHappened();
            assertTrue(domains.isEmpty());
        } finally
        {
            LogMonitoringAppender.removeAppender(appender);
        }
    }

    @Test
    public void testDoublePrecisionAndDefaultInDomain()
    {
        final List<String> domainScript =
                Arrays.asList("create domain dp as double precision default 3.14159");

        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, "ill-formed");
        try
        {
            final Map<String, Integer> domains = DBRestrictionParser.parseDomains(domainScript);
            appender.verifyLogHasNotHappened();
            assertTrue(domains.isEmpty());
        } finally
        {
            LogMonitoringAppender.removeAppender(appender);
        }
    }

    @Test
    public void testColumnLengths()
    {
        final DBRestrictionParser parser = new DBRestrictionParser(sqlScript);

        assertEquals(10, parser.getTableRestrictions("contacts").getLength("cnta_type"));
        assertEquals(30, parser.getTableRestrictions("contacts").getLength("firstname"));
        assertEquals(1, parser.getTableRestrictions("contacts").getLength("midinitial"));
        assertEquals(30, parser.getTableRestrictions("contacts").getLength("lastname"));
        assertEquals(50, parser.getTableRestrictions("contacts").getLength("email"));
        assertEquals(15, parser.getTableRestrictions("contacts").getLength("user_id"));

        assertEquals(8, parser.getTableRestrictions("material_types").getLength("code"));
        assertEquals(80, parser.getTableRestrictions("material_types").getLength("description"));

        assertEquals(50, parser.getTableRestrictions("materials").getLength("name"));
        assertEquals(4, parser.getTableRestrictions("materials").getLength("mate_sub_type"));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testInvalidTable()
    {
        final DBRestrictionParser parser = new DBRestrictionParser("");
        assertEquals(Integer.MAX_VALUE, parser.getTableRestrictions("doesnotexit").getLength(
                "doesnotexist"));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testInvalidColumn()
    {
        final DBRestrictionParser parser =
                new DBRestrictionParser("create table tab (a integer, b varchar(1))");
        assertEquals(Integer.MAX_VALUE, parser.getTableRestrictions("tab")
                .getLength("doesnotexist"));
    }

    @Test
    public void testCheckedConstraints()
    {
        final DBRestrictionParser parser = new DBRestrictionParser(sqlScript);

        assertEquals(new HashSet<String>(Arrays.asList("PERS", "ORGA")), parser
                .getTableRestrictions("contacts").tryGetCheckedConstaint("cnta_type"));
        assertEquals(new HashSet<String>(Arrays.asList("STOB", "MATE")), parser
                .getTableRestrictions("materials").tryGetCheckedConstaint("mate_sub_type"));
    }

}
