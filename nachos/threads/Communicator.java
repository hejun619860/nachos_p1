package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

public class Communicator {
	/**
	 * Allocate a new communicator.
	 */
Lock lock;
LinkedList<Integer> buffer;
Condition speaker;
Condition listener;
int speakercounter;
int listenercounter;

	public Communicator() {
    	lock=new Lock();
    	buffer=new LinkedList<Integer>();
    	speaker=new Condition(lock);
    	listener=new Condition(lock);
    	speakercounter=0;
    	listenercounter=0;
    }

    
 public void speak(int word) {
    	boolean intStatus = Machine.interrupt().disable();
       	lock.acquire();
    	if(listenercounter==0)
        {
    		speakercounter++;
    	buffer.offer(word);
        speaker.sleep();
        listener.wake();
        speakercounter--;
        }
    	else
    	{
    	 buffer.offer(word);
    	 listener.wake();
    	}
       lock.release();
       Machine.interrupt().restore(intStatus);
       return;
    }
    
public int listen() {
    	boolean intStatus = Machine.interrupt().disable();
    	lock.acquire();
    	if(speakercounter!=0)
    	{speaker.wake();
    	 listener.sleep();
    	 }
    	else
    	{listenercounter++;
    	 listener.sleep();
    	 listenercounter--;
    	}
    	lock.release();
    	Machine.interrupt().restore(intStatus);
	    return buffer.poll();
    }
	
	public static void selfTest(){
	    final Communicator com = new Communicator();
	    final long times[] = new long[4];
	    final int words[] = new int[2];
	    KThread speaker1 = new KThread( new Runnable () {
	        public void run() {
	        	System.out.println("speaker1");
	            com.speak(4);
	            times[0] = Machine.timer().getTime();
	        }
	    });
	    speaker1.setName("S1");
	    KThread speaker2 = new KThread( new Runnable () {
	        public void run() {
	        	System.out.println("speaker2");
	            com.speak(7);
	            times[1] = Machine.timer().getTime();
	        }
	    });
	    speaker2.setName("S2");
	    KThread listener1 = new KThread( new Runnable () {
	        public void run() {
	        	System.out.println("listener1");
	            times[2] = Machine.timer().getTime();
	            words[0] = com.listen();
	        }
	    });
	    listener1.setName("L1");
	    KThread listener2 = new KThread( new Runnable () {
	        public void run() {
	        	System.out.println("listener2");
	            times[3] = Machine.timer().getTime();
	            words[1] = com.listen();
	        }
	    });
	    listener2.setName("L2");
	    
	    speaker1.fork(); speaker2.fork(); listener1.fork(); listener2.fork();
	    System.out.println("before join");
	    speaker1.join(); speaker2.join(); listener1.join(); listener2.join();
	    System.out.println("after join");
	    
	    Lib.assertTrue(words[0] == 4, "Didn't listen back spoken word."); 
	    Lib.assertTrue(words[1] == 7, "Didn't listen back spoken word.");
	    Lib.assertTrue(times[0] > times[2], "speak() returned before listen() called.");
	    Lib.assertTrue(times[1] > times[3], "speak() returned before listen() called.");
	}
}
