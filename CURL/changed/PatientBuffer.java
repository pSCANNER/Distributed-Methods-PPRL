import java.util.*;
import java.util.concurrent.SynchronousQueue;

public class PatientBuffer {
	static Queue<Integer> queue;
	public PatientBuffer(){
		queue = new LinkedList<Integer>();
	}
	public  void printQueue(){
		System.out.println(queue.poll());
	}
	public static class ReaderThread extends Thread{

		@Override
		public void run() {
			try {
				for(;;){
					if(queue.size() != 0){
						System.out.println(queue.poll());
					}
					else{
						sleep(150);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	public static class AddThread implements Runnable{

		@Override
		public void run() {
			for(int i = 0; i < 10; i++){
				queue.add(new Integer(i));
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}
	public static void main( String args[] ){
		PatientBuffer pb = new PatientBuffer();
		PatientBuffer.AddThread at = new PatientBuffer.AddThread();
		Thread a = new Thread(at);
		a.start();
		PatientBuffer.ReaderThread rt = new PatientBuffer.ReaderThread();
	    Thread t = new Thread(rt);
		t.start();
	}
	
}
