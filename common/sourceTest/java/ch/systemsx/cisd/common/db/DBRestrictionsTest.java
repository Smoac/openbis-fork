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

package ch.systemsx.cisd.common.db;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for the {@link DBRestrictions}.
 * 
 * @author Bernd Rinn
 */
public class DBRestrictionsTest
{

    private String sqlScript;
    
    @ BeforeClass
    public void setup()
    {
        LogInitializer.init();
        sqlScript = FileUtilities.loadToString(
                        new File("sourceTest/java/ch/systemsx/cisd/common/db/DBRestrictionsTest.sql"));
        assert sqlScript != null;
    }

    @Test
    public void testNormalize()
    {
        final List<String> normalizedList = DBRestrictions.normalize(" a  1 ;; B\t2;\n\nC; \n--D\n E ");
        assertEquals(Arrays.asList("a 1", "b 2", "c", "e"), normalizedList);
    }

    @Test
    public void testGetDomains()
    {
        final List<String> domainScript =
                Arrays.asList("create table sometable", "create domain user_id as varchar(15)",
                        "create domain bla for varchar(0)", "create domain code as varchar(8)",
                        "create domain description_80 as varchar(81)");
        final Map<String, Integer> domains = DBRestrictions.parseDomains(domainScript);
        final Map<String, Integer> expectedDomains = new HashMap<String, Integer>();
        expectedDomains.put("user_id", 15);
        expectedDomains.put("code", 8);
        expectedDomains.put("description_80", 81);
        assertEquals(expectedDomains, domains);
    }

    @Test
    public void testColumnLengths()
    {
        final DBRestrictions parser = new DBRestrictions(sqlScript);

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
        final DBRestrictions parser = new DBRestrictions("");
        assertEquals(Integer.MAX_VALUE, parser.getTableRestrictions("doesnotexit").getLength("doesnotexist"));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testInvalidColumn()
    {
        final DBRestrictions parser = new DBRestrictions("create table tab (a integer, b varchar(1))");
        assertEquals(Integer.MAX_VALUE, parser.getTableRestrictions("tab").getLength("doesnotexist"));
    }

    @Test
    public void testCheckedConstraints()
    {
        final DBRestrictions parser = new DBRestrictions(sqlScript);

        assertEquals(new HashSet<String>(Arrays.asList("PERS", "ORGA")), parser.getTableRestrictions("contacts")
                .getCheckedConstaint("cnta_type"));
        assertEquals(new HashSet<String>(Arrays.asList("STOB", "MATE")), parser.getTableRestrictions("materials")
                .getCheckedConstaint("mate_sub_type"));
    }

    @Test
    public void testCheckOK()
    {
        final DBRestrictions parser = new DBRestrictions(sqlScript);

        parser.check("contacts", "cnta_type", "ORGA");
        parser.check("material_types", "code", "somecode");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCheckViolateLength()
    {
        final DBRestrictions parser = new DBRestrictions(sqlScript);

        try
        {
            parser.check("material_types", "code", "somecode1");
        } catch (UserFailureException ex)
        {
            System.err.println(ex.getMessage());
            throw ex;
        }
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCheckAlternatives()
    {
        final DBRestrictions parser = new DBRestrictions(sqlScript);

        try
        {
            parser.check("materials", "mate_sub_type", "stob");
        } catch (UserFailureException ex)
        {
            System.err.println(ex.getMessage());
            throw ex;
        }
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCheckNotNullConstraint()
    {
        final DBRestrictions parser = new DBRestrictions(sqlScript);

        try
        {
            parser.check("material_types", "description", null);
        } catch (UserFailureException ex)
        {
            System.err.println(ex.getMessage());
            throw ex;
        }
    }
}
