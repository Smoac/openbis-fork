package ch.systemsx.cisd.common.parser;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

/**
 * A default <code>ILineTokenizer</code> implementation that parses a line into an array of <code>String</code>
 * objects. This implementation uses {@link StrTokenizer} as internal worker.
 * <p>
 * The default separator chars used here are <code>\t</code>. If you want to change that, use
 * {@link #setProperty(PropertyKey, String)} with corresponding property key defined here.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class DefaultLineTokenizer implements ILineTokenizer
{
    
    /** Allowed <code>Properties</code> keys. */
    public static enum PropertyKey {
        /** The property key for setting some delimiter characters. */
        SEPARATOR_CHARS,
        /** The property key for setting some quote characters. */
        QUOTE_CHARS,
        /** The property key for setting some ignored characters. */
        IGNORED_CHARS,
        /** The property key for setting some trimmer characters. */
        TRIMMER_CHARS,
    }
    
    /** Some properties for this tab parser. */
    private final Map<PropertyKey, String> properties;
    
    private StrTokenizer tokenizer;
    
    public DefaultLineTokenizer()
    {
        this.properties = new EnumMap<PropertyKey, String>(PropertyKey.class);
    }

    /**
     * Sets a property for this <code>TabReaderParser</code>.
     * 
     * @throws IllegalArgumentException if given <code>key</code> could not found in {@link PropertyKey}.
     */
    public final void setProperty(PropertyKey key, String value)
    {
        properties.put(key, value);
    }
    
    /** Converts a defined <code>PropertyKey</code> into <code>StrMatcher</code>. */
    private final StrMatcher getStrMatcher(PropertyKey key, StrMatcher defaultMatcher) {
        StrMatcher strMatcher = defaultMatcher;
        String value = properties.get(key);
        if (value != null)
        {
            strMatcher = StrMatcher.charSetMatcher(value);
        }
        return strMatcher;
    }
    
    ///////////////////////////////////////////////////////
    // ILineTokenizer
    ///////////////////////////////////////////////////////
    
    public final void init()
    {   
        StrTokenizer strTokenizer = new StrTokenizer();
        strTokenizer.setDelimiterMatcher(getStrMatcher(PropertyKey.SEPARATOR_CHARS, StrMatcher.tabMatcher()));
        strTokenizer.setQuoteMatcher(getStrMatcher(PropertyKey.QUOTE_CHARS, StrMatcher.noneMatcher()));
        strTokenizer.setTrimmerMatcher(getStrMatcher(PropertyKey.TRIMMER_CHARS, StrMatcher.trimMatcher()));
        strTokenizer.setIgnoredMatcher(getStrMatcher(PropertyKey.IGNORED_CHARS, StrMatcher.noneMatcher()));
        strTokenizer.setEmptyTokenAsNull(false);
        strTokenizer.setIgnoreEmptyTokens(false);
        this.tokenizer = strTokenizer;
    }
    
    public final String[] tokenize(int lineNumber, String line)
    {
        return tokenizer.reset(line).getTokenArray();
    }
    
    public final void destroy()
    {
        tokenizer = null;
    }
}