package oz.zomg.jport.common;

import java.awt.Color;
import javax.swing.JPopupMenu;


/**
 * Name space class.
 * <H3><I><FONT color="#770000">Subset of original source.</FONT></I></H3>
 *
 * @author <SMALL>Copyright 2012 by Stephen Baber
 * &nbsp; <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/deed.en_US">
 * <img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-sa/3.0/80x15.png" /></a><br />
 * This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/deed.en_US">
 * Creative Commons Attribution-ShareAlike 3.0 Unported License</a>.</SMALL>
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
