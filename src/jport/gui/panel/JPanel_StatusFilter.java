package jport.gui.panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jport.PortsConstants;
import jport.PortsConstants.EPortStatus;
import jport.TheApplication;
import jport.TheOsBinaries;
import jport.common.GuiUtil_;
import jport.common.Reset.Resetable;
import jport.gui.TheUiHolder;
import jport.type.Portable;
import jport.type.Portable.Predicatable;


/**
 * Filter by Port status.
 *
 * @author sbaber
 */
@SuppressWarnings("serial")
public class JPanel_StatusFilter extends JPanel
    implements 
          ActionListener
        , ChangeListener
{
    @Deprecated
    static private enum EDuration
            { SYNC     ( "Last Port Sync", -1L )
            , DAY      ( "Day"     , 1000 * 60 * 60 * 36L ) // day.5
            , WEEK_1   ( "Week"    , 1000 * 60 * 60 * 24 *  7L )
            , WEEKS_2  ( "2 Weeks" , 1000 * 60 * 60 * 24 * 14L )
            , MONTH_1  ( "Month"   , 1000 * 60 * 60 * 24 * 32L )
            , MONTHS_2 ( "2 Months", 1000 * 60 * 60 * 24 * 31L * 2 )
            , MONTHS_3 ( "3 Months", 1000 * 60 * 60 * 24 * 31L * 3 )
            ;
                    private EDuration( final String text, final long durationMillisec ) { fText = text; fDurationMillisec = durationMillisec; }
                    final private String fText;
                    final private long fDurationMillisec; // 'long' because month > 2 billion millisec
                    long getDurationMillisec() { return fDurationMillisec; }
                    @Override public String toString() { return fText; }
            }

    static final private int DEFAULT_WHATS_NEW_DAY = 2;

    final private ButtonGroup    fButtonGroup    = new ButtonGroup(); // needed for prefs
    final private AbstractButton ab_Marked       = new JToggleButton( "<HTML><FONT size=+0><I>Marked" );
    final private AbstractButton ab_Native       = new JToggleButton( "<HTML><I>Native" );
    final private AbstractButton ab_WhatsNew     = new JToggleButton( "<HTML><I>What's new?" );
    final private JComboBox      jCombo_Duration = new JComboBox( EDuration.values() );
    final private JSpinner       jSpin_Day       = new JSpinner( new SpinnerNumberModel( DEFAULT_WHATS_NEW_DAY, 1, 9999, 1 ) ); // val min max step

          private long mAgeMillisec =  ( 1000 * 60 * 60 * 24L * DEFAULT_WHATS_NEW_DAY );

    public JPanel_StatusFilter()
    {
        super( new BorderLayout() );

        this.setBorder( BorderFactory.createEmptyBorder( 10, GuiUtil_.GAP_PIXEL, GuiUtil_.GAP_PIXEL, 0 ) ); // t l b r
        this.setOpaque( false ); // otherwise messes up Mac-PLAF tab pit darkening // this.setBackground( null ) <- copying in the JTabbedPane's background didn't work to darken as expected on Mac-PLAF

        jSpin_Day  .setToolTipText( "<HTML>Show Ports that have had their<BR>information updated within <I>X</I> days" );
        ab_WhatsNew.setToolTipText( "<HTML>Show Ports that have had their<BR>information updated within <I>X</I> days" );
        ab_Native  .setToolTipText( "<HTML>Show set of binaries that are<BR>also provided by your OS" );
        ab_Marked  .setToolTipText( "<HTML>Show Ports that have a pending<BR>status change request mark" );

        final int BUTTON_HEIGHT_PIX = 34; // needs to be big enough so that the square button Mac-PLAF

        // needs a sub-panel, otherwise buttons will not horiz stretch in BoxLayout
        final JPanel northPanel = new JPanel( new GridLayout( 0, 1, 0, GuiUtil_.GAP_PIXEL ) );
        northPanel.setOpaque( false ); // otherwise messes up Mac-PLAF tab pit darkening

        final AbstractButton[] enumAbs = new AbstractButton[ EPortStatus.VALUES.length ]; // nulls

        int i = 0;
        for( final EPortStatus e : EPortStatus.VALUES )
        {
            final AbstractButton ab = new JToggleButton( e.toString() );
            ab.setActionCommand( e.name() );
            ab.setToolTipText( e.provideTipText() ); // built into enum
            ab.setForeground( ( e.provideColor() == null ) ? null : e.provideColor().darker() );
            ab.setFocusable( false );
            ab.setOpaque( false ); // otherwise messes up Mac-PLAF tab pit darkening // ab.setBackground( null ) <- not on Mac-PLAF

            fButtonGroup.add( ab );
            northPanel.add( ab );

            switch( e )
            {
                case ALL         : // fall-thru
                case UNINSTALLED : // fall-thru
                case INSTALLED   : // fall-thru
                case OUTDATED    :
                        ab.setText( "<HTML><FONT size=+0><B>"+ e.toString() );
                        break;
            }
            
            switch( e )
            {
                case ALL :
                        ab.setSelected( true );
                        break;

                case OBSOLETE :
                        northPanel.add( ab_Native );
                        break;
                    
                case OUTDATED : // case Obsolete :
                      northPanel.add( ab_Marked ); // 0=first, -1=last
                      northPanel.add( Box.createVerticalStrut( BUTTON_HEIGHT_PIX ) );
                        break; // spacer also controls height of all buttons
            }

            enumAbs[ i++ ] = ab;

            ab.addActionListener( this );
        }

        northPanel.add( Box.createVerticalStrut( BUTTON_HEIGHT_PIX ) );

        // sub
        final JPanel subPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 0, 0 ) );
        subPanel.setOpaque( false );
        subPanel.add( new JLabel( "<HTML><SMALL><RIGHT>Days Ago " ) );
        subPanel.add( jSpin_Day );

        // south
        final JPanel southPanel = new JPanel( new GridLayout( 0, 1, 0, GuiUtil_.GAP_PIXEL ) );
        southPanel.setOpaque( false ); // otherwise messes up Mac-PLAF tab pit darkening
        ab_WhatsNew.setEnabled( PortsConstants.HAS_MAC_PORTS );
        jCombo_Duration.setEnabled( false );
        jSpin_Day.setEnabled( false );

        ab_Marked.setFocusable( false );
        ab_Native.setFocusable( false );
        ab_WhatsNew.setFocusable( false );

        fButtonGroup.add( ab_Marked );
        fButtonGroup.add( ab_Native );
        fButtonGroup.add( ab_WhatsNew );
        
        southPanel.add( Box.createVerticalStrut( BUTTON_HEIGHT_PIX ) );
//        southPanel.add( ab_Marked ); // 0=first, -1=last
        southPanel.add( ab_WhatsNew );
//        southPanel.add( jCombo_Duration );
        southPanel.add( subPanel );

        this.add( northPanel, BorderLayout.NORTH );
        this.add( southPanel, BorderLayout.SOUTH );
        this.add( Box.createHorizontalStrut( 134 ), BorderLayout.CENTER ); // keep subPanel from wrapping

        // listener
        ab_Marked.addActionListener( this );
        ab_Native.addActionListener( this );
        ab_WhatsNew.addActionListener( this );
        jCombo_Duration.addActionListener( this );

        jSpin_Day.addChangeListener( this );

        final AbstractButton ab_All = enumAbs[ EPortStatus.ALL.ordinal() ];
        TheUiHolder.getResetNotifier().addListener( new Resetable() // anonymous class
                {   @Override public void reset()
                    {   ab_All.doClick();
                        jSpin_Day.setValue( DEFAULT_WHATS_NEW_DAY ); // autobox
//                        jCombo_Duration.setSelectedIndex( 0 );
                    }
                } );
    }

    @Override public void actionPerformed( ActionEvent e )
    {
        final Object obj = e.getSource();

        if( obj instanceof AbstractButton )
        {
            final AbstractButton ab = (AbstractButton)obj;
            
            final Predicatable predicate; // para-lambda
            if( ab == ab_WhatsNew )
            {
                jCombo_Duration.setEnabled( true );
                jSpin_Day.setEnabled( true );
                setEpochFilter( mAgeMillisec );
                return; // required
            }
            else if( ab == ab_Native )
            {
                predicate = new Predicatable() // anonymous class
                        {   @Override public boolean evaluate( final Portable port )
                            {   return TheOsBinaries.INSTANCE.has( port.getName() );
                            }
                        };
            }
            else if( ab == ab_Marked )
            {
                predicate = new Predicatable() // anonymous class
                        {   @Override public boolean evaluate( final Portable port )
                            {   return port.isUnmarked() == false;
                            }
                        };
            }
            else
            {
                final EPortStatus statusEnum = EPortStatus.valueOf( ab.getActionCommand() );
                switch( statusEnum )
                {
                    case ALL :
                            predicate = Predicatable.ANY;
                            break;

                    default :
                            predicate = new Predicatable() // anonymous class
                                    {   @Override public boolean evaluate( final Portable port )
                                        {   return port.hasStatus( statusEnum );
                                        }
                                    };
                            break;
                }
            }

            jCombo_Duration.setEnabled( false );
            jSpin_Day.setEnabled( false );
            
            TheUiHolder.INSTANCE.getPortFilterPredicate().setStatusFilter( predicate );
        }
        else if( obj instanceof JComboBox )
        {
            if( ab_WhatsNew.isSelected() == true )
            {
                setEpochFilter( (EDuration)jCombo_Duration.getSelectedItem() );
            }
        }
    }

    @Override public void stateChanged( ChangeEvent e )
    {
        if( e.getSource() == jSpin_Day )
        {
            if( ab_WhatsNew.isSelected() == true )
            {   // check because Reset button was accidentally activating filter
                final int days = (Integer)jSpin_Day.getValue(); // autobox
                setEpochFilter( 1000 * 60 * 60 * 24L * days );
            }
        }
    }

    private void setEpochFilter( final long ageMillisec )
    {
        mAgeMillisec = ageMillisec;
        final long epochAfterMillisec = System.currentTimeMillis() - ageMillisec;

        // para-lambda
        final Predicatable predicate = new Predicatable() // anonymous class
                    {   @Override public boolean evaluate( final Portable port )
                        {   return port.getModificationEpoch() >= epochAfterMillisec;
                        }
                    };
        TheUiHolder.INSTANCE.getPortFilterPredicate().setStatusFilter( predicate );
    }

    @Deprecated
    private void setEpochFilter( final EDuration duration )
    {
        final long epochAfterMillisec = ( duration == EDuration.SYNC )
                ? System.currentTimeMillis() + TheApplication.INSTANCE.getPortsCatalog().getLastSyncEpoch() // ok
                : duration.getDurationMillisec();
        setEpochFilter( epochAfterMillisec );
    }
}
