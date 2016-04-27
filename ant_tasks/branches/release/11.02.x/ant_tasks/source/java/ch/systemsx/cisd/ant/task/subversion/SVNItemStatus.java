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

package ch.systemsx.cisd.ant.task.subversion;

import java.util.HashMap;
import java.util.Map;

/**
 * The status information for one item in a subversion working copy (as returned by
 * {@link ISVNStatus}.
 * 
 * @author Bernd Rinn
 */
class SVNItemStatus
{

    /** The possible status an item can have. */
    public enum StatusFlag
    {
        UPTODATE(' '), ADDED('A'), DELETED('D'), MODIFIED('M'), REPLACED('R'), CONFLICTED('C'),
        EXTERNALSRELATED('X'), IGNORED('I'), UNVERSIONED('?'), MISSING('!'), TYPECHANGED('~');

        private final char statusCharacter;

        StatusFlag(char statusCharacter)
        {
            this.statusCharacter = statusCharacter;
        }

        char getStatusCharacter()
        {
            return statusCharacter;
        }

    }

    private static Map<Character, StatusFlag> characterToStatusMap;

    private static void ensureCharacterStatusMap()
    {
        if (null == characterToStatusMap)
        {
            characterToStatusMap = new HashMap<Character, StatusFlag>();
            for (StatusFlag flag : StatusFlag.values())
            {
                characterToStatusMap.put(flag.getStatusCharacter(), flag);
            }
        }
    }

    private static final Map<Character, StatusFlag> getCharacterToStatusMap()
    {
        ensureCharacterStatusMap();
        return characterToStatusMap;
    }

    private final StatusFlag flag;

    private final String path;

    SVNItemStatus(char statusCharacter, String path)
    {
        assert path != null;
        ensureCharacterStatusMap();

        this.flag = getCharacterToStatusMap().get(statusCharacter);
        this.path = path;

        assert flag != null;
    }

    /**
     * @return The status flag of the working copy item.
     */
    public StatusFlag getFlag()
    {
        return flag;
    }

    /**
     * @return The relative path of the working copy item.
     */
    public String getPath()
    {
        return path;
    }

}
