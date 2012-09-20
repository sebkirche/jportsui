package jport.type;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import jport.PortsConstants.EPortStatus;



/**
 * These instances have more information from CLI calls than the "QuickIndex" parse of the BsdPort.
 * Usually locally installed, but possibly outdated.
 *
 * @author sbaber
 */
class InstalledPort extends BsdPort
{
    final private String   fVersionInstalled;
    final private String   fRevisionInstalled;
    final private String[] fVariantsInstalled;

    final Set<EPortStatus> fStatusSet = EnumSet.noneOf( EPortStatus.class );

    InstalledPort
            ( final BsdPort  copyPort
            , final String   versionInstalled
            , final String   revisionInstalled
            , final String[] variantsInstalled
            )
    {
        super( copyPort );

        fStatusSet.add( EPortStatus.ALL );
        fStatusSet.add( EPortStatus.INSTALLED );

        fVersionInstalled  = versionInstalled;
        fRevisionInstalled = revisionInstalled;
        fVariantsInstalled = variantsInstalled;
        
        Arrays.sort( variantsInstalled );

//.. did talk to the CLI
    }

    @Override public boolean isInstalled() { return true; }

    @Override public void setStatus( final EPortStatus statusEnum )
    {
        fStatusSet.add( statusEnum );

//    if(false) //... each installed port may have seperate versions now
//        switch( statusEnum )
//        {
//            case UNINSTALLED :
//                    clearStatus( EPortStatus.INSTALLED );
//                    clearStatus( EPortStatus.ACTIVE );
//                    clearStatus( EPortStatus.INACTIVE );
//                    clearStatus( EPortStatus.ACTINACT );
//                    clearStatus( EPortStatus.OUTDATED );
//                    clearStatus( EPortStatus.LEAVES );
//                    break;
//
//            case INSTALLED :
//                    clearStatus( EPortStatus.UNINSTALLED );
//                    break;
//
//            case ACTIVE :
//                    clearStatus( EPortStatus.INACTIVE );
//                    break;
//
//            case INACTIVE :
//                    clearStatus( EPortStatus.ACTIVE );
//                    break;
//
//            case REQUESTED :
//                    clearStatus( EPortStatus.UNREQUESTED );
//                    break;
//
//            case UNREQUESTED :
//                    clearStatus( EPortStatus.REQUESTED );
//                    break;
//        }
    }

    private void clearStatus( final EPortStatus status )
    {
        fStatusSet.remove( status );
    }

    @Override public boolean hasStatus( final EPortStatus status )
    {
        return fStatusSet.contains( status );
    }

    @Override public String   getVersionInstalled()  { return fVersionInstalled; }
    @Override public String   getRevisionInstalled() { return fRevisionInstalled; }
    @Override public String[] getVariantsInstalled() { return fVariantsInstalled; }
}
