package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;

class KThreadTimer {
	private KThread thread = null;
	private long waketime = 0;
public KThreadTimer(KThread thread, long waketime) {
		this.thread = thread;
		this.waketime = waketime;
	}
public KThread getThread() {
		return thread;
}
public long getWakeTime() {
		return waketime;
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
	private LinkedList<KThreadTimer> linkedlist = new LinkedList<KThreadTimer>();
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
		long currenttime = Machine.timer().getTime();
		int size = linkedlist.size();
if (size == 0)
			;
		else
			for (int i = 0; i < size; i++) {
				if (currenttime < linkedlist.get(i).getWakeTime());
				else {
					KThread thread = linkedlist.get(i).getThread();
					thread.ready();
					linkedlist.remove(i);
					size--;
					i = 0;
					currenttime = Machine.timer().getTime();
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
		KThreadTimer kthreadtimer = new KThreadTimer(KThread.currentThread(), waketime);
		int size = linkedlist.size();
		if (size == 0)
			linkedlist.add(kthreadtimer);
		else  //order kthreadtimer by increasing order of time
			for (int i = 0; i < size; i++) {
				if (waketime < linkedlist.get(i).getWakeTime()) {
					linkedlist.add(i, kthreadtimer);
					break;
				}
				if (i == size - 1
						&& waketime >= linkedlist.get(i).getWakeTime())
					linkedlist.add(i + 1, kthreadtimer);
			}
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
	            System.out.println("Thread woken up after:" + (Machine.timer().getTime() - time1));
	            Lib.assertTrue((Machine.timer().getTime() - time1) >= waitTime, " thread woke up too early.");
	            
	        }
	    });
	    t1.setName("T1");
	    t1.fork();
	    t1.join();
	}
}
