package lualint;
import org.gjt.sp.jedit.EditPlugin;

public class LualintPlugin extends EditPlugin
{
	BufferWatcher bw;
	public void start()
	{
		System.out.println("LualintPlugin");
		bw = new BufferWatcher();
	}
	public void stop() 
	{
		bw.shutdown();
		bw = null;
	}
}
