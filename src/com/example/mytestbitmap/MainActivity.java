package com.example.mytestbitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import net.tsz.afinal.FinalBitmap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;

@TargetApi(12)
public class MainActivity extends Activity {
	private AtomicInteger i = new AtomicInteger(0);
	private final byte[] LOCKED = new byte[0];
	protected Bitmap bitmap;
	protected Handler mhander;
	private boolean bitmaplock =true ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        int size =0;
        mhander = new Handler(){
        	
        	@Override
        	public void handleMessage(Message msg) {
        		// TODO Auto-generated method stub
        		if(msg.what== 1)
        		{
        			bitmaplock =false;
        		}
        		
        		
        		
        		
        	}
        };
        bitmap = BitMapUtil.getBitmap("/data/DU8A2927.jpg");
        bitmap = BitMapUtil.getBitmapUnZoom("/data/DU8A2927.jpg");
//      
//		
//			BitmapFactory.Options mopOptions = new Options();
////			ArrayList<SoftReference> mlist = new ArrayList<SoftReference>();
			final ArrayList<Bitmap> mlist = new ArrayList<Bitmap>();
//			for( i =1;i<50;i++){
//			
//			mopOptions.inSampleSize = 1;
//			
//			try {
//				new Thread(new Runnable() {
//					
//					@Override
//					public void run() {
//						 Bitmap bitmap = null;
//						// TODO Auto-generated method stub
//						if((i&1)==0)
//							bitmap =BitMapUtil.getBitmap("/data/mytest/1"+i+".jpg");// 将输入流转换成bitmap
//						else
//							bitmap = BitMapUtil.getBitmap("/data/mytest/1"+i+".jpg", 400, 200);
//						 System.out.println("mytestsize1111"+"bitmap size="+bitmap.getWidth() + "   " + bitmap.getHeight());
//						 mlist.add(bitmap);
////							size = size +bitmap.getByteCount();
//							bitmap =null;
//							 System.out.println("mytest1111112222222"+"bitmap size="+BitMapUtil.getCacheSize());
//					}
//				}).start();
//				
////				File file = new File("/data/mytest/1"+i+".jpg");
////				InputStream in =null;
////				if(file.exists())
////					try {
////						in = new FileInputStream(file);
////						bitmap=BitmapFactory.decodeStream(in, null, mopOptions);
////					} catch (FileNotFoundException e) {
////						// TODO Auto-generated catch block
////						e.printStackTrace();
////					}
////			
//				
//				
//				
////				SoftReference<Bitmap> abcSoftRef=new SoftReference<Bitmap>(bitmap);
////				mlist.add(abcSoftRef);
//				
//			} catch (OutOfMemoryError e) {
//				// TODO: handle exception
////				 System.out.println("mytest"+"bitmap size="+size);
////				 	if(size >100 * 1024 *1024)
////				 		break;
////				 	else{
//				 		 System.out.println("mytest111111"+"bitmap size="+BitMapUtil.getCacheSize());
////				 		 break;
////				 	}
//				e.printStackTrace();
//				break;
//			}
//		
//			}
			ImageView im = new ImageView(this);
//			  FinalBitmap fb =FinalBitmap.create(this);
//
//			  fb.configLoadingImage(R.drawable.ic_launcher);
//			  fb.display(im, "http://img0.178.com/wow/200912/53807051644/53807097589.jpg");
//			  setContentView(im);
			bitmaplock =true;
			new Thread(new Runnable() {
				
			
				@Override
				public void run() {
					// TODO Auto-generated method stub
					 bitmap = BitMapUtil.getBitmap("http://img0.178.com/wow/200912/53807051644/53807097589.jpg");
					
						 bitmaplock=false;
					
					
				}
			}).start();
			
			while(bitmaplock){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			im.setImageBitmap(bitmap);
			setContentView(im);
			
			
			ExecutorService exec = Executors.newCachedThreadPool();
			
			
				
			
			for ( int k =0 ; k<50;k++)
			{
				exec.execute(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub\
						synchronized (LOCKED) {
						int k = i.get();
						System.out.println("mytest url before  " + k);
						i.incrementAndGet();
						Bitmap bitmap = BitMapUtil.getBitmap("http://10.3.34.43/test/1"+k+".jpg");
						if(bitmap !=null)
							mlist.add(bitmap) ;
						else
							System.out.println("mytest bitmap =null  " + k);
						}
					}
				});
			
				}
		
			exec.shutdown();
       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
