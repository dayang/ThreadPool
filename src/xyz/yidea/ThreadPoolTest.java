package xyz.yidea;

import org.junit.*;

public class ThreadPoolTest {
	
	private ThreadPool pool = null;
	
	@Before
	public void init(){
		pool = new ThreadPool(10,15,30);
	}
	
	@After
	public void release(){
		try{
			Thread.sleep(5000);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		pool.destory();
		pool = null;
	}
	
	@Test
	public void testnormal(){
		for(int i = 0; i<50;i++){
			TestTask tt = new TestTask();
			pool.execute(tt);
		}
	}
	
	public static class TestTask implements ThreadPool.Task{
		
		@Override
		public void run() {
			try{
				Thread.sleep(30);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			System.out.println(Thread.currentThread().getName());
		}
		
	}
}
