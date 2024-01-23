package integrate.recommenders.ironman.wizard.pages;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import integrate.recommenders.ironman.definition.services.Item;
import integrate.recommenders.ironman.definition.services.Service;
import integrate.recommenders.ironman.wizard.pages.contents.SelectItemContentProvider;
import integrate.recommenders.ironman.wizard.pages.label.SelectItemRecommenderProvider;

import static integrate.recommenders.ironman.wizard.utils.IronManWizardUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SelectTargetItems extends WizardPage {
	
	private CheckboxTreeViewer checkboxTreeViewer;
	private Map<String,List<Service>> mapServiceToRecommender;
	
	private boolean refresh;
	
	protected SelectTargetItems(String pageName) {
		super(pageName);	
		setTitle(IRONMAN_WIZARD_PAGE_SELECT_TARGET_ITEMS_NAME);
		this.refresh = true;
		this.mapServiceToRecommender = null;
	}

	@Override
	public void createControl(Composite parent) {
		//Container Composite
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(GridLayoutFactory.fillDefaults().create());
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL,true,true,1,1));
		
		checkboxTreeViewer = new CheckboxTreeViewer(container, SWT.VIRTUAL | SWT.BORDER | SWT.CHECK ); 
		checkboxTreeViewer.getTree().setHeaderVisible(true);
		checkboxTreeViewer.getTree().setLinesVisible(true);
		checkboxTreeViewer.setUseHashlookup(true);
		
		checkboxTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createColumns(checkboxTreeViewer);
		treeViewerStyle(checkboxTreeViewer);
				
		checkboxTreeViewer.setContentProvider(new SelectItemContentProvider());	
				
		checkboxTreeViewer.getTree().addSelectionListener(selectTreeViewerItem());
		
		final Composite configureTree = new Composite(container, SWT.NONE);
		configureTree.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		
		WidgetFactory.button(SWT.NONE).text("Expand All").onSelect(
				e -> {this.checkboxTreeViewer.expandAll();}).create(configureTree);
		
		WidgetFactory.button(SWT.NONE).text("Collapse All").onSelect(
				e -> {this.checkboxTreeViewer.collapseAll();})
						.create(configureTree);	
		
		WidgetFactory.button(SWT.NONE).text("Select All").onSelect(
				e -> {
					for (Object element : this.checkboxTreeViewer.getExpandedElements()) {
						this.checkboxTreeViewer.setSubtreeChecked(element, true);
					}					
				}).create(configureTree);	
		
		setControl(container);
		setPageComplete(true);		
	}
	
	private void createColumns(CheckboxTreeViewer checkboxTreeViewer2) {
		//Choose Items Column
		TreeViewerColumn itemColumn = new TreeViewerColumn(checkboxTreeViewer, SWT.LEFT);
		itemColumn.getColumn().setWidth(280);
		itemColumn.getColumn().setText("Select Items");
			
		//Provider Diagram Description Column
		itemColumn.setLabelProvider(new SelectItemRecommenderProvider());		
	}

	@Override
	public IronManWizard getWizard() {
		return (IronManWizard) super.getWizard();
	}
	
	public Map<String,List<Service>> mapServerToSelectedRecommender() {
		if (this.mapServiceToRecommender == null)
			this.mapServiceToRecommender = 
			((SelectRecommenders)getWizard()
				.getPage(IronManWizard.SELECT_RECOMMENDER_PAGE_NAME)).mapServerToSelectedRecommender();
		return this.mapServiceToRecommender;
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible == true & this.refresh == true) {
			this.checkboxTreeViewer.setInput(mapServerToSelectedRecommender()
					.values().stream().flatMap(List::stream).collect(Collectors.toList()));
			this.checkboxTreeViewer.refresh();	
			this.checkboxTreeViewer.expandAll();
			this.refresh = false;
		}
	}
	
	public CheckboxTreeViewer getCheckboxTreeViewer() {
		return checkboxTreeViewer;
	}
	
	public Map<String,List<Service>> getSelectedServerToRecommender() {
		final Map<String,List<Service>> selectedServerToRecommender = 
				new HashMap<String, List<Service>>();
		final Object[] selectedElements = this.checkboxTreeViewer.getCheckedElements();
		Service currentService = null;
		for (Object object : selectedElements) {
			if (object instanceof Service) {
				//Search in the map
				final Service service = (Service) object;
				Map.Entry<String, List<Service>> entryRecommender = entryService(service);
				List<Service> listOfServices = selectedServerToRecommender.get(entryRecommender.getKey());
				final Service newService = new Service(service);
				currentService = newService;
				if (listOfServices == null) {					
					final List<Service> newListOfServices = new ArrayList<Service>();
					newListOfServices.add(newService);
					selectedServerToRecommender.put(entryRecommender.getKey(), newListOfServices);					
				} else {
					listOfServices.add(newService);					
				}					
			} else if (object instanceof Item) {
				final Item newItem = new Item((Item) object);
				currentService.getDetail().getItems().add(newItem);
			}
		}		
		return selectedServerToRecommender;
	}
	
	private Map.Entry<String, List<Service>> entryService(Service service) {
		for (Map.Entry<String, List<Service>> entryRecommender : this.mapServiceToRecommender.entrySet()) {
			final List<Service> listOfServices = entryRecommender.getValue();
			if (listOfServices.contains(service))
				return entryRecommender;
		}
		throw new IllegalArgumentException("Illegal service as parameter: " + service.toString());
	}
}
