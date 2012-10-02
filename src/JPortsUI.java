// default package

import javax.swing.SwingUtilities;
import jport.PortConstants;
import jport.TheApplication;
import jport.TheOsBinaries;
import jport.gui.TheUiHolder;


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
        final Object obj = TheApplication.INSTANCE; // agro-passo class loading

        final Object MONITOR = JPortsUI.class;

        // fork GUI building into Event Dispatch thread as required by Swing guidelines
        SwingUtilities.invokeLater( new Runnable() // anonymous class
                {   @Override public void run()
                    {   synchronized( MONITOR )
                        {
                            TheOsBinaries.INSTANCE.has( "?" ); // hey lazy Swing thread, do some work!
                            TheUiHolder.INSTANCE.init(); // start Swing in EDT thread
                            MONITOR.notifyAll();
                        }
                    }
                } );

        // if only one Processor Core, wait until GUI finishes constructing
        if( Runtime.getRuntime().availableProcessors() == 1 )
        {
            try
            {
                synchronized( MONITOR )
                {
                    while( TheUiHolder.isReady() == false )
                    {
                        MONITOR.wait( 2000 ); // *BLOCKS*
                    }
                }
            }
            catch( InterruptedException ex )
            {}
        }

        // but start parsing "PortsIndex" in this thread
        TheApplication.INSTANCE.probeUpdate(); // *BLOCKS*

        // enable ports table in EDT even if GUI reported not ready
        SwingUtilities.invokeLater( new Runnable() // anonymous class
                {   @Override public void run()
                    {   TheUiHolder.INSTANCE.goLive();
                    }
                } );
    }
}
