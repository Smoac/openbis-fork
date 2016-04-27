/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.net.uniprot;

import static ch.systemsx.cisd.common.net.uniprot.UniprotColumn.ID;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.TabFileLoader;

/**
 * A class for querying the Uniprot database.
 * 
 * @author Bernd Rinn
 */
public final class UniprotQuery
{
    private static final int RETRY_COUNT = 3;

    private static final String BASE_URL = "http://www.uniprot.org/uniprot/";

    private static final String QUERY_INIT_STR = "query=";

    private static final String COLUMN_INIT_STR = "columns=";

    private static final String FORMAT_STR = "format=tab";

    private static final String OFFSET_STR = "offset=";

    private static final String LIMIT_STR = "limit=";

    /**
     * Create a set of Uniprot columns.
     */
    public static Set<UniprotColumn> columns(UniprotColumn... columns)
    {
        final Set<UniprotColumn> set = EnumSet.noneOf(UniprotColumn.class);
        for (UniprotColumn col : columns)
        {
            set.add(col);
        }
        return set;
    }

    private final String columnsSpecification;

    /**
     * Construct a Uniprot query, adding the given database <var>columns</var>. Note that
     * {@link UniprotColumn#ID} will always be added to the set of columns.
     */
    public UniprotQuery(UniprotColumn... columns)
    {
        this(columns(columns));
    }

    /**
     * Construct a Uniprot query, adding the given database <var>columns</var>. Note that
     * {@link UniprotColumn#ID} will always be added to the set of columns.
     */
    public UniprotQuery(Set<UniprotColumn> columns)
    {
        columns.add(ID);
        columnsSpecification = createColumnSpecification(columns);
    }

    private String createColumnSpecification(Set<UniprotColumn> columns)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(COLUMN_INIT_STR);
        for (UniprotColumn col : columns)
        {
            builder.append(col.getFieldName());
            builder.append(',');
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    private String buildQueryURLForKeys(List<String> keys)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(BASE_URL);
        builder.append('?');
        builder.append(QUERY_INIT_STR);
        for (String key : keys)
        {
            builder.append("accession:");
            builder.append(key);
            builder.append("+or+");
        }
        builder.setLength(builder.length() - "+or+".length());
        builder.append('&');
        builder.append(FORMAT_STR);
        builder.append('&');
        builder.append(columnsSpecification);
        return builder.toString();
    }

    private String buildQueryURLForQueryExpression(String queryExpression, int limit, int offset)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(BASE_URL);
        builder.append('?');
        builder.append(QUERY_INIT_STR);
        builder.append(queryExpression.replaceAll(" ", "%20"));
        builder.append('&');
        builder.append(FORMAT_STR);
        builder.append('&');
        builder.append(columnsSpecification);
        if (limit > 0)
        {
            builder.append('&');
            builder.append(LIMIT_STR);
            builder.append(Integer.toString(limit));
        }
        if (offset > 0)
        {
            builder.append('&');
            builder.append(OFFSET_STR);
            builder.append(Integer.toString(offset));
        }
        return builder.toString();
    }

    private Iterable<UniprotEntry> runQuery(final String queryURL) throws IOExceptionUnchecked
    {
        final HttpClient client = new HttpClient();
        final GetMethod method = new GetMethod(queryURL);
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(RETRY_COUNT, false));
        try
        {
            final int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK)
            {
                throw new IOExceptionUnchecked(new IOException("GET failed: "
                        + method.getStatusLine()));
            }

            final TabFileLoader<UniprotEntry> parser =
                    new TabFileLoader<UniprotEntry>(new IParserObjectFactoryFactory<UniprotEntry>()
                        {
                            public IParserObjectFactory<UniprotEntry> createFactory(
                                    IPropertyMapper propertyMapper) throws ParserException
                            {
                                return new UniprotEntryParserFactory(propertyMapper);
                            }
                        });
            return new Iterable<UniprotEntry>()
                {
                    boolean hasIterated = false;

                    public Iterator<UniprotEntry> iterator()
                    {
                        try
                        {
                            if (hasIterated)
                            {
                                throw new IllegalStateException();
                            }
                            hasIterated = true;
                            return new Iterator<UniprotEntry>()
                                {
                                    final Iterator<UniprotEntry> delegate =
                                            parser.iterate(method.getResponseBodyAsStream());

                                    public boolean hasNext()
                                    {
                                        final boolean hasNext = delegate.hasNext();
                                        if (hasNext == false)
                                        {
                                            method.releaseConnection();
                                        }
                                        return hasNext;
                                    }

                                    public UniprotEntry next()
                                    {
                                        try
                                        {
                                            return delegate.next();
                                        } catch (RuntimeException ex)
                                        {
                                            method.releaseConnection();
                                            throw ex;
                                        }
                                    }

                                    public void remove()
                                    {
                                        method.releaseConnection();
                                        throw new UnsupportedOperationException();
                                    }
                                };
                        } catch (IOException ex)
                        {
                            method.releaseConnection();
                            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                        }
                    }
                };
        } catch (IOException ex)
        {
            method.releaseConnection();
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public Iterable<UniprotEntry> queryForIds(String... keys) throws IOExceptionUnchecked
    {
        return queryForIds(Arrays.asList(keys));
    }

    public Iterable<UniprotEntry> queryForIds(List<String> keys) throws IOExceptionUnchecked
    {
        final String queryURL = buildQueryURLForKeys(keys);
        return runQuery(queryURL);
    }

    /**
     * Runs a query against Uniprot with the given <var>queryExpression</var>.
     * 
     * @param queryExpression The query expression to use for the query. See <a
     *            href="http://www.uniprot.org/help/text-search">Uniprot Online Help</a> for details
     *            on the query language.
     */
    public Iterable<UniprotEntry> query(String queryExpression) throws IOExceptionUnchecked
    {
        final String queryURL = buildQueryURLForQueryExpression(queryExpression, 0, 0);
        return runQuery(queryURL);
    }

    /**
     * Runs a query against Uniprot with the given <var>queryExpression</var>.
     * 
     * @param queryExpression The query expression to use for the query. See <a
     *            href="http://www.uniprot.org/help/text-search">Uniprot Online Help</a> for details
     *            on the query language.
     * @param limit The maximum number of result entries to return.
     * @param offset The offset, that is the first result entry to return when counting starts with
     *            0.
     */
    public Iterable<UniprotEntry> query(String queryExpression, int limit, int offset)
            throws IOExceptionUnchecked
    {
        final String queryURL = buildQueryURLForQueryExpression(queryExpression, limit, offset);
        return runQuery(queryURL);
    }

}
