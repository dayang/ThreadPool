package xyz.yidea;

import java.util.*;
import xyz.yidea.ThreadPool.Task;

public class TaskQueue {
	private int max_value = 0;
	private int taskNumber = 0;
	
	private List<ThreadPool.Task> queue = 
			new LinkedList<ThreadPool.Task>();
	
	public TaskQueue(int max_value){
		if(max_value<0){
			throw new IllegalArgumentException(
					"task queue length should not less than zero!");
			
		}
		this.max_value = max_value;
	}
	
	public void addFirst(Task task) throws ExceedMaxException{
		if(taskNumber >= max_value)                                    //if tasks number is greater than max task number,throw exception
			throw new ExceedMaxException("task queue current size:"
					+ taskNumber + " exceed maximum value: " + max_value);
		taskNumber++;
		((LinkedList<Task>)queue).addFirst(task);
	}
	
	public boolean isEmpty(){
		return queue.isEmpty();
	}
	
	public Task removeLast(){
		if(isEmpty())
			return null;
		taskNumber--;
		return ((LinkedList<Task>)queue).removeLast();
	}
	
	class ExceedMaxException extends Exception{
		private static final long serialVersionUID = 2194487581753097550L;
		
		private String message;
		
		private Throwable t;
		
		public ExceedMaxException(){
			this(null,null);
		}
		
		public ExceedMaxException(String message){
			this(message,null);
		}
		
		public ExceedMaxException(String message,Throwable t){
			super(message,t);
			this.message = message;
			this.t= t;
		}
		
		public String getMessage(){
			return message;
		}
		
		public void setMessage(String message){
			this.message = message;
		}
		
		public Throwable getT(){
			return t;
		}
		
		public void setT(Throwable t){
			this.t= t;
		}
	}
}
