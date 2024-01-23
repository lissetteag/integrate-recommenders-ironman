package integrate.recommenders.ironman.wizard.pages;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import integrate.recommenders.ironman.definition.integration.EvaluateContributionsHandler;
import integrate.recommenders.ironman.definition.integration.IIntegration;
import integrate.recommenders.ironman.definition.services.Service;

import static integrate.recommenders.ironman.wizard.utils.IronManWizardUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigureModellingTools extends WizardPage {

	private Map<String,IIntegration> extensionToIntegration;
	private List<Button> integrationButtons;
	
	protected ConfigureModellingTools(String pageName) {
		super(pageName);	
		setTitle(IRONMAN_WIZARD_PAGE_CONFIGURE_MODELLING_NAME);
		integrationButtons = new ArrayList<Button>();
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);		
		container.setLayout(GridLayoutFactory.fillDefaults().create());
		
		this.extensionToIntegration = EvaluateContributionsHandler.getPlatformIntegrationExtensions();
		
		WidgetFactory.label(SWT.NONE).text("List of Tools for Integration").create(container);
		//Add a check button per each integration extension
		for (Map.Entry<String, IIntegration> entryIntegration : this.extensionToIntegration.entrySet()) {
			final Button button = WidgetFactory.button(SWT.CHECK).text(entryIntegration.getKey())
				.create(container);
			button.addSelectionListener(buttonSelectionAdapter());
			integrationButtons.add(button);
		}		
		setControl(container);
		setPageComplete(true);		
	}

	private SelectionListener buttonSelectionAdapter() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Button btn = (Button) event.getSource();
				final boolean selection = btn.getSelection();
				if (selection == true) {
					var integration = extensionToIntegration.get(btn.getText());
					//Check the extension
					final Map<String, List<Service>> serverToServices = getWizard().getSelectedServerToRecommender();
					boolean configure = EvaluateContributionsHandler.executeConfigure(integration,
							serverToServices, 
							getWizard().getMappingConfiguration().getEPackage() != null 
									? getWizard().getMappingConfiguration() 
									: null );
					if (configure) {
						boolean success = EvaluateContributionsHandler.executeCutomizeIntegration(integration, 
								serverToServices);
						btn.setSelection(success);
					}						
				}			
			}
		};
	}
	
	@Override
	public IronManWizard getWizard() {
		return (IronManWizard)super.getWizard();
	}
	
	//Selected extensions
	public List<IIntegration> getSelectedExtensionToIntegration() {
		final List<IIntegration> listOfSelectedIntegrations = new ArrayList<IIntegration>();
		for (Button button : integrationButtons) {
			if(button.getSelection()) {
				final IIntegration selectedIntegration = this.extensionToIntegration.get(button.getText());
				listOfSelectedIntegrations.add(selectedIntegration);
			}
		}
		return listOfSelectedIntegrations;
	}

}
