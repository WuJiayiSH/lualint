package lualint;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class LualintOptionPane extends AbstractOptionPane 
{
	
	private static final long serialVersionUID = 1L;
	
	private JTextField luaPathTF;
	private JCheckBox passBufferCheckBox;
	public LualintOptionPane()
	{
		super("Lualint");
		setBorder(new EmptyBorder(5, 5, 5, 5));
		
		luaPathTF = new JTextField(jEdit.getProperty( "Lualint.lua_path", "luac") , 40);
		addComponent("Luac Path: ", luaPathTF);
		
		passBufferCheckBox = new JCheckBox(
			"Parse buffer on file save",
			jEdit.getBooleanProperty("Lualint.parse_buffer", true));
		addComponent(passBufferCheckBox);
		
		
	}
	
	
	@Override
	protected void _save()
	{
		jEdit.setProperty("Lualint.lua_path", luaPathTF.getText());
		jEdit.setBooleanProperty("Lualint.parse_buffer", passBufferCheckBox.isSelected());
	}
}

