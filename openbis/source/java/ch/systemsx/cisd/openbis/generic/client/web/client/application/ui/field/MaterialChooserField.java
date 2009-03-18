/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.TextField;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.EntityChooserDialog.ChosenEntitySetter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * A field for selecting a material from a list or by specifying code and type.
 * 
 * @author Tomasz Pylak
 */
public final class MaterialChooserField extends TextField<String> implements
        ChosenEntitySetter<Material>
{
    public static Field<?> create(final String labelField, final boolean mandatory,
            final MaterialType materialTypeOrNull, String initialValueOrNull,
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final MaterialChooserField chosenMaterialField =
                new MaterialChooserField(mandatory, materialTypeOrNull, initialValueOrNull,
                        viewContext);

        Button chooseButton = new Button(viewContext.getMessage(Dict.BUTTON_BROWSE));
        chooseButton.addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    browseMaterials(viewContext, chosenMaterialField, materialTypeOrNull);
                }
            });
        return new MultiField<Field<?>>(labelField, chosenMaterialField, new AdapterField(
                chooseButton));
    }

    private static void browseMaterials(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ChosenEntitySetter<Material> chosenMaterialField, MaterialType materialTypeOrNull)
    {
        DisposableEntityChooser<Material> materialBrowser =
                MaterialBrowserGrid.create(viewContext, materialTypeOrNull);
        String title = viewContext.getMessage(Dict.TITLE_CHOOSE_MATERIAL);
        new EntityChooserDialog<Material>(materialBrowser, chosenMaterialField, title, viewContext)
                .show();
    }

    // ------------------

    // the pattern used to validate material identifier expression
    private final static String CODE_AND_TYPE_PATTERN =
            CodeField.CODE_CHARS + " " + "\\(" + CodeField.CODE_CHARS + "\\)";

    private final boolean mandatory;

    public void setChosenEntity(Material materialOrNull)
    {
        if (materialOrNull != null)
        {
            MaterialIdentifier chosenMaterial = createIdentifier(materialOrNull);
            super.setValue(chosenMaterial.print());
        }
    }

    private static MaterialIdentifier createIdentifier(Material material)
    {
        MaterialIdentifier ident = new MaterialIdentifier();
        ident.setCode(material.getCode());
        ident.setTypeCode(material.getMaterialType().getCode());
        return ident;
    }

    private MaterialChooserField(boolean mandatory, MaterialType materialTypeOrNull,
            String initialValueOrNull, IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.mandatory = mandatory;

        setValidateOnBlur(true);
        setAutoValidate(true);

        setRegex(CODE_AND_TYPE_PATTERN);
        getMessages().setRegexText(viewContext.getMessage(Dict.INCORRECT_MATERIAL_SYNTAX));
        if (initialValueOrNull != null)
        {
            setValue(initialValueOrNull);
        }
        FieldUtil.setMandatoryFlag(this, mandatory);
    }

    @Override
    protected boolean validateValue(String val)
    {
        boolean valid = super.validateValue(val);
        if (valid == false)
        {
            return false;
        }
        if (mandatory && getValue() == null)
        {
            forceInvalid(GXT.MESSAGES.textField_blankText());
            return false;
        }
        clearInvalid();
        return true;
    }

}