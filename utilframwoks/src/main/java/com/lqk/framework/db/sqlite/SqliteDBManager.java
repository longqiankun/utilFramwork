package com.lqk.framework.db.sqlite;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;

import com.lqk.framework.db.annotation.Column;

/**
 * 数据库操作类
 * 
 * @author longqiankun 2014-06-29
 */

public class SqliteDBManager {
	private final int BUFFER_SIZE = 400000;
	public static String DB_NAME = "lqk.db"; // 保存的数据库文件名
	private static String MASTER = "sqlite_master";
	private final static String SECRET_KEY = "95279527";
	private static String Encrypt = "";
	private static boolean DBINSdCard;
	// 数据库版本
	private static final int DB_VERSION = 1;

	// 执行open()打开数据库时，保存返回的数据库对象
	public SQLiteDatabase mWriteDatabase = null;
	public SQLiteDatabase mReadDatabase = null;

	// 由SQLiteOpenHelper继承过来
	private DatabaseHelper mDatabaseHelper = null;

	// 本地Context对象
	private Context mContext = null;

	private static SqliteDBManager dbConn = null;

	// 查询游标对象
	private Cursor cursor;

	/**
	 * SQLiteOpenHelper内部类
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		private static DatabaseHelper mHelper;

		private DatabaseHelper(Context context) {
			// 当调用getWritableDatabase()或 getReadableDatabase()方法时,创建一个数据库
			super(context, getMyDatabaseName(context), null, DB_VERSION);
		}

		public synchronized static DatabaseHelper getInstance(Context context) {
			if (mHelper == null) {
				mHelper = new DatabaseHelper(context);
			}
			return mHelper;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub

		}
	}

	private static String getMyDatabaseName(Context context) {
		String databasename = DB_NAME;
		boolean isSdcardEnable = DBINSdCard;

		String dbPath = null;
		if (isSdcardEnable) {
			dbPath = Environment.getExternalStorageDirectory().getPath()
					+ "/database/";
		} else {// 未插入SDCard，建在内存中
			dbPath = context.getFilesDir().getPath() + "/database/";
		}
		File dbp = new File(dbPath);
		if (!dbp.exists()) {
			dbp.mkdirs();
		}
		databasename = dbPath + DB_NAME;
		return databasename;
	}

	/**
	 * 构造函数
	 * 
	 * @param mContext
	 */
	private SqliteDBManager(Context mContext, String dbName, String Encrypt,
			boolean DBINSdCard) {
		super();
		this.mContext = mContext;
		this.DB_NAME = dbName;
		this.Encrypt = Encrypt;
		this.DBINSdCard = DBINSdCard;

	}

	public static SqliteDBManager getInstance(Context mContext) {
		return getInstance(mContext, DB_NAME);
	}

	public static SqliteDBManager getInstance(Context mContext, String dbName) {
		return getInstance(mContext, dbName, "");
	}

	public static SqliteDBManager getInstance(Context mContext, String dbName,
			String Encrypt) {
		return getInstance(mContext, dbName, Encrypt, true);
	}

	public static SqliteDBManager getInstanceEncryptSdCard(Context mContext) {
		return getInstance(mContext, DB_NAME, SECRET_KEY, true);
	}

	public static SqliteDBManager getInstanceEncryptSdCard(Context mContext,
			String dbName) {
		return getInstance(mContext, dbName, SECRET_KEY, true);
	}

	public static SqliteDBManager getInstanceEncrypt(Context mContext) {
		return getInstance(mContext, DB_NAME, SECRET_KEY, false);
	}

	public static SqliteDBManager getInstanceSdCard(Context mContext) {
		return getInstance(mContext, DB_NAME, "", true);
	}

	public static SqliteDBManager getInstanceSdCard(Context mContext,
			String dbName) {
		return getInstance(mContext, dbName, "", true);
	}

	public static SqliteDBManager getInstanceNoEncrypt(Context mContext) {
		return getInstance(mContext, DB_NAME, "", false);
	}

	private static SqliteDBManager getInstance(Context mContext, String dbName,
			String Encrypt, boolean DBINSdCard) {
		if (null == dbConn) {
			dbConn = new SqliteDBManager(mContext, dbName, Encrypt, DBINSdCard);
		}
		return dbConn;
	}

	/**
	 * 打开数据库
	 */
	public void open() {
		/*
		 * SQLiteDatabase.loadLibs(mContext); SQLiteDatabase.create(null,
		 * Encrypt);
		 */
		/*SQLiteDatabase.loadLibs(mContext);

		mWriteDatabase = DatabaseHelper.getInstance(mContext)
				.getWritableDatabase(Encrypt);
		mReadDatabase = DatabaseHelper.getInstance(mContext)
				.getReadableDatabase(Encrypt);*/

		mWriteDatabase = DatabaseHelper.getInstance(mContext)
				.getWritableDatabase();
		mReadDatabase = DatabaseHelper.getInstance(mContext)
				.getReadableDatabase();
	}

	private SQLiteDatabase openDatabase(String dbfile) {
//		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile,Encrypt, null);
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
		return db;
	}

	/**
	 * 关闭数据库
	 */
	public void close() {
		if (null != cursor) {
			cursor.close();
		}
		if (null != mDatabaseHelper) {
			mDatabaseHelper.close();
		}
	}

	/**
	 * @description 在数据库中存储集合数据 思路： 0.开启事务 1.遍历容器中的对象 2.通过对象得到该对象的类
	 *              3.在该类中获取所有的字段 4.创建一个存储字段值的容器 5.获取每个字段的值 6.将每个字段值存储在容器中
	 *              7.类名就作为表名，容器中的数据就是要向数据库中插入的数据。 8.调用数据库的插入方法，将表名和容器作为参数传进去。
	 *              9.关闭事务
	 * @param collection
	 *            集合容器
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	public  <T> void insert(Collection<T> collection)
			throws IllegalArgumentException, IllegalAccessException {
		insert(collection, "", null, null);
	}

	public  <T> void insert(T t) throws IllegalArgumentException,
			IllegalAccessException {
		insert(t, "");
	}

	/**
	 * 
	 * @Title: insert
	 * @Description:插入数据到数据库
	 * @param @param t
	 * @param @param nullColume
	 * @param @throws IllegalArgumentException
	 * @param @throws IllegalAccessException
	 * @return void
	 * @throws
	 */
	public  <T> void insert(T t, String key)
			throws IllegalArgumentException, IllegalAccessException {
		List<T> collection = new ArrayList<T>();
		collection.add(t);
		insert(collection, key, null, null);
	}

	/**
	 * 
	 * 描述:根据key先查询看是否有该条信息，如果有就更新，没有就插入
	 * 
	 * @param collection
	 *            插入或更新的数据
	 * @param key
	 *            判断的关键字，列名
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 *             思路： 1.遍历传入的容器，获取容器中的每个对象。 2.使用反射技术获取对象中的属性和值
	 *             3.获取传入的key（列名），判断是否包含当前的属性，如果包含，则获取当前的值放在一个数组中。
	 *             4.获取属性和对应的值，使用多条件查询，查询表中是否存在该条信息，如果存在，使用更新操作，否则进行插入操作。
	 */
	public  <T> void insert(Collection<T> collection, String[] key)
			throws IllegalArgumentException, IllegalAccessException {
		insert(collection, key, null, null);
	}

	/**
	 * 
	 * 描述:根据key先查询看是否有该条信息，如果有就更新，没有就插入
	 * 
	 * @param collection
	 *            插入或更新的数据
	 * @param key
	 *            判断的关键字
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public  <T> void insert(Collection<T> collection, String key)
			throws IllegalArgumentException, IllegalAccessException {
		insert(collection, key, null, null);
	}

	/**
	 * 
	 * @Title: insertColum
	 * @Description: 指定插入的列
	 * @param @param collection
	 * @param @param key
	 * @param @param insertColum
	 * @param @throws IllegalArgumentException
	 * @param @throws IllegalAccessException
	 * @return void
	 * @throws
	 */
	public  <T> void insert(Collection<T> collection, String key,
			Set<String> insertColum) throws IllegalArgumentException,
			IllegalAccessException {
		insert(collection, key, insertColum, null);
	}

	/**
	 * 
	 * @author longqiankun
	 * @description : 指定对象的列不进行插入操作
	 * @param collection
	 *            保存的列表
	 * @param key
	 *            键
	 * @param noInsertColum
	 *            不操作的字段列表
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public  <T> void insertNoColum(Collection<T> collection,
			String key, Set<String> noInsertColum)
			throws IllegalArgumentException, IllegalAccessException {
		insert(collection, key, null, noInsertColum);
	}

	/**
	 * 
	 * @author longqiankun
	 * @description : 指定对象的列不进行插入操作和指定的列进行插入操作
	 * @param collection
	 *            保存的列表
	 * @param key
	 *            键
	 * @param insertColum
	 *            操作的字段列表
	 * @param noInsertColum
	 *            不操作的字段列表
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public  <T> void insert(Collection<T> collection, String key,
			Set<String> insertColum, Set<String> noInsertColum)
			throws IllegalArgumentException, IllegalAccessException {
		String[] keys = null;
		if (!TextUtils.isEmpty(key)) {
			keys = new String[1];
			keys[0] = key;
		} else {
			keys = new String[0];
		}
		insert(collection, keys, insertColum, noInsertColum);
	}

	/**
	 * 
	 * @author longqiankun
	 * @description : 指定对象的列不进行插入操作和指定的列进行插入操作
	 * @param collection
	 *            保存的列表
	 * @param key
	 *            键
	 * @param insertColum
	 *            操作的字段列表
	 * @param noInsertColum
	 *            不操作的字段列表
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public synchronized <T> void insert(Collection<T> collection, String[] key,
			Set<String> insertColum, Set<String> noInsertColum)
			throws IllegalArgumentException, IllegalAccessException {

		// 判断保存的列表是否为空
		if (collection != null && collection.size() > 0) {
			String value = "";
			String[] vaules = null;
			Iterator<T> iterator = collection.iterator();
			if (key != null && key.length > 0) {
				vaules = new String[key.length];
			}
			mWriteDatabase.beginTransaction();
			String className = null;
			boolean isCreateTable = false;
			try {
				// 遍历列表数据
				while (iterator.hasNext()) {
					ContentValues contentValues = new ContentValues();
					T t = iterator.next();
					if (!isCreateTable) {
						isCreateTable = true;
						// 创建表
						createTable(t);
					}
					Class clazz = t.getClass();
					className = getTableName(clazz);

					// 获取对象中的所有字段
					Field[] fields = clazz.getDeclaredFields();
					for (Field field : fields) {

						// 判断是否是要进行数据库操作的字段
						if (!field.isAnnotationPresent(Column.class))
							continue;

						// 设置字段可以访问
						field.setAccessible(true);
						Class<?> type = field.getType();
						// String simpleName=type.getSimpleName();

						// 获取字段名称
						String fieldName = field.getName();

						// 判断当前字段是否在需要操作的列表中
						if (insertColum != null) {
							if (!insertColum.contains(fieldName)) {
								continue;
							}
						}

						// 判断当前字段是否在不需要操作的列表中
						if (noInsertColum != null) {
							if (noInsertColum.contains(fieldName)) {
								continue;
							}
						}

						Object obj = field.get(t);
						if (obj != null) {
							// 设置保存的字段和值
							contentValues.put(fieldName, obj.toString());
						} else {
							contentValues.put(fieldName, "");
						}

						if (key != null && key.length > 0) {
							for (int i = 0; i < key.length; i++) {
								String keyObj = key[i];
								if (fieldName.equals(keyObj)) {
									if (obj != null) {
										vaules[i] = obj.toString();
									} else {
										vaules[i] = "";
									}

								}
							}
						}
					}

					if(key==null||key.length==0){
						mWriteDatabase.insert(className, null, contentValues);
					}else{
						List<T> findBy = findBy(t, key, vaules);
						if (findBy.size() > 0) {
							update(t, key, vaules, contentValues);
						}else{
							mWriteDatabase.insert(className, null, contentValues);
						}
					}
				}

				// 回调数据改变方法
				if (mUpdateListener != null && className != null) {
					mUpdateListener.onUpdateTable(className);
				}
				mWriteDatabase.setTransactionSuccessful();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				mWriteDatabase.endTransaction();
			}
		}
	}

	private <T> String getTableName(Class clazz) {
		// 解析表名
		String className = clazz.getName();
		className = className.substring(className.lastIndexOf(".") + 1);
		return className;
	}

	/**
	 * @description 根据关键字删除数据库信息指定信息
	 * @param t
	 *            指对象，可以根据这个对象来获取对应的表
	 * @param key
	 *            表中的关键列名
	 * @param id
	 *            条件的值
	 * @throws Exception
	 */
	public <T> void deleteByKey(T t, String key, String id) throws Exception {
		delete(t, new String[] { key }, new String[] { id });
	}

	/**
	 * 
	 * @Title: delete
	 * @Description: 删除表中所有数据
	 * @param @param t
	 * @param @throws Exception
	 * @return void
	 * @throws
	 */
	public <T> void delete(T t) throws Exception {
		delete(t, null, null);
	}

	/**
	 * 
	 * @Title: delete
	 * @Description:根据条件删除指定数据
	 * @param @param t 实体映射对应的表名
	 * @param @param names 字段名
	 * @param @param whereArgs 字段条件值
	 * @param @throws Exception
	 * @return void
	 * @throws
	 */
	public synchronized <T> void delete(T t, String[] names, String[] whereArgs)
			throws Exception {
		Class clazz = t.getClass();
		String className = getTableName(clazz);
		StringBuffer selection = null;
		if (names != null && names.length > 0) {
			selection = new StringBuffer();
			for (int i = 0; i < names.length; i++) {
				selection.append(names[i]);
				selection.append(" = ?");
				if (i != names.length - 1) {
					selection.append(" and ");
				}
			}
		}
		if (tabbleIsExist(className)) {
			mWriteDatabase.delete(className, selection.toString(), whereArgs);
		}
	}

	/**
	 * 查找表的所有数据
	 * 
	 * @param tableName
	 *            表名
	 * @param columns
	 *            如果返回所有列，则填null
	 * @return
	 * @throws Exception
	 */
	private Cursor findAll(String tableName, String[] columns) throws Exception {
		try {
			cursor = mReadDatabase.query(tableName, columns, null, null, null,
					null, null);
			return cursor;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * @descritpion 获取表中的所有数据
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public synchronized <T> List<T> findAll(T t) throws Exception {
		List<T> colls = new ArrayList<T>();
		Class clazz = t.getClass();
		String className = getTableName(clazz);
		createTable(t);
		Cursor mCursor = findAll(className, null);
		Field[] fields = clazz.getDeclaredFields();
		if (mCursor != null) {
			try {
				colls=parseCursor(mCursor, clazz);
			} finally {
				if (mCursor != null) {
					mCursor.close();
				}
			}
		}
		return colls;
	}

	/**
	 * 根据主键查找数据
	 * 
	 * @param tableName
	 *            表名
	 * @param key
	 *            主键名
	 * @param id
	 *            主键值
	 * @param columns
	 *            如果返回所有列，则填null
	 * @return Cursor游标
	 * @throws Exception
	 */
	private Cursor findById(String tableName, String key, String id,
			String[] columns) {
		return mReadDatabase.query(tableName, columns, key + " =?",
				new String[] { id }, null, null, null);
	}

	/**
	 * 根据主键查找数据
	 * 
	 * @param tableName
	 *            表名
	 * @param key
	 *            主键名
	 * @param id
	 *            主键值
	 * @param columns
	 *            如果返回所有列，则填null
	 * @return Cursor游标
	 * @throws Exception
	 */
	private Cursor findLike(String tableName, String key, String id,
			String[] columns) {
		return mReadDatabase.query(tableName, columns, key + " like?",
				new String[] { "%" + id + "%" }, null, null, null);
	}

	/**
	 * 
	 * 描述: 多条件查询，模糊查询的组合
	 * 
	 * @param tableName
	 *            表名
	 * @param names
	 *            条件的列名
	 * @param values
	 *            列名对应的值
	 * @param likekey
	 *            模糊查询的列
	 * @param likevalue
	 *            模糊查询的值
	 * @param columns
	 *            返回的列
	 * @return
	 */
	private Cursor findLike(String tableName, String[] names, String[] values,
			String likekey, String likevalue, String[] columns) {
		StringBuffer selection = new StringBuffer();
		String[] value = null;
		if (names != null && values != null && names.length > 0) {
			value = new String[names.length + 1];
			for (int i = 0; i < names.length; i++) {
				selection.append(names[i]);
				selection.append(" = ?");
				if (i != names.length - 1) {
					selection.append(" and ");
				}
				value[i] = values[i];
			}
			value[names.length] = "%" + likevalue + "%";
			;
			selection.append(" and " + likekey + " like?");
		} else {
			value = new String[1];
			selection.append(likekey + " like?");
			value[0] = "%" + likevalue + "%";
		}
		return mReadDatabase.query(tableName, columns, selection.toString(),
				value, null, null, null);
	}

	/**
	 * @descritpion 获取表中的所有数据
	 * @param t
	 * @throws Exception
	 */
	public synchronized <T> List<T> findById(T t, String key, String value)
			throws Exception {
		return find(t, new String[]{key}, new String[]{value}, null);
	}

	/**
	 * 
	 * 描述:多条件查询，返回所有列
	 * 
	 * @param t
	 *            表名
	 * @param key
	 *            列名
	 * @param value
	 *            值
	 * @return
	 * @throws Exception
	 */
	public synchronized <T> List<T> findBy(T t, String[] key, String[] value)
			throws Exception {
		return find(t, key, value, null);
	}

	/**
	 * 
	 * 描述:多条件查询，返回指定列
	 * 
	 * @param t
	 *            表名
	 * @param key
	 *            列名
	 * @param value
	 *            值
	 * @param colums
	 *            列名
	 * @return
	 * @throws Exception
	 */
	public synchronized <T> List<T> find(T t, String[] key, String[] value,
			String[] colums) throws Exception {
		List<T> colls = new ArrayList<T>();
		Class clazz = t.getClass();
		String className =getTableName(clazz);
		createTable(t);
		Cursor mCursor = find(className, key, value, colums, null, null, null);
		if (mCursor != null) {
			mReadDatabase.beginTransaction();
			try {
				colls=	parseCursor(mCursor, clazz);
				mReadDatabase.setTransactionSuccessful();
			} finally {
				mReadDatabase.endTransaction();
				if (mCursor != null) {
					mCursor.close();
				}
			}
		}
		return colls;
	}



	/**
	 * 
	 * 描述: 多条件查询，模糊查询的组合
	 * @return
	 */
	public synchronized <T> List<T> findByLike(T t, String[] names,
			String[] values, String key, String value) throws Exception {
		List<T> colls = new ArrayList<T>();
		Class clazz = t.getClass();
		String className = clazz.getName();
		className = className.substring(className.lastIndexOf(".") + 1);
		createTable(t);
		Cursor mCursor = findLike(className, names, values, key, value, null);
		Field[] fields = clazz.getDeclaredFields();
		if (mCursor != null) {
			int count = mCursor.getCount();
			mReadDatabase.beginTransaction();
			try {
				colls=	parseCursor(mCursor, clazz);
				mReadDatabase.setTransactionSuccessful();
			} finally {
				mReadDatabase.endTransaction();
				if (mCursor != null) {
					mCursor.close();
				}
			}
		}
		return colls;
	}

	/**
	 * @descritpion 获取表中的所有数据
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public synchronized <T> List<T> findByLike(T t, String key, String value)
			throws Exception {
		List<T> colls = new ArrayList<T>();
		Class clazz = t.getClass();
		String className = clazz.getName();
		className = className.substring(className.lastIndexOf(".") + 1);
		createTable(t);
		Cursor mCursor = findLike(className, key, value, null);
		Field[] fields = clazz.getDeclaredFields();
		if (mCursor != null) {
			int count = mCursor.getCount();
			mReadDatabase.beginTransaction();
			try {
				colls=	parseCursor(mCursor, clazz);
				mReadDatabase.setTransactionSuccessful();
			} finally {
				mReadDatabase.endTransaction();
				if (mCursor != null) {
					mCursor.close();
				}
			}
		}
		return colls;
	}

	/**
	 * 根据条件查询数据
	 * 
	 * @param tableName
	 *            表名
	 * @param names
	 *            查询条件
	 * @param values
	 *            查询条件值
	 * @param columns
	 *            如果返回所有列，则填null
	 * @param orderColumn
	 *            排序的列 ASC表示升序排序，DESC表示降序排序。
	 * @param limit
	 *            限制返回数
	 * @return Cursor游标
	 * @throws Exception
	 */
	private synchronized Cursor find(String tableName, String[] names,
			String[] values, String[] columns, String orderColumn,
			String minid, String limit) throws Exception {
		try {
			StringBuffer selection = new StringBuffer();
			if (names != null && names.length > 0) {
				for (int i = 0; i < names.length; i++) {
					selection.append(names[i]);
					selection.append(" = ?");
					if (i != names.length - 1) {
						selection.append(" and ");
					}
				}
			}
			String lim;
			if (limit == null || minid == null) {
				lim = null;
			} else {
				String mid = (Integer.valueOf(minid))
						* (Integer.valueOf(limit)) + "";
				lim = mid + "," + limit;
			}
			cursor = mReadDatabase.query(true, tableName, columns,
					selection.toString(), values, null, null, orderColumn, lim);
			return cursor;
		} catch (Exception e) {
			throw e;
		}
	}

	
	
	public synchronized Cursor sqlQuery(String sql) {
		Cursor rawQuery = mReadDatabase.rawQuery(sql, new String[] {});
		return rawQuery;
	}

	public synchronized Cursor sqlQuery(String sql, String[] selectionArgs) {
		Cursor rawQuery = mReadDatabase.rawQuery(sql, selectionArgs);
		return rawQuery;
	}

	/**
	 * @descritpion 获取表中的所有数据
	 * @param t
	 * @param names
	 *            查询条件
	 * @param values
	 *            查询条件值
	 * @param columns
	 *            如果返回所有列，则填null
	 * @param orderColumn
	 *            排序的列 ASC表示升序排序，DESC表示降序排序。
	 * @param limit
	 *            限制返回数
	 * @return Cursor游标
	 * @throws Exception
	 * @return
	 * @throws Exception
	 */
	public synchronized <T> List<T> findLimit(T t, String[] names,
			String[] values, String[] columns, String orderColumn,
			String minid, String limit) throws Exception {
		List<T> colls = new ArrayList<T>();
		Class clazz = t.getClass();
		String className = clazz.getName();
		className = className.substring(className.lastIndexOf(".") + 1);
		createTable(t);
		Cursor mCursor = find(className, names, values, columns, orderColumn,
				minid, limit);
		if (mCursor != null) {
			int count = mCursor.getCount();
			mReadDatabase.beginTransaction();
			try {
				colls=	parseCursor(mCursor, clazz);
				mReadDatabase.setTransactionSuccessful();
			} finally {
				mReadDatabase.endTransaction();
				if (mCursor != null) {
					mCursor.close();
				}
			}
		}
		return colls;
	}


	/**
	 * 
	 * @author longqiankun
	 * @description : 解析游标
	 * @param mCursor
	 * @param clazz
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static <T> List<T> parseCursor(Cursor mCursor, Class clazz)
			throws InstantiationException, IllegalAccessException {
		List<T> colls = new ArrayList<T>();
		if (mCursor != null && clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			while (mCursor.moveToNext()) {
				T t2 = (T) clazz.newInstance();
				for (Field field : fields) {
					if (!field.isAnnotationPresent(Column.class))
						continue;
					field.setAccessible(true);
					Class<?> type = field.getType();
					String fieldName = field.getName();
					String value = mCursor.getString(mCursor
							.getColumnIndex(fieldName));
					if (!TextUtils.isEmpty(value)) {
						field.set(t2, value);
					} else {
						field.set(t2, "");
					}

				}
				colls.add(t2);
			}
		}
		return colls;
	}

	public <T> boolean update(T t, String[] names, String[] values)
			throws Exception {
		return update(t, names, values, null, null, null);
	}
	
	public <T> boolean update(T t, String[] names, String[] values,ContentValues args)
			throws Exception {
		return update(t, names, values, null, null, args);
	}
	public <T> boolean update(T t, String[] names, String[] values,Set<String> updateColum)
			throws Exception {
		return update(t, names, values, updateColum, null, null);
	}
	/**
	 * 
	 * @author longqiankun
	 * @description : 更新数据
	 * @param t
	 * @param names
	 * @param values
	 * @param updateColum
	 * @param noUpdateColum
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public synchronized <T> boolean update(T t, String[] names, String[] values,Set<String> updateColum, Set<String> noUpdateColum,ContentValues args)
			throws Exception {
		Class clazz = t.getClass();
		String className = getTableName(clazz);
		createTable(t);
		if(args==null){
		 args = new ContentValues();
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (!field.isAnnotationPresent(Column.class))
				continue;
		
			field.setAccessible(true);
			Class<?> type = field.getType();
			String fieldName = field.getName();
			// 判断当前字段是否在需要操作的列表中
			if (updateColum != null) {
				if (!updateColum.contains(fieldName)) {
					continue;
				}
			}

			// 判断当前字段是否在不需要操作的列表中
			if (noUpdateColum != null) {
				if (noUpdateColum.contains(fieldName)) {
					continue;
				}
			}
			String value = (String) field.get(t);
			args.put(fieldName, value);
		}
		}
		StringBuffer selection = new StringBuffer("");
		if (names != null && names.length > 0) {
			for (int i = 0; i < names.length; i++) {
				selection.append(names[i]);
				selection.append(" = ?");
				if (i != names.length - 1) {
					selection.append(" and ");
				}
			}
		}
	
		boolean isOk = mWriteDatabase.update(className, args,
				selection.toString(), values) > 0;
				if (mUpdateListener != null) {
					mUpdateListener.onUpdateTable(className);
				}
		return isOk;
	}
	/**
	 * 执行sql语句，包括创建表、删除、插入
	 * 
	 * @param sql
	 */
	public synchronized void executeSql(String sql) {
		mWriteDatabase.execSQL(sql);
	}
	/**
	 * 根据表名查看是否存在
	 * 
	 * @param tableName
	 *            表名
	 * @return
	 */
	public boolean tabbleIsExist(String tableName) {
		boolean result = false;
		if (tableName == null) {
			return false;
		}
		Cursor cursor = null;
		try {
			String sql = "select count(*) as c from " + MASTER
					+ " where type ='table' and name ='" + tableName.trim()
					+ "' ";
			cursor = mReadDatabase.rawQuery(sql, null);
			if(cursor!=null){
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}
			}

		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			if(cursor!=null){
				cursor.close();
			}
		}
		return result;
	}

	/**
	 * 根据实体对象判断对应表是否存在
	 * 
	 * @param t
	 *            实体对象
	 * @return
	 */
	public <T> boolean tabbleIsExist(T t) {
		Class clazz = t.getClass();
		String tableName = getTableName(clazz);
		return tabbleIsExist(tableName);
	}

	/**
	 * 创建数据库表
	 */

	String lastTable;
	public <T> void createTable(T t) {
		Class<? extends Object> class1 = t.getClass();
		String tableName = getTableName(class1);
		if(null!=lastTable&&null!=tableName&&tableName.equals(lastTable)){
			return;
		}
		lastTable=tableName;
		StringBuilder sb = new StringBuilder();// 生产sql语句
		// 判断表是否被创建，如果没创建则创建
		if (tabbleIsExist(tableName)) {
			Field[] fields = class1.getFields();
			/*Cursor query = mReadDatabase.query(tableName, null, null, null,
					null, null, null);
			String[] columnNames = query.getColumnNames();*/
			// 判断列数了是否有变化
			for (int i = 0; i < fields.length; i++) {
				if (!fields[i].isAnnotationPresent(Column.class))
					continue;
				String filed = fields[i].getName();
				// 方法1
				/*
				 * boolean flag=true;//表示表中是否包含 for (int j = 0; j <
				 * columnNames.length; j++) { String columeName=columnNames[j];
				 * if(filed.equals(columeName)){//如果 flag=false; } }
				 * if(flag){//表中不包含该字段 StringBuffer sql=new StringBuffer();
				 * sql.append("alter table "); sql.append(tableName);
				 * sql.append(" add "); sql.append(filed); sql.append("text;");
				 * mSQLiteDatabase.execSQL(sql.toString());// 在表中添加新字段 }
				 */

				// 方法2
				if (!checkColumnExist1(tableName, filed)) {
					StringBuffer sql = new StringBuffer();
					sql.append("alter table ");
					sql.append(tableName);
					sql.append(" add ");
					sql.append(filed);
					sql.append(" text;");
					mWriteDatabase.execSQL(sql.toString());// 在表中添加新字段
				}
			}
			// }
		} else {
			sb.append("create table ");
			sb.append(tableName);
			sb.append("(");
			Field[] fields = class1.getFields();

			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (!field.isAnnotationPresent(Column.class))
					continue;
				String _id = field.getName();
				if ("_id".equals(_id)) {// 判断是否是自增编号
					sb.append(_id);// 字段
					if (i != fields.length - 1) {// 判断是否是最后一个字段，如果是，不加后面的逗号，否则加上后面逗号
						sb.append(" integer primary key autoincrement, ");// 字段类型
					} else {
						sb.append(" integer primary key autoincrement ");// 字段类型
					}
				} else {
					sb.append(_id);// 字段
					if (i != fields.length - 1) {// 判断是否是最后一个字段，如果是，不加后面的逗号，否则加上后面逗号
						sb.append(" text, ");// 字段类型
					} else {
						sb.append(" text ");// 字段类型
					}
				}
			}

			String execSQL = sb.toString();
			if (execSQL.contains("text")
					&& execSQL.substring(execSQL.lastIndexOf("text"),
							execSQL.length()).contains(", ")) {
				sb.deleteCharAt(execSQL.lastIndexOf(","));
			}
			sb.append(");");
			if (mWriteDatabase != null) {
				mWriteDatabase.execSQL(sb.toString());
			}
		}
	}

	/**
	 * 方法1：检查某表列是否存在
	 * 
	 * @param db
	 * @param tableName
	 *            表名
	 * @param columnName
	 *            列名
	 * @return
	 */
	private boolean checkColumnExist1(String tableName,
			String columnName) {
		boolean result = false;
		Cursor cursor = null;
		try {
			// 查询一行
			cursor = mReadDatabase.rawQuery("SELECT * FROM " + tableName + " LIMIT 0",
					null);
			result = cursor != null && cursor.getColumnIndex(columnName) != -1;
		} catch (Exception e) {
		} finally {
			if (null != cursor && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return result;
	}

	/**
	 * 方法2：检查表中某列是否存在
	 * 
	 * @param db
	 * @param tableName
	 *            表名
	 * @param columnName
	 *            列名
	 * @return
	 */
	private boolean checkColumnExists2(String tableName,
			String columnName) {
		boolean result = false;
		Cursor cursor = null;
		try {
			cursor = mReadDatabase
					.rawQuery(
							"select * from sqlite_master where name = ? and sql like ?",
							new String[] { tableName, "%" + columnName + "%" });
			result = null != cursor && cursor.moveToFirst();
		} catch (Exception e) {
		} finally {
			if (null != cursor && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return result;
	}

	/**
	 * 获取所有的表名 CREATE TABLE sqlite_master ( type TEXT, name TEXT, tbl_name TEXT,
	 * rootpage INTEGER, sql TEXT );
	 * 
	 * @return
	 */
	private List<String> getAllTableNames(SQLiteDatabase db) {
		List<String> tables = new ArrayList<String>();
		Cursor cursor = null;
		try {
			cursor = db
					.rawQuery(
							"select name,tbl_name from sqlite_master where type='table' order by name",
							null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String tName = cursor.getString(cursor
							.getColumnIndex("tbl_name"));
					tables.add(tName);
				}
			}
		} catch (Exception e) {
		} finally {
			if (null != cursor && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return tables;
	}

	/**
	 * 删除所有的表数据
	 */
	public void delAllTablesData() {
		List<String> allTableNames = getAllTableNames(mWriteDatabase);
		for (int i = 0; i < allTableNames.size(); i++) {
			String tName = allTableNames.get(i);
			mWriteDatabase.delete(tName, null, null);
		}
	}

	/**
	 * 
	 * 描述:获取表中数据的数量
	 * 
	 * @param t
	 *            表名
	 * @param key
	 *            列名
	 * @param value
	 *            值
	 * @return
	 * @throws Exception
	 */
	public <T> int getCount(T t, String[] names, String[] value)
			throws Exception {
		List<T> colls = new ArrayList<T>();
		Class clazz = t.getClass();
		String className = getTableName(clazz);
		createTable(t);
		StringBuffer selection = new StringBuffer();
		if (names != null && names.length > 0) {
			selection.append(" where ");
			for (int i = 0; i < names.length; i++) {
				selection.append(names[i]);
				selection.append(" = ");
				selection.append("'" + value[i] + "'");
				if (i != names.length - 1) {
					selection.append(" and ");
				}
			}
		}
		long count = 0;
		Cursor cursor = mReadDatabase.rawQuery("Select  count(*) from "
				+ className + selection.toString() + ";", null);
		if (cursor != null) {

			cursor.moveToFirst();
			// 获取数据中的LONG类型数据
			count = cursor.getLong(0);
			cursor.close();
		}
		return (int) count;

	}
	// 监听表的跟新
	private OnTableUpdateListener mUpdateListener;

	public void setOnTableUpdateListener(OnTableUpdateListener mUpdateListener) {
		this.mUpdateListener = mUpdateListener;
	}

	public interface OnTableUpdateListener {
		void onUpdateTable(String tableName);
	}
}
