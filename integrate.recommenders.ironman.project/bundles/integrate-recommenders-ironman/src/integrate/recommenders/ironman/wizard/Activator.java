package integrate.recommenders.ironman.wizard;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {
	
	// The plug-in ID
	public static final String PLUGIN_ID = "integrate.recommenders.ironman.wizard";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
		// Do nothing
	}
	
	/**
	 * This method is called when the plugin is started. It initializes the plugin
	 * and sets the plugin instance to the static field 'plugin'.
	 * 
	 * @param context the bundle context for the plugin
	 * @throws Exception if an error occurs during initialization
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/**
	 * This method is called when the plugin is stopped. It sets the plugin instance
	 * to null and performs any necessary cleanup operations.
	 * 
	 * @param context the bundle context for the plugin
	 * @throws Exception if an error occurs during cleanup
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
}
