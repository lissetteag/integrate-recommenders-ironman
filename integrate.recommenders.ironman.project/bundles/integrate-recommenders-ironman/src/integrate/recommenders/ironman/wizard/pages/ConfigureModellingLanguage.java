package integrate.recommenders.ironman.wizard.pages;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import integrate.recommenders.ironman.definition.mapping.ActualFeature;
import integrate.recommenders.ironman.definition.mapping.MLMappingConfiguration;
import integrate.recommenders.ironman.definition.mapping.ReadFeature;
import integrate.recommenders.ironman.definition.mapping.TargetElement;
import integrate.recommenders.ironman.definition.mapping.TargetItemElement;
import integrate.recommenders.ironman.definition.mapping.WriteFeature;
import integrate.recommenders.ironman.definition.services.Item;
import integrate.recommenders.ironman.definition.services.Service;
import integrate.recommenders.ironman.wizard.pages.contents.MLConfigureLanguageContentProvider;
import integrate.recommenders.ironman.wizard.pages.editing.EditingTargetLangElements;
import integrate.recommenders.ironman.wizard.pages.label.MLSourceLanguageProvider;
import integrate.recommenders.ironman.wizard.pages.label.MLTargetLanguageProvider;

import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.presentation.EcoreActionBarContributor.ExtendedLoadResourceAction.RegisteredPackageDialog;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import static integrate.recommenders.ironman.wizard.utils.IronManWizardUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class ConfigureModellingLanguage extends WizardPage {

	private Label labelNsUri;
	private MLMappingConfiguration mapping;
	private Button mappingLanguageButton;
	private TreeViewer configureLangTreeViewer;
	private Button checkedButton;
		
	protected ConfigureModellingLanguage(String pageName) {
		super(pageName);
		setTitle(IRONMAN_WIZARD_PAGE_CONFIGURE_MODELLING_LANGUAGE);	
		//Get Selected Target & Items
		mapping = new MLMappingConfiguration();
		this.mappingLanguageButton = null;
		this.checkedButton = null;
	}	

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);		
		container.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());		
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL,GridData.FILL_VERTICAL));
		//Check if mapping is necessary
		this.checkedButton = WidgetFactory.button(SWT.CHECK)
				.text("Configure a Mapping to Another Language")
				.create(container);					
	
		checkedButton.addListener(SWT.Selection, enableListener(container));
		
		WidgetFactory.label(SWT.NONE).create(container);
		WidgetFactory.label(SWT.NONE).create(container);
		
		//Create composite to select the mapping to a language
		this.labelNsUri = WidgetFactory.label(SWT.NONE).text("Mapping to Language: [Language not defined]").create(container);
		//Button to Search Package
		buttonChangeModellingLang(container);

		configureLangTreeViewer = new TreeViewer(container, SWT.VIRTUAL | SWT.BORDER);
		configureLangTreeViewer.getTree().setHeaderVisible(true);
		configureLangTreeViewer.getTree().setLinesVisible(true);
		configureLangTreeViewer.setUseHashlookup(true);
		
		configureLangTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL,GridData.FILL, true, true,3,1));
		createColumns();
		treeViewerStyle(configureLangTreeViewer);
			
		configureLangTreeViewer.setContentProvider(new MLConfigureLanguageContentProvider());	
		configureLangTreeViewer.setInput(this.mapping);
		
		mappingModellingLanguageOptions(false);
		
		setControl(container);
		setPageComplete(true);
	}

	private void mappingModellingLanguageOptions(boolean enabled) {
		this.configureLangTreeViewer.getTree().setEnabled(enabled);
		this.mappingLanguageButton.setEnabled(enabled);		
	}

	private void buttonChangeModellingLang(final Composite container) {
		this.mappingLanguageButton  = WidgetFactory.button(SWT.NONE).text("Add Modelling Language").create(container);
		this.mappingLanguageButton.addListener(SWT.Selection, event -> {
			final RegisteredPackageDialog registeredPackageDialog = new RegisteredPackageDialog(getShell());
			  registeredPackageDialog.setMultipleSelection(false);
			  registeredPackageDialog.open();
			  Object [] result = registeredPackageDialog.getResult();
              if (result != null && result.length == 1) {
            	  final String sourceNsUris = (String) result[0];
            	  if (registeredPackageDialog.isDevelopmentTimeVersion()) {
            		  Map<String, URI> ePackageNsURItoGenModelLocationMap = EcorePlugin.getEPackageNsURIToGenModelLocationMap(false);       		  
            		  URI location = ePackageNsURItoGenModelLocationMap.get(sourceNsUris);
            		  ResourceSet reset = new ResourceSetImpl();
            		  Resource genModelResource = reset.getResource(location, true);
            		  final GenModel genModel = (GenModel) genModelResource.getContents().get(0);            		  
            		  this.mapping.setGenModel(genModel);
            		  this.mapping.setNsURIPackage(sourceNsUris);
            		  labelNsUri.setText("Mapping to Language: " + sourceNsUris);
            		  labelNsUri.redraw();            		  
            	  }
              }              
		});
	}
	
	private Listener enableListener(Composite composite) {
		return new Listener() {			
			@Override
			public void handleEvent(Event event) {
				 if (event.widget instanceof Button) {
					final Button button = (Button) event.widget;
					mappingModellingLanguageOptions(button.getSelection());					
				}									
			}
		};		
	}

	private void createColumns() {
		//Target and Items (Source Language)
		TreeViewerColumn srcLanguageColumn = new TreeViewerColumn(this.configureLangTreeViewer, SWT.LEFT);
		srcLanguageColumn.getColumn().setWidth(220);
		srcLanguageColumn.getColumn().setText("Recommender Language");		
		//Provider Target and Items from the Source Language
		srcLanguageColumn.setLabelProvider(new MLSourceLanguageProvider());
		
		//Target and Items (Target Language)
		TreeViewerColumn targetLanguageColumn = new TreeViewerColumn(this.configureLangTreeViewer, SWT.LEFT);
		targetLanguageColumn.getColumn().setWidth(220);
		targetLanguageColumn.getColumn().setText("Modelling Language");
		targetLanguageColumn.setEditingSupport(new EditingTargetLangElements(this.configureLangTreeViewer, this.mapping));
		//Provide Target and Items from the Target Language
		targetLanguageColumn.setLabelProvider(new MLTargetLanguageProvider());				
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (this.mapping.getEPackage() == null) {		
			this.mapping.setMapTargetElementToTargetItems(getMapTargetElementToTargetItems());
			this.configureLangTreeViewer.refresh();
			((Composite)getControl()).layout();
		}
	}
	
	@Override
	public IronManWizard getWizard() {
		return (IronManWizard) super.getWizard();
	}
	
	private Map<TargetElement,List<TargetItemElement>> getMapTargetElementToTargetItems() {
		final var selectedServerToRecommender 
							= getWizard().getSelectedServerToRecommender();		
		final var mapTargetElementToTargetItems = new  HashMap<TargetElement,List<TargetItemElement>>();		
		for (Entry<String, List<Service>> entryRecommender : selectedServerToRecommender.entrySet()) {
			final List<Service> recommenders = entryRecommender.getValue();
			for (Service recommender : recommenders) {
				TargetElement targetElement = isTargetPresent(mapTargetElementToTargetItems, 
					recommender.getDetail().getTarget());
				List<TargetItemElement> listOfItems = new ArrayList<TargetItemElement>();
				if (targetElement == null) {
					targetElement = new TargetElement(recommender.getDetail().getTarget());	
					mapTargetElementToTargetItems.put(targetElement, listOfItems);
				} else {
					listOfItems = mapTargetElementToTargetItems.get(targetElement);
				}
				for(Item item: recommender.getDetail().getItems()) {
					final boolean isItemPresent = isItemPresent(mapTargetElementToTargetItems.get(targetElement), item); 
					if (!isItemPresent) {
						final TargetItemElement targetItemElement = new TargetItemElement();
						targetItemElement.setClassName(item.getClassName());
						new ReadFeature(targetItemElement, item.getRead());
						new WriteFeature(targetItemElement, item.getWrite());
						new ActualFeature(targetItemElement, item.getFeatures());						
						listOfItems.add(targetItemElement);
					}					
				}				
			}			
		}	
		return mapTargetElementToTargetItems;
	}
	
	private boolean isItemPresent(List<TargetItemElement> list, Item item) {
		return list.stream().anyMatch(i -> i.getFeature().getItem().equals(item.getFeatures())
								&& i.getRead().getItem().equals(item.getRead())
								&& i.getWrite().getItem().equals(item.getWrite())
								);	
	}

	private TargetElement isTargetPresent(final Map<TargetElement, List<TargetItemElement>> sourceToTargetMap, String target) {
		final Optional<Entry<TargetElement, List<TargetItemElement>>> targetElement = sourceToTargetMap.entrySet().stream()
			 .filter(e -> e.getKey().getSourceElement().equals(target))
			 .findAny();
		 if (targetElement.isPresent())
			 return targetElement.get().getKey(); 
		 return null;		
	}
	
	public MLMappingConfiguration getMapping() {
		return this.mapping;
	}	
}
