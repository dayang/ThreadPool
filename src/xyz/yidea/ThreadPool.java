package xyz.yidea;

import java.util.LinkedList;
import java.util.List;

import xyz.yidea.TaskQueue.ExceedMaxException;

public class ThreadPool extends Thread{
	private final static int DEFAULT_MIN_THREAD_SIZE = 5;
	
	private final static int DEFAULT_ACTIVE_THREAD_SIZE = 8;
	
	private final static int DEFAULT_MAX_THREAD_SIZE = 10;
	
	private final static int DEFAULT_MAX_TASK_SIZE = 30;
	
	private int min_thread_size = 0;
	
	private int active_thread_size = 0;
	
	private int max_thread_size = 0;
	
	private int max_task_size = 0;
	
	private TaskQueue taskQueue = null;      //任务队列
	
	private List<ChildThread> pools = null;       //线程池
	
	private boolean destory = false;
	
	public ThreadPool(){
		this(DEFAULT_MIN_THREAD_SIZE,DEFAULT_ACTIVE_THREAD_SIZE,DEFAULT_MAX_THREAD_SIZE,DEFAULT_MAX_TASK_SIZE);
	}
	
	public ThreadPool(int min_thread_size,int active_thread_size,
			int max_thread_size,int max_task_size){
		this.min_thread_size = min_thread_size;
		this.active_thread_size = active_thread_size;
		this.max_thread_size = max_thread_size;
		this.max_task_size = max_task_size;
		createPool();
	}
	
	private void createPool(){
		taskQueue = new TaskQueue(max_task_size);
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
				synchronized(taskQueue){
					((LinkedList<ChildThread>)pools).removeFirst();
				}
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
	
	/**
	 * destroy thread pool and close all child thread in thread pool,
	 * close every thread for loop
	 */
	public void destory(){
		this.destory = true;   //close thread pool thread
		synchronized(taskQueue){
			for(ChildThread t : pools){
				t.close();       //close child thread one by one
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
			//create new thread to process extra task
			createNewThread();
		}
		
		synchronized(taskQueue){
			try {
				taskQueue.addFirst(task);
			} catch (ExceedMaxException e) {
				e.printStackTrace();
			}
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
	
	/**
	 * get thread pool current running thread size
	 * @return running thread size
	 */
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
	
	
	/**
	 * every thread must implement this interface
	 * @author 杨永华
	 *
	 */
	public static interface Task{
		public void run();
	}
	
	
	/**
	 * current runner state thread
	 * @author 杨永华
	 *
	 */
	private class ChildThread extends Thread{
		private boolean state = false;
		
		private boolean closed = false;
		
		private TaskQueue pools = null;
		
		public ChildThread(TaskQueue pools){
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
						task = pools.removeLast();
					}
				}
				
				state = true;   //identify this thread is running state
				
				if(null != task)
					task.run();
				state = false;  //set this thread have finished task but no stop
			}
		}
		
		/**
		 * close thread by set boolean variable
		 */
		public void close(){
			closed = true;
		}
		
		
		/**
		 * get worker thread current state
		 * @return
		 */
		public boolean getCurrentThreadState(){
			return state;
		}
	}
}
