package ch.systemsx.cisd.common.utilities;

/**
 * Controls <code>Collection</code> string representation for {@link CollectionUtils}.
 * 
 * @author Christian Ribeaud
 */
public enum CollectionStyle
{
    /** Default <code>CollectionStyle</code>. */
    DEFAULT_COLLECTION_STYLE("[", "]", ", "), NO_BOUNDARY_COLLECTION_STYLE("", "", ", ");

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