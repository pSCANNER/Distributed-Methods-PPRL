package rosita.linkage.tests;
import rosita.linkage.util.StopWatch;

public class TimerTest 
{
	public static void wait (int n)
	{
		long t0,t1;
		t0 = System.currentTimeMillis();
		do
		{
			t1=System.currentTimeMillis();
		}
		while (t1-t0<(n));
	}

	public static void main(String[] args)
	{
		
		StopWatch s = new StopWatch();
		
		s.start();
		
		wait(3123);

		s.stop();
		
		System.out.println("time=" + s.getElapsedTimeSecsDouble(2));


	}
}
