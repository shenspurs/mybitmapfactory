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
	private static final int CACHE_MOMORY = 16*1024*1024;//�����С
	private static int cache_size=0;
	private static final LinkedList<String> CACHE_ENTRIES = new LinkedList<String>(); // �˶�����������Bitmap�Ļ���˳��,��֤���ʹ�õ�ͼƬ������

	private static final Map<String, SoftReference<Bitmap>> IMG_CACHE_INDEX = new HashMap<String, SoftReference<Bitmap>>(); // ����Bitmap ͨ��ͼƬ·��,ͼƬ��С
	private static final int DEFAULT_WIDTH=1280;
	private static final int DEFAULT_HEIGT=720;

	private static int CACHE_SIZE = 200; // ����ͼƬ����


	/**
	 * ����һ��ͼƬ ����������Ѿ�����,�򷵻ػ����е�ͼ,���򴴽�һ���µĶ���,�����뻺��
	 * 1280*720���ϵ� ����Ĭ������1280*720���ϵ� 
	 * �����Ҫ��ȡԭͼ����� getBitmapUnZoom();
	 * @param path		ͼƬ·��
	 * @return
	 */
	public static Bitmap getBitmap(String path){
		return getBitmap(path ,0,0);
	}
	
	/**
	 * ����һ�Ų����ŵ�ԭͼ�����ͼƬ������߽���������null
	 * @param path ͼƬ��·�� ��ֻ�����ļ�·����
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
	 * ����һ��ͼƬ ����������Ѿ�����,�򷵻ػ����е�ͼ,���򴴽�һ���µĶ���,�����뻺��
	 * ���,�߶�,Ϊ������ԭͼ�����ڴ��,�������Ŀ�,��,��ԭͼ��,����ԭͼ
	 * ����Ϊ0 �����Ĭ������1280*720���ϵ� 
	 * �����Ҫ��ȡԭͼ����� getBitmapUnZoom();
	 * 
	 * @param path		ͼƬ·�� 
	 * @param width		��Ҫ�Ŀ�� ����Ϊ0 ����Ĭ�ϳ���1280 �Զ����� 
	 * @param height	��Ҫ�ĸ߶�����Ϊ0 ����Ĭ�ϳ���720�Զ�����
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
			//�������Ĵ���
		}
		return bitMap;
		
		
	}
	/**
	 * ���û���ͼƬ���� ������븺��,������쳣
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
	 * ͨ��ͼƬ·������ͼƬʵ�ʴ�С
	 * @param path		ͼƬ����·��
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
				hp.disconnect();// �ر�����
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
	
	//��ȡͼƬʵ�ʳߴ�
	public static Size getBitMapSize(InputStream in) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds =true;
		BitmapFactory.decodeStream(in, null, options);
		
		Size realsize = new Size( options.outWidth, options.outHeight);
		return	realsize;
	}
	// ------------------------------------------------------------------ private Methods
	// ��ͼƬ�������ͷ
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

	// �������һ��ͼƬ
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
	// ������
	private static String createKey(String path, int width, int height) {
		if (null == path || path.length() == 0) {
			return "";
		}
		return path + "_" + width + "_" + height;
	}

	/**
	 * ͨ��ͼƬ·��,��ȸ߶ȴ���һ��Bitmap����
	 * @param path ͼƬ��·��������������·��
	 * @param width  ͼƬ��Ҫ���ŵĿ��
	 * @param height ͼƬ ��Ҫ���ŵĸ߶�
	 * @return ��������·����Bitmap
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
					hp.disconnect();// �ر�����
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
	// �ر�������
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
		
	// ͼƬ��С
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

	// ���л����������
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
