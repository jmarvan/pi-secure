package com.synapticpath.pisecure;

/**
 * Modules that can be disabled need to implement this interface through
 * which they will report their state.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
public interface Disableable {

	/**
	 * Each module can be disabled and enabled. Disabled modules are responsible for handling
	 * their state.  If for example Module is an event listener, it should take no action
	 * when receiving an event while being in disabled state.
	 * 
	 * @param disabled
	 */
	void setDisabled(boolean disabled);
	
	/**
	 * Returns the current state of a Module.
	 * @return
	 */
	boolean isDisabled();
	
}
