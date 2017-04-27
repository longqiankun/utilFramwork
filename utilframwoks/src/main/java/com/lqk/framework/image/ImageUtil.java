package com.lqk.framework.image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.text.TextUtils;

import com.lqk.framework.util.Logger;

/**
 * 
 * @ClassName: ImageUtil
 * @Description:图片工具
 * @author longqiankun
 * @date 2014-7-7 上午11:40:22
 * 
 */
public class ImageUtil {
	/**
	 * 根据资源ID获取资源
	 */
	public static Drawable getDrawableById(Context context, int resId) {
		if (context == null) {
			return null;
		}
		return context.getResources().getDrawable(resId);
	}

	/**
	 * 根据资源ID获取资源
	 */
	public static Bitmap getBitmapById(Context context, int resId) {
		if (context == null) {
			return null;
		}
		return BitmapFactory.decodeResource(context.getResources(), resId);

	}

	/**
	 * 
	 * 将bitmap转换出字节
	 * 
	 * @param bitmap
	 * @return
	 */
	public static byte[] bitmap2byte(Bitmap bitmap) {
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			byte[] array = baos.toByteArray();
			baos.flush();
			baos.close();
			return array;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将字节转换成bitmap
	 * 
	 * @param data
	 * @return
	 */
	public static Bitmap byte2bitmap(byte[] data) {
		if (null == data) {
			return null;
		}
		return BitmapFactory.decodeByteArray(data, 0, data.length);
	}

	/**
	 * 
	 * 将Drawable转换Bitmap
	 * 
	 * @param drawable
	 * @return
	 */

	public static Bitmap drawable2bitmap(Drawable drawable) {
		if (null == drawable) {
			return null;
		}
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);// �ص�
		return bitmap;
	}

	/**
	 * 将bitmap转换drawable
	 * 
	 * @param bitmap
	 * @return
	 */

	public static Drawable bitmap2Drawable(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		return new BitmapDrawable(bitmap);
	}

	/**
	 * 
	 * 缩放bitmap
	 * 
	 * @param bitmap
	 * @param w
	 * @param h
	 * @return
	 */

	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		if (bitmap == null) {
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		return newbmp;

	}

	/**
	 * 保存bitmap到指定目录
	 * 
	 * @param bitmap
	 * 
	 * @param path
	 */

	public static boolean saveBitmap(Bitmap bitmap, String path) {
		try {
			File file = new File(path);
			File parent = file.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(file);
			boolean b = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
			return b;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 * 将bitmap压缩后保存到指定的目录
	 * 
	 * @param bitmap
	 * @param quality
	 * 
	 *            Hint to the compressor, 0-100. 0 meaning compress for small
	 * 
	 *            size, 100 meaning compress for max quality. Some formats, like
	 * 
	 *            PNG which is lossless, will ignore the quality setting
	 * 
	 * @return
	 */

	public static boolean saveBitmap(Bitmap bitmap, String path,
			CompressFormat format, int quality) {
		try {
			File file = new File(path);
			File parent = file.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(file);
			boolean b = bitmap.compress(format, quality, fos);
			fos.flush();
			fos.close();
			return b;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 * 获取圆角的bitmap
	 * 
	 * @param bitmap
	 * @param roundPx
	 * @return
	 */

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		if (bitmap == null) {
			return null;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		final int reflectionGap = 4;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
				width, height / 2, matrix, false);
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + height / 2), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint deafalutPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
				0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);
		return bitmapWithReflection;
	}

	public static final int ALL = 347120;
	public static final int TOP = 547120;
	public static final int LEFT = 647120;
	public static final int RIGHT = 747120;
	public static final int BOTTOM = 847120;

	/**
	 * @param type
	 *            画圆角的位置，该类中的ALL 表示4个面，分别：TOP LEFT RIGHT BOTTOM
	 * @param bitmap
	 *            要被更改的图片
	 * @param roundPx
	 *            圆角的角度像素
	 * @return
	 */
	public static Bitmap fillet(int type, Bitmap bitmap, int roundPx) {
		try {
			// 其原理就是：先建立一个与图片大小相同的透明的Bitmap画板
			// 然后在画板上画出一个想要的形状的区域。
			// 最后把源图片帖上。
			final int width = bitmap.getWidth();
			final int height = bitmap.getHeight();

			Bitmap paintingBoard = Bitmap.createBitmap(width, height,
					Config.ARGB_8888);
			Canvas canvas = new Canvas(paintingBoard);
			canvas.drawARGB(Color.TRANSPARENT, Color.TRANSPARENT,
					Color.TRANSPARENT, Color.TRANSPARENT);

			final Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);

			if (TOP == type) {
				clipTop(canvas, paint, roundPx, width, height);
			} else if (LEFT == type) {
				clipLeft(canvas, paint, roundPx, width, height);
			} else if (RIGHT == type) {
				clipRight(canvas, paint, roundPx, width, height);
			} else if (BOTTOM == type) {
				clipBottom(canvas, paint, roundPx, width, height);
			} else {
				clipAll(canvas, paint, roundPx, width, height);
			}

			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			// 帖子图
			final Rect src = new Rect(0, 0, width, height);
			final Rect dst = src;
			canvas.drawBitmap(bitmap, src, dst, paint);
			return paintingBoard;
		} catch (Exception exp) {
			return bitmap;
		}
	}

	private static void clipLeft(final Canvas canvas, final Paint paint,
			int offset, int width, int height) {
		final Rect block = new Rect(offset, 0, width, height);
		canvas.drawRect(block, paint);
		final RectF rectF = new RectF(0, 0, offset * 2, height);
		canvas.drawRoundRect(rectF, offset, offset, paint);
	}

	private static void clipRight(final Canvas canvas, final Paint paint,
			int offset, int width, int height) {
		final Rect block = new Rect(0, 0, width - offset, height);
		canvas.drawRect(block, paint);
		final RectF rectF = new RectF(width - offset * 2, 0, width, height);
		canvas.drawRoundRect(rectF, offset, offset, paint);
	}

	private static void clipTop(final Canvas canvas, final Paint paint,
			int offset, int width, int height) {
		final Rect block = new Rect(0, offset, width, height);
		canvas.drawRect(block, paint);
		final RectF rectF = new RectF(0, 0, width, offset * 2);
		canvas.drawRoundRect(rectF, offset, offset, paint);
	}

	private static void clipBottom(final Canvas canvas, final Paint paint,
			int offset, int width, int height) {
		final Rect block = new Rect(0, 0, width, height - offset);
		canvas.drawRect(block, paint);
		final RectF rectF = new RectF(0, height - offset * 2, width, height);
		canvas.drawRoundRect(rectF, offset, offset, paint);
	}

	private static void clipAll(final Canvas canvas, final Paint paint,
			int offset, int width, int height) {
		final RectF rectF = new RectF(0, 0, width, height);
		canvas.drawRoundRect(rectF, offset, offset, paint);
	}

	/**
	 * 
	 * @Description: 给bitmap添加边框
	 * @param source
	 * @param newHeight
	 * @param newWidth
	 * @return see_to_target
	 */
	public static Bitmap scaleCenterCrop(Bitmap source, int newHeight,
			int newWidth) {
		return scaleCenterCrop(source, newHeight, newWidth, Color.TRANSPARENT,
				10);
	}

	/**
	 * 
	 * @Description:给bitmap添加边框
	 * @param source
	 * @param newHeight
	 * @param newWidth
	 * @param color
	 *            边框的颜色
	 * @return see_to_target
	 */
	public static Bitmap scaleCenterCrop(Bitmap source, int newHeight,
			int newWidth, int color, int edgewidth) {
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();

		// Compute the scaling factors to fit the new height and width,
		// respectively.
		// To cover the final image, the final scaling will be the bigger
		// of these two.
		float xScale = (float) newWidth / sourceWidth;
		float yScale = (float) newHeight / sourceHeight;
		float scale = Math.max(xScale, yScale);

		// Now get the size of the source bitmap when scaled
		float scaledWidth = scale * sourceWidth;
		float scaledHeight = scale * sourceHeight;

		// Let's find out the upper left coordinates if the scaled bitmap
		// should be centered in the new size give by the parameters
		float left = (newWidth - scaledWidth) / 2;
		float top = (newHeight - scaledHeight) / 2;

		// The target rectangle for the new, scaled version of the source bitmap
		// will now
		// be
		RectF targetRect = new RectF(left + edgewidth, top + edgewidth, left
				+ scaledWidth, top + scaledHeight);
		RectF clipRect = new RectF(left, top, left + scaledWidth, top
				+ scaledHeight);
		// Finally, we create a new bitmap of the specified size and draw our
		// new,
		// scaled bitmap onto it.
		Bitmap dest = Bitmap.createBitmap(newWidth + edgewidth, newHeight
				+ edgewidth, source.getConfig());
		Canvas canvas = new Canvas(dest);
		// canvas.drawColor(color);
		// Path p=new Path();
		// p.addRect(clipRect,Direction.CW);
		// canvas.clipRect(clipRect);
		// canvas.save();
		// canvas.clipPath(p);
		Paint paint = new Paint();
		paint.setColor(color);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(2);
		paint.setStyle(Style.STROKE);

		canvas.drawRect(clipRect, paint);
		canvas.drawBitmap(source, null, targetRect, null);
		// canvas.restore();
		return dest;
	}

	/**
	 * 旋转bitmap的角度
	 * 
	 * @param b
	 * @param degrees
	 *            旋转的角度
	 * @return
	 */
	public static Bitmap rotate(Bitmap b, int degrees) {
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) b.getWidth() / 2,
					(float) b.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
						b.getHeight(), m, true);
				if (b != b2) {
					b.recycle(); // Android开发网再次提示Bitmap操作完应该显示的释放
					b = b2;
				}
			} catch (OutOfMemoryError ex) {
				// 建议大家如何出现了内存不足异常，最好return 原始的bitmap对象。.
			}
		}
		return b;
	}

	/**
	 * 获取缩放的bitmap
	 * 
	 * @param bitmap
	 * @param screenWidth
	 * @param screenHight
	 * @return
	 */
	public static Bitmap getScacleBitmap(Bitmap bitmap, int screenWidth,
			int screenHight) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scale = (float) screenWidth / w;
		float scale2 = (float) screenHight / h;
		matrix.postScale(scale, scale);
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
	}

	/**
	 * 根据指定的图像路径和大小来获取缩略图 此方法有两点好处： 1.
	 * 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
	 * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。 2.
	 * 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使 用这个工具生成的图像不会被拉伸。
	 * 
	 * @param imagePath
	 *            图像的路径
	 * @param width
	 *            指定输出图像的宽度
	 * @param height
	 *            指定输出图像的高度
	 * @return 生成的缩略图
	 */
	public static Bitmap getImageThumbnail(String imagePath, int width,
			int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	 * 获取视频的缩略图 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	 * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	 * 
	 * @param videoPath
	 *            视频的路径
	 * @param width
	 *            指定输出视频缩略图的宽度
	 * @param height
	 *            指定输出视频缩略图的高度度
	 * @param kind
	 *            参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	 *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	 * @return 指定大小的视频缩略图
	 */
	public static Bitmap getVideoThumbnail(String videoPath, int width,
			int height, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	 * 图片创建倒影 TODO(这里用一句话描述这个方法的作用)
	 * 
	 * @param originalImage
	 * @param number
	 * @return Bitmap
	 */
	public static Bitmap createReflectedImage(Bitmap originalImage, int number) {
		final int reflectionGap = 0; // 倒影和原图片间的距离
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		double reflectHeight = number / 100.00;

		number = (int) (height * reflectHeight);
		// 倒影部分
		Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, number,
				width, number, matrix, false);
		// 要返回的倒影图片
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + number), Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		// 画原来的图片
		canvas.drawBitmap(originalImage, 0, 0, null);

		// Paint defaultPaint = new Paint();
		// //倒影和原图片间的距离
		// canvas.drawRect(0, height, width, height + reflectionGap,
		// defaultPaint);
		// 画倒影部分
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0,
				originalImage.getHeight(), 0, bitmapWithReflection.getHeight()
						+ reflectionGap, 0x70ffffff, 0x00ffffff,
				TileMode.MIRROR);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);
		return bitmapWithReflection;
	}

	/**
	 * 图片增加边框
	 * 
	 * @param bitmap
	 * @param color
	 * @return Bitmap
	 */
	public static Bitmap addFrame(Bitmap bitmap, int color) {
		Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap2);
		Rect rect = canvas.getClipBounds();
		rect.bottom--;
		rect.right--;
		Paint recPaint = new Paint();
		recPaint.setColor(color);
		recPaint.setStyle(Paint.Style.STROKE);
		canvas.drawRect(rect, recPaint);
		canvas.drawBitmap(bitmap, 0, 0, null);
		return bitmap2;
	}

	/**
	 * 
	 * @Title: saveBitmap
	 * @Description: 保存图片到本地
	 * @param @param url
	 * @return void
	 * @throws
	 */
	public static void saveBitmap(String dir, String url) {
		InputStream bitmapIs = getStreamFromURL(url);
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// opts.inJustDecodeBounds = true;
		Rect rect = new Rect();
		// Bitmap bitmap =BitmapFactory.decodeStream(bitmapIs,rect, opts);
		opts.inSampleSize = computeSampleSize(opts, -1, 90 * 90);
		opts.inJustDecodeBounds = false;
		opts.inPurgeable = true;
		opts.inInputShareable = true;
		Bitmap bitmap = BitmapFactory.decodeStream(bitmapIs, rect, opts);
		if (bitmap != null) {
			File dirs = new File(dir);
			if (!dirs.exists()) {
				dirs.mkdirs();
			}
			File bitmapFile = new File(dir,
					url.substring(url.lastIndexOf("/") + 1));
			if (!bitmapFile.exists()) {
				try {
					bitmapFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(bitmapFile);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
		}
	}

	/**
	 * 
	 * @Title: computeSampleSize
	 * @Description: 计算图片的缩放比例
	 * @param @param options
	 * @param @param minSideLength
	 * @param @param maxNumOfPixels
	 * @param @return
	 * @return int
	 * @throws
	 */
	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	/**
	 * 
	 * @Title: computeInitialSampleSize
	 * @Description:计算图片的初始缩放比例
	 * @param @param options
	 * @param @param minSideLength
	 * @param @param maxNumOfPixels
	 * @param @return
	 * @return int
	 * @throws
	 */
	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	/**
	 * 
	 * @Title: getStreamFromURL
	 * @Description: 根据路径获取输入流
	 * @param @param imageURL
	 * @param @return
	 * @return InputStream
	 * @throws
	 */
	public static InputStream getStreamFromURL(String imageURL) {
		InputStream in = null;
		try {
			URL url = new URL(imageURL);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			in = connection.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return in;

	}

	/**
	 * 
	 * @Title: compressImage
	 * @Description:图片压缩
	 * @param @param image 要压缩的图片
	 * @return Bitmap
	 * @throws
	 */
	public static Bitmap compressImage(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			if (options < 0) {
				break;
			}
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.PNG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;// 每次都减少10
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap;
	}

	/**
	 * 
	 * 描述:
	 * 
	 * @param srcPath
	 * @param width
	 * @param height
	 * @param isCompress
	 *            是否需要进行质量压缩
	 * @return
	 */
	public static Bitmap getImage(String srcPath, float width, float height,
			boolean isCompress) {
		File file = new File(srcPath);
		if (!file.exists())
			return null;
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空
		zoomImg(width, height, newOpts);
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		if (isCompress) {
			return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
		} else {
			return bitmap;
		}

	}

	private static void zoomImg(float width, float height,
			BitmapFactory.Options newOpts) {
		newOpts.inJustDecodeBounds = false;
		if ((width > 0 || height > 0)) {
			int w = newOpts.outWidth;
			int h = newOpts.outHeight;
			// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
			float hh = width;
			float ww = height;// 这里设置宽度为480f
			// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
			int be = 1;// be=1表示不缩放
			if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
				be = (int) (newOpts.outWidth / ww);
			} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
				be = (int) (newOpts.outHeight / hh);
			}
			if (be <= 0)
				be = 1;
			newOpts.inSampleSize = be;// 设置缩放比例
		} else {
			newOpts.inSampleSize = 1;
		}
	}

	/**
	 * 
	 * 描述: 将字节数组转换成图片，并进行压缩
	 * 
	 * @param srcPath
	 * @param width
	 * @param height
	 * @param isCompress
	 *            是否需要进行质量压缩
	 * @return
	 */
	public static Bitmap getImage(byte[] bCode, float width, float height,
			boolean isCompress) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeByteArray(bCode, 0, bCode.length,
				newOpts);// 此时返回bm为空
		zoomImg(width, height, newOpts);
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeByteArray(bCode, 0, bCode.length, newOpts);
		if (isCompress) {
			return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
		} else {
			return bitmap;
		}

	}

	/**
	 * 
	 * @Title: getImage
	 * @Description: 根据路径获取图片
	 * @param @param srcPath
	 * @param @return
	 * @return Bitmap
	 * @throws
	 */
	public static Bitmap getImage(String srcPath, float width, float height) {
		return getImage(srcPath, width, height, false);
	}

	/**
	 * 
	 * @Title: getImage
	 * @Description: 根据路径获取图片
	 * @param @param srcPath
	 * @param @return
	 * @return Bitmap
	 * @throws
	 */
	public static Bitmap getImage(String srcPath, boolean isCompress) {
		return getImage(srcPath, 0, 0, isCompress);
	}

	public static Bitmap getImage(String srcPath) {
		return getImage(srcPath, 0, 0, false);
	}

	/**
	 * 
	 * @Title: comp
	 * @Description: 对图片进行质量和尺寸压缩
	 * @param @param image
	 * @param @return
	 * @return Bitmap
	 * @throws
	 */
	public static Bitmap comp(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, baos);
		if (baos.toByteArray().length / 1024 > 100) {// 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.PNG, 50, baos);// 这里压缩50%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		zoomImg(480, 800, newOpts);
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
	}

	/**
	 * 
	 * @Title: saveImgTOLocal
	 * @Description: 保存图片到本地
	 * @param @param path
	 * @param @param bitmap
	 * @return void
	 * @throws
	 */
	public static void saveImgTOLocal(String path, Bitmap bitmap) {
		if (bitmap != null && !TextUtils.isEmpty(path)) {
			File bitmapFile = new File(path);
			if (!bitmapFile.exists()) {
				try {
					bitmapFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(bitmapFile);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Logger.getLogger("ImageFileCache").i("bitmap is null can't save");
		}
	}

	public static final int min_mosaic_block_size = 4;

	/**
	 * 
	 * @description 添加马赛克
	 * @param bitmap
	 * @param targetRect
	 * @param blockSize
	 *            {@link #min_mosaic_block_size}
	 * @return
	 * @throws Exception
	 */
	public static Bitmap makeMosaic(Bitmap bitmap, Rect targetRect,
			int blockSize) throws OutOfMemoryError {
		if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0
				|| bitmap.isRecycled()) {
			throw new RuntimeException("bad bitmap to add mosaic");
		}
		if (blockSize < min_mosaic_block_size) {
			blockSize = min_mosaic_block_size;
		}
		if (targetRect == null) {
			targetRect = new Rect();
		}
		int bw = bitmap.getWidth();
		int bh = bitmap.getHeight();
		if (targetRect.isEmpty()) {
			targetRect.set(0, 0, bw, bh);
		}
		//
		int rectW = targetRect.width();
		int rectH = targetRect.height();
		int[] bitmapPxs = new int[bw * bh];
		// fetch bitmap pxs
		bitmap.getPixels(bitmapPxs, 0, bw, 0, 0, bw, bh);
		//
		int rowCount = (int) Math.ceil((float) rectH / blockSize);
		int columnCount = (int) Math.ceil((float) rectW / blockSize);
		int maxX = bw;
		int maxY = bh;
		for (int r = 0; r < rowCount; r++) { // row loop
			for (int c = 0; c < columnCount; c++) {// column loop
				int startX = targetRect.left + c * blockSize + 1;
				int startY = targetRect.top + r * blockSize + 1;
				dimBlock(bitmapPxs, startX, startY, blockSize, maxX, maxY);
			}
		}
		return Bitmap.createBitmap(bitmapPxs, bw, bh, Config.ARGB_8888);
	}

	/**
	 * 从块内取样，并放大，从而达到马赛克的模糊效果
	 * 
	 * @param pxs
	 * @param startX
	 * @param startY
	 * @param blockSize
	 * @param maxX
	 * @param maxY
	 */
	private static void dimBlock(int[] pxs, int startX, int startY,
			int blockSize, int maxX, int maxY) {
		int stopX = startX + blockSize - 1;
		int stopY = startY + blockSize - 1;
		if (stopX > maxX) {
			stopX = maxX;
		}
		if (stopY > maxY) {
			stopY = maxY;
		}
		//
		int sampleColorX = startX + blockSize / 2;
		int sampleColorY = startY + blockSize / 2;
		//
		if (sampleColorX > maxX) {
			sampleColorX = maxX;
		}
		if (sampleColorY > maxY) {
			sampleColorY = maxY;
		}
		int colorLinePosition = (sampleColorY - 1) * maxX;
		int sampleColor = pxs[colorLinePosition + sampleColorX - 1];// 像素从1开始，但是数组层0开始
		for (int y = startY; y <= stopY; y++) {
			int p = (y - 1) * maxX;
			for (int x = startX; x <= stopX; x++) {
				// 像素从1开始，但是数组层0开始
				pxs[p + x - 1] = sampleColor;
			}
		}
	}
}
