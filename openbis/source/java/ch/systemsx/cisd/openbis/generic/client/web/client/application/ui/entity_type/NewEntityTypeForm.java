package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataSetKindModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.BorderLayoutDataFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.DataSetKindSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETNewPTAssigments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewPTNewAssigment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class NewEntityTypeForm extends ContentPanel
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "new-entity-type-form";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    //
    // Entity Type
    //
    private final EntityKind kind;

    private final EntityType entityToEdit;

    private NewETNewPTAssigments newTypeWithAssigments;

    //
    // Entity Form Related
    //
    private AbstractEntityTypeGrid<? extends EntityType> typeGrid;

    private FormPanel dialogForm;

    //
    // Form Creation
    //
    private NewEntityTypeForm(EntityKind kind, EntityType entityToEdit, IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.kind = kind;
        this.entityToEdit = entityToEdit;
        this.viewContext = viewContext;

        // Main panel
        setLayout(new BorderLayout());
        this.setHeaderVisible(false);
        this.setBorders(false);
        this.setBodyBorder(false);

        initForm(false);
    }

    public static DatabaseModificationAwareComponent create(
            EntityKind kind,
            EntityType entityToEdit,
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        NewEntityTypeForm form = new NewEntityTypeForm(kind, entityToEdit, viewContext);
        return new DatabaseModificationAwareComponent(form, new CompositeDatabaseModificationObserver());
    }

    private void initForm(boolean isRefresh)
    {
        // Cleanup for the form and for the in memory structure
        removeAll();
        newTypeWithAssigments = new NewETNewPTAssigments();
        newTypeWithAssigments.setAssigments(new ArrayList<NewPTNewAssigment>());

        // Top panel
        initEntityTypeForm();
        if (entityToEdit == null || isRefresh)
        {
            initCreateEntity();
        } else
        {
            initEditEntity();
        }
        dialogForm.setHeaderVisible(false);
        dialogForm.setBorders(false);
        dialogForm.setBodyBorder(false);
        dialogForm.setLabelWidth(180);
        add(dialogForm, BorderLayoutDataFactory.create(LayoutRegion.NORTH, 350));

        // Central panel
        PropertyTypeAssignmentGrid grid = (PropertyTypeAssignmentGrid) PropertyTypeAssignmentGrid
                .create(viewContext, null, newTypeWithAssigments).getComponent();
        final Component centerPanel = grid;
        add(centerPanel, BorderLayoutDataFactory.create(LayoutRegion.CENTER, 170));

        // Bottom panel
        final FormPanel bottomPanel = getSaveButton();
        add(bottomPanel, BorderLayoutDataFactory.create(LayoutRegion.SOUTH, 70));

        layout();
    }

    private void initEntityTypeForm()
    {
        AddEntityTypeDialog<? extends EntityType> dialog = null;

        switch (kind)
        {
            case SAMPLE:
                typeGrid = (SampleTypeGrid) SampleTypeGrid.create(viewContext).getComponent();
                dialog = ((SampleTypeGrid) typeGrid).getNewDialog((SampleType) new SampleType());
                break;
            case DATA_SET:
                typeGrid = (DataSetTypeGrid) DataSetTypeGrid.create(viewContext).getComponent();
                dialog = ((DataSetTypeGrid) typeGrid).getNewDialog((DataSetType) new DataSetType());
                break;
            case EXPERIMENT:
                typeGrid = (ExperimentTypeGrid) ExperimentTypeGrid.create(viewContext).getComponent();
                dialog = ((ExperimentTypeGrid) typeGrid).getNewDialog((ExperimentType) new ExperimentType());
                break;
            case MATERIAL:
                typeGrid = (MaterialTypeGrid) MaterialTypeGrid.create(viewContext).getComponent();
                dialog = (AddEntityTypeDialog<MaterialType>) ((MaterialTypeGrid) typeGrid)
                        .getNewDialog((MaterialType) new MaterialType());
                break;
        }

        // afterwards
        List<Component> dialogFormIntoList = dialog.getItems();
        dialogForm = (FormPanel) dialogFormIntoList.get(0);
    }

    private void initCreateEntity()
    {
        AddEntityTypeDialog<? extends EntityType> dialog = null;

        switch (kind)
        {
            case SAMPLE:
                SampleType sampleType = new SampleType();
                sampleType.setSampleTypePropertyTypes(new ArrayList<SampleTypePropertyType>());
                newTypeWithAssigments.setEntity(sampleType);
                break;
            case DATA_SET:
                DataSetType dataSetType = new DataSetType();
                dataSetType.setDataSetTypePropertyTypes(new ArrayList<DataSetTypePropertyType>());
                newTypeWithAssigments.setEntity(dataSetType);
                break;
            case EXPERIMENT:
                ExperimentType experimentType = new ExperimentType();
                experimentType.setExperimentTypePropertyTypes(new ArrayList<ExperimentTypePropertyType>());
                newTypeWithAssigments.setEntity(experimentType);
                break;
            case MATERIAL:
                MaterialType materialType = new MaterialType();
                materialType.setMaterialTypePropertyTypes(new ArrayList<MaterialTypePropertyType>());
                newTypeWithAssigments.setEntity(materialType);
                break;
        }

        newTypeWithAssigments.getEntity().setCode("" + System.currentTimeMillis()); // Just needed so the grid don't break
    }

    private void initEditEntity()
    {
        List<Field<?>> formFields = dialogForm.getFields();

        switch (kind)
        {
            case SAMPLE:
                SampleType sampleToEdit = (SampleType) entityToEdit;

                ((CodeField) formFields.get(0)).setValue(sampleToEdit.getCode());
                ((CodeField) formFields.get(0)).setEnabled(false);
                ((DescriptionField) formFields.get(1)).setValue(sampleToEdit.getDescription());
                if (sampleToEdit.getValidationScript() != null)
                {
                    ((ScriptChooserField) formFields.get(2)).setValue(sampleToEdit.getValidationScript().getName());
                }
                ((CheckBoxField) formFields.get(3)).setValue(sampleToEdit.isListable());
                ((CheckBoxField) formFields.get(4)).setValue(sampleToEdit.isShowContainer());
                ((CheckBoxField) formFields.get(5)).setValue(sampleToEdit.isShowParents());
                ((CheckBoxField) formFields.get(6)).setValue(sampleToEdit.isSubcodeUnique());
                ((CheckBoxField) formFields.get(7)).setValue(sampleToEdit.isAutoGeneratedCode());
                ((CheckBoxField) formFields.get(8)).setValue(sampleToEdit.isShowParentMetadata());
                ((TextField<String>) formFields.get(9)).setValue(sampleToEdit.getGeneratedCodePrefix());
                break;
            case DATA_SET:
                DataSetType datasetToEdit = (DataSetType) entityToEdit;

                ((CodeField) formFields.get(0)).setValue(datasetToEdit.getCode());
                ((CodeField) formFields.get(0)).setEnabled(false);
                ((DescriptionField) formFields.get(1)).setValue(datasetToEdit.getDescription());
                if (datasetToEdit.getValidationScript() != null)
                {
                    ((ScriptChooserField) formFields.get(2)).setValue(datasetToEdit.getValidationScript().getName());
                }
                ((DataSetKindSelectionWidget) formFields.get(3)).setValue(new DataSetKindModel(datasetToEdit.getDataSetKind()));
                ((DataSetKindSelectionWidget) formFields.get(3)).setEnabled(false);
                ((CheckBoxField) formFields.get(4)).setValue(datasetToEdit.isDeletionDisallow());
                ((TextField<String>) formFields.get(5)).setValue(datasetToEdit.getMainDataSetPattern());
                ((TextField<String>) formFields.get(6)).setValue(datasetToEdit.getMainDataSetPath());
                break;
            case EXPERIMENT:
                ExperimentType experimentToEdit = (ExperimentType) entityToEdit;

                ((CodeField) formFields.get(0)).setValue(experimentToEdit.getCode());
                ((CodeField) formFields.get(0)).setEnabled(false);
                ((DescriptionField) formFields.get(1)).setValue(experimentToEdit.getDescription());
                if (experimentToEdit.getValidationScript() != null)
                {
                    ((ScriptChooserField) formFields.get(2)).setValue(experimentToEdit.getValidationScript().getName());
                }
                break;
            case MATERIAL:
                MaterialType materialToEdit = (MaterialType) entityToEdit;

                ((CodeField) formFields.get(0)).setValue(materialToEdit.getCode());
                ((CodeField) formFields.get(0)).setEnabled(false);
                ((DescriptionField) formFields.get(1)).setValue(materialToEdit.getDescription());
                if (materialToEdit.getValidationScript() != null)
                {
                    ((ScriptChooserField) formFields.get(2)).setValue(materialToEdit.getValidationScript().getName());
                }
                break;
        }

        newTypeWithAssigments.setEntity(entityToEdit);

        for (int i = 0; i < entityToEdit.getAssignedPropertyTypes().size(); i++)
        {
            NewPTNewAssigment assigment = new NewPTNewAssigment();
            assigment.setExistingPropertyType(true);
            assigment.setPropertyType(entityToEdit.getAssignedPropertyTypes().get(i).getPropertyType());

            NewETPTAssignment newETPTAssigment = new NewETPTAssignment();
            newETPTAssigment.setPropertyTypeCode(entityToEdit.getAssignedPropertyTypes().get(i).getPropertyType().getCode());
            newETPTAssigment.setEntityKind(entityToEdit.getEntityKind());
            newETPTAssigment.setOrdinal(entityToEdit.getAssignedPropertyTypes().get(i).getOrdinal());
            newETPTAssigment.setSection(entityToEdit.getAssignedPropertyTypes().get(i).getSection());
            newETPTAssigment.setMandatory(entityToEdit.getAssignedPropertyTypes().get(i).isMandatory());
            newETPTAssigment.setDynamic(entityToEdit.getAssignedPropertyTypes().get(i).isDynamic());
            newETPTAssigment.setManaged(entityToEdit.getAssignedPropertyTypes().get(i).isManaged());
            newETPTAssigment.setShownInEditView(entityToEdit.getAssignedPropertyTypes().get(i).isShownInEditView());
            newETPTAssigment.setShowRawValue(entityToEdit.getAssignedPropertyTypes().get(i).getShowRawValue());
            newETPTAssigment.setModificationDate(entityToEdit.getAssignedPropertyTypes().get(i).getModificationDate());

            if (entityToEdit.getAssignedPropertyTypes().get(i).getScript() != null)
            {
                assigment.getAssignment().setScriptName(entityToEdit.getAssignedPropertyTypes().get(i).getScript().getName());
            }

            assigment.setAssignment(newETPTAssigment);
            newTypeWithAssigments.getAssigments().add(assigment);
        }
    }

    private FormPanel getSaveButton()
    {
        final FormPanel formWithButtons = new FormPanel();
        formWithButtons.setHeaderVisible(false);
        formWithButtons.setBorders(false);
        formWithButtons.setBodyBorder(false);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setMinButtonWidth(100);
        buttonBar.setAlignment(HorizontalAlignment.RIGHT);
        formWithButtons.add(buttonBar);

        final Button save = new Button("Save", new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (dialogForm.isValid())
                    {
                        // Update Entity Type
                        setEntityFromForm();
                        // To order of the ordinals for the database start at 0
                        newTypeWithAssigments.updateOrdinalToDBOrder();
                        // Update Entity Type Code at the Property Types
                        for (NewPTNewAssigment assigment : newTypeWithAssigments.getAssigments())
                        {
                            assigment.getAssignment().setEntityTypeCode(newTypeWithAssigments.getEntity().getCode());
                        }
                        // Register/Update Call

                        if (entityToEdit == null)
                        {
                            viewContext.getService().registerEntitytypeAndAssignPropertyTypes(newTypeWithAssigments, new AsyncCallbackEntityType());
                        } else
                        {
                            viewContext.getService().updateEntitytypeAndPropertyTypes(newTypeWithAssigments, new AsyncCallbackEntityType());
                        }
                    }
                }
            });
        save.setWidth("100px");
        save.setHeight("20px");
        buttonBar.add(save);

        return formWithButtons;
    }

    //
    // Save Functionality
    //
    private void setEntityFromForm()
    {
        List<Field<?>> formFields = dialogForm.getFields();

        switch (kind)
        {
            case SAMPLE:
                SampleType toSaveSample = (SampleType) newTypeWithAssigments.getEntity();
                toSaveSample.setCode((String) formFields.get(0).getValue());
                toSaveSample.setDescription((String) formFields.get(1).getValue());
                ScriptChooserField scriptChooserField = (ScriptChooserField) formFields.get(2);
                if (formFields.get(2).getValue() != null)
                {
                    Script script = new Script();
                    script.setName((String) formFields.get(2).getValue());
                    toSaveSample.setValidationScript(script);
                }
                toSaveSample.setListable((Boolean) formFields.get(3).getValue());
                toSaveSample.setShowContainer((Boolean) formFields.get(4).getValue());
                toSaveSample.setShowParents((Boolean) formFields.get(5).getValue());
                toSaveSample.setSubcodeUnique((Boolean) formFields.get(6).getValue());
                toSaveSample.setAutoGeneratedCode((Boolean) formFields.get(7).getValue());
                toSaveSample.setShowParentMetadata((Boolean) formFields.get(8).getValue());
                toSaveSample.setGeneratedCodePrefix((String) formFields.get(9).getValue());
                newTypeWithAssigments.setEntity(toSaveSample);
                break;
            case DATA_SET:
                DataSetType toSaveDataSet = (DataSetType) newTypeWithAssigments.getEntity();
                toSaveDataSet.setCode((String) formFields.get(0).getValue());
                toSaveDataSet.setDescription((String) formFields.get(1).getValue());
                if (formFields.get(2).getValue() != null)
                {
                    Script script = new Script();
                    script.setName((String) formFields.get(2).getValue());
                    toSaveDataSet.setValidationScript(script);
                }
                toSaveDataSet.setDataSetKind(((DataSetKindSelectionWidget) formFields.get(3)).getValue().getBaseObject());
                toSaveDataSet.setDeletionDisallow((Boolean) formFields.get(4).getValue());
                toSaveDataSet.setMainDataSetPattern((String) formFields.get(5).getValue());
                toSaveDataSet.setMainDataSetPath((String) formFields.get(6).getValue());
                newTypeWithAssigments.setEntity(toSaveDataSet);
                break;
            case EXPERIMENT:
                ExperimentType toSaveExperiment = (ExperimentType) newTypeWithAssigments.getEntity();
                toSaveExperiment.setCode((String) formFields.get(0).getValue());
                toSaveExperiment.setDescription((String) formFields.get(1).getValue());
                if (formFields.get(2).getValue() != null)
                {
                    Script script = new Script();
                    script.setName((String) formFields.get(2).getValue());
                    toSaveExperiment.setValidationScript(script);
                }
                newTypeWithAssigments.setEntity(toSaveExperiment);
                break;
            case MATERIAL:
                MaterialType toSaveMaterial = (MaterialType) newTypeWithAssigments.getEntity();
                toSaveMaterial.setCode((String) formFields.get(0).getValue());
                toSaveMaterial.setDescription((String) formFields.get(1).getValue());
                if (formFields.get(2).getValue() != null)
                {
                    Script script = new Script();
                    script.setName((String) formFields.get(2).getValue());
                    toSaveMaterial.setValidationScript(script);
                }
                newTypeWithAssigments.setEntity(toSaveMaterial);
                break;
        }
    }

    private class AsyncCallbackEntityType implements AsyncCallback<String>
    {
        @Override
        public void onFailure(Throwable throwable)
        {
            String message = "Error";
            if (throwable instanceof UserFailureException)
            {
                // In case it fails the order of the properties need to be the one at the grid
                newTypeWithAssigments.updateOrdinalToGridOrder();
                // Show error message
                UserFailureException userException = (UserFailureException) throwable;
                String details = GWTUtils.translateToHtmlLineBreaks(userException.getMessage());
                if (details != null)
                {
                    message = details;
                }
            }
            MessageBox.alert("Error", message, null);
        }

        @Override
        public void onSuccess(String result)
        {
            // In case of success, in case the user reopen it without refreshing to avoid a glitch
            newTypeWithAssigments.updateOrdinalToGridOrder();

            if (entityToEdit == null)
            {
                MessageBox.alert("Success", "Registration Successful.", null);
                initForm(true);
            } else
            {
                MessageBox.alert("Success", "Update Successful.", null);
            }
        }
    }
}
