package oz.zomg.jport.ports;

import java.util.HashSet;
import java.util.Set;
import oz.zomg.jport.TheApplication;
import oz.zomg.jport.common.StringsUtil_;
import oz.zomg.jport.type.Portable;
import oz.zomg.jport.type.Portable.Predicatable;


/**
 *
 * @author sgbaber
 */
public class PortsHistogramFactory
{
    /**
     * In UI order!
     * Transform / Predicate is Map / Reduce.
     */
    static public enum EHistogram
            { Categories   { @Override public String[] transform( final Portable port ) { return port.getCategories(); } }
            , Licenses     { @Override public String[] transform( final Portable port ) { return port.getLicenses(); } }
            , Homepages    { @Override public String[] transform( final Portable port ) { return new String[] { port.getHomepage() }; } }
            , Domains      { @Override public String[] transform( final Portable port ) { return new String[] { port.getDomain() }; } }
            , Maintainers  { @Override public String[] transform( final Portable port ) { return port.getMaintainers(); } }
            , Dependencies { @Override public String[] transform( final Portable port ) { return StringsUtil_.toStrings( port.getFullDependencies() ); } }
            , Variants     { @Override public String[] transform( final Portable port ) { return port.getVariants(); } }
            , Dates        { @Override public String[] transform( final Portable port ) { return new String[] { StringsUtil_.getDateString( port.getModificationEpoch() ) }; } }
            , Words        { @Override public String[] transform( final Portable port ) { return _getUniqueWords( port ); } }
            ;
                    abstract public String[] transform( final Portable port );
            }

    static final private Set<String> _SCRATCH_WORD_SET = new HashSet<String>();

    private PortsHistogramFactory() {}

    static private String[] _getUniqueWords( final Portable port )
    {
        final String string = port.getShortDescription() +' '+ port.getLongDescription() +' '+ ' '; // port.getName()

        int begin = 0;
        final int end = string.length() - 1; // two spaces at end for teminus
        for( int index = 0; index < end; index++ )
        {
            final char c = string.charAt( index );
            switch( c )
            {
                case ' ' :  // find word breaks but not at -, +, @, #, $, %, &, ~
                case '\t' : case '\n' : case '\r' : case '\'' : case '\"' : case '\\' :
                case '\u2018' : case '\u2019' : case '\u201B' : case '\u201C' : case '\u201D' : case '\u201F' : // curly quotes
                case ',' : case '.' : case '!' : case '?' : case ';' : case ':' :
                case '/' : case '*' : case '=' : case '|' : case '_' : case '`' :
                case '(' : case ')' : case '<' : case '>' : case '[' : case ']' : case '{' : case '}' :
                    boolean ok = true;
                    if( begin == index || string.charAt( begin ) == '-' )
                    {   // non-empty only, no starts with hyphen
                        ok = false;
                    }
                    else if( c == '.' )
                    {
                        final char n = string.charAt( index + 1 );
                        switch( n )
                        {   // only period followed by white space
                            case ' ' : case '\t' : case '\n' : case '\r' : break;
                            default : ok = false; break;
                        }
                    }

                    if( ok == true )
                    {
                        final String sub = string.substring( begin, index ).toLowerCase().intern();
                        _SCRATCH_WORD_SET.add( sub );
                    }
                    begin = index + 1; // skip space or other word break char
                    break;
            }
        }

        final String[] words = StringsUtil_.toStrings( _SCRATCH_WORD_SET );
        _SCRATCH_WORD_SET.clear(); // no leak and prepare for next time
        return words;
    }

    static private boolean _dependentOn( final Portable examinePort, final String selectedPortName )
    {
        final Portable selectedPort = TheApplication.INSTANCE.getPortsCatalog().parse( selectedPortName );
        return examinePort.hasDependency( selectedPort ); // works!
    }

    /**
     *
     * @param ofHistogram
     * @param search context
     * @return
     */
    static public Predicatable createPredicate( final EHistogram ofHistogram, final String search )
    {
        return new Predicatable() // anonymous class
                {   @Override public boolean evaluate( final Portable port )
                    {   switch( ofHistogram )
                        {   // not associated in EHistogram because Predicatable has no context for search
                            case Domains      : return search.equals( port.getDomain() );
                            case Homepages    : return search.equals( port.getHomepage() );
                            case Dates        : return search.equals( StringsUtil_.getDateString( port.getModificationEpoch() ) );
                            case Licenses     : return StringsUtil_.equals( search, port.getLicenses() );
                            case Maintainers  : return StringsUtil_.equals( search, port.getMaintainers() );
                            case Categories   : return StringsUtil_.equals( search, port.getCategories() );
                            case Variants     : return StringsUtil_.equals( search, port.getVariants() );
                            case Words        : return StringsUtil_.equals( search, _getUniqueWords( port ) );
                            case Dependencies : return _dependentOn( port, search );
                            default : return true;
                        }
                    }
                };

// made for messy code -> return ofHistogram.create( search );
    }
}