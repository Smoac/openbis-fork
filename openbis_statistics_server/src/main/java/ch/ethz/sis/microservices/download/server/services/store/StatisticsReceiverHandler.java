/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.microservices.download.server.services.store;

import ch.ethz.sis.microservices.download.server.json.jackson.JacksonObjectMapper;
import ch.ethz.sis.microservices.download.server.services.Service;
import ch.ethz.sis.microservices.download.server.startup.StatisticsMain;
import com.fasterxml.jackson.core.type.TypeReference;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsReceiverHandler extends Service
{

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(StatisticsMain.class);

    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private static final String STATISTICS_FILE_SUFFIX = "-statistics.csv";

    private static final String FLAGGED_STATISTICS_FILE_SUFFIX = "-statistics-flagged.csv";

    private static final String STATISTICS_FOLDER_PATH_PARAM = "statisticsFolderPath";

    private static final Map<String, Instant> INSTANT_BY_SERVER_ID = new ConcurrentHashMap<>();

    private static final Map<String, Instant> INSTANT_BY_SERVER_IP = new ConcurrentHashMap<>();

    /** Allowed time difference (in seconds) for requests not to be flagged. */
    private static final long ALLOWED_SEND_RECEIVE_TIME_DIFFERENCE = 10;

    /** Allowed time difference (in seconds) for repeated requests. */
    private static final long ALLOWED_REPETITION_TIME_DIFFERENCE = 300;

    private static final String UNKNOWN_COUNTRY_CODE = "?";

    private static final String FORWARDED_HEADER = "X-FORWARDED-FOR";

    static
    {
        final long cleanupPeriod = ALLOWED_REPETITION_TIME_DIFFERENCE / 2;
        new Timer(HistoryMapsCleaner.class.getSimpleName(), true).scheduleAtFixedRate(new HistoryMapsCleaner(),
                cleanupPeriod, cleanupPeriod);
    }

    private final DatabaseReader reader;

    public StatisticsReceiverHandler() throws IOException
    {
        final String dbPath = "/GeoLite2-Country.mmdb";
        final InputStream ipDbInputStream = StatisticsReceiverHandler.class.getResourceAsStream(dbPath);

        if (ipDbInputStream == null)
        {
            throw new IOException(String.format("Resource '%s' not found.", dbPath));
        }

        reader = new DatabaseReader.Builder(ipDbInputStream).withCache(new CHMCache()).build();
    }

    protected void doAction(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        final Instant receivedInstant = Instant.now();
        final String todaysDate = DATE_FORMAT.format(Date.from(receivedInstant));
        final String statisticsFolderParam = this.getServiceConfig().getParameters().get(STATISTICS_FOLDER_PATH_PARAM);
        final String statisticsFilePath = statisticsFolderParam + FILE_SEPARATOR + todaysDate + STATISTICS_FILE_SUFFIX;
        final String flaggedStatisticsFilePath = statisticsFolderParam + FILE_SEPARATOR + todaysDate
                + FLAGGED_STATISTICS_FILE_SUFFIX;

        try
        {
            final Map<StatisticsKeys, String> statisticsMap = JacksonObjectMapper.getInstance().readValue(
                    request.getInputStream(), new TypeReference<HashMap<StatisticsKeys, String>>() {});

            final String serverId = statisticsMap.get(StatisticsKeys.SERVER_ID);
            final String submissionTimestampString = statisticsMap.get(StatisticsKeys.SUBMISSION_TIMESTAMP);
            final Instant submissionInstant = Instant.parse(submissionTimestampString);
            final Integer usersCount = Integer.valueOf(statisticsMap.get(StatisticsKeys.USERS_COUNT));
            final String openbisVersion = statisticsMap.get(StatisticsKeys.OPENBIS_VERSION);

            final Enumeration<String> headers = request.getHeaders(FORWARDED_HEADER);

            // Getting the last FORWARDED_HEADER. The last one will be truly from the reverse proxy on the server.
            String xForwardedHeader = null;
            while (headers.hasMoreElements())
            {
                xForwardedHeader = headers.nextElement();
            }

            final String serverIp;
            if (xForwardedHeader == null)
            {
                serverIp = request.getRemoteAddr();
                LOGGER.trace("Getting server IP from the request directly.");
            } else
            {
                serverIp = xForwardedHeader.split(",", 2)[0].trim();
                LOGGER.trace(String.format("Getting server IP from %s header.", FORWARDED_HEADER));
            }
            LOGGER.trace(String.format("Server IP: %s", serverIp));

            final String serverCountryCode = findCountryCodeByIp(serverIp);
            LOGGER.trace(String.format("Country code: %s", serverCountryCode));

            LOGGER.trace(String.format("Request headers: %s", getHeadersFromRequest(request)));
            LOGGER.trace(String.format("Request data: %s", statisticsMap.toString()));

            final String csvLine = convertToCSV(serverId, receivedInstant.toString(),
                    String.valueOf(usersCount), serverCountryCode, openbisVersion);

            LOGGER.trace(String.format("Writing to CSV: '%s'", csvLine));

            final boolean callSuspicious = isCallSuspicious(serverId, serverIp, submissionInstant, receivedInstant);
            writeLineToFile(callSuspicious ? flaggedStatisticsFilePath : statisticsFilePath, csvLine);

            LOGGER.trace(String.format("callSuspicious=%b", callSuspicious));

            INSTANT_BY_SERVER_ID.put(serverId, receivedInstant);
            if (serverIp != null && !serverIp.isBlank())
            {
                INSTANT_BY_SERVER_IP.put(serverIp, receivedInstant);
            }

            success(response);
        } catch (final Exception ex)
        {
            LOGGER.catching(ex);
            failure(response);
        }
    }

    private String findCountryCodeByIp(final String ip) throws IOException
    {
        try
        {
            return reader.country(InetAddress.getByName(ip)).getCountry().getIsoCode();
        } catch (final GeoIp2Exception e)
        {
            return UNKNOWN_COUNTRY_CODE;
        } catch (final UnknownHostException e)
        {
            LOGGER.catching(e);
            return UNKNOWN_COUNTRY_CODE;
        }
    }

    private static String getHeadersFromRequest(final HttpServletRequest request)
    {
        final StringBuilder builder = new StringBuilder();
        final Enumeration<String> headerNames = request.getHeaderNames();
        headerNames.asIterator().forEachRemaining(headerName ->
                request.getHeaders(headerName).asIterator().forEachRemaining(headerValue ->
                        builder.append(headerName).append(": ").append(headerValue).append(System.lineSeparator())));
        return builder.toString();
    }

    private boolean isCallSuspicious(final String serverId, final String serverIp,
            final Instant submissionInstant, final Instant receivedInstant)
    {
        return INSTANT_BY_SERVER_ID.containsKey(serverId) || INSTANT_BY_SERVER_IP.containsKey(serverIp) ||
                Math.abs(receivedInstant.getEpochSecond() - submissionInstant.getEpochSecond()) >
                        ALLOWED_SEND_RECEIVE_TIME_DIFFERENCE;
    }

    private static void writeLineToFile(final String filePath, final String line) throws IOException
    {
        final File statisticsFile = new File(filePath);
        statisticsFile.getParentFile().mkdirs();
        statisticsFile.createNewFile();
        try (final PrintWriter pw = new PrintWriter(new FileOutputStream(statisticsFile, true)))
        {
            pw.println(line);
        }
    }

    private static String convertToCSV(String... data) {
        return Stream.of(data)
                .map(StatisticsReceiverHandler::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
            IOException
    {
        doAction(request, response);
    }

    protected void success(final HttpServletResponse response) throws ServletException, IOException
    {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void failure(final HttpServletResponse response) throws ServletException, IOException
    {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Cleans {@link #INSTANT_BY_SERVER_ID} and {@link #INSTANT_BY_SERVER_IP} maps of old values.
     */
    private static class HistoryMapsCleaner extends TimerTask
    {

        @Override
        public void run()
        {
            final Instant presentInstant = Instant.now();
            final Set<String> serverIdsToRemove = new HashSet<>();
            final Set<String> serverIpsToRemove = new HashSet<>();

            INSTANT_BY_SERVER_ID.forEach((id, instant) ->
            {
                if (presentInstant.getEpochSecond() - instant.getEpochSecond() >= ALLOWED_REPETITION_TIME_DIFFERENCE)
                {
                    serverIdsToRemove.add(id);
                }
            });

            INSTANT_BY_SERVER_IP.forEach((ip, instant) ->
            {
                if (presentInstant.getEpochSecond() - instant.getEpochSecond() >= ALLOWED_REPETITION_TIME_DIFFERENCE)
                {
                    serverIpsToRemove.add(ip);
                }
            });

            INSTANT_BY_SERVER_ID.keySet().removeAll(serverIdsToRemove);
            INSTANT_BY_SERVER_IP.keySet().removeAll(serverIpsToRemove);
        }

    }

}
