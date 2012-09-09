package jport;

import java.awt.Color;
import java.io.File;
import jport.common.Providers_.ColorProvidable;
import jport.common.Providers_.TipProvidable;
import jport.common.Providers_.VisibilityProvidable;
import jport.common.SearchTerm2;
import jport.common.Util;
import jport.type.Portable;


/**
 * Constants specific to the JPortsUI application.
 *
 * @author sbaber
 */
public class PortsConstants
{
    /**
     * From CLI "port echo XXX".
     */
    static public enum EPortStatus implements TipProvidable, ColorProvidable
            { ALL         ( "all Ports from each Ports tree<BR>listed in the sources.conf file" )
            //
            , UNINSTALLED ( "Ports in the Ports trees that aren't installed", Color.DARK_GRAY )
            , INSTALLED   ( "set of all installed Ports", Color.GREEN )
            //
            , OUTDATED    ( "installed Ports that are out of date with<BR>respect to their current version/revision in the Ports trees", Color.RED )
            // Updated / New
            // ============
            //
            , ACTIVE      ( "set of installed and active Ports", Color.BLUE )
            , INACTIVE    ( "set of installed but inactive Ports", Color.MAGENTA )
            , ACTINACT    ( "set of installed Ports that have both an<BR>active version and one or more inactive versions", Color.CYAN.darker() )
            , REQUESTED   ( "installed Ports that were explicitly<BR>asked for", Color.GREEN.darker() )
            , UNREQUESTED ( "installed Ports that were installed only<BR>to satisfy dependencies", Color.BLUE.darker() )
            , LEAVES      ( "installed Ports that are unrequested and<BR>have no dependents", Color.GRAY )
            , OBSOLETE    ( "set of Ports that are installed but no<BR>longer exist in any Port trees", Color.YELLOW.darker() )
            // N/A -> Current     ( "the port in the current working directory" )
            ;
                    private EPortStatus( final String tip ) { this( tip, null ); }
                    private EPortStatus( final String tip, final Color color ) { fTip = tip; fColor = color; }
                    final private String fTip;
                    final private Color fColor;
                    @Override public String provideTipText() { return "<HTML>Show "+ fTip; }
                    @Override public Color provideColor() { return fColor; }
                    @Override public String toString() { return this.name().charAt( 0) + this.name().substring( 1 ).toLowerCase(); }

                    /** Avoid array allocation as Java does not have immutable []s */
                    static final public EPortStatus[] VALUES = EPortStatus.values();
            }

    /**
     * Has revocation logic with mutually exclusive stati.
     */
    static public enum EPortMark implements TipProvidable, VisibilityProvidable
            { Uninstall  ( EPortStatus.INSTALLED  , "uninstalled", "-p -u -c" ) // [u]ninstalls inactive ports ; [c]lean ; [p]roceed to next port on error
            , Deactivate ( EPortStatus.ACTIVE     , "deactivated", "-p" ) // [p]roceed to next port on error
            , Activate   ( EPortStatus.INACTIVE   , "activated"  , "-p" ) // [p]roceed to next port on error
            , Install    ( EPortStatus.UNINSTALLED, "installed"  , "-p" ) // [p]roceed to next port when a preceeding port has an error
            , Upgrade    ( EPortStatus.OUTDATED   , "upgraded"   , "-u" ) // [u]ninstalls inactive ports
            , Dependency_Upgrade  ( Upgrade )
            , Dependency_Install  ( Install )
            , Dependant_Uninstall ( Uninstall )
            , Dependency_Activate ( Activate )
            , Dependant_Deactivate( Deactivate )
            //
            , Conflicted( null )
            //? Clean
            ;
                    private EPortMark( EPortMark nonDepMark ) { this( nonDepMark, "", "", null ); } // <- these do not show in mark picker UI
                    private EPortMark( EPortStatus applicableStatus, String tip, String option ) { this( null, tip, option, applicableStatus ); }
                    private EPortMark( EPortMark nonDepMark, String tip, String option, EPortStatus applicableStatus )
                    {   fNonDepMark = nonDepMark;
                        fTip = tip;
                        fText = this.name().replace( '_', ' '  );
                        fOption = option;
                        fApplicableStatus = applicableStatus;
                    }
                    final private String fTip, fText, fOption;
                    final private EPortStatus fApplicableStatus;
                    final private EPortMark fNonDepMark;
                    public EPortMark         getNonDepMark() { return fNonDepMark; }
                    public boolean           isApplicable( final Portable port ) { return port.hasStatus( fApplicableStatus ); }
                    public String            getCliCommand() { return this.name().toLowerCase(); }
                    public String            getCliOption() { return fOption; }
                    @Override public String  provideTipText() { return "<HTML>Marks the selected Port to be<BR><B>"+ fTip +"</B> after Apply is clicked"; }
                    @Override public boolean provideIsVisible() { return fTip.isEmpty() == false; } // hacky
                    @Override public String  toString() { return fText; }

                    /** Avoid array allocation as Java does not have immutable []s */
                    static final public EPortMark[] VALUES = EPortMark.values();
            }

    /**
     * For Search JComboBox or Check boxes.
     */
    @SuppressWarnings("unchecked")
    static public enum ESearchWhere
            { Name         { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getName() ); } }
            , Descr_Name   { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getName() +' '+ port.getShortDescription() +' '+ port.getLongDescription() ); }
                             @Override public String toString() { return "Description & Name"; } }
            , Category     { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getCategories() ); } }
            , Maintainer   { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getMaintainers() ); } }
            , Dependencies { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getFullDependencies() ); } }
            , Licenses     { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getLicenses() ); } }
            , Variants     { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getVariants() ); } }
            ;
                    abstract public boolean doesMatch( final SearchTerm2<?> searchTerm, final Portable port );
            }


    static final public boolean    DEBUG         = false;
    static final public boolean    OPTIMIZATION  = true; // root of all evil -- D. Knuth
    static final public String     APP_NAME      = "JPortsUI";
    static final public String     VERSION       = "2012.08"; // or use self .JAR creation date
    static final public String     PORTS_PATH    = "/opt/local/var/macports/sources/rsync.macports.org/release/ports/"; // Mac only, may want to use `which port`
    static final public boolean    HAS_MAC_PORTS = Util.isOnMac() == true && new File( PORTS_PATH ).exists();
    static final public Portable[] NO_PORTS      = new Portable[ 0 ];
    static final public boolean    IS_SHOWING_FAVICON = true; // Java not so good with ".ico" image format and transparency, bummer!

    private PortsConstants() {}
}
