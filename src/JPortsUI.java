// default package

import oz.zomg.jport.TheApplication;


/**
 * Main entry point for project.
 * A class needed here in the default package to give the executing .JAR
 * in the MacOSX application Dock a more sensible name.
 *<P>
 * Call it JPortsUI if this project is to also support FreeBSD or NetBSD.
 * Call it MacNaptic if it can integrate with HomeBrew package manager for the Mac.
 * JMacPortUI? PortExplorer? PortSnort?
 *
 * @author sbaber
 */
public class JPortsUI
{
    static
    {}

    private JPortsUI() {}

    public static void main( String[] args )
    {
        TheApplication.INSTANCE.init();
    }
}
