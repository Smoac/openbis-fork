package ch.systemsx.cisd.common.parser;

/**
 * A small object that represents a line in a <code>File</code> context.
 * 
 * @author Christian Ribeaud
 */
public final class Line
{
    private final String text;

    private final int number;

    Line(final int number, final String text)
    {
        assert text != null : "Unspecified text.";
        this.number = number;
        this.text = text;
    }

    public final String getText()
    {
        return text;
    }

    public final int getNumber()
    {
        return number;
    }
    
    
}