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

package ch.systemsx.cisd.common.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Bean with build and environment information.
 * <p>
 * Does <em>not</em> depend on any library jar files.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public final class BuildAndEnvironmentInfo
{
    private static final String UNKNOWN = "UNKNOWN";

    /**
     * The one-and-only instance.
     */
    public static final BuildAndEnvironmentInfo INSTANCE = new BuildAndEnvironmentInfo();

    private final String version;

    private final String revision;

    private final boolean cleanSources;

    private BuildAndEnvironmentInfo()
    {
        String extractedVersion = UNKNOWN;
        String extractedRevision = UNKNOWN;
        boolean extractedCleanFlag = false;
        InputStream stream = BuildAndEnvironmentInfo.class.getResourceAsStream("/BUILD.INFO");
        if (stream != null)
        {
            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringTokenizer tokenizer = new StringTokenizer(reader.readLine(), ":");
                extractedVersion = tokenizer.nextToken();
                extractedRevision = tokenizer.nextToken();
                extractedCleanFlag = "clean".equals(tokenizer.nextToken());
            } catch (IOException ex)
            {
                // ignored
            } finally
            {
                try
                {
                    stream.close();
                } catch (IOException ex)
                {
                    // ignored
                }
            }
        }
        this.version = extractedVersion;
        this.revision = extractedRevision;
        this.cleanSources = extractedCleanFlag;
    }

    private final static String getProperty(final String property)
    {
        return System.getProperty(property, UNKNOWN);
    }

    private final static boolean isUnknown(final String osName)
    {
        return osName.equals(UNKNOWN);
    }

    /**
     * @return Name of the CPU architecture.
     */
    public final String getCPUArchitecture()
    {
        return getProperty("os.arch");
    }

    /**
     * @return Name and version of the operating system.
     */
    public final String getOS()
    {
        final String osName = getProperty("os.name");
        final String osVersion = getProperty("os.version");
        if (isUnknown(osName) || isUnknown(osVersion))
        {
            return osName;
        }
        return osName + " (v" + osVersion + ")";
    }

    /**
     * @return Name and version of the Java Virtual Machine.
     */
    public final String getJavaVM()
    {
        final String vmName = getProperty("java.vm.name");
        final String vmVersion = getProperty("java.vm.version");
        if (isUnknown(vmName) || isUnknown(vmVersion))
        {
            return vmName;
        }
        return vmName + " (v" + vmVersion + ")";
    }

    /**
     * @return The version of the software.
     */
    public final String getVersion()
    {
        return version;
    }

    /**
     * @return <code>true</code> if the versioned entities of the working copy have been clean when this build has
     *         been made, in other words, whether the revision given by {@link #getRevision()} does really identify the
     *         source that is build has been produced from.
     */
    public final boolean isCleanSources()
    {
        return cleanSources;
    }

    /**
     * @return The revision number.
     */
    public final String getRevision()
    {
        return revision;
    }

    /**
     * Returns the build number of the software,
     */
    public final String getBuildNumber()
    {
        final StringBuilder builder = new StringBuilder();
        final String rev = getRevision();
        final boolean isDirty = isCleanSources() == false;
        builder.append(getVersion());
        if (isUnknown(rev) == false)
        {
            builder.append(" (r").append(rev);
            if (isDirty)
            {
                builder.append("*");
            }
            builder.append(")");
        } else
        {
            if (isDirty)
            {
                builder.append("*");
            }
        }
        return builder.toString();
    }

    /**
     * Returns version, build number, Java VM, and OS as a {@link List} with four entries.
     */
    public List<String> getEnvironmentInfo()
    {
        final List<String> environmentInfo = new ArrayList<String>();
        environmentInfo.add("Version: " + getVersion());
        environmentInfo.add("Build Number: " + getBuildNumber());
        environmentInfo.add("Java VM: " + getJavaVM());
        environmentInfo.add("CPU Architecture: " + getCPUArchitecture());
        environmentInfo.add("OS: " + getOS());
        return environmentInfo;
    }

    /**
     * Returns version, build number, Java VM, and OS in a four-liner as one {@link String}.
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        List<String> environmentInfo = getEnvironmentInfo();
        for (int i = 0, n = environmentInfo.size(); i < n; i++)
        {
            builder.append(environmentInfo.get(i));
            if (i < n - 1)
            {
                builder.append(OSUtilities.LINE_SEPARATOR);
            }
        }
        return builder.toString();
    }

    /**
     * Shows build and environment information on the console.
     */
    public static void main(String[] args)
    {
        System.out.println(BuildAndEnvironmentInfo.INSTANCE);
    }

}
