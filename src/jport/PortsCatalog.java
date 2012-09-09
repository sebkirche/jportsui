package jport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jport.common.Util;
import jport.gui.TheUiHolder;
import jport.type.PortFactory;
import jport.type.Portable;


/**
 * Reads all from the "PortIndex" file and then status is updated by the Port CLI tool.
 *
 * @author sbaber
 */
public class PortsCatalog
{
    static final private String _PORTS_FILE_NAME = "PortIndex";

    static final PortsCatalog NONE = new PortsCatalog( false );

    static
    {}

    /** Time of "PortIndex" file parse. */
    final private long fParse_EpochTimeMillisec = System.currentTimeMillis();

    /** Key is the case-insensitive port name. */
    final private Map<String,Portable> fCiName_to_PortMap;

    /** All values in alphabetical order. */
    final private Portable[] fAllPorts;

    final private PortsDep fPortsDep = new PortsDep( this );

    final private PortsVariants fPortsVariants = new PortsVariants();

    volatile private PortsDate vPortsDate = null;

    /**
     * For initial NONE catalog.
     *
     * @param ignore code smell
     */
    private PortsCatalog( final boolean ignore )
    {
        fCiName_to_PortMap = Collections.emptyMap();
        fAllPorts = PortsConstants.NO_PORTS;
    }

    /**
     * Read from the Mac's "PortIndex" file.
     */
    PortsCatalog()
    {
        this( PortsConstants.PORTS_PATH + _PORTS_FILE_NAME );
    }

    private PortsCatalog( final String filePathName )
    {
        final Map<String,Portable> map = new HashMap<String,Portable>( 20000 ); // normally ~15K

        try
        {
            final File filePath = ( PortsConstants.HAS_MAC_PORTS == true )
                    ? new File( filePathName )
                    : new File( _PORTS_FILE_NAME ); // fall back to project folder for dev work

            if( filePath.exists() == false )
            {   // PortIndex file not found
                JOptionPane.showMessageDialog( null, filePathName +"\n does not seem to exist." );
                System.exit( 1 );
            }

            if( PortsConstants.OPTIMIZATION )
            {   // scanner uses regex, this is 2x faster
                try
                {
                    final byte[] bytes = Util.retreiveFileBytes( filePath ); // assumes UTF-8 encoding when constructing the String from bytes?
                    int p = 0;
                    int q = 1;
                    while( q < bytes.length )
                    {   // get port name and info length line indexes
                        while( bytes[ q ] != '\n' && q < bytes.length )
                        {
                            q++;
                        }

                        // extract info length
                        int size = 0;
                        int magnitude = 1;
                        int r = q - 1;
                        do
                        {   // extract positive size
                            final int digit = bytes[ r ] - (byte)'0';
                            size += digit * magnitude;
                            magnitude *= 10;
                            r--;
                        }
                        while( bytes[ r ] != 0x20 ); // space

                        // offset from that short line
                        // final String shortLine = new String( bytes, p, q - p );
                        p = q + 1;
                        q = p + 1;

                        // jump ahead by the info length
                        q += size;
                        q -= 2;

                        // wait...what, sometimes the info length is too short!?
                        while( bytes[ q ] != '\n' && q < bytes.length )
                        {
                            q++;
                        }

                        // longer info line
                        final String line = new String( bytes, p, q - p );
                        p = q + 1;
                        q = p + 1;

                        final Portable port = PortFactory.createFromPortIndexFile( line );
                        if( port != Portable.NONE )
                        {   // parsed ok
                            final String ci_portName = port.getCaseInsensitiveName();

                            // no name collisions occur, this means we only get the lastest version from the file
                            if( false && map.containsKey( ci_portName ) ) { System.out.println( port ); }

                            map.put( ci_portName, port );
                        }
                    }
                }
                catch( IOException ex )
                {
                    ex.printStackTrace();
                }
            }
            else
            {   // 2x slower but 10x more maintainable
                // misses the case where the port info crosses multiple lines
                final Scanner scanner = new Scanner( filePath, "UTF-8" ); // *THROWS* FileNotFoundException
                while( scanner.hasNext() == true ) // scan.useDelimiter( "\\n" ) <- this regex works but is not needed
                {   // Scanner default is to read file line-by-line
                    final String line = scanner.nextLine(); // some lines are empty ""
                    if( line.length() > 40 )
                    {
                        final Portable port = PortFactory.createFromPortIndexFile( line );
                        if( port != Portable.NONE )
                        {   // parsed ok
                            final String ci_portName = port.getCaseInsensitiveName();

                            // no name collisions occur, this means we only get the lastest version from the file
                            if( false && map.containsKey( ci_portName ) ) { System.out.println( port ); }

                            map.put( ci_portName, port );
                        }
                    }
                    else
                    {   // wrong, just junk '\n' inside a {} Needs to keep going
                        // System.out.println( line );
                    }
                }
                scanner.close();
            }
        }
        catch( FileNotFoundException ex )
        {
            Logger.getLogger( TheUiHolder.class.getName() ).log( Level.SEVERE, null, ex );
        }

        fCiName_to_PortMap = map;

        // values to array and sort by name and version
        final Collection<Portable> setCollection = map.values();
        fAllPorts = setCollection.toArray( new Portable[ setCollection.size() ] );
        Arrays.sort( fAllPorts );
    }

    public PortsDep getDeps() { return fPortsDep; }

    public PortsVariants getPortsVariants() { return fPortsVariants; }

    /**
     * Look up a port by name.
     *
     * @param portName case-insensitive
     * @return Portable.NONE if not found, there are a couple dozen of these
     */
    public Portable parse( final String portName )
    {
        final Portable port = fCiName_to_PortMap.get( portName.toLowerCase() );
        return ( port != null )
                ? port
                : Portable.NONE;
    }

    /**
     * Compares old port entries to freshly CLI interrogated ports.
     * Called after "port echo $PSEUDO_NAME".
     *
     * @param fromCliChangeSet updated information from CLI is a CliPort
     */
    synchronized Set<Portable> inform( final Set<Portable> fromCliChangeSet )
    {
        for( final Portable cliPort : fromCliChangeSet )
        {
            if( PortsConstants.DEBUG ) System.out.println( PortsCatalog.class.getName() +".inform("+  cliPort +')' );

            final Portable prevPort = fCiName_to_PortMap.get( cliPort.getCaseInsensitiveName() );
            fCiName_to_PortMap.put( cliPort.getCaseInsensitiveName(), cliPort );

            final int i = Util.indexOfIdentity( prevPort, fAllPorts );
            if( i != Util.INVALID_INDEX )
            {   // valid index
                fAllPorts[ i ] = cliPort;
                //... port changed event for detail views?
            }
        }

        return fromCliChangeSet;
    }

    /**
     *
     * @return in alphabetical order, all ports described in the "PortIndex" file
     */
    synchronized public Portable[] getAllPorts() { return fAllPorts; }

    public long getModificationEpoch( final Portable port )
    {
        return ( vPortsDate != null )
                ? vPortsDate.getModificationEpoch( port )
                : -1L;
    }

    /**
     * Lengthy operation.
     */
    void scanDates()
    {
        vPortsDate = new PortsDate( getAllPorts() );
    }

    public long getLastSyncEpoch()
    {
        if( vPortsDate != null )
        {
            return vPortsDate.getLastSyncEpoch();
        }
        else
        {
            return -1L;
        }
    }

    /**
     * TESTING
     * 
     * @param args
     */
    static public void main( String[] args )
    {
        // test
        final long startMillisec = System.currentTimeMillis();
        final PortsCatalog portsCatalog = new PortsCatalog();
//        final Portable[] dependPorts = portsCatalog.getDeps().getFullDependenciesOf( portsCatalog.parse( "graphviz" ) );
        // OR -> portsCatalog.parse( "graphviz" ).buildFullDependencies()
        System.out.println( PortsCatalog.class.getSimpleName() +".main() ms="+ ( System.currentTimeMillis() - startMillisec ) );
  //      System.out.println( dependPorts.length +"="+ Arrays.toString( dependPorts ) );
    }
}



// "graphviz" -> 87=[apr, apr-util, autoconf, automake, bzip2, cairo, cmake, curl-ca-bundle, cyrus-sasl2, db46, expat, fontconfig, freetype, gd2, gdbm, gettext, ghostscript, glib2, gobject-introspection, gperf, groff, gts, help2man, jasper, jbig2dec, jbigkit, jpeg, kerberos5, lcms2, libedit, libffi, libiconv, libidn, libLASi, libpaper, libpixman, libpng, libtool, libxml2, m4, ncurses, neon, netpbm, openssl, p5.12-locale-gettext, pango, perl5, perl5.12, pkgconfig, psutils, python27, python_select, serf1, sqlite3, subversion, texinfo, tiff, unzip, urw-fonts, Xft2, xorg-bigreqsproto, xorg-inputproto, xorg-kbproto, xorg-libice, xorg-libpthread-stubs, xorg-libsm, xorg-libX11, xorg-libXau, xorg-libXaw, xorg-libxcb, xorg-libXdmcp, xorg-libXext, xorg-libXmu, xorg-libXt, xorg-renderproto, xorg-util-macros, xorg-xcb-proto, xorg-xcb-util, xorg-xcmiscproto, xorg-xextproto, xorg-xf86bigfontproto, xorg-xproto, xorg-xtrans, xpm, xrender, xz, zlib]
// missing?
//
// CLI -> port echo rdepof:graphviz | wc -l
// 87
//    apr
//    apr-util
//    autoconf
//    automake
//    bzip2
//    cairo
//    cmake
//    curl-ca-bundle
//    cyrus-sasl2
//    db46
//    expat
//    fontconfig
//    freetype
//    gd2
//    gdbm
//    gettext
//    ghostscript
//    glib2
//    gobject-introspection
//    gperf
//    groff
//    gts
//    help2man
//    jasper
//    jbig2dec
//    jbigkit
//    jpeg
//    kerberos5
//    lcms2
//    libedit
//    libffi
//    libiconv
//    libidn
//    libLASi
//    libpaper
//    libpixman
//    libpng
//    libtool
//    libxml2
//    m4
//    ncurses
//    neon
//    netpbm
//    openssl
//    p5.12-locale-gettext
//    pango
//    perl5
//    perl5.12
//    pkgconfig
//    psutils
//    python27
//    python_select
//    serf1
//    sqlite3
//    subversion
//    texinfo
//    tiff
//    unzip
//    urw-fonts
//    Xft2
//    xorg-bigreqsproto
//    xorg-inputproto
//    xorg-kbproto
//    xorg-libice
//    xorg-libpthread-stubs
//    xorg-libsm
//    xorg-libX11
//    xorg-libXau
//    xorg-libXaw
//    xorg-libxcb
//    xorg-libXdmcp
//    xorg-libXext
//    xorg-libXmu
//    xorg-libXt
//    xorg-renderproto
//    xorg-util-macros
//    xorg-xcb-proto
//    xorg-xcb-util
//    xorg-xcmiscproto
//    xorg-xextproto
//    xorg-xf86bigfontproto
//    xorg-xproto
//    xorg-xtrans
//    xpm
//    xrender
//    xz
//    zlib
