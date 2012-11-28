package oz.zomg.jport.ports;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import oz.zomg.jport.common.Util;
import oz.zomg.jport.type.Portable;


/**
 *
 * @author sbaber
 */
public class WordSeqTest
{
    static final private WordSeqTest _ROOT = new WordSeqTest( "" );

    static
    {
        final PortsCatalog portsCatalog = new PortsCatalog( PortsCatalog.NONE );

        final List<String> sentencesList = new ArrayList<String>( 1024 );
        for( final Portable port : portsCatalog.getPortsInventory().getAllPorts() )
        {
            final String shortDescr = port.getShortDescription();
            final String longDescr = port.getLongDescription();

            sentencesList.clear();
            final String paragraph = shortDescr +' '+ ( shortDescr.equals( longDescr ) ? "" : longDescr +' ' ) +' ';
            WordSeqTest._wordSplit( sentencesList, paragraph );

            WordSeqTest._add( _ROOT, 0, sentencesList );

//            final int end = sentencesList.size() - 1;
//            for( int i = 0; i < end ; i++  )
//            {
//                Test._add( Test._ROOT, sentencesList.get( i ), sentencesList.get( i + 1 ) );
//            }
        }
    }

    final private String fWord;
    final private Map<String,WordSeqTest> fMap = new TreeMap<String,WordSeqTest>();

    private int mCount = 0;

    private WordSeqTest( final String word )
    {
        fWord = word;
    }

    @Override public String toString()
    {
        final StringBuilder sb = new StringBuilder( fWord );
        sb.append( '=' ).append( mCount ).append( '\n' );

        for( final Map.Entry<String,WordSeqTest> entry : fMap.entrySet() )
        {
            sb.append( '\t' ).append( entry.getKey() ).append( '\n' );
        }

        return sb.toString();
    }

    static private void _add( final WordSeqTest parent, final int index, final List<String> list )
    {
        if( index >= list.size() ) return;

        final String word0 = list.get( index );

        if( parent.fMap.containsKey( word0 ) == false )
        {
            final WordSeqTest wb = new WordSeqTest( word0 );
            parent.fMap.put( word0, wb );
        }

        final WordSeqTest wb = parent.fMap.get( word0 );
        wb.mCount++;

        _add( wb, index + 1, list );

        
    }

//    static private void _add( final Test parent, final String word0, final String word1 )
//    {
//        if( parent.fMap.containsKey( word0 ) == false )
//        {
//            final Test wb = new Test( word0 );
//            parent.fMap.put( word0, wb );
//        }
//
//        final Test wb = parent.fMap.get( word0 );
//        wb.mCount++;
//
//        if( word1.isEmpty() == false )
//        {
//            _add( wb, word1, "" );
//        }
//    }

    static private void _wordSplit( final List<String> sentencesList, final String paragraph )
    {
        int begin = 0;
        final int end = paragraph.length() - 1; // two spaces at end for teminus
        for( int index = 0; index < end; index++ )
        {
            final char c = paragraph.charAt( index );
            switch( c )
            {
                case ' ' :  // find word breaks but not at -, +, @, #, $, %, &, ~
                case '\t' : case '\n' : case '\r' : case '\'' : case '\"' : case '\\' :
                case '\u2018' : case '\u2019' : case '\u201B' : case '\u201C' : case '\u201D' : case '\u201F' : // curly quotes
                case ',' : case '.' : case '!' : case '?' : case ';' : case ':' :
                case '/' : case '*' : case '=' : case '|' : case '_' : case '`' :
                case '(' : case ')' : case '<' : case '>' : case '[' : case ']' : case '{' : case '}' :
                    boolean ok = true;
                    if( begin == index || paragraph.charAt( begin ) == '-' )
                    {   // non-empty only, no starts with hyphen
                        ok = false;
                    }
                    else if( c == '.' )
                    {
                        final char n = paragraph.charAt( index + 1 );
                        switch( n )
                        {   // only period followed by white space
                            case ' ' : case '\t' : case '\n' : case '\r' : break;
                            default : ok = false; break;
                        }
                    }

                    if( ok == true )
                    {
                        final String sub = paragraph.substring( begin, index ).toLowerCase().intern();
                        sentencesList.add( sub );

                        if( c == '.' ) sentencesList.add( "." );
                    }
                    begin = index + 1; // skip space or other word break char
                    break;
            }
        }

        if( sentencesList.isEmpty() == false && ".".equals( sentencesList.get( sentencesList.size() - 1 ) ) == false )
        {   // ensure ends with '.'
            sentencesList.add( "." );
        }
    }

    static public void main( String[] args )
    {
      
        if( false )
        {   // show all relations
            for( final Map.Entry<String,WordSeqTest> entry : WordSeqTest._ROOT.fMap.entrySet() )
            {
                System.out.println( entry.getValue() );
            }
        }
        else
        {
            final Random r = new Random();
//            final Map.Entry<String,Test>[] rootEntries = Util.createMapEntryArray( Test._ROOT.fMap );

            for( int j = 0; j < 10; j++ )
            {
                WordSeqTest wb = WordSeqTest._ROOT;
                boolean done = false;
                while( done == false )
                {
                    final Map.Entry<String,WordSeqTest>[] entries = Util.createMapEntryArray( wb.fMap );
                    String word = "";

                    switch( entries.length )
                    {
                        case 0 :
                            {   wb = WordSeqTest._ROOT;
                            }   break;

                        case 1 :
                            {   word = entries[ 0 ].getKey();
                                wb = entries[ 0 ].getValue();
                            }   break;

                        default:
                            {   final int index = r.nextInt( entries.length );
                                word = entries[ index ].getKey();
                                wb = entries[ index ].getValue();
                            }   break;
                    }

                    System.out.print( word + " " );

                    if( ".".equals( word ) )
                    {
                        done = true;
                    }
                }

                System.out.println( '\n' );
            }
        }
    }

}