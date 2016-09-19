/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigReader
{

    private static final String DATA_SOURCE_URL_PROPERTY_NAME = "resource-list-url";

    private static final String DATA_SOURCE_OPENBIS_URL_PROPERTY_NAME = "data-source-openbis-url";

    private static final String DATA_SOURCE_DSS_URL_PROPERTY_NAME = "data-source-dss-url";

    private static final String DATA_SOURCE_SPACES_PROPERTY_NAME = "data-source-spaces";

    private static final String DATA_SOURCE_PREFIX_PROPERTY_NAME = "data-souce-prefix";

    private static final String DATA_SOURCE_AUTH_REALM_PROPERTY_NAME = "data-source-auth-realm";

    private static final String DATA_SOURCE_AUTH_USER_PROPERTY_NAME = "data-source-auth-user";

    private static final String DATA_SOURCE_AUTH_PASS_PROPERTY_NAME = "data-source-auth-pass";

    private static final String HARVESTER_SPACES_PROPERTY_NAME = "harvester-spaces";

    private static final String HARVESTER_TEMP_DIR_PROPERTY_NAME = "harvester-tmp-dir";

    private static final String DEFAULT_LAST_SYNC_TIMESTAMP_FILE = "last-sync-timestamp-file.txt";

    private static final String HARVESTER_LAST_SYNC_TIMESTAMP_FILE = "last-sync-timestamp-file";



    private Pattern sectionRegex = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");

    private Pattern keyValueRegex = Pattern.compile("\\s*([^=]*)=(.*)");

    private Map<String, Map<String, String>> entries = new LinkedHashMap<>();

    public ConfigReader(String path) throws IOException
    {
        load(path);
    }

    public ConfigReader(File file) throws IOException
    {
        loadFile(file);
    }

    public static void main(String[] args)
    {
        ConfigReader reader;
        try
        {
            reader = new ConfigReader("/Users/gakin/Documents/workspace_openbis_trunk/datastore_server/harvester.ini");
            for (int i = 0; i < reader.getSectionCount(); i++)
            {
                System.out.println(reader.getSection(i));
            }
            
            SyncConfig config = new SyncConfig();
            String section1 = reader.getSection(0);
            
            config.setDataSourceURI(reader.getString(section1, DATA_SOURCE_URL_PROPERTY_NAME, null));
            config.setDataSourceOpenbisURL(reader.getString(section1, DATA_SOURCE_OPENBIS_URL_PROPERTY_NAME, null));
            config.setDataSourceDSSURL(reader.getString(section1, DATA_SOURCE_DSS_URL_PROPERTY_NAME, null));
            config.setRealm(reader.getString(section1, DATA_SOURCE_AUTH_REALM_PROPERTY_NAME, null));
            config.setUser(reader.getString(section1,DATA_SOURCE_AUTH_USER_PROPERTY_NAME, null));
            config.setPass(reader.getString(section1, DATA_SOURCE_AUTH_PASS_PROPERTY_NAME, null));
            config.setDataSourceSpaces(reader.getString(section1, DATA_SOURCE_SPACES_PROPERTY_NAME, null));
            config.setHarvesterSpaces(reader.getString(section1, DATA_SOURCE_PREFIX_PROPERTY_NAME, null));
            config.setHarvesterTempDir(reader.getString(section1, HARVESTER_TEMP_DIR_PROPERTY_NAME, "targets/store"));
            config.setLastSyncTimestampFileName(reader.getString(section1, HARVESTER_LAST_SYNC_TIMESTAMP_FILE, DEFAULT_LAST_SYNC_TIMESTAMP_FILE));

            config.printConfig();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int getSectionCount()
    {
        return entries.keySet().size();
    }

    public String getSection(int index)
    {
        if (index > getSectionCount())
        {
            throw new RuntimeException("Section with index " + index + " does not exist.");
        }
        return entries.keySet().toArray(new String[entries.keySet().size()])[index];
    }

    public boolean sectionExists(String name)
    {
        Map<String, String> kvMap = entries.get(name);
        if (kvMap == null)
        {
            return false;
        }
        return true;
    }

    public void loadFile(File file) throws IOException
    {
        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            String section = null;
            while ((line = br.readLine()) != null)
            {
                Matcher m = sectionRegex.matcher(line);
                if (m.matches())
                {
                    section = m.group(1).trim();
                }
                else if (section != null)
                {
                    m = keyValueRegex.matcher(line);
                    if (m.matches())
                    {
                        String key = m.group(1).trim();
                        String value = m.group(2).trim();
                        Map<String, String> map = entries.get(section);
                        if (map == null)
                        {
                            entries.put(section, map = new HashMap<>());
                        }
                        map.put(key, value);
                    }
                }
            }
        }
    }

    public void load(String path) throws IOException
    {
        loadFile(new File(path));
    }

    public String getString(String section, String key, String defaultvalue)
    {
        Map<String, String> map = entries.get(section);
        if (map == null)
        {
            return defaultvalue;
        }
        return map.get(key);
    }

    public int getInt(String section, String key, int defaultvalue)
    {
        Map<String, String> map = entries.get(section);
        if (map == null)
        {
            return defaultvalue;
        }
        return Integer.parseInt(map.get(key));
    }

    public double getDouble(String section, String key, double defaultvalue)
    {
        Map<String, String> map = entries.get(section);
        if (map == null)
        {
            return defaultvalue;
        }
        return Double.parseDouble(map.get(key));
    }
}