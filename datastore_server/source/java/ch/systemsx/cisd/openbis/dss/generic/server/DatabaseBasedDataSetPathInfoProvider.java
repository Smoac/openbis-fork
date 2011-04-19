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
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DatabaseBasedDataSetPathInfoProvider implements IDataSetPathInfoProvider
{
    @Private public static final class DataSetFileRecord
    {
        // Attribute names as defined in database schema
        public long id;
        
        public Long parent_id;
        
        public String relative_path;
        
        public String file_name;
        
        public long size_in_bytes;
        
        public boolean is_directory;
    }
    
    @Private static interface IPathInfoDAO extends BaseQuery
    {
        @Select("select id from data_sets where code = ?{1}")
        public Long tryToGetDataSetId(String dataSetCode);
        
        @Select("select id, parent_id, relative_path, size_in_bytes, is_directory from data_set_files where dase_id = ?{1}")
        public List<DataSetFileRecord> listDataSetFiles(long dataSetId);
        
        @Select("select id, parent_id, relative_path, size_in_bytes, is_directory from data_set_files where dase_id = ?{1} and relative_path ~ ?{2}")
        public List<DataSetFileRecord> listDataSetFilesByRegularExpression(long dataSetId, String regex);
    }
    
    private static interface ILoader
    {
        List<DataSetFileRecord> listDataSetFiles(long dataSetId);
    }
    
    private IPathInfoDAO dao;
    
    public DatabaseBasedDataSetPathInfoProvider()
    {
    }
    
    @Private DatabaseBasedDataSetPathInfoProvider(IPathInfoDAO dao)
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
                    return getDao().listDataSetFilesByRegularExpression(dataSetId,
                            "^" + regularExpression + "$");
                }
            }).getInfos();
    }

    public DataSetPathInfo tryGetDataSetRootPathInfo(String dataSetCode)
    {
        return new Loader(dataSetCode, new ILoader()
            {
                public List<DataSetFileRecord> listDataSetFiles(long dataSetId)
                {
                    return getDao().listDataSetFiles(dataSetId);
                }
            }).getRoot();
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
            dao = QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathInfoDAO.class);
        }
        return dao;
    }

}
