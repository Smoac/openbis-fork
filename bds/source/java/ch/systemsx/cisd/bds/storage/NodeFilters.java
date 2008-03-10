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

package ch.systemsx.cisd.bds.storage;

/**
 * Some useful {@link INodeFilter} implementations.
 * 
 * @author Christian Ribeaud
 */
public final class NodeFilters
{
    private NodeFilters()
    {
    }

    /**
     * A <code>INodeFilter</code> implementation which always returns <code>true</code>.
     */
    public final static INodeFilter TRUE_NODE_FILTER = new INodeFilter()
        {

            //
            // INodeFilter
            //

            public final boolean accept(final INode node)
            {
                return true;
            }
        };

    /**
     * Creates filter nodes based on the extension (what the filename ends with).
     */
    public final static INodeFilter createExtensionNodeFilter(final boolean ignoreCase, final String... extensions)
    {
        assert extensions != null : "Given extensions can not be null.";
        return new SuffixNodeFilter(ignoreCase, toSuffixes(extensions));
    }

    private final static String[] toSuffixes(final String... extensions)
    {
        final int length = extensions.length;
        final String[] suffixes = new String[length];
        for (int i = 0; i < length; i++)
        {
            suffixes[i] = "." + extensions[i];
        }
        return suffixes;
    }

    //
    // Helper classes
    //

    private final static class SuffixNodeFilter implements INodeFilter
    {
        private final String[] suffixes;

        private final boolean ignoreCase;

        SuffixNodeFilter(final boolean ignoreCase, final String... suffixes)
        {
            assert suffixes != null : "Suffixes can not be null.";
            this.suffixes = suffixes;
            this.ignoreCase = ignoreCase;
        }

        //
        // INodeFilter
        //

        public final boolean accept(final INode node)
        {
            assert node != null : "Given node can not be null.";
            final String nameWithRightCase = toRightCase(node.getName());
            for (final String suffix : suffixes)
            {
                final String suffixWithRightCase = toRightCase(suffix);
                if (nameWithRightCase.endsWith(suffixWithRightCase))
                {
                    return true;
                }
            }
            return false;
        }

        private final String toRightCase(String s)
        {
            if (ignoreCase)
            {
                return s.toLowerCase(); 
            } else
            {
                return s;
            }
        }
    }
    
}
