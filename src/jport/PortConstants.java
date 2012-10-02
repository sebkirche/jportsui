package jport;

import java.io.File;
import jport.common.Util;
import jport.type.Portable;


/**
 * Constants specific to the JPortsUI application.
 *
 * @author sbaber
 */
public class PortConstants
{
    static final public String     APP_NAME      = "JPortsUI";
    static final public String     VERSION       = "2012.09"; // or use self .JAR creation date
    static final public String     PORTS_PATH    = "/opt/local/var/macports/sources/rsync.macports.org/release/ports/"; // Mac only, may want to use `which port`

    static final public boolean    HAS_MAC_PORTS = Util.isOnMac() == true && new File( PORTS_PATH ).exists();
    static final public boolean    IS_SHOWING_FAVICON = true; // Java not so good with ".ico" image format and transparency, bummer!
    static final public boolean    DEBUG         = false;
    static final public boolean    OPTIMIZATION  = true; // root of all evil -- D. Knuth

    static final public Portable[] NO_PORTS      = new Portable[ 0 ];

    private PortConstants() {}
}
