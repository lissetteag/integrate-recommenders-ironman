package integrate.recommenders.ironman.wizard.pages;

import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import integrate.recommenders.ironman.definition.algorithm.EvaluateMetaSearchContributionHandler;

import static integrate.recommenders.ironman.wizard.utils.IronManWizardUtils.*;

import java.util.Collection;

public class ConfigureAggregationMethod extends WizardPage {
	
	private Composite compositeContainer;

	protected ConfigureAggregationMethod(String pageName) {
		super(pageName);	
		setTitle(IRONMAN_WIZARD_PAGE_AGGREGATION_METHOD_NAME);
	}

	@Override
	public void createControl(Composite parent) {
		compositeContainer = new Composite(parent, SWT.NONE);		
		compositeContainer.setLayout(new RowLayout(SWT.VERTICAL));
		
		final Collection<String> algorithms = EvaluateMetaSearchContributionHandler.getPlatformIntegrationExtensions();
		WidgetFactory.label(SWT.NONE).text("List of aggregation methods:").create(compositeContainer);
		for (String algorithmName : algorithms) {
			WidgetFactory.button(SWT.RADIO).text(algorithmName).create(compositeContainer);
		}		
		setControl(compositeContainer);
		setPageComplete(true);		
	}
	
	public String getSelectedAlgorithm() {
		final Control[] controls = this.compositeContainer.getChildren();
		for (Control control : controls) {
			if (control instanceof Button &&
					((Button) control).getSelection()) {
				 return ((Button) control).getText();
			}
		}
		throw new IllegalAccessError("Cannot find the Meta Fusion Algorithm");
	}
}
