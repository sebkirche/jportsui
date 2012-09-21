package jport.common;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * General utilities.
 * Some are a subset of original source.
 *
 * @author sbaber
 */
public class Util
{
    static final public int INVALID_INDEX = -1;

    private Util() {}

    static public boolean isOnMac()
    {
        return System.getProperty( "os.name" ).toLowerCase().startsWith( "mac" );
    }

    static public boolean isOnWindows()
    {
        return System.getProperty( "os.name" ).toLowerCase().startsWith( "win" );
    }

    /**
     * Linear Search an array for an identity with '==' instead of
     * using Arrays.binarySearch() when the array can not be sort ordered
     * or when less than approx. 10 elements requiring the hashcode generation
     * to seek into an IdentityHashMap.
     *
     * @param <E> will be inferred
     * @param searchReferent to locate in the array.  Can be 'null'
     * @param inArray will -not- be altered!  Note: to avoid some compiler warnings in client code, the type needs to be 'Object[]' not {...}
     * @return index -1 if not found in the [], otherwise a ZERO-based index of the -FIRST- occurrence
     */
    static public <E> int indexOfIdentity( final E searchReferent, final E[] inArray )
    {
        switch( inArray.length )
        {
            case 0 : return INVALID_INDEX;

            case 1 : return ( searchReferent == inArray[ 0 ] ) ? 0 : INVALID_INDEX; // 0=found index

            default:
                    int i = 0;
                    for( final E obj : inArray )
                    {   // linear search faster than indentity hashing until more than ~10 elements
                        if( searchReferent == obj ) return i; // found
                        i += 1;
                    }
                    return INVALID_INDEX;
        }
    }

    /**
     * Linear Search an array for an equality with .equals() instead of
     * using Arrays.binarySearch() when the array can not be sort ordered.
     *
     * @param <E> will be inferred
     * @param obj to locate in the array. Can be 'null'
     * @param inArray will -not- be altered!  Note: to avoid compiler warnings in client code, the type needs to be 'Object[]' not {...}
     * @return index -1 if not found in the [], otherwise a ZERO-based index of the -FIRST- occurrence
     */
    static public <E> int indexOf( final E obj, final E[] inArray )
    {
        if( obj == null ) return indexOfNull( inArray );

        switch( inArray.length )
        {
            case 0 : return INVALID_INDEX;

            case 1 : return ( obj.equals( inArray[ 0 ] ) ) ? 0 : INVALID_INDEX; // 0=found index

            default:
                    int i = 0;
                    for( final E element : inArray )
                    {   // linear search faster than hashing until more than ~20 elements
                        if( obj.equals( element ) ) return i; // found
                        i += 1;
                    }
                    return INVALID_INDEX;
        }
    }

    /**
     * Where is null?
     *
     * @param <E> will be inferred
     * @param inArray will -not- be altered!
     * @return -1 if there is no 'null' reference in array else the ZERO-based index of the -FIRST- 'null' occurrence
     */
    static public <E> int indexOfNull( final E[] inArray )
    {
        switch( inArray.length )
        {
            case 0 : return INVALID_INDEX;

            case 1 : return ( inArray[ 0 ] == null ) ? 0 : INVALID_INDEX; // 0=found index

            default:
                    int i = 0;
                    for( final E element : inArray )
                    {
                        if( element == null ) return i; // found
                        i += 1;
                    }
                    return INVALID_INDEX;
        }
    }

    /**
     * Reverse the array index order of the object refs in place.
     *
     * @param objs within same array is reverse ordered, no new allocation will occur
     */
    static public void reverseOrderInPlace( final Object[] objs )
    {
        final int length = objs.length;
        if( length <= 1 ) return; // 0 and 1 are guaranteed palindromes

        for( int top = 0, end = length - 1; top < end; top++, end-- )
        {   // exchange the first and last
            final Object element = objs[ top ];
            objs[ top ] = objs[ end ];
            objs[ end ] = element;
        }
    }

    //BUG FIX middle element missing, doesn't effect in-place because middle remains put
    /**
     * Reverse index order the object references.
     * Employs optimizations that may result in the originating source [].
     *
     * @param <E> will be inferred
     * @param elements will -not- be altered!  Zero lengths are okay.
     * @return reversal not an in-place operation, i.e. a new reified [] is allocated if length > 1
     */
    static public <E> E[] reverseOrder( final E[] elements )
    {
        final int length = elements.length;
        switch( length )
        {
            case 0 : // length 0 and 1 are guaranteed palindromes
            case 1 : return elements;

            default:
                {   @SuppressWarnings("unchecked")
                    final E[] revs = (E[])Array.newInstance( getElementalClass( elements ), length );
                    for( int start = 0, end = length - 1; start < length; start++, end-- )
                    {
                        revs[ start ] = elements[ end ];
                    }
                    return revs;
                }

        }
    }

    /**
     * From a reified array, extract the []'s elemental class type.
     * Can be a primitive type such as byte.class, an array such as int[] when 2D int[][],
     * or object references like Byte.class or SomeClass.class.
     *
     * @param <E> will be inferred
     * @param typedArray
     * @return the particular Class type of all elements in the reified array
     */
    @SuppressWarnings("unchecked")
    static public <E> Class<E> getElementalClass( final E[] typedArray )
    {
        return (Class<E>)(typedArray.getClass().getComponentType()); // BTW- not an java.awt.Component
    }

    /**
     *
     * @param <T>
     * @param withElementsOfClassType
     * @param length
     * @return
     */
    @SuppressWarnings("unchecked")
    static public <T> T[] createArray( final Class<T> withElementsOfClassType, final int length )
    {
        return ( length == 0 )
                ? EmptyArrayFactory_.get( withElementsOfClassType )
                : ( withElementsOfClassType != Object.class )
                        ? (T[])Array.newInstance( withElementsOfClassType, length )
                        : (T[])new Object[ length ]; // optimization present in java.lang.Arrays.copyOf().  Perhaps makes System.arrayCopy() work faster?
    }

    /**
     * Programmatically create a reified array from a Generic Collection.
     * Special cased to cache EMPTY[0]{} arrays by Class type via EmptyArrayFactory.
     * This could be a feature of certain JVMs but we guarantee it here.
     *
     * Implement .toArray() for Set<T> or List<T> without all the drama of an empty allocation with new <T>[0] or new <T>[n]
     *
     * @param <S> super type of T
     * @param <T> will be inferred
     * @param withElementOfSuperClassType required for Array.newInstance() as Java Type Erasure means Generics are just Object refs, sort of
     * @param theEntireCollection is a List, Set, or Queue (but not an Iterable as no .size() method)
     * @return new array with a copy of the collection's elements
     */
    @SuppressWarnings("unchecked")
    static public <S,T extends S> S[] createArray( final Class<S> withElementOfSuperClassType, final Collection<T> theEntireCollection )
    {
        final int length = theEntireCollection.size();
        switch( length )
        {
            case 0 : // caches an empty immutable array to avoid a common allocation scenario
                    return EmptyArrayFactory_.get( withElementOfSuperClassType );

            //... case 1 : not available because no easy way to get a single element out without an Iterator allocation

            default:
                    if( withElementOfSuperClassType != Object.class )
                    {
                        final S[] elements = (S[])Array.newInstance( withElementOfSuperClassType, length );
                        theEntireCollection.toArray( elements );
                        return elements;
                    }
                    else
                    {   // plain-old type-erasured Object[]
                        return (S[])theEntireCollection.toArray();
                    }
        }
    }

    //ENHANCE -> ObjUtil
    /**
     * Reflection based, non-static field values dump for debug.
     * Handles arrays also.
     *
     * @param instance
     * @param needStaticOnly 'true' for static fields only, 'false' for non-static fields only
     * @return is [CR] separated
     */
    static public String dumpFields( final Object instance, final boolean needStaticOnly )
    {
        final StringBuilder sb = new StringBuilder();

        Class<?> ofClassType = instance.getClass();
        do
        {   // 'do-while' allows at least "Object" if a POJO
            final Field[] fields = ofClassType.getDeclaredFields();
            for( final Field field : fields )
            {
                if( Modifier.isStatic( field.getModifiers() ) == needStaticOnly )
                {   // did not want static fields
                    try
                    {
                        field.setAccessible( true ); // otherwise throws IllegalAccessException
                        final Object value = field.get( instance );
                        if( value != null )
                        {
                            final String valueString = ( value.getClass().isArray() == false )
                                    ? value.toString()
                                    : Arrays.toString( (Object[])value );

                            if( valueString.isEmpty()          == false
                                && "[]" .equals( valueString ) == false
                                && "0"  .equals( valueString ) == false
                                && "0.0".equals( valueString ) == false
                              )
                            {   // not empty or "[]" or "0" or "0.0" or 'null'
                                final String fieldName = field.getName();
                                sb.append( fieldName ).append( "\t " ).append( valueString ).append( '\n' );
                            }
                            // else ignore default fields
                        }
                        // else ignore uninitialized fields
                    }
                    catch( IllegalArgumentException ex )
                    {}
                    catch( IllegalAccessException ex )
                    {}
                }
                // else is a static field like NONE or EMPTY
            }

            ofClassType = ofClassType.getSuperclass();
        }
        while( Object.class.equals( ofClassType ) == false && ofClassType != null );

        return sb.toString();
    }

    //ENHANCE CollectionUtil
    /**
     *
     * @param <K> will be swapped to a Value class type
     * @param <V> will be swapped to a Key class type
     * @param inverseNeedsOrderedKeys 'true' requires Comparable elements
     * @param inverseNeedsOrderedValues 'true' requires Comparable elements
     * @param kvMap map to be inverted
     * @return an inverse mapping where Values are now mapped to potentially multiple Keys
     */
    static public <K,V> Map<V,Set<K>> createInverseMultiMapping
            ( final boolean inverseNeedsOrderedKeys
            , final boolean inverseNeedsOrderedValues
            , final Map<K,V> kvMap
            )
    {
        final Map<V,Set<K>> invMap = ( inverseNeedsOrderedKeys == true )
                ? new TreeMap<V, Set<K>>()
                : new HashMap<V, Set<K>>();

        // invert keys - values
        for( final Map.Entry<K,V> entry : kvMap.entrySet() )
        {
            final V invKey   = entry.getValue(); // alias
            final K invValue = entry.getKey(); // alias

            if( invKey != null )
            {   // values maybe 'null' but keys can not be
                if( invMap.containsKey( invKey ) == false )
                {   // a singleton element is always ordered
                    final Set<K> set = Collections.singleton( invValue );
                    invMap.put( invKey, set );
                }
                else
                {   // seen the inverse key before
                    final Set<K> set = invMap.get( invKey );
                    if( set.size() > 1 )
                    {   // set already bigger
                        set.add( invValue );
                    }
                    else
                    {   // copy to a bigger, non-singleton set
                        final Set<K> biggerSet = ( inverseNeedsOrderedValues == true )
                                ? new TreeSet<K>( set )
                                : new HashSet<K>( set );
                        biggerSet.add( invValue );
                        invMap.put( invKey, biggerSet );
                    }
                }
            }
        }

        // replacing 'null' value Sets with Collections.emptySet() does not have to be done because 'null' keys are prohibited
        return invMap;
    }

    //ENHANCE CollectionsUtil
    static private <K,V> Map<V,Set<K>> createInverseMultiMapping
            ( final boolean inverseNeedsOrderedKeys
            , final Map<K,Set<V>> kvMap
            )
    {
        final Map<V,Set<K>> invMap = ( inverseNeedsOrderedKeys == true )
                ? new TreeMap<V, Set<K>>()
                : new HashMap<V, Set<K>>();

        // invert keys - values
        for( final Map.Entry<K,Set<V>> entry : kvMap.entrySet() )
        {
            final Set<V> invKeySet = entry.getValue(); // alias
            final K invValue = entry.getKey(); // alias
            
            if( invKeySet != null )
            {   // values maybe 'null' but keys can not be
                for( final V invKey : invKeySet )
                {
                    if( invMap.containsKey( invKey ) == false )
                    {   // a singleton element is always ordered
                        final Set<K> set = Collections.singleton( invValue );
                        invMap.put( invKey, set );
                    }
                    else
                    {   // seen the inverse key before
                        final Set<K> set = invMap.get( invKey );
                        if( set.size() > 1 )
                        {   // set already bigger
                            set.add( invValue );
                        }
                        else
                        {   // copy to a bigger, non-singleton set
                            final Set<K> biggerSet = new HashSet<K>( set );
                            biggerSet.add( invValue );
                            invMap.put( invKey, biggerSet );
                        }
                    }
                }
            }
        }

        // replacing 'null' value Sets with Collections.emptySet() does not have to be done because 'null' keys are prohibited
        return invMap;
    }

    /**
     * Performs a complete read of an existing File.
     * Bringing in very large files may require a large starting JVM heap.
     *
     * @param filePath whose contents are less than 2 gigabytes in logical size
     * @return all content bytes for the file
     * @throws IOException
     */
    static public byte[] retreiveFileBytes( final File filePath ) throws IOException
    {
        final long size = filePath.length(); // file should not truncate before operation complete
        if( size > Integer.MAX_VALUE ) throw new IllegalArgumentException( "File contents too large to fit into a Java6 array " + filePath.getAbsolutePath() ); // Doh!

        final byte[] bytes = new byte[ (int)size ];
        final FileInputStream fis = new FileInputStream( filePath );
        final DataInputStream dis = new DataInputStream( fis );

        dis.readFully( bytes );

        dis.close();
        fis.close();        
        return bytes;
    }

    //ENHANCE -> IoFileUtil parameter
    /**
     * Just close the file, connection, stream, etc. without IOException.
     *
     * @param closeable
     * @return success 'true' if closed without IOException thrown
     */
    static public boolean close( final Closeable closeable )
    {
        if( closeable == null ) return false; // nothing to do

        try
        {
            closeable.close();
            return true;
        }
        catch( IOException ex )
        {   // not expected
            return false;
        }
    }
}
