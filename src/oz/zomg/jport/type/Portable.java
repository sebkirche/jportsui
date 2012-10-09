package oz.zomg.jport.type;

import oz.zomg.jport.common.Providers_.ForeColorProvidable;
import oz.zomg.jport.common.Providers_.TipProvidable;


/**
 * Methods required of a Port implementation.
 *
 * @author <SMALL>Copyright 2012 by Stephen Baber
 * &nbsp; <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/deed.en_US">
 * <img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-sa/3.0/80x15.png" /></a><br />
 * This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/deed.en_US">
 * Creative Commons Attribution-ShareAlike 3.0 Unported License</a>.</SMALL>
 */
public interface Portable
    extends
          Comparable<Portable>
        , ForeColorProvidable
        , TipProvidable
{
    static final public Portable NONE = BsdPort.BSD_NONE;

    abstract public String      getName();
    abstract public String      getShortDescription();
    abstract public String      getLongDescription();
    abstract public String      getLatestVersion();
    abstract public String      getLatestRevision();
    abstract public String      getHomepage();
    abstract public String      getDomain();

    // multi
    abstract public String[]    getCategories();
    abstract public String[]    getLicenses();
    abstract public String[]    getMaintainers();
    abstract public String[]    getVariants();

    /** @return Used for comparisons */
    abstract public String      getCaseInsensitiveName();
    abstract public String      getPortDirectory();
    abstract public long        getModificationEpoch();

    // deps
    abstract public boolean     hasDependency( final Portable onPort );
    abstract public Portable[]  getFullDependencies();
    abstract public Portable[]  getDeps();
    abstract public Portable[]  getDependants();

    // status change request marks
    abstract public boolean     isUnmarked();
    abstract public EPortMark   getMark();
    abstract public void        setMark( final EPortMark markEnum );
    abstract public void        unmark();

    // only applies to installed ports
    abstract public boolean     isInstalled();
    abstract public boolean     hasStatus( final EPortStatus statusEnum );
    abstract public void        setStatus( final EPortStatus statusEnum );
    abstract public String[]    getVariantsInstalled();
    abstract public String      getVersionInstalled();
    abstract public String      getRevisionInstalled();


    // ================================================================================
    /**
     *
     */
    static public interface Providable
    {
        abstract Portable providePort();
    }


    // ================================================================================
    /**
     * Reduce interface for a map-reduce para-lambda expression.
     */
    static public interface Predicatable
    {
        /** No intended narrowing, wide open. */
        static final public Predicatable ANY = new Predicatable() { @Override public boolean evaluate( Portable port ) { return true; } };

        abstract boolean evaluate( final Portable port );
    }
}

@Deprecated // not worth converting from type erased []s
interface Dependable
{
    boolean hasDependency( final Portable aPort, final Portable onPort );
    Portable[] getFullDependenciesOf( final Portable aPort );

    boolean isADependant( final Portable aDependant, final Portable ofPort );

    /**
     * @param ofPort
     * @return is a dependant of
     */
    Portable[] getDependants( final Portable ofPort );
}
