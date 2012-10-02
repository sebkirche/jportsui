package oz.zomg.jport.type;


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
                , cpi.getRevisionInstalled()
                , cpi.getVariants()
                );
    }
}
