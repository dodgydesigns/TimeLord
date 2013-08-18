package widgets;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

/**
 * This utility is used to limit the number of lines and characters that can be
 * added to a text area.
 * 
 * @author mullsy
 *
 */
public class LimitedLinesDocument extends DefaultStyledDocument
{
	private static final long serialVersionUID = 1L;

	private static final String EOL = "\n";

	private final int maxLines;
	private final int maxChars;


	public LimitedLinesDocument(int maxLines, int maxChars)
	{
		this.maxLines = maxLines;
		this.maxChars = maxChars;
	}


	@Override
	public void insertString(int offs, String str, AttributeSet attribute) throws BadLocationException
	{
		boolean ok = true;

		String currentText = getText(0, getLength());

		// check max lines
		if (str.contains(EOL))
		{
			if (occurs(currentText, EOL) >= maxLines - 1)
			{
				ok = false;
			}
		}
		else
		{
			// check max chars
			String[] lines = currentText.split("\n");
			int lineBeginPos = 0;
			for (int lineNum = 0; lineNum < lines.length; lineNum++)
			{
				int lineLength = lines[lineNum].length();
				int lineEndPos = lineBeginPos + lineLength;

				if (lineBeginPos <= offs && offs <= lineEndPos)
				{
					if (lineLength + 1 > maxChars)
					{
						ok = false;
						break;
					}
				}
				lineBeginPos = lineEndPos;
				lineBeginPos++; // for \n
			}
		}

		if (ok)
			super.insertString(offs, str, attribute);
	}


	public int occurs(String str, String subStr)
	{
		int occurrences = 0;
		int fromIndex = 0;

		while (fromIndex > -1)
		{
			fromIndex = str.indexOf(subStr, occurrences == 0 ? fromIndex : fromIndex + subStr.length());
			if (fromIndex > -1)
			{
				occurrences++;
			}
		}

		return occurrences;
	}
}