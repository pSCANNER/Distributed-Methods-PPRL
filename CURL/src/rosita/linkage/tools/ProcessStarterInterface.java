package rosita.linkage.tools;

import cdc.configuration.ConfiguredSystem;

public interface ProcessStarterInterface {
	public void startProcessAndWait(ConfiguredSystem system);
}
