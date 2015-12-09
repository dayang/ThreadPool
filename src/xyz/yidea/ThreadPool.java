package xyz.yidea;

import java.util.LinkedList;
import java.util.List;

public class ThreadPool extends Thread{
	private final static int DEFAULT_MIN_THREAD_SIZE = 5;
	
	private final static int DEFAULT_ACTIVE_THREAD_SIZE = 8;
	
	private final static int DEFAULT_MAX_THREAD_SIZE = 10;
	
	private int min_thread_size = 0;
	
	private int active_thread_size = 0;
	
	private int max_thread_size = 0;
	
	private List<Task> taskQueue = null;
	
	private List<ChildThread> pools = null;
	
	private boolean destory = false;
	
	public ThreadPool(){
		this(DEFAULT_MIN_THREAD_SIZE,DEFAULT_ACTIVE_THREAD_SIZE,DEFAULT_MAX_THREAD_SIZE);
	}
	
	public ThreadPool(int min_thread_size,int active_thread_size,
			int max_thread_size){
		this.min_thread_size = min_thread_size;
		this.active_thread_size = active_thread_size;
		this.max_thread_size = max_thread_size;
		createPool();
	}
	
	private void createPool(){
		taskQueue = new LinkedList<Task>();
		pools = new LinkedList<ChildThread>();
		for(int i = 0;i<min_thread_size;i++){
			ChildThread t = new ChildThread(taskQueue);
			((LinkedList<ChildThread>)pools).add(t);
			t.start();
		}
		this.start();
	}
	
	private Object lock = new Object();
	
	public void run(){
		while(!destory){
			if(getFreeThreadSize() >= active_thread_size){
				((LinkedList<ChildThread>)pools).removeFirst();
				System.out.println("destory one thread");
				synchronized(lock){
					try{
						lock.wait(1000);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}else{
				System.out.println("task number less than active size");
				synchronized(lock){
					try{
						lock.wait(3000);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void destory(){
		this.destory = false;
		synchronized(taskQueue){
			for(ChildThread t : pools){
				t.close();
				t.interrupt();
			}
		}
	}
	
	public void execute(Task task){
		int threadSize = pools.size();
		if(threadSize >= max_thread_size){
			System.out.println("more than thread pool size....");
		}
		
		if(threadSize >= min_thread_size && threadSize < max_thread_size){
			createNewThread();
		}
		
		synchronized(taskQueue){
			((LinkedList<Task>)taskQueue).addFirst(task);
			taskQueue.notify();
		}
	}
	
	private void createNewThread(){
		System.out.println("begin create new thread1");
		ChildThread t = new ChildThread(taskQueue);
		((LinkedList<ChildThread>)pools).add(t);
		t.start();
		System.out.println("new thread have create success!");
	}
	
	public int getRunningThreadSize(){
		int count = 0;
		synchronized(taskQueue){
			for(ChildThread c: pools){
				if(c.getCurrentThreadState()){
					count++;
				}
			}
		}
		System.out.println("running count:" + count);
		return count;
	}
	
	public int getFreeThreadSize(){
		int count = 0;
		synchronized(taskQueue){
			for(ChildThread c : pools){
				if(!c.getCurrentThreadState()){
					count++;
				}
			}
		}
		System.out.println("free count:"+count);
		return count;
	}
	
	public static interface Task{
		public void run();
	}
	
	private class ChildThread extends Thread{
		private boolean state = false;
		
		private boolean closed = false;
		
		private List<Task> pools = null;
		
		public ChildThread(List<Task> pools){
			System.out.println("Thread Name:" + getName());
			this.pools = pools;
		}
		
		public void run(){
			while(!closed){
				Task task = null;
				synchronized(pools){
					if(pools.isEmpty()){
						try{
							pools.wait();
						}catch(InterruptedException e){
							e.printStackTrace();
						}
					}else{
						task = ((LinkedList<Task>)pools).removeLast();
					}
				}
				
				state = true;   //identify this thread is running state
				
				if(null != task)
					task.run();
				state = false;  //set this thread have finished task but no stop
			}
		}
		
		public void close(){
			closed = true;
		}
		
		public boolean getCurrentThreadState(){
			return state;
		}
	}
}
