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

package ch.systemsx.cisd.common.collection;

import org.apache.commons.lang.StringUtils;

/**
 * Some useful utlities methods for {@link String}s.
 * <p>
 * If you are tempted to add new functionality to this class, ensure that the new functionality does not yet exist in {@link StringUtils}, see <a
 * href= "http://jakarta.apache.org/commons/lang/api-release/org/apache/commons/lang/StringUtils.html" >javadoc</a>.
 * 
 * @author Bernd Rinn
 */
public final class StringUtilities
{

    private static final String[] STRINGS =
            new String[]
            { "phalanx", "nightmare", "concierge", "asbestos", "cody", "hermit", "nbc",
                    "couplet", "dice", "thumbnail", "finley", "figure", "exclamation",
                    "whoosh", "punish", "servitor", "portend", "boulevard", "bacterial",
                    "dilate", "emboss", "birmingham", "illustrate", "pomona", "truk",
                    "bursitis", "trustworthy", "harriman", "schenectady", "obligate",
                    "oceania", "knew", "quickstep", "woo", "strickland", "sadie", "malabar",
                    "posit", "breadfruit", "grandfather", "vishnu", "vacuous", "melpomene",
                    "assam", "blaine", "taskmaster", "polymeric", "hector",
                    "counterrevolution", "compassionate", "linkage", "distant", "vet", "shako",
                    "eagan", "neutronium", "stony", "lie", "hoydenish", "dial", "hecate",
                    "pinch", "olin", "piglet", "basswood", "yawn", "ouzo", "scrupulosity",
                    "bestiary", "subpoena", "nudge", "baton", "thing", "hallmark", "bossy",
                    "preferential", "bambi", "narwhal", "brighten", "omnipotent", "forsake",
                    "flapping", "orthodoxy", "upcome", "teaspoonful", "wabash", "lipid",
                    "enjoin", "shoshone", "wartime", "gatekeeper", "litigate", "siderite",
                    "sadden", "visage", "boogie", "scald", "equate", "tragic", "ordinary",
                    "wick", "gigawatt", "desultory", "bambi", "aureomycin", "car", "especial",
                    "rescue", "protector", "burnett", "constant", "heroes", "filmstrip",
                    "homeown", "verdant", "governor", "cornwall", "predisposition", "sedan",
                    "resemblant", "satellite", "committeemen", "given", "narragansett",
                    "switzer", "clockwatcher", "sweeten", "monologist", "execrate", "gila",
                    "lad", "mahayanist", "solicitation", "linemen", "reading", "hoard",
                    "phyla", "carcinoma", "glycol", "polymer", "hangmen", "dualism",
                    "betrayal", "corpsman", "stint", "hannah", "balsam", "granola",
                    "charitable", "osborn", "party", "laboratory", "norwich", "laxative",
                    "collude", "rockefeller", "crack", "lamarck", "purposeful", "neuroanotomy",
                    "araby", "crucible", "oratorical", "dramaturgy", "kitty", "pit", "ephesus",
                    "bum", "amuse", "clogging", "joker", "fobbing", "extent", "colossal",
                    "macromolecule", "choppy", "tennessee", "primrose", "glassine", "vampire",
                    "chap", "precursor", "incorrigible", "slither", "interrogate", "spectral",
                    "debut", "creche", "pyrolysis", "homicidal", "sonnet", "gin", "science",
                    "magma", "metaphor", "cobble", "dyer", "narrate", "goody", "optometric" };

    private StringUtilities()
    {
        // This class can not be instantiated.
    }

    /**
     * Returns an array of as many strings as you request.
     */
    public static final String[] getStrings(int numberOfStrings)
    {
        String[] result = new String[numberOfStrings];
        for (int i = 0; i < numberOfStrings; i++)
        {
            result[i] = getString();
        }
        return result;
    }

    /** Returns a random string. */
    public static final String getString()
    {
        return STRINGS[(int) (Math.random() * STRINGS.length - 1)];
    }

}
