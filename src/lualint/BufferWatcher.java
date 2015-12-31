package lualint;

import java.util.Vector;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.EditBus.EBHandler;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.jEdit;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicBoolean;

import errorlist.ErrorSource;
import errorlist.DefaultErrorSource;
import errorlist.DefaultErrorSource.DefaultError;

public class BufferWatcher 
{
	
	
	private Pattern errorPattern = Pattern.compile("^((?:\\w:)?[^:]+?):(\\d+):\\s*(.+)");
	
	private   DefaultErrorSource errorSrc;
	
	public BufferWatcher()
	{
		EditBus.addToBus(this);
		errorSrc = new DefaultErrorSource(this.getClass().getName(), jEdit.getActiveView());
		ErrorSource.registerErrorSource(errorSrc);
	}
	
	public void shutdown()
	{
		EditBus.removeFromBus(this);
		if(errorSrc != null)
		{
			ErrorSource.unregisterErrorSource(errorSrc);
			errorSrc = null;		
		}
	}
	
	private void updateErrorSourceView(View v)
	{
		
		if (errorSrc.getView() != v) {
			ErrorSource.unregisterErrorSource(errorSrc);
			errorSrc = new DefaultErrorSource(this.getClass().getName(), v);
			ErrorSource.registerErrorSource(errorSrc);
		}
	
	}
	
	@EBHandler
	public void handleBufferUpdate(BufferUpdate bu)
	{
		if (( bu.getWhat() == BufferUpdate.SAVED) && jEdit.getBooleanProperty("lualint.parse_buffer", true))
		{
			
			Buffer buffer = bu.getBuffer();
			if(buffer.getMode().toString().equals("lua"))
			{
				String path = buffer.getPath();
				System.out.println(jEdit.getProperty( "Lualint.lua_path", "luac") + " -p " + path);
				clearErrors();
				updateErrorSourceView(bu.getView());
				
				
				ProcessBuilder processBuilder = new ProcessBuilder(jEdit.getProperty( "Lualint.lua_path", "luac"), "-p", path);
				processBuilder.redirectErrorStream(true);
				BufferedReader reader = null;
				try 
				{ 
					Process process = processBuilder.start();  
					reader = new BufferedReader(new InputStreamReader(process.getInputStream()));  
					
					String line = reader.readLine();
					while (line != null)
					{  
						System.out.println(line);
						parseError(path, line);
						line = reader.readLine();
					}  
				} catch (IOException e) {  
					
				} finally {  
					try 
					{ 
						if(reader != null)reader.close();
					}catch(IOException e)
					{
					}
				}  
				
			}
				
		}
	}
	
	
	public void parseError(final String path, String line)
	{
		String prefix = jEdit.getProperty( "Lualint.lua_path", "luac") + ": ";
		if(!line.startsWith(prefix))
		{
			return;
		}
		
		line = line.substring(prefix.length());
		final Matcher matcher = errorPattern.matcher(line);
		if(matcher.find() && matcher.groupCount() >= 3)
		{
			
			SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						errorSrc.addError(new DefaultError(errorSrc,
							ErrorSource.ERROR, 
							path, 
							Integer.parseInt(matcher.group(2)) - 1 , 
							0,
							0,
							matcher.group(3) ));
					}
				});
			
		}
	}
	
	
	
	private void clearErrors()
	{
		errorSrc.clear();
	}
	
}
