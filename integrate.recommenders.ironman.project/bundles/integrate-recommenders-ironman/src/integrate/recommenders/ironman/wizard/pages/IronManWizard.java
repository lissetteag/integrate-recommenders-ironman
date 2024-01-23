package integrate.recommenders.ironman.wizard.pages;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import static integrate.recommenders.ironman.wizard.utils.IronManWizardUtils.*;

import integrate.recommenders.ironman.definition.integration.EvaluateContributionsHandler;
import integrate.recommenders.ironman.definition.integration.IIntegration;
import integrate.recommenders.ironman.definition.mapping.MLMappingConfiguration;
import integrate.recommenders.ironman.definition.mapping.TargetElement;
import integrate.recommenders.ironman.definition.mapping.TargetItemElement;
import integrate.recommenders.ironman.definition.services.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public class IronManWizard extends Wizard implements INewWizard {
	
	//Wizard Pages
	private SelectRecommenders selectRec;
	private SelectTargetItems selectTargetItems;
	private ConfigureModellingLanguage modellingLanguages;
	private ConfigureAggregationMethod aggMethod;
	private ConfigureModellingTools modellingTools;
	
	//Wizard Pages's Name
	public static final String SELECT_RECOMMENDER_PAGE_NAME = "selRecommender"; 
	
	public static final String SELECT_TARGET_ITEM_PAGE_NAME = "selTargetItems";
	
	public static final String SELECT_MODELLING_LANGUAGE_PAGE_NAME = "configLanguage"; 
	
	public static final String SELECT_AGGREGATION_METHOD_PAGE_NAME = "selAggMethod";
	
	public static final String SELECT_MODELLING_TOOLS_PAGE_NAME = "modeToolsIntegration";

	private final Map<String,List<Service>> servicesBySource;
	
	public IronManWizard() {
		setWindowTitle(IRONMAN_WIZARD_NAME);
		//Get Services By Source
		servicesBySource = getAllRecommendersBySource();
	}
	
	@Override
	public void addPages() {
		// Select Recommender - page 1
		selectRec = new SelectRecommenders(SELECT_RECOMMENDER_PAGE_NAME);
		//Select Target & Items
		selectTargetItems = new SelectTargetItems(SELECT_TARGET_ITEM_PAGE_NAME);
		//Configure the Mapping to another modelling language
		modellingLanguages = new ConfigureModellingLanguage(SELECT_MODELLING_LANGUAGE_PAGE_NAME);
		//Select Aggregation Method
		aggMethod = new ConfigureAggregationMethod(SELECT_AGGREGATION_METHOD_PAGE_NAME);
		// Select Modelling Tools
		modellingTools = new ConfigureModellingTools(SELECT_MODELLING_TOOLS_PAGE_NAME);
		
		addPage(selectRec);
		addPage(selectTargetItems);
		addPage(modellingLanguages);
		addPage(aggMethod);
		addPage(modellingTools);
	}
	
	public Map<String,List<Service>> getServicesBySource() {
		return servicesBySource;
	}
	
	private Map<String,List<Service>> getAllRecommendersBySource() {
		//Get All Recommenders
		final Map<String, List<Service>> recommenderToServices = getAllRecommender();
		final Map<String,List<Service>> mapServicesBySource = new HashMap<String,List<Service>>();
		for (Map.Entry<String, List<Service>> entryService : recommenderToServices.entrySet()) {
			final List<Service> listOfServices = entryService.getValue();
			for (Service service : listOfServices) {
				final List<Service> services = mapServicesBySource.get(service.getDetail().getSource());
				if (services == null) {
					final List<Service> newListServices = new ArrayList<Service>();
					newListServices.add(service);
					mapServicesBySource.put(service.getDetail().getSource(), newListServices);
				} else {
					services.add(service);
				}
			}			
		}
		return mapServicesBySource;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// Do nothing
	}

	@Override
	public boolean performFinish() {
		//Get all selected extensions
		final List<IIntegration> listOfSelectedIntegrations = modellingTools.getSelectedExtensionToIntegration();
		//Get the Aggregation Method
		final String metasearchAlgorithm = aggMethod.getSelectedAlgorithm();
		// Generate all projects
		for (IIntegration iIntegration : listOfSelectedIntegrations) {
				EvaluateContributionsHandler.executeGenerateIntegration(metasearchAlgorithm, 
						iIntegration, getSelectedServerToRecommender());
		}			
		return true;
	}
	
	public MLMappingConfiguration getMappingConfiguration() {		
		final MLMappingConfiguration mapping = this.modellingLanguages.getMapping();
		final MLMappingConfiguration copyMapping = new MLMappingConfiguration(
				(Map<TargetElement, List<TargetItemElement>>) mapping.getMapTargetElementToTargetItems().entrySet()
					.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
				, mapping.getGenModel(), mapping.getNsURIPackage());
		//Remove empty mapping
		copyMapping.getMapTargetElementToTargetItems().entrySet().forEach(entry -> {
			entry.getValue().removeIf(targetItem -> targetItem.getRead().getStructFeature() == null);
		});
			
		return copyMapping;
	}
	
	public Map<String,List<Service>> getSelectedServerToRecommender() {
		return selectTargetItems.getSelectedServerToRecommender();
	}	
}
