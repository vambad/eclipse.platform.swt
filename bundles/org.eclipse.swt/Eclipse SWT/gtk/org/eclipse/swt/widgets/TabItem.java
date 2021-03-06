/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.widgets;


import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.graphics.*;

/**
 * Instances of this class represent a selectable user interface object
 * corresponding to a tab for a page in a tab folder.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#tabfolder">TabFolder, TabItem snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TabItem extends Item {
	long /*int*/ labelHandle, imageHandle, pageHandle;
	Control control;
	TabFolder parent;
	String toolTipText;

/**
 * Constructs a new instance of this class given its parent
 * (which must be a <code>TabFolder</code>) and a style value
 * describing its behavior and appearance. The item is added
 * to the end of the items maintained by its parent.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * lists the style constants that are applicable to the class.
 * Style bits are also inherited from superclasses.
 * </p>
 *
 * @param parent a composite control which will be the parent of the new instance (cannot be null)
 * @param style the style of control to construct
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public TabItem (TabFolder parent, int style) {
	super (parent, style);
	this.parent = parent;
	createWidget (parent.getItemCount ());
}

/**
 * Constructs a new instance of this class given its parent
 * (which must be a <code>TabFolder</code>), a style value
 * describing its behavior and appearance, and the index
 * at which to place it in the items maintained by its parent.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * lists the style constants that are applicable to the class.
 * Style bits are also inherited from superclasses.
 * </p>
 *
 * @param parent a composite control which will be the parent of the new instance (cannot be null)
 * @param style the style of control to construct
 * @param index the zero-relative index to store the receiver in its parent
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the parent (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public TabItem (TabFolder parent, int style, int index) {
	super (parent, style);
	this.parent = parent;
	createWidget (index);
}

@Override
protected void checkSubclass () {
	if (!isValidSubclass ()) error (SWT.ERROR_INVALID_SUBCLASS);
}

@Override
void createWidget (int index) {
	parent.createItem (this, index);
	setOrientation (true);
	hookEvents ();
	register ();
	text = "";
}

@Override
void deregister() {
	super.deregister ();
	if (labelHandle != 0) display.removeWidget (labelHandle);
}

@Override
void destroyWidget () {
	parent.destroyItem (this);
	releaseHandle ();
}

/**
 * Returns a rectangle describing the receiver's size and location
 * relative to its parent.
 *
 * @return the receiver's bounding rectangle
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.4
 */
public Rectangle getBounds () {
	checkWidget();
	GtkAllocation allocation = new GtkAllocation ();
	gtk_widget_get_allocation (handle, allocation);
	int x = allocation.x;
	int y = allocation.y;
	int width = (state & ZERO_WIDTH) != 0 ? 0 : allocation.width;
	int height = (state & ZERO_HEIGHT) != 0 ? 0 : allocation.height;
	if ((parent.style & SWT.MIRRORED) != 0) x = parent.getClientWidth () - width - x;
	return new Rectangle (x, y, width, height);
}

/**
 * Returns the control that is used to fill the client area of
 * the tab folder when the user selects the tab item.  If no
 * control has been set, return <code>null</code>.
 * <p>
 * @return the control
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Control getControl () {
	checkWidget ();
	return control;
}

/**
 * Returns the receiver's parent, which must be a <code>TabFolder</code>.
 *
 * @return the receiver's parent
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public TabFolder getParent () {
	checkWidget ();
	return parent;
}

/**
 * Returns the receiver's tool tip text, or null if it has
 * not been set.
 *
 * @return the receiver's tool tip text
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public String getToolTipText () {
	checkWidget ();
	return toolTipText;
}

@Override
long /*int*/ gtk_enter_notify_event (long /*int*/ widget, long /*int*/ event) {
	parent.gtk_enter_notify_event (widget, event);
	return 0;
}

@Override
long /*int*/ gtk_mnemonic_activate (long /*int*/ widget, long /*int*/ arg1) {
	return parent.gtk_mnemonic_activate (widget, arg1);
}

@Override
void hookEvents () {
	super.hookEvents ();
	if (labelHandle != 0) OS.g_signal_connect_closure_by_id (labelHandle, display.signalIds [MNEMONIC_ACTIVATE], 0, display.getClosure (MNEMONIC_ACTIVATE), false);
	OS.g_signal_connect_closure_by_id (handle, display.signalIds [ENTER_NOTIFY_EVENT], 0, display.getClosure (ENTER_NOTIFY_EVENT), false);
}

@Override
void register () {
	super.register ();
	if (labelHandle != 0) display.addWidget (labelHandle, this);
}


@Override
void release (boolean destroy) {
	if (OS.GTK3) {
		//Since controls are now nested under the tabItem,
		//tabItem is responsible for it's release.
		if (control != null && !control.isDisposed ()) {
			control.release (destroy);
		}
	}
	super.release (destroy);
}

@Override
void releaseHandle () {
	super.releaseHandle ();
	pageHandle = labelHandle = imageHandle = 0;
	parent = null;
}

@Override
void releaseParent () {
	super.releaseParent ();
	int index = parent.indexOf (this);
	if (index == parent.getSelectionIndex ()) {
		if (control != null) control.setVisible (false);
	}
}

/**
 * Sets the control that is used to fill the client area of
 * the tab folder when the user selects the tab item.
 * <p>
 * @param control the new control (or null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li>
 *    <li>ERROR_INVALID_PARENT - if the control is not in the same widget tree</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setControl (Control control) {
	checkWidget ();
	if (control != null) {
		if (control.isDisposed ()) error (SWT.ERROR_INVALID_ARGUMENT);
		if (control.parent != parent) error (SWT.ERROR_INVALID_PARENT);
	}

	if (control != null && OS.GTK3) {
		/*
		* Bug 454936 (see also other 454936 references in TabFolder)
		* Architecture Fix:
		*  We reparent the child to be a child of the 'tab' rather than tabfolder's parent swtFixed container.
		*  Note, this reparenting is only on the GTK side, not on the SWT side.
		*
		*  Note, GTK2 and GTK3 child nesting behaviour is different now.
		*  GTK2:
		*    swtFixed
		*    |-- GtkNoteBook
		*    |   |-- tabLabel1
		*    |   |-- tabLabel2
		*    |-- swtFixed (child1)  //child is sibling of Notebook
		*    |-- swtFixed (child2)
		*
		*  GTK3+:
		*  	swtFixed
		*  	|--	GtkNoteBook
		*  		|-- tabLabel1
		*  		|-- tabLabel2
		*  		|-- swtFixed (child1) //child now child of Notebook.
		*  		|-- swtFixed (child2)
		*
		*  This corrects the hierarchy so that children are beneath gtkNotebook (as oppose to
		*  being siblings) and thus fixes DND and background color issues.
		*  In gtk2, reparenting doesn't function properly (tab content appear blank),
		*  so this is a gtk3-specific behavior.
		*
		*  Note about the reason for reparenting:
		*   Reparenting (as oppose to adding widget to a tab in the first place) is neccessary
		*   because you can have a situation where you create a widget before you create a tab. e.g
		*     TabFolder tabFolder = new TabFolder(shell, 0);
		*     Composite composite = new Composite(tabFolder, 0);
		*     TabItem tabItem = new TabItem(tabFolder, 0);
		*     tabitem.setControl(composite);
		*/
		OS.gtk_widget_reparent (control.topHandle (), pageHandle);
	}

	Control oldControl = this.control, newControl = control;
	this.control = control;
	int index = parent.indexOf (this), selectionIndex = parent.getSelectionIndex();
	if (index != selectionIndex) {
		if (newControl != null) {
			if (selectionIndex != -1) {
				Control selectedControl = parent.getItem(selectionIndex).getControl();
				if (selectedControl == newControl) return;
			}
			newControl.setVisible(false);
			return;
		}
	}
	if (newControl != null) {
		newControl.setBounds (parent.getClientArea ());
		newControl.setVisible (true);
	}

	if (oldControl != null && newControl != null && oldControl != newControl)
		oldControl.setVisible (false);
}

void setFontDescription (long /*int*/ font) {
	setFontDescription (labelHandle, font);
	setFontDescription (imageHandle, font);
}

void setForegroundColor (GdkColor color) {
	/* Don't set the color in vbox handle (it doesn't draw) */
	setForegroundColor (labelHandle, color, false);
	setForegroundColor (imageHandle, color, false);
}

@Override
public void setImage (Image image) {
	checkWidget ();
	super.setImage (image);
	if (image != null) {
		ImageList imageList = parent.imageList;
		if (imageList == null) imageList = parent.imageList = new ImageList ();
		int imageIndex = imageList.indexOf (image);
		if (imageIndex == -1) {
			imageIndex = imageList.add (image);
		} else {
			imageList.put (imageIndex, image);
		}
		long /*int*/ pixbuf = imageList.getPixbuf (imageIndex);
		gtk_image_set_from_pixbuf (imageHandle, pixbuf);
		OS.gtk_widget_show (imageHandle);
	} else {
		gtk_image_set_from_pixbuf (imageHandle, 0);
		OS.gtk_widget_hide (imageHandle);
	}
}

@Override
void setOrientation (boolean create) {
	if ((parent.style & SWT.RIGHT_TO_LEFT) != 0 || !create) {
		int dir = (parent.style & SWT.RIGHT_TO_LEFT) != 0 ? OS.GTK_TEXT_DIR_RTL : OS.GTK_TEXT_DIR_LTR;
		if (handle != 0) OS.gtk_widget_set_direction (handle, dir);
		if (labelHandle != 0) OS.gtk_widget_set_direction (labelHandle, dir);
		if (imageHandle != 0) OS.gtk_widget_set_direction (imageHandle, dir);
		if (pageHandle != 0) OS.gtk_widget_set_direction (pageHandle, dir);
	}
}

/**
 * Sets the receiver's text.  The string may include
 * the mnemonic character.
 * </p>
 * <p>
 * Mnemonics are indicated by an '&amp;' that causes the next
 * character to be the mnemonic.  When the user presses a
 * key sequence that matches the mnemonic, a selection
 * event occurs. On most platforms, the mnemonic appears
 * underlined but may be emphasised in a platform specific
 * manner.  The mnemonic indicator character '&amp;' can be
 * escaped by doubling it in the string, causing a single
 * '&amp;' to be displayed.
 * </p>
 *
 * @param string the new text
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 */
@Override
public void setText (String string) {
	checkWidget ();
	if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
	super.setText (string);
	char [] chars = fixMnemonic (string);
	byte [] buffer = Converter.wcsToMbcs (null, chars, true);
	OS.gtk_label_set_text_with_mnemonic (labelHandle, buffer);
	if (string.length () != 0) {
		OS.gtk_widget_show (labelHandle);
	} else {
		OS.gtk_widget_hide (labelHandle);
	}
}

/**
 * Sets the receiver's tool tip text to the argument, which
 * may be null indicating that the default tool tip for the
 * control will be shown. For a control that has a default
 * tool tip, such as the Tree control on Windows, setting
 * the tool tip text to an empty string replaces the default,
 * causing no tool tip text to be shown.
 * <p>
 * The mnemonic indicator (character '&amp;') is not displayed in a tool tip.
 * To display a single '&amp;' in the tool tip, the character '&amp;' can be
 * escaped by doubling it in the string.
 * </p>
 *
 * @param string the new tool tip text (or null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setToolTipText (String string) {
	checkWidget ();
	toolTipText = string;
}

}
