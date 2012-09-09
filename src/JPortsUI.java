// default package

import javax.swing.SwingUtilities;
import jport.TheApplication;
import jport.gui.TheUiHolder;


/**
 * Main entry point for project.
 * A class needed here in the default package to give the executing .JAR
 * in the MacOSX application Dock a more sensible name.
 *<P>
 * Call it JPortsUI if this project is to also support FreeBSD or NetBSD.
 * Call it MacNaptic if it can integrate with HomeBrew package manager for the Mac.
 * JMacPortUI? PortExplorer?
 *
 * @author sbaber
 */
public class JPortsUI
{
    static final private boolean _THREADS_PROOF = false;

    private JPortsUI() {}

    public static void main( String[] args )
    {
        // fork GUI building into Event Dispatch thread as required by Swing guidelines
        SwingUtilities.invokeLater( new Runnable() // anonymous class
                {   @Override public void run()
                    {   synchronized( JPortsUI.class )
                        {
                            if( _THREADS_PROOF ) System.out.println( "TheUiHolder begin constructing" );
                            TheUiHolder.INSTANCE.init(); // start Swing in EDT thread
                            if( _THREADS_PROOF ) System.out.println( "TheUiHolder end constructing" );

                            JPortsUI.class.notifyAll();
                        }
                    }
                } );

        if( Runtime.getRuntime().availableProcessors() < 2 )
        {   // if only one Processor Core, wait until GUI finishes constructing
            try
            {
                synchronized( JPortsUI.class )
                {
                    if( _THREADS_PROOF ) System.out.println( "TheApplication is waiting" );
                    while( TheUiHolder.isReady() == false )
                    {
                        JPortsUI.class.wait(); // *BLOCKS*
                    }
                    if( _THREADS_PROOF ) System.out.println( "TheApplication done waiting" );
                }
            }
            catch( InterruptedException ex )
            {}
        }

        // but start parsing "PortsIndex" in this thread
        if( _THREADS_PROOF ) System.out.println( "TheApplication begin constructing" );
        TheApplication.INSTANCE.probeUpdate(); // *BLOCKS*
        if( _THREADS_PROOF ) System.out.println( "TheApplication end constructing" );

        // enable table even if GUI reported not ready
        SwingUtilities.invokeLater( new Runnable() // anonymous class
                {   @Override public void run()
                    {   TheUiHolder.INSTANCE.goLive();
                    }
                } );
    }
}


// common libs needs a new project name
// zomg
//  * http://en.wikipedia.org/wiki/Chippewa
