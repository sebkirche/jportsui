package oz.zomg.jport.common;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * Utilities for Swing and AWT Graphical User Interfaces.
 * <H3><I><FONT color="#770000">Subset of original source.</FONT></I></H3>
 *
 * @author sbaber
 */
public class GuiUtil_
{
    static final public int GAP_PIXEL = 5;
    static final public Insets ZERO_INSET = new Insets( 0, 0, 0, 0 );

    static
    {}

    private GuiUtil_() {}

// ENHANCE
    /**
     * Get all children from a safely-cast Container of a certain Class type.
     *
     * @param <T> is inferred
     * @param ofClassType
     * @param fromContainers and their child sub-Containers
     * @return
     */
    @SuppressWarnings("unchecked")
    static public <T extends Component> Set<T> getChildren( final Class<T> ofClassType, final Container... fromContainers )
    {
        if( fromContainers.length == 1 && fromContainers[ 0 ].getComponentCount() == 0 ) return Collections.emptySet();

        final Set<Component> allChildrenSet = new HashSet<Component>();

        for( final Container fromContainer : fromContainers )
        {
            _putAllChildren( allChildrenSet, fromContainer );
        }

        final Set<T> filteredSet = new HashSet<T>();
        for( final Component child : allChildrenSet )
        {
            if( ofClassType.isInstance( child ) == true ) // note: polymorphic just like 'instanceof'
            {
                filteredSet.add( (T)child );
            }
        }

        return filteredSet;
    }

    /**
     * Recursive inventory of all child Containers and Components using depth-first traversal of Containers.
     *
     * @param intoSet also receives top-level parent and lower-level parent Containers
     * @param components
     */
    static private void _putAllChildren( final Set<Component> intoSet, final Component... components )
    {
        if( components.length == 0 ) return;

        for( final Component component : components )
        {
            if( component instanceof Container )
            {
                final Container childContainer = (Container)component;
                _putAllChildren( intoSet, childContainer.getComponents() );
            }
        }

        intoSet.addAll( Arrays.asList( components ) ); // fast wrapper
    }

// ENHANCE to JTreeFactory / JTreeUtil
    static private <E extends Enum<E>, C extends Collection<?>> JTree createJTree( final boolean isExpanded, final Map<E,C> map )
    {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        for( final Map.Entry<E,C> entry : map.entrySet() )
        {
            final E e = entry.getKey();
            final C collection = entry.getValue();

            if( collection.isEmpty() == false )
            {
                final DefaultMutableTreeNode branch = new DefaultMutableTreeNode( e, true );
                root.add( branch );

                for( final Object element : collection )
                {
                    branch.add( new DefaultMutableTreeNode( element, false ) ); // leaf
                }
            }
        }

        final JTree jTree = new JTree( root );
        jTree.setRootVisible( false );

        if( isExpanded == true )
        {
            for( int i = 0; i < jTree.getRowCount(); i++ )
            {   // expand all 1st tier branches
                jTree.expandRow( i );
            }
        }

        return jTree;
    }
}
