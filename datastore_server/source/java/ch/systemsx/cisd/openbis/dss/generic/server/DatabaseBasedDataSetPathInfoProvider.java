/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.Select;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;

/**
 * @author Franz-Josef Elmer
 */
public class DatabaseBasedDataSetPathInfoProvider implements IDataSetPathInfoProvider
{
    @Private
    public static final class DataSetFileRecord
    {
        // Attribute names as defined in database schema
        public long id;

        public Long parent_id;

        public String relative_path;

        public String file_name;

        public long size_in_bytes;

        public boolean is_directory;
    }

    @Private
    static interface IPathInfoDAO extends BaseQuery
    {
        static String SELECT_DATA_SET_FILES =
                "SELECT id, parent_id, relative_path, file_name, size_in_bytes, is_directory FROM data_set_files ";

        @Select("SELECT id FROM data_sets WHERE code = ?{1}")
        public Long tryToGetDataSetId(String dataSetCode);

        @Select(SELECT_DATA_SET_FILES + "WHERE dase_id = ?{1}")
        public List<DataSetFileRecord> listDataSetFiles(long dataSetId);

        @Select(SELECT_DATA_SET_FILES + "WHERE dase_id = ?{1} AND parent_id is null")
        public DataSetFileRecord getDataSetRootFile(long dataSetId);

        @Select(SELECT_DATA_SET_FILES + "WHERE dase_id = ?{1} AND relative_path = ?{2}")
        public DataSetFileRecord tryToGetRelativeDataSetFile(long dataSetId, String relativePath);

        @Select(SELECT_DATA_SET_FILES + "WHERE dase_id = ?{1} AND parent_id = ?{2}")
        public List<DataSetFileRecord> listChildrenByParentId(long dataSetId, long parentId);

        @Select(SELECT_DATA_SET_FILES + "WHERE dase_id = ?{1} AND relative_path ~ ?{2}")
        public List<DataSetFileRecord> listDataSetFilesByRelativePathRegex(long dataSetId,
                String relativePathRegex);

        @Select(SELECT_DATA_SET_FILES + "WHERE dase_id = ?{1} AND relative_path LIKE ?{2}")
        public List<DataSetFileRecord> listDataSetFilesByRelativePathLikeExpression(long dataSetId,
                String relativePathLikeExpression);

        @Select(SELECT_DATA_SET_FILES
                + "WHERE dase_id = ?{1} AND relative_path like '?{2}' AND file_name ~ ?{3}")
        public List<DataSetFileRecord> listDataSetFilesByFilenameRegex(long dataSetId,
                String startingPath, String filenameRegex);
    }

    private static interface ILoader
    {
        List<DataSetFileRecord> listDataSetFiles(long dataSetId);
    }

    private IPathInfoDAO dao;

    public DatabaseBasedDataSetPathInfoProvider()
    {
    }

    @Private
    DatabaseBasedDataSetPathInfoProvider(IPathInfoDAO dao)
    {
        this.dao = dao;
    }

    public List<DataSetPathInfo> listPathInfosByRegularExpression(String dataSetCode,
            final String regularExpression)
    {
        return new Loader(dataSetCode, new ILoader()
            {
                public List<DataSetFileRecord> listDataSetFiles(long dataSetId)
                {
                    String likeExpression = translateToLikeForm("^" + regularExpression + "$");

                    if (likeExpression == null)
                    {
                        return getDao().listDataSetFilesByRelativePathRegex(dataSetId,
                                "^" + regularExpression + "$");
                    } else
                    {
                        return getDao().listDataSetFilesByRelativePathLikeExpression(dataSetId,
                                likeExpression);
                    }
                }
            }).getInfos();
    }

    public DataSetPathInfo tryGetFullDataSetRootPathInfo(String dataSetCode)
    {
        return new Loader(dataSetCode, new ILoader()
            {
                public List<DataSetFileRecord> listDataSetFiles(long dataSetId)
                {
                    return getDao().listDataSetFiles(dataSetId);
                }
            }).getRoot();
    }

    public ISingleDataSetPathInfoProvider tryGetSingleDataSetPathInfoProvider(String dataSetCode)
    {
        final Long dataSetId = getDao().tryToGetDataSetId(dataSetCode);
        if (dataSetId != null)
        {
            return new SingleDataSetPathInfoProvider(dataSetId, getDao());
        }
        return null;
    }

    static class SingleDataSetPathInfoProvider implements ISingleDataSetPathInfoProvider
    {
        private final Long dataSetId;

        private final IPathInfoDAO dao;

        public SingleDataSetPathInfoProvider(Long dataSetId, IPathInfoDAO dao)
        {
            this.dataSetId = dataSetId;
            this.dao = dao;
        }

        public DataSetPathInfo getRootPathInfo()
        {
            DataSetFileRecord record = dao.getDataSetRootFile(dataSetId);
            if (record != null)
            {
                return asPathInfo(record);
            } else
            {
                throw new IllegalStateException("root path wasn't found");
            }
        }

        public DataSetPathInfo tryGetPathInfoByRelativePath(String relativePath)
        {
            DataSetFileRecord record = dao.tryToGetRelativeDataSetFile(dataSetId, relativePath);
            if (record != null)
            {
                return asPathInfo(record);
            } else
            {
                return null;
            }
        }

        public List<DataSetPathInfo> listChildrenPathInfos(DataSetPathInfo parent)
        {
            List<DataSetFileRecord> records = dao.listChildrenByParentId(dataSetId, parent.getId());
            return asPathInfos(records);
        }

        public List<DataSetPathInfo> listMatchingPathInfos(String relativePathPattern)
        {
            String likeExpression = translateToLikeForm(prepareDBStyleRegex(relativePathPattern));
            List<DataSetFileRecord> records;
            if (likeExpression == null)
            {
                records =
                        dao.listDataSetFilesByRelativePathRegex(dataSetId,
                                prepareDBStyleRegex(relativePathPattern));
            } else
            {
                records =
                        dao.listDataSetFilesByRelativePathLikeExpression(dataSetId, likeExpression);
            }
            return asPathInfos(records);
        }

        public List<DataSetPathInfo> listMatchingPathInfos(String startingPath,
                String fileNamePattern)
        {
            List<DataSetFileRecord> records =
                    dao.listDataSetFilesByFilenameRegex(dataSetId, startingPath,
                            prepareDBStyleRegex(fileNamePattern));
            return asPathInfos(records);
        }

        private DataSetPathInfo asPathInfo(DataSetFileRecord record)
        {
            DataSetPathInfo result = new DataSetPathInfo();
            result.setId(record.id);
            result.setFileName(record.file_name);
            result.setRelativePath(record.relative_path);
            result.setDirectory(record.is_directory);
            result.setSizeInBytes(record.size_in_bytes);
            return result;
        }

        private List<DataSetPathInfo> asPathInfos(List<DataSetFileRecord> records)
        {
            List<DataSetPathInfo> results = new ArrayList<DataSetPathInfo>();
            for (DataSetFileRecord record : records)
            {
                results.add(asPathInfo(record));
            }
            return results;
        }

    }

    private final class Loader
    {
        private Map<Long, DataSetPathInfo> idToInfoMap = new HashMap<Long, DataSetPathInfo>();

        private DataSetPathInfo root;

        Loader(String dataSetCode, ILoader loader)
        {
            Long dataSetId = getDao().tryToGetDataSetId(dataSetCode);
            if (dataSetId != null)
            {
                List<DataSetFileRecord> dataSetFileRecords = loader.listDataSetFiles(dataSetId);
                Map<Long, List<DataSetPathInfo>> parentChildrenMap =
                        new HashMap<Long, List<DataSetPathInfo>>();
                for (DataSetFileRecord dataSetFileRecord : dataSetFileRecords)
                {
                    DataSetPathInfo dataSetPathInfo = new DataSetPathInfo();
                    dataSetPathInfo.setFileName(dataSetFileRecord.file_name);
                    dataSetPathInfo.setRelativePath(dataSetFileRecord.relative_path);
                    dataSetPathInfo.setDirectory(dataSetFileRecord.is_directory);
                    dataSetPathInfo.setSizeInBytes(dataSetFileRecord.size_in_bytes);
                    idToInfoMap.put(dataSetFileRecord.id, dataSetPathInfo);
                    Long parentId = dataSetFileRecord.parent_id;
                    if (parentId == null)
                    {
                        root = dataSetPathInfo;
                    } else
                    {
                        List<DataSetPathInfo> children = parentChildrenMap.get(parentId);
                        if (children == null)
                        {
                            children = new ArrayList<DataSetPathInfo>();
                            parentChildrenMap.put(parentId, children);
                        }
                        children.add(dataSetPathInfo);
                    }
                }
                linkParentsWithChildren(parentChildrenMap);
            }
        }

        @SuppressWarnings("deprecation")
        private void linkParentsWithChildren(Map<Long, List<DataSetPathInfo>> parentChildrenMap)
        {
            for (Entry<Long, List<DataSetPathInfo>> entry : parentChildrenMap.entrySet())
            {
                Long parentId = entry.getKey();
                DataSetPathInfo parent = idToInfoMap.get(parentId);
                if (parent != null)
                {
                    List<DataSetPathInfo> children = entry.getValue();
                    for (DataSetPathInfo child : children)
                    {
                        parent.addChild(child);
                        child.setParent(parent);
                    }
                }
            }
        }

        DataSetPathInfo getRoot()
        {
            return root;
        }

        List<DataSetPathInfo> getInfos()
        {
            return new ArrayList<DataSetPathInfo>(idToInfoMap.values());
        }
    }

    private IPathInfoDAO getDao()
    {
        if (dao == null)
        {
            dao =
                    QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(),
                            IPathInfoDAO.class);
        }
        return dao;
    }

    // java style patterns match the whole text, db style patterns match any fragment
    private static String prepareDBStyleRegex(String pattern)
    {
        return "^" + pattern + "$";
    }

    private static String translateToLikeForm(String pattern)
    {
        StringBuilder result = new StringBuilder();

        int startPosition = 0;
        if (pattern.startsWith("^"))
        {
            startPosition++;
        } else
        {
            result.append('%');
        }

        while (startPosition < pattern.length())
        {
            char ch = pattern.charAt(startPosition);
            if (Character.isLetter(ch) || Character.isDigit(ch) || Character.isWhitespace(ch))
            {
                result.append(ch);
                startPosition++;
            } else
            {
                switch (ch)
                {
                    case '/':
                    case ',':
                    case '-':
                    case '#':
                    case '@':
                    case '&':
                    case '\'':
                    case '"':
                    case ':':
                    case ';':
                    case '`':
                    case '~':
                    case '=':
                        result.append(ch);
                        startPosition++;
                        break;
                    case '%':
                    case '_':
                        result.append('\\').append(ch);
                        startPosition++;
                        break;
                    case '.':
                        startPosition++;
                        if (startPosition < pattern.length()
                                && pattern.charAt(startPosition) == '*')
                        {
                            result.append('%');
                            startPosition++;
                        } else if (startPosition < pattern.length()
                                && pattern.charAt(startPosition) == '+')
                        {
                            result.append('_').append('%');
                            startPosition++;
                        } else
                        {
                            result.append('_');
                        }
                        break;
                    case '$':
                        startPosition++;
                        if (startPosition < pattern.length())
                        {
                            result.append(ch);
                        }
                        break;
                    case '\\':
                        startPosition++;
                        if (startPosition < pattern.length())
                        {
                            char escaped = pattern.charAt(startPosition);
                            switch (escaped)
                            {
                                case '\\':
                                    startPosition++;
                                    result.append('\\').append('\\');
                                    break;
                                case '.':
                                case '$':
                                case '^':
                                case '(':
                                case ')':
                                case '[':
                                case ']':
                                case '?':
                                case '*':
                                case '{':
                                case '}':
                                case '|':
                                case '+':
                                case '-':
                                case '#':
                                case '@':
                                case '&':
                                case '\'':
                                case '"':
                                case ':':
                                case ';':
                                case '`':
                                case '~':
                                case '=':
                                    startPosition++;
                                    result.append(escaped);
                                    break;
                                case '%':
                                case '_':
                                    startPosition++;
                                    result.append('\\').append(escaped);
                                    break;
                                default:
                                    return null;
                            }
                        } else
                        {
                            return null;
                        }
                        break;
                    default:
                        return null;
                }
            }
        }

        if (false == pattern.endsWith("$"))
        {
            result.append('%');
        }
        return result.toString();
    }
}
