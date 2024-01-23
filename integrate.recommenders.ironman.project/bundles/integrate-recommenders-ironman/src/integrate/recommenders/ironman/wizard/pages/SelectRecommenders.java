package integrate.recommenders.ironman.wizard.pages;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import integrate.recommenders.ironman.definition.services.Service;
import integrate.recommenders.ironman.wizard.pages.contents.LanguageRecommenderContentProvider;
import integrate.recommenders.ironman.wizard.pages.label.ItemRecommenderProvider;
import integrate.recommenders.ironman.wizard.pages.label.LanguageRecommenderProvider;
import integrate.recommenders.ironman.wizard.pages.label.TargetRecommenderProvider;

import static integrate.recommenders.ironman.wizard.utils.IronManWizardUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SelectRecommenders extends WizardPage {
	
	private CheckboxTreeViewer checkboxTreeViewer;
	
	protected SelectRecommenders(String pageName) {
		super(pageName);	
		setTitle(IRONMAN_WIZARD_PAGE_SELECT_RECOMMENDER_NAME);		
	}

	@Override
	public void createControl(Composite parent) {
		//Composite
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(GridLayoutFactory.fillDefaults().create());
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL,GridData.FILL_VERTICAL,true,true,1,1));
		
		checkboxTreeViewer = new CheckboxTreeViewer(container, SWT.VIRTUAL | SWT.BORDER | SWT.CHECK ); 
		checkboxTreeViewer.getTree().setHeaderVisible(true);
		checkboxTreeViewer.getTree().setLinesVisible(true);
		checkboxTreeViewer.setUseHashlookup(true);
		
		checkboxTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		checkboxTreeViewer.getTree().addSelectionListener(selectTreeViewerItem());
		
		createColumns(checkboxTreeViewer);	
		treeViewerStyle(checkboxTreeViewer);
				
		checkboxTreeViewer.setContentProvider(new LanguageRecommenderContentProvider());	
		checkboxTreeViewer.setInput(getWizard().getServicesBySource());		
		
		final Composite configureTree = new Composite(container, SWT.NONE);
		configureTree.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		WidgetFactory.button(SWT.NONE).text("Expand All").onSelect(
				e -> {this.checkboxTreeViewer.expandAll();}).create(configureTree);
		
		WidgetFactory.button(SWT.NONE).text("Collapse All").onSelect(
				e -> {this.checkboxTreeViewer.collapseAll();})
						.create(configureTree);
		
		setControl(container);
		this.checkboxTreeViewer.expandAll();
		setPageComplete(true);		
	}	
	
	private void createColumns(CheckboxTreeViewer checkboxTreeViewer) {
		//Language & Recommenders Column
		TreeViewerColumn languageColumn = new TreeViewerColumn(checkboxTreeViewer, SWT.CENTER);
		languageColumn.getColumn().setWidth(300);
		languageColumn.getColumn().setText("Language And Recommenders");
		
		//Provider Diagram Description Column
		languageColumn.setLabelProvider(new LanguageRecommenderProvider());
		
		//Target Column
		TreeViewerColumn targetColumn = new TreeViewerColumn(checkboxTreeViewer, SWT.CENTER);
		targetColumn.getColumn().setWidth(180);
		targetColumn.getColumn().setText("Target");
		
		//Provider Diagram Description Column
		targetColumn.setLabelProvider(new TargetRecommenderProvider());
		
		//Items Column
		TreeViewerColumn itemsColumn = new TreeViewerColumn(checkboxTreeViewer, SWT.CENTER);
		itemsColumn.getColumn().setWidth(230);
		itemsColumn.getColumn().setText("Items");
		
		//Provider Diagram Description Column
		itemsColumn.setLabelProvider(new ItemRecommenderProvider());		
	}
	
	@Override
	public IronManWizard getWizard() {
		return (IronManWizard) super.getWizard();
	}	
	
	public Map<String,List<Service>> mapServerToSelectedRecommender() {
		final Object[] selectedObjects = checkboxTreeViewer.getCheckedElements();
		final Map<String,List<Service>> mapServerToSelectedRecommender = new HashMap<String,List<Service>>();
		String server = null;
		for (Object object : selectedObjects) {
			if (object instanceof Map.Entry) {
				final Entry<?, ?> serversToServices = (Map.Entry<?, ?>) object;
				server = (String) serversToServices.getKey();	
				mapServerToSelectedRecommender.put(server, new ArrayList<Service>());
			} else if (object instanceof Service) {
				mapServerToSelectedRecommender.get(server).add((Service)object);
			}
		}		
		return mapServerToSelectedRecommender;
	}
	
	public CheckboxTreeViewer getCheckboxTreeViewer() {
		return checkboxTreeViewer;
	}
}
