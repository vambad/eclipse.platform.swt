package org.eclipse.swt.examples.javaviewer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.*;
import java.io.*;

class JavaLineStyler implements LineStyleListener {
	JavaScanner scanner = new JavaScanner();
	int[] tokenColors;
	Color[] colors;
	Vector blockComments = new Vector();

	public static final int EOF= -1;
	public static final int EOL= 10;

	public static final int WORD=		0;
	public static final int WHITE=		1;
	public static final int KEY=		2;
	public static final int COMMENT=	3;	// single line comment:	//
	public static final int STRING=		5;
	public static final int OTHER=		6;
	public static final int NUMBER=		7;

	public static final int MAXIMUM_TOKEN= 8;

public JavaLineStyler() {
	initializeColors();
	scanner = new JavaScanner();
}

Color getColor(int type) {
	if (type < 0 || type >= tokenColors.length) {
		return null;
	}
	return colors[tokenColors[type]];
}

boolean inBlockComment(int start, int end) {
	for (int i=0; i<blockComments.size(); i++) {
		int[] offsets = (int[])blockComments.elementAt(i);
		// start of comment in the line
		if ((offsets[0] >= start) && (offsets[0] <= end)) return true;
		// end of comment in the line
		if ((offsets[1] >= start) && (offsets[1] <= end)) return true;
		if ((offsets[0] <= start) && (offsets[1] >= end)) return true;
	}
	return false;
}

void initializeColors() {
	Display display = Display.getDefault();
	colors= new Color[] {
		new Color(display, new RGB(0, 0, 0)),		// black
		new Color(display, new RGB(255, 0, 0)),		// red
		new Color(display, new RGB(0, 255, 0)),		// green
		new Color(display, new RGB(0,   0, 255))	// blue
	};
	tokenColors= new int[MAXIMUM_TOKEN];
	tokenColors[WORD]=		0;
	tokenColors[WHITE]=		0;
	tokenColors[KEY]=		3; 
	tokenColors[COMMENT]=	1; 
	tokenColors[STRING]= 	2; 
	tokenColors[OTHER]=		0;
	tokenColors[NUMBER]=	0;
}

void disposeColors() {
	for (int i=0;i<colors.length;i++) {
		colors[i].dispose();
	}
}

/**
 * Event.detail			line start offset (input)	
 * Event.text 			line text (input)
 * LineStyleEvent.styles 	Enumeration of StyleRanges, need to be in order. (output)
 * LineStyleEvent.background 	line background color (output)
 */
public void lineGetStyle(LineStyleEvent event) {
	Vector styles = new Vector();
	int token;
	StyleRange lastStyle;
	if (inBlockComment(event.lineOffset, event.lineOffset + event.lineText.length())) {
		styles.addElement(new StyleRange(event.lineOffset, event.lineText.length(), colors[1], null));
		event.styles = new StyleRange[styles.size()];
		styles.copyInto(event.styles);
		return;
	}
	scanner.setRange(event.lineText);
	token = scanner.nextToken();
	while (token != EOF) {
		if (token == OTHER) {
			// do nothing
		} else if ((token == WHITE) && (!styles.isEmpty())) {
			int start = scanner.getStartOffset() + event.lineOffset;
			lastStyle = (StyleRange)styles.lastElement();
			if (lastStyle.fontStyle != SWT.NORMAL) {
				if (lastStyle.start + lastStyle.length == start) {
					// have the white space take on the style before it to minimize font style
					// changes
					lastStyle.length += scanner.getLength();
				}
			}
		} else {		
			Color color = getColor(token);
			if (color != colors[0]) {		// hardcoded default foreground color, black
				StyleRange style = new StyleRange(scanner.getStartOffset() + event.lineOffset, scanner.getLength(), color, null);
				if (token == KEY) {
					style.fontStyle = SWT.BOLD;
				}
				if (styles.isEmpty()) {
					styles.addElement(style);
				} else {
					lastStyle = (StyleRange)styles.lastElement();
					if (lastStyle.similarTo(style) && (lastStyle.start + lastStyle.length == style.start)) {
						lastStyle.length += style.length;
					} else {
						styles.addElement(style); 
					}
				} 
			} 
		}
		token= scanner.nextToken();
	}
	event.styles = new StyleRange[styles.size()];
	styles.copyInto(event.styles);
}

public void parseBlockComments(String text) {
	blockComments = new Vector();
	StringBufferInputStream buffer = new StringBufferInputStream(text);
	int ch;
	boolean blkComment = false;
	int cnt = 0;
	int[] offsets = new int[2];
	boolean done = false;
	while (!done) {
		switch (ch = buffer.read()) {
			case -1 : {
				if (blkComment) {
					offsets[1] = cnt;
					blockComments.addElement(offsets);
				}
				done = true;
				break;
			}
			case '/' : {
				ch = buffer.read();
				if ((ch == '*') && (!blkComment)) {
					offsets = new int[2];
					offsets[0] = cnt;
					blkComment = true;
					cnt++;	
				} else {
					cnt++;
				}						
				cnt++;
				break;
			}
			case '*' : {
				if (blkComment) {
					ch = buffer.read();
					cnt++;
					if (ch == '/') {
						blkComment = false;	
						offsets[1] = cnt;
						blockComments.addElement(offsets);
					}
				}
				cnt++;	
				break;
			}
			default : {
				cnt++;				
				break;
			}
		}
	}		
}

/**
 * A simple fuzzy scanner for Java
 */
public class JavaScanner {

	protected Hashtable fgKeys= null;
	protected StringBuffer fBuffer= new StringBuffer();
	protected String fDoc;
	protected int fPos;
	protected int fEnd;
	protected int fStartToken;
	protected boolean fEofSeen= false;

	private String[] fgKeywords= { 
		"abstract",
		"boolean", "break", "byte",
		"case", "catch", "char", "class", "continue",
		"default", "do", "double",
		"else", "extends",
		"false", "final", "finally", "float", "for",
		"if", "implements", "import", "instanceof", "int", "interface",
		"long",
		"native", "new", "null",
		"package", "private", "protected", "public",
		"return",
		"short", "static", "super", "switch", "synchronized",
		"this", "throw", "throws", "transient", "true", "try",
		"void", "volatile",
		"while"
	};

	public JavaScanner() {
		initialize();
	}

	/**
	 * Returns the ending location of the current token in the document.
	 */
	public final int getLength() {
		return fPos - fStartToken;
	}

	/**
	 * Initialize the lookup table.
	 */
	void initialize() {
		fgKeys= new Hashtable();
		Integer k= new Integer(KEY);
		for (int i= 0; i < fgKeywords.length; i++)
			fgKeys.put(fgKeywords[i], k);
	}

	/**
	 * Returns the starting location of the current token in the document.
	 */
	public final int getStartOffset() {
		return fStartToken;
	}

	/**
	 * Returns the next lexical token in the document.
	 */
	public int nextToken() {
		int c;
		fStartToken= fPos;
		while (true) {
			switch (c= read()) {			
			case EOF:
				return EOF;				
			case '/':	// comment
				c= read();
				if (c == '/') {
					while (true) {
						c= read();
						if ((c == EOF) || (c == EOL)) {
							unread(c);
							return COMMENT;
						}
					}
				} else {
					unread(c);
				}
				return OTHER;
			case '\'':	// char const
				character: for(;;) {
					c= read();
					switch (c) {
						case '\'':
							return STRING;
						case EOF:
							unread(c);
							return STRING;
						case '\\':
							c= read();
							break;
						}
				}

			case '"':	// string
				string: for (;;) {
					c= read();
					switch (c) {
						case '"':
							return STRING;
						case EOF:
							unread(c);
							return STRING;
						case '\\':
							c= read();
							break;
						}
				}

			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				do {
					c= read();
				} while(Character.isDigit((char)c));
				unread(c);
				return NUMBER;
			default:
				if (Character.isWhitespace((char)c)) {
					do {
						c= read();
					} while(Character.isWhitespace((char)c));
					unread(c);
					return WHITE;
				}
				if (Character.isJavaIdentifierStart((char)c)) {
					fBuffer.setLength(0);
					do {
						fBuffer.append((char)c);
						c= read();
					} while(Character.isJavaIdentifierPart((char)c));
					unread(c);
					Integer i= (Integer) fgKeys.get(fBuffer.toString());
					if (i != null)
						return i.intValue();
						return WORD;
				}
				return OTHER;
			}
		}
	}

	/**
	 * Returns next character.
	 */
	protected int read() {
		if (fPos <= fEnd) {
			return fDoc.charAt(fPos++);
		}
		return EOF;
	}

	public void setRange(String text) {
		fDoc= text;
		fPos= 0;
		fEnd= fDoc.length() -1;
	}

	protected void unread(int c) {
		if (c != EOF)
	    	fPos--;
	}
}





}
