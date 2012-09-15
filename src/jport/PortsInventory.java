package jport;

import jport.type.Portable;


/**
 * Alphabetized array of all installed with multiple versions versions and uninstalled (available).
 *
 * @author sbaber
 */
class PortsInventory
{
    /** All values in alphabetical order. */
    final private Portable[] fAllPorts;


    PortsInventory()
    {
        fAllPorts = PortsConstants.NO_PORTS;
    }

    PortsInventory( final PortsCatalog catalog )
    {
//...
        fAllPorts = null;
    }


    /**
     *
     * @return in alphabetical order, all ports described in the "PortIndex" file
     */
    public Portable[] getAllPorts() { return fAllPorts; }


}
