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

package ch.systemsx.cisd.common.collections;

/**
 * Controls <code>Collection</code> string representation for {@link CollectionUtils}.
 * 
 * @author Christian Ribeaud
 */
public enum CollectionStyle
{
    /** Default <code>CollectionStyle</code>. */
    DEFAULT_COLLECTION_STYLE("[", "]", ", "), NO_BOUNDARY_COLLECTION_STYLE("", "", ", "),
    NICE_COLLECTION_STYLE("'", "'", "', '");

    private final String collectionStart;

    private final String collectionEnd;

    private final String collectionSeparator;

    private CollectionStyle(String collectionStart, String collectionEnd, String collectionSeparator)
    {
        this.collectionStart = collectionStart;
        this.collectionEnd = collectionEnd;
        this.collectionSeparator = collectionSeparator;
    }

    /** Returns the token that terminates a collection string representation. */
    public final String getCollectionEnd()
    {
        return collectionEnd;
    }

    /** Returns the token that separates the different items of a given collection. */
    public final String getCollectionSeparator()
    {
        return collectionSeparator;
    }

    /** Returns the token that starts a collection string representation. */
    public final String getCollectionStart()
    {
        return collectionStart;
    }
}