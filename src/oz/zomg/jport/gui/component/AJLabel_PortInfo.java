package oz.zomg.jport.gui.component;

import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import oz.zomg.jport.TheApplication;
import oz.zomg.jport.common.Elemental;
import oz.zomg.jport.common.Elemental.EElemental;
import oz.zomg.jport.common.StringsUtil_;
import oz.zomg.jport.common.Util;
import oz.zomg.jport.ports.PortsCliUtil;
import oz.zomg.jport.type.EPortStatus;
import oz.zomg.jport.type.Portable;


/**
 * Port information for embedding in JTab_Details.
 *
 * @author sbaber
 */
@SuppressWarnings("serial")
public abstract class AJLabel_PortInfo extends JLabel
    implements Elemental.Listenable<Portable>
{
    static final public String SELECT_PORT_TEXT = "<HTML><BIG><I>Select a port from the above list";

    static
    {}

    final private boolean fIsAssignmentLocked;

    /** Mutable for .actionPerformed() and follows table selection via .notify().  Must begin with 'null' */
    transient private Portable mAssignedPort = null;

    /**
     *
     * @param assignedPort Portable.NONE for follows selection
     */
    AJLabel_PortInfo( final Portable assignedPort )
    {
        if( assignedPort == null ) throw new NullPointerException();

        fIsAssignmentLocked = assignedPort != Portable.NONE;

        this.setOpaque( false );
        this.setVerticalAlignment( SwingConstants.TOP );
        this.setHorizontalAlignment( SwingConstants.LEFT );

        // listener
        TheApplication.INSTANCE.getCrudNotifier().addListener( this ); // automatically calls .notify() and updates mAssignedPort conforming the view
    }

    /**
     * Previously listens only if driven by main table selections.
     * Now can monitor any change.
     *
     * @param elemental action
     * @param port of marking view
     */
    @Override final public void notify( final EElemental elemental, final Portable port )
    {
        switch( elemental )
        {
            case RETRIEVED :
                {   if( fIsAssignmentLocked == false || mAssignedPort == null )
                    {
                        mAssignedPort = port;
                        setPort( port );
                    }
                }   break;

            case UPDATED :
                {   if( mAssignedPort == port )
                    {   // filtered out non-related updates
                        setPort( port ); break; // something may have changed
                    }
                }   break;
        }
    }

    /**
     * Convert to multi-line HTML.
     *
     * @param lines
     */
    private void setText( final String[] lines )
    {
        setText( false, lines );
    }

    /**
     *
     * @param needTable if 'true' then each line is a table row, still needs table data tags though
     * @param lines
     */
    private void setText( final boolean needTable, final String[] lines )
    {
        final StringBuilder sb = new StringBuilder( "<HTML>"+ ( needTable == true ? "<TABLE>" : "" ) );

        for( final String line : lines )
        {
            sb.append( needTable == true ? "<TR>" : "" );
            sb.append( line );
            sb.append( needTable == true ? "</TR>" : "<BR>" );
        }

        this.setText( sb.toString() );
    }

    /**
     * @param port conforming the label view
     */
    abstract void setPort( Portable port );


    // ================================================================================
    /**
     * Debug view of port private, etc. fields
     */
    static class JLabel_PortFields extends AJLabel_PortInfo
    {
        JLabel_PortFields( final Portable port )
        {
            super( port );
        }

        @Override void setPort( final Portable port )
        {
            if( port == Portable.NONE )
            {
                setText( SELECT_PORT_TEXT );
            }
            else
            {
                final String text = StringsUtil_.toHtmlTable( false, Util.dumpFields( port, false ) );
                setText( text );
            }
        }
    }


    // ================================================================================
    /**
     * Short and long description of the port.
     */
    static class JLabel_Description extends AJLabel_PortInfo
    {
        JLabel_Description( final Portable port )
        {
            super( port );

            this.setHorizontalAlignment( SwingConstants.CENTER );
            this.setBorder( BorderFactory.createEmptyBorder( 10, 20, 0, 20 ) ); // T L B R
//            this.setHorizontalTextPosition( JLabel.RIGHT );
//            this.setVerticalTextPosition( JLabel.TOP );
        }

        @Override void setPort( final Portable port )
        {
            if( port == Portable.NONE )
            {
                this.setText( SELECT_PORT_TEXT );
            }
            else
            {
                final String text = "<HTML><CENTER><BIG><B>"+ port.getName() +"</B></BIG><BR><BR>"
                        +"<B><I> "+ port.getShortDescription() +"</I></B></CENTER><BR><BR>"
                        + ( ( port.getShortDescription().equals( port.getLongDescription() ) == false ) ? port.getLongDescription() : "" ); // to show, short != long
                this.setText( text );
            }
        }
    }


    // ================================================================================
    /**
     * This port depends on what other ports.
     */
    static class JLabel_Dependencies extends AJLabel_PortInfo
    {
        JLabel_Dependencies( final Portable port )
        {
            super( port );
        }

        @Override void setPort( final Portable port )
        {
            int count = 0;
            if( port == Portable.NONE )
            {
                setText( SELECT_PORT_TEXT );
            }
            else
            {
                final Portable[] ports = port.getFullDependencies();
                final String[] names = StringsUtil_.toStrings( ports );

                for( int i = 0; i < ports.length; i++ )
                {
                    final String description = "</TD><TD><FONT color=gray>"+ ports[ i ].getShortDescription() +"</FONT></TD>";

                    if( ports[ i ].hasStatus( EPortStatus.ACTIVE ) == true )
                    {   // color Active ports green
                        names[ i ] = "<TD> <FONT color=green><B>"+ names[ i ] +" </B></FONT>"+ description; // needs prepended space for sort
                    }
                    else
                    {   // not installed
                        names[ i ] = "<TD>"+ names[ i ] + description;
                    }
                }

                Arrays.sort( names ); // moves Actives <FONT.. to the top
                super.setText( true, names ); // table

                count = ports.length;
            }

            this.setToolTipText( ( count == 0 ) ? null : "<HTML><B>"+ count +"</B> dependencies" );
        }
    }


    // ================================================================================
    /**
     * This ports is depended upon by these other ports.
     */
    static class JLabel_Dependants extends AJLabel_PortInfo
    {
        JLabel_Dependants( final Portable port )
        {
            super( port );
        }

        @Override void setPort( final Portable port )
        {
            int count = 0;
            if( port == Portable.NONE )
            {
                setText( SELECT_PORT_TEXT );
            }
            else
            {
                final Portable[] ports = port.getDependants();
                final String[] names = StringsUtil_.toStrings( ports );

                for( int i = 0; i < ports.length; i++ )
                {
                    final String description = "</TD><TD><FONT color=gray>"+ ports[ i ].getShortDescription() +"</FONT></TD>";

                    if( ports[ i ].hasStatus( EPortStatus.ACTIVE ) == true )
                    {   // color Active ports red
                        names[ i ] = "<TD> <FONT color=red><B>"+ names[ i ] +" </B></FONT>"+ description; // needs prepended space for sort
                    }
                    else
                    {   // not installed
                        names[ i ] = "<TD>"+ names[ i ] + description;
                    }
                }

                Arrays.sort( names ); // moves Actives <FONT.. to the top
                super.setText( true, names ); // table

                count = ports.length;
            }

            this.setToolTipText( ( count == 0 ) ? null : "<HTML><B>"+ count +"</B> Ports dependant on <B>"+ port.getName() );
        }
    }


    // ================================================================================
    /**
     * Ports CLI reports that the following "/opt/..." files were installed by this port.
     */
    static class JLabel_FilesInstalled extends AJLabel_PortInfo
    {
        JLabel_FilesInstalled( final Portable port )
        {
            super( port );
        }

        @Override void setPort( final Portable port )
        {
            if( port == Portable.NONE )
            {
                setText( SELECT_PORT_TEXT );
            }
            else
            {
                super.setText( PortsCliUtil.cliFileContents( port ) );
            }
        }
    }


    // ================================================================================
    /**
     * Other versions of the port still installed and available in the user's "/opt/..."
     */
    static class JLabel_Versioning extends AJLabel_PortInfo
    {
// via port echo $PORT actinact ?
        JLabel_Versioning( final Portable port )
        {
            super( port );
        }

        @Override void setPort( final Portable port )
        {
            if( port == Portable.NONE )
            {
                setText( SELECT_PORT_TEXT );
            }
            else
            {
    //...                 Arrays.sort( ports );
            }
        }
    }
}
