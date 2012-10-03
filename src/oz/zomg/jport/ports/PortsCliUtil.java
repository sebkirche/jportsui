package oz.zomg.jport.ports;

import static oz.zomg.jport.common.CliUtil.BASH_OPT_C;
import static oz.zomg.jport.common.CliUtil.UNIX_BIN_BASH;
//
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import oz.zomg.jport.PortConstants;
import oz.zomg.jport.TheApplication;
import oz.zomg.jport.common.CliUtil;
import oz.zomg.jport.common.StringsUtil_;
import oz.zomg.jport.common.Util;
import oz.zomg.jport.type.EPortMark;
import oz.zomg.jport.type.EPortStatus;
import oz.zomg.jport.type.CliPortInfo;
import oz.zomg.jport.type.Portable;


/**
 * Actual Port status from CLI.
 * Ex. root CLI -> /bin/bash -c echo "*password*" | sudo -S ls -shakl /Library ;
 *
 * @author <SMALL>Copyright 2012 by Stephen Baber
 * &nbsp; <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US">
 * <img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-nc-nd/3.0/80x15.png" /></a><br />
 * This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US">
 * Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License</a>.</SMALL>
 */
public class PortsCliUtil
{
    static private enum ECmd
            { VERSION // cli ports version
            , SYNC       ( true ) // update ports tree
            , SELFUPDATE ( true ) // update macPorts software and ports tree
            , ECHO // print
            , CONTENTS // files installed by the port
            , CLEAN // removes distribution, working, and/or log files
            ;
                    private ECmd() { this( false ); }
                    private ECmd( final boolean needAdmin ) { fNeedAdmin = needAdmin; }
                    final private boolean fNeedAdmin;
                    String _() { return this.name().toLowerCase(); }
            }

    /** Non-running thread for Windows. */
    static final private Thread DEAD_THREAD = new Thread( new Runnable() { @Override public void run() {} }, "NO_RUN_THREAD" );

    static final private String _PORT_BIN_PATH = "/opt/local/bin/port"; //... this is non-portable, use "which port" command instead
    static final public boolean HAS_PORT_CLI = new File( _PORT_BIN_PATH ).exists();

    /** Note: Will be incorrect after the "port selfupdate" that actually gets a new version of the Port CLI tool. */
    static final public String PORT_CLI_VERSION = PortsCliUtil.cliPortVersion();

    /** For a non-Ports environment, needs to be installed. */
    static final private int _NO_PORT_DELAY_MILLISEC = 350;

    static
    {}

    private PortsCliUtil() {}

    static private String _first( final String[] lines )
    {
        return ( lines.length != 0 ) ? lines[ 0 ] : "";
    }

    static public Thread cliTest( final CliUtil.Listener listener )
    {
        return ( HAS_PORT_CLI )
                ? CliUtil.forkCommand( listener, "list", "installed" ) // "locate Portfile" ~= 4,500, "locate perl" ~= 6,200 "locate java" ~= 54,000 "locate /" ~850,000
                : CliUtil.forkCommand( listener, "ping", "-n ", "4", "localhost" ); // waits a sec on Windows
    }

    static public String cliPortVersion()
    {
        if( HAS_PORT_CLI == false ) { Util.sleep( _NO_PORT_DELAY_MILLISEC ); return "NOT AVAILABLE"; }

        return _first( CliUtil.executeCommand( _PORT_BIN_PATH, ECmd.VERSION._() ) );
    }

//... UNTESTED, needs root/admin to work, maybe sudoer file change walkthrough?
//... or run "sudo java -jar ..." with from CLI script asks user automagically?
        // "bash -c port" puts .exec() into port's interactive mode.
//... pass-in users Admin password, might need some sort of script or see 'man expect'

    /**
     *
     * @param port
     * @return files that were installed by the port
     */
    static synchronized public String[] cliFileContents( final Portable port )
    {
        if( HAS_PORT_CLI == false ) { Util.sleep( _NO_PORT_DELAY_MILLISEC ); return StringsUtil_.NO_STRINGS; }

        return CliUtil.executeCommand( _PORT_BIN_PATH, ECmd.CONTENTS._(), port.getName() );
    }

    /**
     * Requests package info from the Ports CLI. Ex.
     * <code> git-core @1.7.12.1_0+credential_osxkeychain+doc+pcre+python27 </code>
     *
     * @param statusEnum type of port pseudo-name, version and variant information to echo
     * @return as reported by the CLI
     */
    static synchronized public Set<CliPortInfo> cliEcho( final EPortStatus statusEnum )
    {
        if( HAS_PORT_CLI == false ) { Util.sleep( _NO_PORT_DELAY_MILLISEC ); return Collections.emptySet(); }

        final Set<CliPortInfo> set = new HashSet<CliPortInfo>();

        final String portStatus = statusEnum.name().toLowerCase(); // a psuedo-name
        final String[] lines = CliUtil.executeCommand( _PORT_BIN_PATH, ECmd.ECHO._(), portStatus );

        for( final String untrimmedLine : lines )
        {   // CLI reported information
            final String line = untrimmedLine.trim(); // required

            final int versionStart  = line.indexOf( '@' ); // installed version
            final int revisionStart = line.indexOf( '_', versionStart ); // installed revision
            final int variantStart  = line.indexOf( '+', revisionStart ); // installed variants, '+' not present if NO variants installed

            final String cliPortName = line.substring( 0, versionStart ).trim();

            // extract installed version after '@' but before '_'
            final String cliVersion = line.substring( versionStart + 1, revisionStart );

            // extract installed revision number after '_' but before '\n' or '+'
            final String cliRevision = ( variantStart != Util.INVALID_INDEX )
                    ? line.substring( revisionStart + 1, variantStart )
                    : line.substring( revisionStart + 1 );

            // extract insalled variants after '+'
            final String multiVariant = ( variantStart != Util.INVALID_INDEX )
                    ? line.substring( variantStart + 1 )
                    : "";

            final String[] variantSplits = multiVariant.split( "[+]" ); // on literal '+'
            final String[] cliVariants;
            if( variantSplits.length == 0 || variantSplits[ 0 ].isEmpty() == true )
            {   // no variants
                cliVariants = StringsUtil_.NO_STRINGS;
            }
            else
            {   // has installed variants
                cliVariants = variantSplits;
                Arrays.sort( cliVariants ); // must sort for .deepEquals()
            }

            final CliPortInfo cpi = new CliPortInfo
                    ( cliPortName.intern()
                    , cliVersion.intern()
                    , cliRevision.intern()
                    , cliVariants
                    );
            set.add( cpi );
        }

        if( PortConstants.DEBUG ) { System.out.println( statusEnum.name() +'='+ set.size() ); }

        return set;
    }

    /**
     * Creates the command line argument representing all the Port status change request marks.
     *
     * @param <S>
     * @param isSimulated 'true' for dry-run testing
     * @param map
     * @return
     */
    static public <S extends Set<? extends Portable>> String getApplyMarksCli( final boolean isSimulated, final Map<EPortMark,S> map )
    {
        final StringBuilder sb = new StringBuilder();
        for( final Map.Entry<EPortMark,S> entry : map.entrySet() )
        {
            final EPortMark portMark = entry.getKey(); // alias
            final S portSet = entry.getValue(); // alias

            if( portMark.provideIsVisible() == true && portSet.isEmpty() == false )
            {
                final String DRY_RUN =  "-y -v -t "; // Dr[y] Run, [v]erbose, [t]est deps
                sb  .append( "sudo port" )
                    .append( ' ' )
                    .append( portMark.getCliOption() )
                    .append( ' ' )
                    .append( ( isSimulated == true ) ? DRY_RUN : "" )
                    .append( portMark.getCliCommand() );

                for( final Portable port : portSet )
                {   // prefixes a space and quotes are required to disambiguate from the "sudo" port itself
                    final String nameVariant = TheApplication.INSTANCE.getPortsCatalog().getPortsVariants().getNameVariant( port );
                    sb.append( ' ' ).append( '\"' ).append( nameVariant ).append( '\"' );
                }

                sb.append( " ; " );
            }
        }

        return sb.toString();
    }

    /**
     * Let CLI "-ru" option resolve any Activate, Install, Upgrade deps according to the CLI port tool.
     *
     * @param <S>
     * @param map the user's marked intentions
     * @param isSimulated 'true' for dry-run test
     * @param listener
     * @param password
     * @return 'null' if no ports
     */
    static synchronized public <S extends Set<? extends Portable>> Thread cliApplyMarks
            ( final String           password
            , final boolean          isSimulated
            , final Map<EPortMark,S> map
            , final CliUtil.Listener listener
            )
    {
        if( HAS_PORT_CLI == false ) { Util.sleep( _NO_PORT_DELAY_MILLISEC ); return DEAD_THREAD; }

        final String cliCmd = getApplyMarksCli( isSimulated, map );
        final String bashIt = cliCmd.replace( "sudo port", "echo \""+ password +"\" | sudo -S "+ _PORT_BIN_PATH );
        return CliUtil.forkCommand( listener, UNIX_BIN_BASH, BASH_OPT_C, bashIt );
    }

    /**
     * Updates the MacPorts CLI software itself, and performs a Port tree rsync as a side-effect.
     *
     * @param password
     * @param listener
     * @return 'null' if no ports
     */
    static synchronized public Thread cliUpdateMacPortsItself( final String password, final CliUtil.Listener listener )
    {
        if( HAS_PORT_CLI == false ) { Util.sleep( _NO_PORT_DELAY_MILLISEC ); return DEAD_THREAD; }

        final String portCmd = "-v "+ ECmd.SELFUPDATE._();
        final String bashIt = "echo \""+ password +"\" | sudo -S "+ _PORT_BIN_PATH +' '+ portCmd + " ; ";
        return CliUtil.forkCommand( listener, UNIX_BIN_BASH, BASH_OPT_C, bashIt ); // ok!
    }

    /**
     * Port bin will call rsync to update the port tree and updates the PortIndex file.
     *
     * @param listener
     * @param password
     * @return 'null' if no ports
     */
    static synchronized public Thread cliSyncUpdate( final String password, final CliUtil.Listener listener )
    {
        if( HAS_PORT_CLI == false ) { Util.sleep( _NO_PORT_DELAY_MILLISEC ); return DEAD_THREAD; }

        final String portCmd = "-v "+ ECmd.SYNC._();
        final String bashIt = "echo \""+ password +"\" | sudo -S "+ _PORT_BIN_PATH +' '+ portCmd + " ; ";
        return CliUtil.forkCommand( listener, UNIX_BIN_BASH, BASH_OPT_C, bashIt ); // ok!
    }

    /**
     * Cleans all installed Ports of distribution files, working files, and logs.
     * Was supposed to Removes all inactive Ports also.
     *
     * @param password
     * @param listener
     * @return 'null' if no ports
     */
    static synchronized public Thread cliCleanInstalled( final String password, final CliUtil.Listener listener )
    {
        if( HAS_PORT_CLI == false ) { Util.sleep( _NO_PORT_DELAY_MILLISEC ); return DEAD_THREAD; }

        final String portCmd = "-u -p "+ ECmd.CLEAN._() +" --all "+ EPortStatus.INSTALLED.name().toLowerCase();
        final String bashIt = "echo \""+ password +"\" | sudo -S "+ _PORT_BIN_PATH +' '+ portCmd +" ; ";
        return CliUtil.forkCommand( listener, UNIX_BIN_BASH, BASH_OPT_C, bashIt );
    }

    static public void main(String... args)
    {
        final Set<CliPortInfo> set = cliEcho( EPortStatus.INSTALLED );
        for( final CliPortInfo cpi : set )
        {
            System.out.println( cpi );
            if( cpi.getVariants().length > 0 )
            {
                System.out.println( "\t variants="+ Arrays.toString( cpi.getVariants() ) );
            }
        }
    }
}
