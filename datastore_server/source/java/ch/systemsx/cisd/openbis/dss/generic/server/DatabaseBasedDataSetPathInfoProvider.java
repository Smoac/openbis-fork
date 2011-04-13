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
import java.util.Collections;
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
        
        public long size_in_bytes;
        
        public boolean is_directory;
    }
    
    @Private static interface IPathInfoDAO extends BaseQuery
    {
        @Select("select id from data_sets where code = ?{1}")
        public Long tryToGetDataSetId(String dataSetCode);
        
        @Select("select id, parent_id, relative_path, size_in_bytes, is_directory from data_set_files where dase_id = ?{1}")
        public List<DataSetFileRecord> listDataSetFiles(long dataSetId);
    }
    
    private IPathInfoDAO dao;
    
    public DatabaseBasedDataSetPathInfoProvider()
    {
    }
    
    @Private DatabaseBasedDataSetPathInfoProvider(IPathInfoDAO dao)
    {
        this.dao = dao;
    }

    public List<DataSetPathInfo> listDataSetRootPathInfos(String dataSetCode)
    {
        Long dataSetId = getDao().tryToGetDataSetId(dataSetCode);
        if (dataSetId == null)
        {
            return Collections.emptyList();
        }
        List<DataSetFileRecord> dataSetFileRecords = getDao().listDataSetFiles(dataSetId);
        Map<Long, DataSetPathInfo> idToInfoMap = new HashMap<Long, DataSetPathInfo>();
        Map<Long, List<DataSetPathInfo>> parentChildrenMap = new HashMap<Long, List<DataSetPathInfo>>();
        List<DataSetPathInfo> roots = new ArrayList<DataSetPathInfo>();
        for (DataSetFileRecord dataSetFileRecord : dataSetFileRecords)
        {
            DataSetPathInfo dataSetPathInfo = new DataSetPathInfo();
            dataSetPathInfo.setRelativePath(dataSetFileRecord.relative_path);
            dataSetPathInfo.setDirectory(dataSetFileRecord.is_directory);
            dataSetPathInfo.setSizeInBytes(dataSetFileRecord.size_in_bytes);
            idToInfoMap.put(dataSetFileRecord.id, dataSetPathInfo);
            Long parentId = dataSetFileRecord.parent_id;
            if (parentId == null)
            {
                roots.add(dataSetPathInfo);
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
        for (Entry<Long, List<DataSetPathInfo>> entry : parentChildrenMap.entrySet())
        {
            Long parentId = entry.getKey();
            DataSetPathInfo parent = idToInfoMap.get(parentId);
            List<DataSetPathInfo> children = entry.getValue();
            for (DataSetPathInfo child : children)
            {
                parent.addChild(child);
                child.setParent(parent);
            }
        }
        return roots;
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
