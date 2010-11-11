/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.TypeMapper;
import net.lemnik.eodsql.Update;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IImagingTransformerDAO extends TransactionQuery
{
    @Update(sql = "update experiments set image_transformer_factory = ?{2} where perm_id = ?{1}", parameterBindings =
        { TransformerFactoryMapper.class, TypeMapper.class/* default */  })
    public void saveTransformerFactoryForExperiment(String experimentPermID,
            IImageTransformerFactory factory);
    
    @Update(sql = "update channels set image_transformer_factory = ?{3} "
            + "where code = ?{2} and exp_id in (select id from experiments where perm_id = ?{1})", parameterBindings =
        { TransformerFactoryMapper.class, TypeMapper.class/* default */, TypeMapper.class /* default */ })
    public void saveTransformerFactoryForChannel(String experimentPermID, String channel,
            IImageTransformerFactory factory);
}
