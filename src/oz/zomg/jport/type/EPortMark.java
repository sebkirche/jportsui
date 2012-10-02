package oz.zomg.jport.type;

import oz.zomg.jport.common.Providers_.TipProvidable;
import oz.zomg.jport.common.Providers_.VisibilityProvidable;


/**
 * Enum's <code>.isApplicable()</code> uses revocation logic with mutually exclusive stati.
 *<P>
 * <code>port</code> CLI options are generally
 * <UL>
 * <LI> <code>[-p]</code> Proceed to next port when a preceeding port has an error
 * <LI> <code>[-u]</code> Uninstalls inactive ports
 * <LI> <code>[-c]</code> Clean
 * </UL>
 */
public enum EPortMark implements TipProvidable, VisibilityProvidable
        { Uninstall  ( EPortStatus.INSTALLED  , "uninstalled", "-p -u -c" ) // [p]roceed to next port on error ; [u]ninstalls inactive ports ; [c]lean
        , Deactivate ( EPortStatus.ACTIVE     , "deactivated", "-p" )       // [p]roceed to next port on error
        , Activate   ( EPortStatus.INACTIVE   , "activated"  , "-p" )       // [p]roceed to next port on error
        , Install    ( EPortStatus.UNINSTALLED, "installed"  , "-p -u" )    // [p]roceed to next port on error ; [u]ninstalls inactive ports ;
        , Upgrade    ( EPortStatus.OUTDATED   , "upgraded"   , "-p -u" )    // [p]roceed to next port on error ; [u]ninstalls inactive ports ;
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
                //
                final private String fTip, fText, fOption;
                final private EPortStatus fApplicableStatus;
                final private EPortMark fNonDepMark;
                public EPortMark         getNonDepMark() { return fNonDepMark; }
                public String            getCliCommand() { return this.name().toLowerCase(); }
                public String            getCliOption() { return fOption; }
                @Override public String  provideTipText() { return "<HTML>Marks the selected Port to be<BR><B>"+ fTip +"</B> after Apply is clicked"; }
                @Override public boolean provideIsVisible() { return fTip.isEmpty() == false; } // hacky
                @Override public String  toString() { return fText; }
                //
                public boolean           isApplicable( final Portable port ) { return port.hasStatus( fApplicableStatus ); }

                /** Avoid array allocation as Java does not have immutable []s */
                static final public EPortMark[] VALUES = EPortMark.values();
        }
