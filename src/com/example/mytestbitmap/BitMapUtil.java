package com.example.mytestbitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.location.Criteria;


@TargetApi(12)
public final class BitMapUtil {

	private static final Size ZERO_SIZE = new Size(0, 0);
	private static final Options OPTIONS_GET_SIZE = new Options();
	private static final Options OPTIONS_DECODE = new Options();
	private static final byte[] LOCKED = new byte[0];  
	private static final int CACHE_MOMORY = 16*1024*1024;//缓存大小
	private static int cache_size=0;
	private static final LinkedList<String> CACHE_ENTRIES = new LinkedList<String>(); // 此对象用来保持Bitmap的回收顺序,保证最后使用的图片被回收

	private static final Map<String, SoftReference<Bitmap>> IMG_CACHE_INDEX = new HashMap<String, SoftReference<Bitmap>>(); // 缓存Bitmap 通过图片路径,图片大小
	private static final int DEFAULT_WIDTH=1280;
	private static final int DEFAULT_HEIGT=720;

	private static int CACHE_SIZE = 200; // 缓存图片数量


	/**
	 * 创建一张图片 如果缓存中已经存在,则返回缓存中的图,否则创建一个新的对象,并加入缓存
	 * 1280*720以上的 进行默认缩放1280*720以上的 
	 * 如果想要获取原图请调用 getBitmapUnZoom();
	 * @param path		图片路径
	 * @return
	 */
	public static Bitmap getBitmap(String path){
		return getBitmap(path ,0,0);
	}
	
	/**
	 * 创建一张不缩放的原图，如过图片过大或者解析出错返回null
	 * @param path 图片的路径 （只能是文件路径）
	 * @return
	 */
	@TargetApi(12)
	public static Bitmap getBitmapUnZoom(String path){
		Size size = getBitMapSize(path);
		Bitmap bitMap = null;
		int width = size.getWidth();
		int height = size.getHeight();
		int bitmapStoreSize = width * height <<2 ;
		if(bitmapStoreSize>CACHE_MOMORY)
		{
			return null;
		}
		
			if (CACHE_ENTRIES.size() >= CACHE_SIZE && !CACHE_ENTRIES.isEmpty()) {
				destoryLast();
			}
			bitMap = useBitmap(path, width, height);
			if (bitMap != null && !bitMap.isRecycled()) {
				return bitMap;
			}	
			while(cache_size + bitmapStoreSize >= CACHE_MOMORY)
			{
				destoryLast();
			}
			try {
				bitMap = BitmapFactory.decodeFile(path);	
			} catch (OutOfMemoryError err) {
				
				if(CACHE_ENTRIES.isEmpty())
				{
					System.out.println("mytest the momory is not so much for bitmap");	
				}
				else
				{
					destoryLast();
					System.out.println(CACHE_SIZE);
					bitMap = BitmapFactory.decodeFile(path);	
				}
			}
		
			String key = createKey(path, width, height);
			synchronized (LOCKED) {  
			cache_size+=bitMap.getByteCount();
			SoftReference <Bitmap> mSoftBitmap = new SoftReference<Bitmap>(bitMap);
			
				IMG_CACHE_INDEX.put(key, mSoftBitmap);
				bitMap =null;
				CACHE_ENTRIES.addFirst(key);
			 }
		return bitMap;
		
	}
	/**
	 * 创建一张图片 如果缓存中已经存在,则返回缓存中的图,否则创建一个新的对象,并加入缓存
	 * 宽度,高度,为了缩放原图减少内存的,如果输入的宽,高,比原图大,返回原图
	 * 输入为0 则进行默认缩放1280*720以上的 
	 * 如果想要获取原图请调用 getBitmapUnZoom();
	 * 
	 * @param path		图片路径 
	 * @param width		需要的宽度 输入为0 则是默认超过1280 自动缩放 
	 * @param height	需要的高度输入为0 则是默认超过720自动缩放
	 * @return
	 */
	@TargetApi(12)
	public static Bitmap getBitmap(String path, int width, int height) {
		Bitmap bitMap = null;
		
			if (CACHE_ENTRIES.size() >= CACHE_SIZE && !CACHE_ENTRIES.isEmpty()) {
				destoryLast();
			}
			bitMap = useBitmap(path, width, height);
			if (bitMap != null && !bitMap.isRecycled()) {
				return bitMap;
			}
			try {
			bitMap = createBitmap(path, width, height);
			if(bitMap ==null){
				return null;
			}
			String key = createKey(path, width, height);
			synchronized (LOCKED) {  
			cache_size+=bitMap.getByteCount();
			SoftReference <Bitmap> mSoftBitmap = new SoftReference<Bitmap>(bitMap);
			
				 IMG_CACHE_INDEX.put(key, mSoftBitmap);
				CACHE_ENTRIES.addFirst(key);
			 }
		} catch (OutOfMemoryError err) {
			
			if(CACHE_ENTRIES.isEmpty())
			{
				System.out.println("mytest the momory is not so much for bitmap");	
			}
			else
			{
				destoryLast();
				System.out.println(CACHE_SIZE);
				bitMap =createBitmap(path, width, height);
			}
		}
		return bitMap;
	}
	public static Bitmap getBitmapInputstrme(InputStream in ,String url)
	{
		return getBitmapInputstrme(in,url,0,0);
		
	}
	@TargetApi(12)
	public static Bitmap getBitmapInputstrme(InputStream in ,String url,int width,int height)
	{
		Bitmap bitMap = null;
	
			if (CACHE_ENTRIES.size() >= CACHE_SIZE && !CACHE_ENTRIES.isEmpty()) {
				destoryLast();
			}
			bitMap = useBitmap(url, ZERO_SIZE.getWidth(), ZERO_SIZE.getHeight());
			if (bitMap != null && !bitMap.isRecycled()) {
				return bitMap;
			}
			
			InputStream inputStream = in;
			Size size =getBitMapSize(in);
			if(bitMap ==null){
				return null;
			}
			int scale = getScale(size,0,0);
			Options opts = new Options();
			opts.inSampleSize=scale;
			int bitmapStoreSize = width * height <<2/(scale*scale) ;
			while(cache_size + bitmapStoreSize >= CACHE_MOMORY)
			{
				destoryLast();
			}
			try {
			bitMap = BitmapFactory.decodeStream(inputStream, null, opts);
			String key = createKey(url,width/scale , height/scale);
			synchronized (LOCKED) {  
			cache_size+=bitMap.getByteCount();
			SoftReference <Bitmap> mSoftBitmap = new SoftReference<Bitmap>(bitMap);
			
				 IMG_CACHE_INDEX.put(key, mSoftBitmap);
				CACHE_ENTRIES.addFirst(key);
				
			 }
		} catch (OutOfMemoryError err) {
			if(CACHE_ENTRIES.isEmpty())
			{
				System.out.println("mytest the momory is not so much for bitmap");	
			}
			else
			{
				destoryLast();
				System.out.println(CACHE_SIZE);
				bitMap =BitmapFactory.decodeStream(inputStream, null, opts);
			}
			//return createBitmap(path, width, height);
			//修正过的代码
		}
		return bitMap;
		
		
	}
	/**
	 * 设置缓存图片数量 如果输入负数,会产生异常
	 * 
	 * @param size
	 */
	public static void setCacheSize(int size) {
		if (size <= 0) {
			throw new RuntimeException("size :" + size);
		}
		synchronized (CACHE_ENTRIES) {
			while (size < CACHE_ENTRIES.size()&& !CACHE_ENTRIES.isEmpty()) {
				destoryLast();
			}
			CACHE_SIZE = size;
		}
		
	}
	

	
	/**
	 * 通过图片路径返回图片实际大小
	 * @param path		图片物理路径
	 * @return
	 */
	public static Size getBitMapSize(String path) {
		
		
		Options mOptions = new Options();
		File file=null;
		try {
			if(path.matches("^http.*")){
				InputStream in = null;
				URL imgUrl = new URL(path);
				HttpURLConnection hp = (HttpURLConnection) imgUrl.openConnection();
				in=hp.getInputStream();
			
				mOptions.inJustDecodeBounds =true;
				BitmapFactory.decodeStream(in, null, mOptions);
				hp.disconnect();// 关闭连接
				closeInputStream(in);
				return new Size(mOptions.outWidth,
						mOptions.outHeight);
			}
			else{
				file = new File(path);
				if(file.exists()){
					InputStream in = null;
					in = new FileInputStream(file);
					
					mOptions.inJustDecodeBounds =true;
					BitmapFactory.decodeStream(in, null, mOptions);
					closeInputStream(in);
					return new Size(mOptions.outWidth,
							mOptions.outHeight);
				}
				else 
					return null;
			}
	
				
			
			} catch (FileNotFoundException e) {
				return ZERO_SIZE;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return ZERO_SIZE;
	}
	
	//获取图片实际尺寸
	public static Size getBitMapSize(InputStream in) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds =true;
		BitmapFactory.decodeStream(in, null, options);
		
		Size realsize = new Size( options.outWidth, options.outHeight);
		return	realsize;
	}
	// ------------------------------------------------------------------ private Methods
	// 将图片加入队列头
	private static Bitmap useBitmap(String path, int width, int height) {
		Bitmap bitMap = null;
		String key = createKey(path, width, height);
			SoftReference<Bitmap> mSoftBimap  = IMG_CACHE_INDEX.get(key);
			if(mSoftBimap ==null )
				return bitMap;
			bitMap = mSoftBimap.get();
			if (null != bitMap) {
				 synchronized (LOCKED) {  
					 if (CACHE_ENTRIES.remove(key)) {
						 CACHE_ENTRIES.addFirst(key);
					 }
				 }
			}
		return bitMap;
	}

	// 回收最后一张图片
	@TargetApi(12)
	private static void destoryLast() {
		System.out.println("mytest    recycle bitmap");
		 synchronized (LOCKED) {  
			String key = CACHE_ENTRIES.removeLast();
			if (key.length() > 0) {
				SoftReference<Bitmap> mSoftBimap  = IMG_CACHE_INDEX.remove(key);
				if(mSoftBimap ==null )
					return ;
				Bitmap bitMap = mSoftBimap.get();
				
				if (bitMap != null && !bitMap.isRecycled()) {
					cache_size=cache_size-bitMap.getByteCount();
					bitMap.recycle();
					 bitMap = null;
					mSoftBimap.clear();
					System.out.println("mytest    mSoftBimap clear");
					
				}
			}
		 }
	}

	private static void destoryALL() {
		System.out.println("mytest    recycle all bitmap");
		 synchronized (LOCKED) {  
			 while(!CACHE_ENTRIES.isEmpty())
			 {
				 String key = CACHE_ENTRIES.removeLast();
				 if (key.length() > 0) {
					 SoftReference<Bitmap> mSoftBimap  = IMG_CACHE_INDEX.remove(key);
					 if(mSoftBimap ==null )
						 return ;
					 Bitmap bitMap = mSoftBimap.get();
				
					 if (bitMap != null && !bitMap.isRecycled()) {
						 cache_size=cache_size-bitMap.getByteCount();
						 bitMap.recycle();
						 bitMap = null;
						 mSoftBimap.clear();
						 System.out.println("mytest    mSoftBimap clear");
					
				}
			}
		 }
		 }
	}
	// 创建键
	private static String createKey(String path, int width, int height) {
		if (null == path || path.length() == 0) {
			return "";
		}
		return path + "_" + width + "_" + height;
	}

	/**
	 * 通过图片路径,宽度高度创建一个Bitmap对象
	 * @param path 图片的路径，可以是网络路径
	 * @param width  图片需要缩放的宽度
	 * @param height 图片 需要缩放的高度
	 * @return 返回所给路径的Bitmap
	 */
	private static Bitmap createBitmap(String path, int width, int height) {
			Size size = getBitMapSize(path);
			
			if (size.equals(ZERO_SIZE)) {
				return null;
			}
			int scale =getScale(size,width, height);
			int bitmapsize =  size.getWidth()* size.getHeight()<<2/(scale*scale);
			synchronized (LOCKED) {  
				while(cache_size +bitmapsize >= CACHE_MOMORY && !CACHE_ENTRIES.isEmpty())
				{
					destoryLast();
				}
			 }
			Bitmap bitmap =null;
			InputStream in = null;
			File file=null;
			try {
				if(path.matches("^http.*")){
					URL imgUrl = new URL(path);
					HttpURLConnection hp = (HttpURLConnection) imgUrl.openConnection();
					in=hp.getInputStream();
					Options mOptions = new Options();
					mOptions.inSampleSize =scale;
					 bitmap = BitmapFactory.decodeStream(in, null,mOptions);
					hp.disconnect();// 关闭连接
				}
				else{
					file = new File(path);
					if(file.exists()){
						in = new FileInputStream(file);
						Options mOptions = new Options();
						mOptions.inSampleSize =scale;
						 bitmap = BitmapFactory.decodeStream(in, null,mOptions);
					}
					else 
						return null;
				}
			
				 
		
				
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				closeInputStream(in);
			}
		
		return bitmap;
	}
	// 关闭输入流
	private static void closeInputStream(InputStream in) {
		if (null != in) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static int getScale(Size size ,int width,int height){
		int scale = 1;
		int realw =size.getWidth();
		int realh = size.getHeight();
		if(width==0 || height==0){	
			if(realw>DEFAULT_WIDTH ||realh>DEFAULT_HEIGT)
			{
				int a = realw / DEFAULT_WIDTH;
				int b = realh / DEFAULT_WIDTH;
				scale = (a>b)?((a&1)==0?a-1:a):((b&1)==0?b-1:b);
			}
			else{
				scale=1;
			}
			
		}
		else{
			int a = realw / width;
			int b = realh / height;
			scale = (a>b)?(a%2==0?a:a-1):(b%2==0?b:b-1);
			
		}
		if(scale==0)
			scale=1;
		return scale;
	}
		
	// 图片大小
	static class Size {
		private int width, height;

		Size(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
	}

	// 队列缓存参数对象
	static class QueueEntry {
		public String path;
		public int width;
		public int height;
	}
	public static int getCacheSize()
	{
		return cache_size;
	}
}
