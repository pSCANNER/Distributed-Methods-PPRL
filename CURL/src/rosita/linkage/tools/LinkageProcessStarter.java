package rosita.linkage.tools;

import cdc.configuration.ConfiguredSystem;

public class LinkageProcessStarter implements ProcessStarterInterface {

	private rosita.linkage.tools.LinkageThread thread;
	
	public void startProcessAndWait(ConfiguredSystem system) {
		thread = new rosita.linkage.tools.LinkageThread(system);
		thread.start();
	}

}
