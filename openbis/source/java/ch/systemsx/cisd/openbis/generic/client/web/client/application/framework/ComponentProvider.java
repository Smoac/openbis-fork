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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GroupsView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.PersonsView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.RolesView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBatchRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project.ProjectGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project.ProjectRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBatchRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyRegistrationForm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

import com.extjs.gxt.ui.client.widget.Component;

/**
 * Creates and provides GUI modules/components (such as sample browser).
 * <p>
 * Note that the returned object must be a {@link ITabItem} implementation.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
public final class ComponentProvider {
	private final CommonViewContext viewContext;

	ComponentProvider(final CommonViewContext viewContext) {
		this.viewContext = viewContext;
	}

	private String getMessage(String key) {
		return viewContext.getMessage(key);
	}

	public final ITabItemFactory getSampleBrowser() {
		return new ITabItemFactory() {
			public ITabItem create() {
				DisposableComponent browser = SampleBrowserGrid
						.create(viewContext);
				return DefaultTabItem.create(getMessage(Dict.SAMPLE_BROWSER),
						browser);
			}

			public String getId() {
				return SampleBrowserGrid.BROWSER_ID;
			}
		};
	}

	public final ITabItemFactory getMaterialBrowser() {
		return new ITabItemFactory() {
			public ITabItem create() {
				DisposableComponent browser = MaterialBrowserGrid
						.createWithTypeChooser(viewContext);
				return DefaultTabItem.create(getMessage(Dict.MATERIAL_BROWSER),
						browser);
			}

			public String getId() {
				return MaterialBrowserGrid.BROWSER_ID;
			}
		};
	}

	public final ITabItemFactory getDummyComponent() {
		return new ITabItemFactory() {
			public ITabItem create() {
				return new DefaultTabItem(getMessage(Dict.NOT_IMPLEMENTED),
						new DummyComponent(), false);
			}

			public String getId() {
				return DummyComponent.ID;
			}
		};
	}

	public final ITabItemFactory getGroupsView() {
		return new ITabItemFactory() {
			public ITabItem create() {
				return new ContentPanelAdapter(new GroupsView(viewContext),
						false);
			}

			public String getId() {
				return GroupsView.ID;
			}
		};
	}

	public final ITabItemFactory getRolesView() {
		return new ITabItemFactory() {
			public ITabItem create() {
				return new ContentPanelAdapter(new RolesView(viewContext),
						false);
			}

			public String getId() {
				return RolesView.ID;
			}
		};
	}

	public final ITabItemFactory getPersonsView() {
		return new ITabItemFactory() {
			public ITabItem create() {
				return new ContentPanelAdapter(new PersonsView(viewContext),
						false);
			}

			public String getId() {
				return PersonsView.ID;
			}
		};
	}

	public final ITabItemFactory getSampleRegistration() {
		return new ITabItemFactory() {
			public ITabItem create() {
				Component component = new SampleRegistrationPanel(viewContext);
				return new DefaultTabItem(getMessage(Dict.SAMPLE_REGISTRATION),
						component, true);
			}

			public String getId() {
				return SampleRegistrationPanel.ID;
			}
		};
	}

	public final ITabItemFactory getExperimentRegistration() {
		return new ITabItemFactory() {
			public ITabItem create() {
				Component component = new ExperimentRegistrationPanel(
						viewContext);
				return new DefaultTabItem(
						getMessage(Dict.EXPERIMENT_REGISTRATION), component,
						true);
			}

			public String getId() {
				return ExperimentRegistrationPanel.ID;
			}
		};
	}

	public final ITabItemFactory getSampleBatchRegistration() {
		return new ITabItemFactory() {
			public ITabItem create() {
				Component component = new SampleBatchRegistrationPanel(
						viewContext);
				return new DefaultTabItem(
						getMessage(Dict.SAMPLE_BATCH_REGISTRATION), component,
						true);
			}

			public String getId() {
				return SampleBatchRegistrationPanel.ID;
			}
		};
	}

	public final ITabItemFactory getMaterialBatchRegistration() {
		return new ITabItemFactory() {
			public ITabItem create() {
				Component component = new MaterialBatchRegistrationPanel(
						viewContext);
				return new DefaultTabItem(getMessage(Dict.MATERIAL_IMPORT),
						component, true);
			}

			public String getId() {
				return MaterialBatchRegistrationPanel.ID;
			}
		};
	}

	public final ITabItemFactory getVocabularyRegistration() {
		return new ITabItemFactory() {
			public ITabItem create() {
				Component component = new VocabularyRegistrationForm(
						viewContext);
				return new DefaultTabItem(
						getMessage(Dict.VOCABULARY_REGISTRATION), component,
						true);
			}

			public String getId() {
				return VocabularyRegistrationForm.ID;
			}
		};
	}

	public final ITabItemFactory getProjectRegistration() {
		return new ITabItemFactory() {
			public ITabItem create() {
				Component component = new ProjectRegistrationForm(viewContext);
				return new DefaultTabItem(
						getMessage(Dict.PROJECT_REGISTRATION), component, true);
			}

			public String getId() {
				return ProjectRegistrationForm.ID;
			}
		};
	}

	public final ITabItemFactory getVocabularyBrowser() {
		return new ITabItemFactory() {
			public ITabItem create() {
				DisposableComponent component = VocabularyGrid
						.create(viewContext);
				return DefaultTabItem.create(
						getMessage(Dict.VOCABULARY_BROWSER), component);
			}

			public String getId() {
				return VocabularyGrid.GRID_ID;
			}
		};
	}

	public final ITabItemFactory getProjectBrowser() {
		return new ITabItemFactory() {
			public ITabItem create() {
				DisposableComponent component = ProjectGrid.create(viewContext);
				return DefaultTabItem.create(getMessage(Dict.PROJECT_BROWSER),
						component);
			}

			public String getId() {
				return ProjectGrid.BROWSER_ID;
			}
		};
	}

	public ITabItemFactory getExperimentBrowser() {
		return new ITabItemFactory() {
			public ITabItem create() {
				DisposableComponent browser = ExperimentBrowserGrid
						.create(viewContext);
				return DefaultTabItem.create(
						getMessage(Dict.EXPERIMENT_BROWSER), browser);
			}

			public String getId() {
				return ExperimentBrowserGrid.BROWSER_ID;
			}
		};
	}

	public ITabItemFactory getPropertyTypeBrowser() {
		return new ITabItemFactory() {
			public ITabItem create() {
				DisposableComponent component = PropertyTypeGrid
						.create(viewContext);
				return DefaultTabItem.create(getMessage(Dict.PROPERTY_TYPES),
						component);
			}

			public String getId() {
				return PropertyTypeGrid.BROWSER_ID;
			}
		};
	}

	public ITabItemFactory getPropertyTypeRegistration() {
		return new ITabItemFactory() {
			public ITabItem create() {
				Component component = new PropertyTypeRegistrationForm(
						viewContext);
				return new DefaultTabItem(
						getMessage(Dict.PROPERTY_TYPE_REGISTRATION), component,
						true);
			}

			public String getId() {
				return PropertyTypeRegistrationForm.ID;
			}
		};
	}

	public ITabItemFactory getPropertyTypeAssignmentBrowser() {
		return new ITabItemFactory() {
			public ITabItem create() {
				DisposableComponent component = PropertyTypeAssignmentGrid
						.create(viewContext);
				return DefaultTabItem.create(
						getMessage(Dict.PROPERTY_TYPE_ASSIGNMENTS), component);
			}

			public String getId() {
				return PropertyTypeAssignmentGrid.BROWSER_ID;
			}
		};
	}

	public ITabItemFactory getPropertyTypeExperimentTypeAssignmentForm() {
		return getPropertyTypeAssignmentForm(EntityKind.EXPERIMENT,
				Dict.ASSIGN_EXPERIMENT_PROPERTY_TYPE);
	}

	public ITabItemFactory getPropertyTypeMaterialTypeAssignmentForm() {
		return getPropertyTypeAssignmentForm(EntityKind.MATERIAL,
				Dict.ASSIGN_MATERIAL_PROPERTY_TYPE);
	}

	public ITabItemFactory getPropertyTypeDataSetTypeAssignmentForm() {
		return getPropertyTypeAssignmentForm(EntityKind.DATA_SET,
				Dict.ASSIGN_DATA_SET_PROPERTY_TYPE);
	}

	public ITabItemFactory getPropertyTypeSampleTypeAssignmentForm() {
		return getPropertyTypeAssignmentForm(EntityKind.SAMPLE,
				Dict.ASSIGN_SAMPLE_PROPERTY_TYPE);
	}

	private ITabItemFactory getPropertyTypeAssignmentForm(
			final EntityKind entityKind, final String messageKey) {
		return new ITabItemFactory() {
			public ITabItem create() {
				Component component = new PropertyTypeAssignmentForm(
						viewContext, entityKind);
				return new DefaultTabItem(getMessage(messageKey), component,
						true);
			}

			public String getId() {
				return PropertyTypeAssignmentForm.createId(entityKind);
			}
		};
	}

	public ITabItemFactory getDataSetSearch() {
		return new ITabItemFactory() {
			public ITabItem create() {
				DisposableComponent browser = DataSetSearchHitGrid
						.create(viewContext);
				return DefaultTabItem.create(getMessage(Dict.DATA_SET_SEARCH),
						browser);
			}

			public String getId() {
				return DataSetSearchHitGrid.BROWSER_ID;
			}
		};
	}

	public ITabItemFactory getSampleTypeBrowser() {
		return new ITabItemFactory() {
			public ITabItem create() {
				DisposableComponent component = SampleTypeGrid
						.create(viewContext);
				return DefaultTabItem.create(getMessage(Dict.SAMPLE_TYPES),
						component);
			}

			public String getId() {
				return SampleTypeGrid.BROWSER_ID;
			}
		};
	}

	public ITabItemFactory getMaterialTypeBrowser() {
		return new ITabItemFactory() {
			public ITabItem create() {
				DisposableComponent component = MaterialTypeGrid
						.create(viewContext);
				return DefaultTabItem.create(getMessage(Dict.MATERIAL_TYPES),
						component);
			}

			public String getId() {
				return MaterialTypeGrid.BROWSER_ID;
			}
		};
	}

	public ITabItemFactory getExperimentTypeBrowser() {
		return new ITabItemFactory() {
			public ITabItem create() {
				DisposableComponent component = ExperimentTypeGrid
						.create(viewContext);
				return DefaultTabItem.create(getMessage(Dict.EXPERIMENT_TYPES),
						component);
			}

			public String getId() {
				return ExperimentTypeGrid.BROWSER_ID;
			}
		};
	}
}