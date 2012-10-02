package oz.zomg.jport.common;

import java.awt.Color;
import javax.swing.JPopupMenu;


/**
 * Name space class.
 * <H3><I><FONT color="#770000">Subset of original source.</FONT></I></H3>
 *
 * @author sbaber
 */
public class Providers_
{
    private Providers_() {}

    // stateless
    static public interface BackColorProvidable          { abstract public Color         provideBackColor(); }
    static public interface ClassProvidable              { abstract public Class<?>      provideClass(); } // use with TabelModel.getColumnClass() etc.
    static public interface ColorProvidable              { abstract public Color         provideColor(); }
    static public interface DisplayTextProvidable        { abstract public String        provideDisplayText(); }
    static public interface EnabledProvidable            { abstract public boolean       provideIsEnabled(); } // Component attribute
    static public interface ForeColorProvidable          { abstract public Color         provideForeColor(); }
    static public interface JPopupMenuProvidable         { abstract public JPopupMenu    provideJPopupMenu(); } // enables JTable right-clicks
    static public interface TipProvidable                { abstract public String        provideTipText(); } // enables any object to provide a text for a Tooltip
    static public interface VisibilityProvidable         { abstract public boolean       provideIsVisible(); } // Component attribute
    static public interface WidthProvidable              { abstract public int           provideWidth(); }

    // contextual
    static public interface ContextualVisibilityProvidable<T> { abstract public boolean provideIsVisible( T context ); }
    static public interface ContextualEnabledProvidable<T>    { abstract public boolean provideIsEnabled( T context ); }
    
    static public interface RowProvidable<R>                  { abstract public R       provideRow( final int index ); } // used with JTable for tooltips
}
