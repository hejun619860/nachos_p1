package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;



class KThreadTimer{
	private KThread thread = null;
	private long time = 0;
	public KThreadTimer(KThread thread, long time){
		this.thread=thread;
		this.time=time;
	}
	public KThread getThread(){
		return thread;
	}
	public long getTime(){
		return time;
	}
}
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	
//	public PriorityQueue<KThreadTimer> sleepingThreads = new PriorityQueue<KThreadTimer>();
	private LinkedList<KThreadTimer> kthreadtimerlist = new LinkedList<KThreadTimer>();
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		boolean status = Machine.interrupt().disable();
		long currentTime = Machine.timer().getTime();
			for (int i = 0; i < kthreadtimerlist.size();i++) {
				if (kthreadtimerlist.get(i).getTime() < currentTime  ){
					KThread thread = kthreadtimerlist.get(i).getThread();
					thread.ready();
					kthreadtimerlist.remove(i);
					i=0;
				}
			}
	    KThread.yield();
		Machine.interrupt().restore(status);
}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		boolean status = Machine.interrupt().disable();
		long waketime = Machine.timer().getTime() + x;
		
	
		kthreadtimerlist.add( new KThreadTimer(KThread.currentThread(), waketime) );
		KThread.sleep();
		Machine.interrupt().restore(status);
	}
	
	// Place this function inside Alarm. And make sure Alarm.selfTest() is called inside ThreadedKernel.selfTest() method.

	public static void selfTest() {
	    KThread t1 = new KThread(new Runnable() {
	        public void run() {
	            long time1 = Machine.timer().getTime();
	            int waitTime = 10000;
	            System.out.println("Thread calling wait at time:" + time1);
	            ThreadedKernel.alarm.waitUntil(waitTime);
	            System.out.println("Thread 1 woken up after:" + (Machine.timer().getTime() - time1));
	            Lib.assertTrue((Machine.timer().getTime() - time1) >= waitTime, " thread woke up too early.");
	            
	        }
	    });
	    t1.setName("T2");
	   
	    KThread t2 = new KThread(new Runnable() {
	        public void run() {
	            long time2 = Machine.timer().getTime();
	            int waitTime = 8000;
	            System.out.println("Thread calling wait at time:" + time2);
	            ThreadedKernel.alarm.waitUntil(waitTime);
	            System.out.println("Thread 2 woken up after:" + (Machine.timer().getTime() - time2));
	            Lib.assertTrue((Machine.timer().getTime() - time2) >= waitTime, " thread woke up too early.");
	            
	        }
	    });
	    t2.setName("T2");
	    t1.fork();
	    t2.fork();

	    t1.join();
	    t2.join();
	}
}
