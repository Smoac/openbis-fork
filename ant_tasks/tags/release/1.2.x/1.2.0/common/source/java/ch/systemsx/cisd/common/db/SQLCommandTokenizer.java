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

/**
 * A class keeping the state when normalizing a SQL script.
 * 
 * @author Bernd Rinn
 */
class SQLCommandTokenizer
{

    private static enum Mode
    {
        SCANNING, CONSTANT, WHITESPACE
    }

    private final String sqlScript;

    private int index = 0;
    
    private Mode mode = Mode.SCANNING;

    private boolean newLine = true;
    
    private boolean newWord = false;
    
    private Character previous = '\0';
    
    private Character current;

    SQLCommandTokenizer(String sqlScript)
    {
        this.sqlScript = sqlScript;
    }

    String getNextCommand()
    {
        final StringBuilder builder = new StringBuilder();
        mode = Mode.SCANNING;
        newWord = false;
        do
        {
            switch (mode)
            {
                case SCANNING:
                    scan(builder);
                    break;
                case CONSTANT:
                    scanConstant(builder);
                    break;
                case WHITESPACE:
                    scanWhitespaces();
                    break;
            }
        } while (current != '\0');
        if (index >= sqlScript.length() && builder.length() == 0)
        {
            return null;
        } else
        {
            return builder.toString();
        }
    }

    private Character next()
    {
        previous = current;
        current = peek();
        if (current != '\0')
        {
            ++index;
        }
        return current;
    }

    private Character peek()
    {
        if (index < sqlScript.length())
        {
            return sqlScript.charAt(index);
        } else
        {
            return '\0';
        }
    }

    private void scan(StringBuilder builder)
    {
        next();
        if (current == '\0')
        {
            return;
        }
        if (newLine && current == '-' && peek() == '-')
        {
            skipLine();
            newWord = false;
            mode = Mode.SCANNING;
            return;
        }
        if (Character.isWhitespace(current))
        {
            if (current == '\n')
            {
                newLine = true;
                newWord = false;
            } else if (newLine == false)
            {
                newWord = true;
            }
            mode = Mode.WHITESPACE;
            return;
        }
        newLine = false;
        if (current == ';')
        {
            newLine = true;
            if (builder.length() > 0)
            {
                current = '\0';
            }
            return;
        }
        if (newWord)
        {
            builder.append(' ');
            newWord = false;
        }
        builder.append(Character.toLowerCase(current));
        if (current == ',' && peek() == ' ')
        {
            mode = Mode.WHITESPACE;
            return;
        }
        if (current == '\'')
        {
            mode = Mode.CONSTANT;
            return;
        }
    }
    
    private void scanConstant(StringBuilder builder)
    {
        do
        {
            next();
            if (current == '\0')
            {
                break;
            }
            builder.append(current);
        } while (current != '\'' || previous == '\\');
        mode = Mode.SCANNING;
    }
    
    private void scanWhitespaces()
    {
        while (Character.isWhitespace(next())) 
        {
            if (current == '\n')
            {
                newLine = true;
            }
        }
        if (current != '\0')
        {
            --index;
        }
        mode = Mode.SCANNING;
    }
    
    private void skipLine()
    {
        do
        {
            next();
        } while (current != '\n' && current != '\0');
        if (current != '\0')
        {
            --index;
        }
    }
    
}
