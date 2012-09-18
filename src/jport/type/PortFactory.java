package jport.type;

import java.util.Arrays;


/**
 * Static factory methods.
 *
 * @author sbaber
 */
public class PortFactory
{
    private PortFactory() {}

    /**
     *
     * @param text plain-text entry for the port
     * @return NONE if was not able to parse the line
     */
    static public Portable createFromPortIndexFile( final String text )
    {
        final PortBuilder pb = new PortBuilder( text );
        return ( pb.didParse() == true )
                ? new BsdPort( pb )
                : Portable.NONE;
    }

    static public Portable createFromCli( final Portable port, final CliPortInfo cpi )
    {
        return new InstalledPort
                ( (BsdPort)port
                , cpi.getVersionInstalled()
                , cpi.getVariants()
                );
    }

    /**
     *
     * @param prevPort
     * @param cliVersion
     * @param cliVariants
     * @return if prevPort is a match then it is returned
     */
    static private Portable create
            ( final Portable prevPort
            , final String cliVersion
            , final String[] cliVariants
            )
    {
        final boolean isMatch = prevPort.isInstalled() == true
                && prevPort.getVersionInstalled().equals( cliVersion )
//...                && prevPort.getVersion().equals( cliVersion )
                && Arrays.deepEquals( prevPort.getVariantsInstalled(), cliVariants );

        return ( isMatch == true )
                ? prevPort // simply return
                : new InstalledPort // does not match, needs to update
                    ( (BsdPort)prevPort
                    , cliVersion
                    , cliVariants
                    );
    }
}
