package jport.type;

/**
 * 
 *
 * @author sbaber
 */
@Deprecated
class PortId
    implements Comparable<PortId>
{
    static final public PortId NONE = new PortId( "", "", "" );

    static
    {}

    final private String fCi_name;
    final private String fName;
    final private String fVersion;

    private PortId( final String name, final String version )
    {
        this( name.toLowerCase(), name, version );
    }


    /**
     *
     * @param ci_name case-insensitive name for sorting
     * @param name
     * @param version
     */
    private PortId
        ( final String ci_name
        , final String name
        , final String version
        )
    {
        fCi_name = ci_name;
        fName    = name;
        fVersion = version;
    }

    public String getCaseInsensitiveName() { return fCi_name; }
    public String getName() { return fName; }
    public String getVersion() { return fVersion; }

    @Override public int hashCode()
    {
        return 37 * fCi_name.hashCode() + fVersion.hashCode();
    }

    @Override final public boolean equals( final Object obj )
    {
        if( obj == this ) return true;

        if( obj instanceof PortId )
        {
            final PortId other = (PortId)obj;
            return fCi_name.equals( other.fCi_name ) && fVersion.equals( other.fVersion );
        }
        return false;
    }

    @Override final public int compareTo( final PortId another )
    {
        if( another == this ) return 0;

        final int compared = fCi_name.compareTo( another.fCi_name );
        return ( compared != 0 )
                ? compared
                : fVersion.compareTo( another.fVersion);
    }

    @Override final public String toString()
    {
        return fName +'@'+ fVersion;
    }
}
