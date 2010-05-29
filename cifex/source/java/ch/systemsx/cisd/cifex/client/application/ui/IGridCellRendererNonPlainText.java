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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * Interface for a renderer that does not return plain text but HTML formatted text. Such a renderer
 * is not suitable for being used in filters, thus it needs to provide a plain text renderer which
 * is then used when filtering.
 * 
 * @author Bernd Rinn
 */
public interface IGridCellRendererNonPlainText<M extends ModelData> extends GridCellRenderer<M>
{
    /**
     * Returns the renderer for plain text, or <code>null</code>, if {@link Object#toString()}
     * already is the rendered plain text.
     */
    public GridCellRenderer<M> getPlainTextRenderer();
}
