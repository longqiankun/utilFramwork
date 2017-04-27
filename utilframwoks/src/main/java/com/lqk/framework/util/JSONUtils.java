package com.lqk.framework.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;

import com.lqk.framework.app.Ioc;

/**
 * Json Utils
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-5-12
 */
public class JSONUtils {

	public static boolean isPrintException = true;

	private static Map<Class<?>, ArrayList<fieldEntity>> method_map = new HashMap<Class<?>, ArrayList<fieldEntity>>();

	/**
	 * json字符串转化为集合
	 * 
	 * @param str
	 * @return Object
	 */
	public static Object JsonToHashMap(String str) {
		LinkedHashMap<String, Object> json = new LinkedHashMap<String, Object>();
		try {
			Object object = new JSONTokener(str).nextValue();
			if (object instanceof JSONArray) {
				JSONArray root = new JSONArray(str);
				ArrayList<Object> list = new ArrayList<Object>();
				if (root.length() > 0) {
					for (int i = 0; i < root.length(); i++) {
						list.add(JsonToCollection(root.getString(i)));
					}
					return list;
				}
				return list.add(str);
			} else if (object instanceof JSONObject) {
				JSONObject root = new JSONObject(str);
				if (root.length() > 0) {
					@SuppressWarnings("unchecked")
					Iterator<String> rootName = root.keys();
					String name;
					while (rootName.hasNext()) {
						name = rootName.next();
						json.put(name, JsonToCollection(root.getString(name)));
					}
				}
				return json;
			} else {
				return str;
			}
		} catch (JSONException e) {
			Ioc.getIoc().getLogger().d("错误字符串：" + str);
			return str;
		}
	}

	/**
	 * json转为对象
	 * 
	 * @param str
	 * @param entity
	 * @return Object
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object JsonToBean(String str, Object entity) {
		try {
			Object object = new JSONTokener(str).nextValue();
			if (object instanceof JSONArray) {
				JSONArray root = new JSONArray(str);
				if (root.length() > 0) {
					ArrayList<Object> list = new ArrayList<Object>();
					for (int i = 0; i < root.length(); i++) {
						Object value = new JSONTokener(root.getString(i))
								.nextValue();
						if (classes.contains(value.getClass())) {
							list.add(root.getString(i));
						} else {
							list.add(JsonToBean(root.getString(i), entity
									.getClass().newInstance()));
						}
					}
					return list;
				}
				Ioc.getIoc().getLogger().e("数组" + entity + "解析出错");
				return null;
			} else if (object instanceof JSONObject) {
				JSONObject root = new JSONObject(str);
				if (root.length() > 0) {
					Iterator<String> rootName = root.keys();
					String name;
					while (rootName.hasNext()) {
						name = rootName.next();
						boolean isHas = false;
						Class template = entity.getClass();
						while (template != null && !classes.contains(template)) {
							ArrayList<fieldEntity> arrayList = method_map
									.get(template);
							for (fieldEntity fieldEntity : arrayList) {
								fieldEntity.field.setAccessible(true);
								Object obj = null;
								if (name.equals(fieldEntity.field.getName())) {
									isHas = true;
									if (fieldEntity.clazz == null) {
										Class clazz = fieldEntity.field
												.getType();
										if (clazz == String.class) {
											obj = root.getString(name);
										}
										if (clazz == int.class) {
											obj = root.getInt(name);
										}
										if (clazz == boolean.class) {
											obj = root.getBoolean(name);
										}
									} else {
										Object obj2 = new JSONTokener(
												root.getString(name))
												.nextValue();
										Class value_class = fieldEntity.field
												.getType();
										if (classes.contains(value_class)) {
											JSONArray array = (JSONArray) obj2;
											ArrayList<Object> list = new ArrayList<Object>();
											for (int i = 0; i < array.length(); i++) {
												if (fieldEntity.clazz == String.class) {
													obj = array.get(i)
															.toString();
												}
												if (fieldEntity.clazz == int.class) {
													obj = Integer.valueOf(array
															.get(i).toString());
												}
												if (fieldEntity.clazz == boolean.class) {
													obj = Boolean.valueOf(array
															.get(i).toString());
												}
												list.add(obj);
											}
											obj = list;
										} else {
											try {
												obj = JsonToBean(
														root.getString(name),
														fieldEntity.clazz
																.newInstance());
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}
									try {
										fieldEntity.field.set(entity, obj);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
							template = template.getSuperclass();
						}

						if (!isHas) {
							Ioc.getIoc().getLogger()
									.e("字段" + name + "在实体类" + entity + "不存在");
						}
					}
				} else {
					Ioc.getIoc().getLogger().e("数据长度不对 解析出错");
				}
				return entity;
			} else {
				return entity;
			}
		} catch (Exception e) {
			Ioc.getIoc().getLogger().d("错误字符串：" + str);
			return entity;
		}
	}

	@SuppressWarnings({ "rawtypes", "serial" })
	static HashSet<Class> classes = new HashSet<Class>() {
		{
			add(Object.class);
			add(Double.class);
			add(Float.class);
			add(Integer.class);
			add(Long.class);
			add(String.class);
			add(int.class);
			add(boolean.class);
		}
	};

	public static void getMethod(Class<?> clazz) {

		Class<?> template = clazz;
		while (template != null && template != Object.class) {
			if (method_map.get(template) != null
					&& method_map.get(template).size() > 0) {
				return;
			}
			template = template.getSuperclass();
		}
		template = clazz;
		while (template != null && !classes.contains(template)) {
			// -----------------------------------解析变量------------------------------------------------
			ArrayList<fieldEntity> entities = new ArrayList<fieldEntity>();
			for (Field m : template.getDeclaredFields()) {
				Type type = m.getGenericType();
				int modifiers = m.getModifiers();
				if (Modifier.isStatic(modifiers)) {
					continue;
				}
				if (type instanceof ParameterizedType) {
					Type[] types = ((ParameterizedType) type)
							.getActualTypeArguments();
					for (Type type2 : types) {
						if (!classes.contains(m.getType())) {
							getMethod((Class<?>) type2);
							entities.add(new fieldEntity(m, (Class<?>) type2));
						} else {
							entities.add(new fieldEntity(m, null));
						}
						break;
					}
					continue;
				}
				if (!classes.contains(m.getType())) {
					getMethod((Class<?>) type);
					entities.add(new fieldEntity(m, (Class<?>) type));
				} else {
					entities.add(new fieldEntity(m, null));
				}
			}
			method_map.put(template, entities);
			// -----------------------------------解析完毕------------------------------------------------
			template = template.getSuperclass();
		}
	}

	/**
	 * json字符串转换为bean
	 * 
	 * @param clazz
	 * @param json
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	public static <T> T JsonToBean(Class<?> clazz, String json) {
		getMethod(clazz);
		T object = null;
		try {
			object = (T) JsonToBean(json, clazz.newInstance());
		} catch (Exception e) {
		}
		return object;
	}

	@SuppressWarnings("unchecked")
	public static <T> T JsonToCollection(String str) {
		T object = null;
		try {
			object = (T) JsonToHashMap(str);
		} catch (Exception e) {
		}
		return object;
	}

	/**
	 * 
	 * @author longqiankun
	 * @description :对象转换成json
	 * @param obj
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static String objectToJson(Object obj)
			throws IllegalArgumentException, IllegalAccessException {
		StringBuffer sb = new StringBuffer();
		if (obj != null) {
			Class tc = obj.getClass();
			if (tc.isPrimitive() || tc.isAssignableFrom(String.class)
					|| tc.isAssignableFrom(Byte.class)
					|| tc.isAssignableFrom(Short.class)
					|| tc.isAssignableFrom(Integer.class)
					|| tc.isAssignableFrom(Long.class)
					|| tc.isAssignableFrom(Float.class)
					|| tc.isAssignableFrom(Double.class)
					|| tc.isAssignableFrom(Character.class)
					|| tc.isAssignableFrom(Boolean.class)) {
				if (tc.isAssignableFrom(String.class)) {
					sb.append("\"" + obj.toString() + "\"");
				} else {
					sb.append(obj.toString());
				}
			} else if (obj.getClass().isArray()) {
				Object[] objs = (Object[]) obj;
				addSE(sb, obj, true);
				for (int i = 0; i < objs.length; i++) {
					sb.append(objectToJson(objs[i]));
					if ((objs.length - 1) != i) {
						sb.append(",");
					}
				}
				addSE(sb, obj, false);
			} else if (obj instanceof List<?>) {
				List<?> list = (List<?>) obj;
				addSE(sb, obj, true);
				for (int i = 0; i < list.size(); i++) {
					sb.append(objectToJson(list.get(i)));
					if ((list.size() - 1) != i) {
						sb.append(",");
					}
				}
				addSE(sb, obj, false);
			} else if (obj instanceof Set<?>) {
				Set<?> list = (Set<?>) obj;
				addSE(sb, obj, true);
				Iterator<?> iterator = list.iterator();
				int index = 0;
				while (iterator.hasNext()) {
					sb.append(objectToJson(iterator.next()));
					if ((list.size() - 1) != index) {
						sb.append(",");
					}
					index = index + 1;
				}
				addSE(sb, obj, false);
			} else if (obj instanceof Map<?, ?>) {
				Map<?, ?> map = (Map<?, ?>) obj;
				Set<?> entrySet = map.entrySet();
				addSE(sb, obj, true);
				Iterator<?> iterator = entrySet.iterator();
				int index = 0;
				while (iterator.hasNext()) {
					Map.Entry<?, ?> mapentry = (Map.Entry<?, ?>) iterator
							.next();
					Object key2 = mapentry.getKey();
					Object value2 = mapentry.getValue();
					sb.append("\"" + key2 + "\":");
					sb.append(objectToJson(value2));
					if ((entrySet.size() - 1) != index) {
						sb.append(",");
					}
					index = index + 1;
				}
				addSE(sb, obj, false);
			} else {
				addSE(sb, obj, true);
				Field[] fields = tc.getDeclaredFields();
				int count = 0;
				for (Field f : fields) {
					f.setAccessible(true);
					Class fc = f.getType();
					String key = f.getName();
					Object value = f.get(obj);
					if (value == null) {
						sb.append("\"" + key + "\":" + null);
					} else if (fc.isPrimitive()
							|| fc.isAssignableFrom(String.class)
							|| fc.isAssignableFrom(Byte.class)
							|| fc.isAssignableFrom(Short.class)
							|| fc.isAssignableFrom(Integer.class)
							|| fc.isAssignableFrom(Long.class)
							|| fc.isAssignableFrom(Float.class)
							|| fc.isAssignableFrom(Double.class)
							|| fc.isAssignableFrom(Character.class)
							|| fc.isAssignableFrom(Boolean.class)) {
						if (fc.isAssignableFrom(String.class)) {
							String v = "";
							if (value != null) {
								v = value.toString();
							}
							sb.append("\"" + key + "\":\"" + v + "\"");
						} else {
							String v = "";
							if (value != null) {
								v = value.toString();
							}
							sb.append("\"" + key + "\":" + v);

						}
					} else {
						if (value.getClass().isArray()) {
							Object[] objs = (Object[]) value;
							sb.append("\"" + key + "\":");
							addSE(sb, value, true);
							for (int i = 0; i < objs.length; i++) {
								sb.append(objectToJson(objs[i]));
								if ((objs.length - 1) != i) {
									sb.append(",");
								}
							}
							addSE(sb, value, false);
						} else if (fc.isAssignableFrom(List.class)) { // 判断是否为List
							List<?> list = (List<?>) value;
							sb.append("\"" + key + "\":");
							addSE(sb, value, true);
							for (int i = 0; i < list.size(); i++) {
								sb.append(objectToJson(list.get(i)));
								if ((list.size() - 1) != i) {
									sb.append(",");
								}
							}
							addSE(sb, value, false);
						} else if (fc.isAssignableFrom(Map.class)) {
							Map<?, ?> map = (Map<?, ?>) value;
							sb.append("\"" + key + "\":");
							addSE(sb, value, true);
							Set<?> entrySet = map.entrySet();
							Iterator<?> iterator = entrySet.iterator();
							int index = 0;
							while (iterator.hasNext()) {
								Map.Entry<?, ?> mapentry = (Map.Entry<?, ?>) iterator
										.next();
								Object key2 = mapentry.getKey();
								Object value2 = mapentry.getValue();
								sb.append("\"" + key2 + "\":");
								sb.append(objectToJson(value2));
								if ((entrySet.size() - 1) != index) {
									sb.append(",");
								}
								index = index + 1;
							}
							addSE(sb, value, false);
						} else if (fc.isAssignableFrom(Set.class)) {
							Set<?> list = (Set<?>) value;
							sb.append("\"" + key + "\":");
							addSE(sb, value, true);

							Iterator<?> iterator = list.iterator();
							int index = 0;
							while (iterator.hasNext()) {
								sb.append(objectToJson(iterator.next()));
								if ((list.size() - 1) != index) {
									sb.append(",");
								}
								index = index + 1;
							}
							addSE(sb, value, false);
						}

					}
					if ((fields.length - 1) != count) {
						sb.append(",");
					}
					count = count + 1;
				}
				addSE(sb, obj, false);
			}
			return sb.toString();
		}
		return null;
	}

	/**
	 * 
	 * @author longqiankun
	 * @description : 添加标记
	 * @param sb
	 * @param obj
	 * @param isStart
	 * @return
	 */
	private static StringBuffer addSE(StringBuffer sb, Object obj,
			boolean isStart) {
		if (obj instanceof Collection || obj.getClass().isArray()) {
			if (isStart) {
				sb.append("[");
			} else {
				sb.append("]");
			}
		} else {
			if (isStart) {
				sb.append("{");
			} else {
				sb.append("}");
			}
		}
		return sb;
	}

	/**
	 * 解析内部类
	 * 
	 */
	public static class fieldEntity {
		public Field field;
		public Class<?> clazz;

		public fieldEntity(Field field, Class<?> clazz) {
			this.field = field;
			this.clazz = clazz;
		}

		@Override
		public String toString() {
			return "fieldEntity [field=" + field.getName() + ", clazz=" + clazz
					+ "]";
		}

	}

	/**
	 * get Long from jsonObject
	 * 
	 * @param jsonObject
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if key is null or empty, return defaultValue</li>
	 *         <li>if {@link JSONObject#getLong(String)} exception, return
	 *         defaultValue</li>
	 *         <li>return {@link JSONObject#getLong(String)}</li>
	 *         </ul>
	 */
	public static Long getLong(JSONObject jsonObject, String key,
			Long defaultValue) {
		if (jsonObject == null || StringUtils.isEmpty(key)) {
			return defaultValue;
		}

		try {
			return jsonObject.getLong(key);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * get Long from jsonData
	 * 
	 * @param jsonData
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if jsonData {@link JSONObject#JSONObject(String)} exception,
	 *         return defaultValue</li>
	 *         <li>return
	 *         {@link JSONUtils#getLong(JSONObject, String, JSONObject)}</li>
	 *         </ul>
	 */
	public static Long getLong(String jsonData, String key, Long defaultValue) {
		if (StringUtils.isEmpty(jsonData)) {
			return defaultValue;
		}

		try {
			JSONObject jsonObject = new JSONObject(jsonData);
			return getLong(jsonObject, key, defaultValue);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * @param jsonObject
	 * @param key
	 * @param defaultValue
	 * @return
	 * @see JSONUtils#getLong(JSONObject, String, Long)
	 */
	public static long getLong(JSONObject jsonObject, String key,
			long defaultValue) {
		return getLong(jsonObject, key, (Long) defaultValue);
	}

	/**
	 * @param jsonData
	 * @param key
	 * @param defaultValue
	 * @return
	 * @see JSONUtils#getLong(String, String, Long)
	 */
	public static long getLong(String jsonData, String key, long defaultValue) {
		return getLong(jsonData, key, (Long) defaultValue);
	}

	/**
	 * get Int from jsonObject
	 * 
	 * @param jsonObject
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if key is null or empty, return defaultValue</li>
	 *         <li>if {@link JSONObject#getInt(String)} exception, return
	 *         defaultValue</li>
	 *         <li>return {@link JSONObject#getInt(String)}</li>
	 *         </ul>
	 */
	public static Integer getInt(JSONObject jsonObject, String key,
			Integer defaultValue) {
		if (jsonObject == null || StringUtils.isEmpty(key)) {
			return defaultValue;
		}

		try {
			return jsonObject.getInt(key);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * get Int from jsonData
	 * 
	 * @param jsonData
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if jsonData {@link JSONObject#JSONObject(String)} exception,
	 *         return defaultValue</li>
	 *         <li>return
	 *         {@link JSONUtils#getInt(JSONObject, String, JSONObject)}</li>
	 *         </ul>
	 */
	public static Integer getInt(String jsonData, String key,
			Integer defaultValue) {
		if (StringUtils.isEmpty(jsonData)) {
			return defaultValue;
		}

		try {
			JSONObject jsonObject = new JSONObject(jsonData);
			return getInt(jsonObject, key, defaultValue);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * @param jsonObject
	 * @param key
	 * @param defaultValue
	 * @return
	 * @see JSONUtils#getInt(JSONObject, String, Integer)
	 */
	public static int getInt(JSONObject jsonObject, String key, int defaultValue) {
		return getInt(jsonObject, key, (Integer) defaultValue);
	}

	/**
	 * @param jsonObject
	 * @param key
	 * @param defaultValue
	 * @return
	 * @see JSONUtils#getInt(String, String, Integer)
	 */
	public static int getInt(String jsonData, String key, int defaultValue) {
		return getInt(jsonData, key, (Integer) defaultValue);
	}

	/**
	 * get Double from jsonObject
	 * 
	 * @param jsonObject
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if key is null or empty, return defaultValue</li>
	 *         <li>if {@link JSONObject#getDouble(String)} exception, return
	 *         defaultValue</li>
	 *         <li>return {@link JSONObject#getDouble(String)}</li>
	 *         </ul>
	 */
	public static Double getDouble(JSONObject jsonObject, String key,
			Double defaultValue) {
		if (jsonObject == null || StringUtils.isEmpty(key)) {
			return defaultValue;
		}

		try {
			return jsonObject.getDouble(key);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * get Double from jsonData
	 * 
	 * @param jsonData
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if jsonData {@link JSONObject#JSONObject(String)} exception,
	 *         return defaultValue</li>
	 *         <li>return
	 *         {@link JSONUtils#getDouble(JSONObject, String, JSONObject)}</li>
	 *         </ul>
	 */
	public static Double getDouble(String jsonData, String key,
			Double defaultValue) {
		if (StringUtils.isEmpty(jsonData)) {
			return defaultValue;
		}

		try {
			JSONObject jsonObject = new JSONObject(jsonData);
			return getDouble(jsonObject, key, defaultValue);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * @param jsonObject
	 * @param key
	 * @param defaultValue
	 * @return
	 * @see JSONUtils#getDouble(JSONObject, String, Double)
	 */
	public static double getDouble(JSONObject jsonObject, String key,
			double defaultValue) {
		return getDouble(jsonObject, key, (Double) defaultValue);
	}

	/**
	 * @param jsonObject
	 * @param key
	 * @param defaultValue
	 * @return
	 * @see JSONUtils#getDouble(String, String, Double)
	 */
	public static double getDouble(String jsonData, String key,
			double defaultValue) {
		return getDouble(jsonData, key, (Double) defaultValue);
	}

	/**
	 * get String from jsonObject
	 * 
	 * @param jsonObject
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if key is null or empty, return defaultValue</li>
	 *         <li>if {@link JSONObject#getString(String)} exception, return
	 *         defaultValue</li>
	 *         <li>return {@link JSONObject#getString(String)}</li>
	 *         </ul>
	 */
	public static String getString(JSONObject jsonObject, String key,
			String defaultValue) {
		if (jsonObject == null || StringUtils.isEmpty(key)) {
			return defaultValue;
		}

		try {
			return jsonObject.getString(key);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * get String from jsonData
	 * 
	 * @param jsonData
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if jsonData {@link JSONObject#JSONObject(String)} exception,
	 *         return defaultValue</li>
	 *         <li>return
	 *         {@link JSONUtils#getString(JSONObject, String, JSONObject)}</li>
	 *         </ul>
	 */
	public static String getString(String jsonData, String key,
			String defaultValue) {
		if (StringUtils.isEmpty(jsonData)) {
			return defaultValue;
		}

		try {
			JSONObject jsonObject = new JSONObject(jsonData);
			return getString(jsonObject, key, defaultValue);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * get String array from jsonObject
	 * 
	 * @param jsonObject
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if key is null or empty, return defaultValue</li>
	 *         <li>if {@link JSONObject#getJSONArray(String)} exception, return
	 *         defaultValue</li>
	 *         <li>if {@link JSONArray#getString(int)} exception, return
	 *         defaultValue</li>
	 *         <li>return string array</li>
	 *         </ul>
	 */
	public static String[] getStringArray(JSONObject jsonObject, String key,
			String[] defaultValue) {
		if (jsonObject == null || StringUtils.isEmpty(key)) {
			return defaultValue;
		}

		try {
			JSONArray statusArray = jsonObject.getJSONArray(key);
			if (statusArray != null) {
				String[] value = new String[statusArray.length()];
				for (int i = 0; i < statusArray.length(); i++) {
					value[i] = statusArray.getString(i);
				}
				return value;
			}
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
		return defaultValue;
	}

	/**
	 * get String array from jsonData
	 * 
	 * @param jsonData
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if jsonData {@link JSONObject#JSONObject(String)} exception,
	 *         return defaultValue</li>
	 *         <li>return
	 *         {@link JSONUtils#getStringArray(JSONObject, String, JSONObject)}</li>
	 *         </ul>
	 */
	public static String[] getStringArray(String jsonData, String key,
			String[] defaultValue) {
		if (StringUtils.isEmpty(jsonData)) {
			return defaultValue;
		}

		try {
			JSONObject jsonObject = new JSONObject(jsonData);
			return getStringArray(jsonObject, key, defaultValue);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * get JSONObject from jsonObject
	 * 
	 * @param jsonObject
	 *            <em><em></em></em>
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if key is null or empty, return defaultValue</li>
	 *         <li>if {@link JSONObject#getJSONObject(String)} exception, return
	 *         defaultValue</li>
	 *         <li>return {@link JSONObject#getJSONObject(String)}</li>
	 *         </ul>
	 */
	public static JSONObject getJSONObject(JSONObject jsonObject, String key,
			JSONObject defaultValue) {
		if (jsonObject == null || StringUtils.isEmpty(key)) {
			return defaultValue;
		}

		try {
			return jsonObject.getJSONObject(key);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * get JSONObject from jsonData
	 * 
	 * @param jsonData
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if jsonData {@link JSONObject#JSONObject(String)} exception,
	 *         return defaultValue</li>
	 *         <li>return
	 *         {@link JSONUtils#getJSONObject(JSONObject, String, JSONObject)}</li>
	 *         </ul>
	 */
	public static JSONObject getJSONObject(String jsonData, String key,
			JSONObject defaultValue) {
		if (StringUtils.isEmpty(jsonData)) {
			return defaultValue;
		}

		try {
			JSONObject jsonObject = new JSONObject(jsonData);
			return getJSONObject(jsonObject, key, defaultValue);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * get JSONArray from jsonObject
	 * 
	 * @param jsonObject
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if key is null or empty, return defaultValue</li>
	 *         <li>if {@link JSONObject#getJSONArray(String)} exception, return
	 *         defaultValue</li>
	 *         <li>return {@link JSONObject#getJSONArray(String)}</li>
	 *         </ul>
	 */
	public static JSONArray getJSONArray(JSONObject jsonObject, String key,
			JSONArray defaultValue) {
		if (jsonObject == null || StringUtils.isEmpty(key)) {
			return defaultValue;
		}

		try {
			return jsonObject.getJSONArray(key);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * get JSONArray from jsonData
	 * 
	 * @param jsonData
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if jsonData {@link JSONObject#JSONObject(String)} exception,
	 *         return defaultValue</li>
	 *         <li>return
	 *         {@link JSONUtils#getJSONArray(JSONObject, String, JSONObject)}</li>
	 *         </ul>
	 */
	public static JSONArray getJSONArray(String jsonData, String key,
			JSONArray defaultValue) {
		if (StringUtils.isEmpty(jsonData)) {
			return defaultValue;
		}

		try {
			JSONObject jsonObject = new JSONObject(jsonData);
			return getJSONArray(jsonObject, key, defaultValue);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * get Boolean from jsonObject
	 * 
	 * @param jsonObject
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if key is null or empty, return defaultValue</li>
	 *         <li>return {@link JSONObject#getBoolean(String)}</li>
	 *         </ul>
	 */
	public static boolean getBoolean(JSONObject jsonObject, String key,
			Boolean defaultValue) {
		if (jsonObject == null || StringUtils.isEmpty(key)) {
			return defaultValue;
		}

		try {
			return jsonObject.getBoolean(key);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * get Boolean from jsonData
	 * 
	 * @param jsonData
	 * @param key
	 * @param defaultValue
	 * @return <ul>
	 *         <li>if jsonObject is null, return defaultValue</li>
	 *         <li>if jsonData {@link JSONObject#JSONObject(String)} exception,
	 *         return defaultValue</li>
	 *         <li>return
	 *         {@link JSONUtils#getBoolean(JSONObject, String, Boolean)}</li>
	 *         </ul>
	 */
	public static boolean getBoolean(String jsonData, String key,
			Boolean defaultValue) {
		if (StringUtils.isEmpty(jsonData)) {
			return defaultValue;
		}

		try {
			JSONObject jsonObject = new JSONObject(jsonData);
			return getBoolean(jsonObject, key, defaultValue);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return defaultValue;
		}
	}

	/**
	 * get map from jsonObject.
	 * 
	 * @param jsonObject
	 *            key-value pairs json
	 * @param key
	 * @return <ul>
	 *         <li>if jsonObject is null, return null</li>
	 *         <li>return {@link JSONUtils#parseKeyAndValueToMap(String)}</li>
	 *         </ul>
	 */
	public static Map<String, String> getMap(JSONObject jsonObject, String key) {
		return JSONUtils.parseKeyAndValueToMap(JSONUtils.getString(jsonObject,
				key, null));
	}

	/**
	 * get map from jsonData.
	 * 
	 * @param jsonData
	 *            key-value pairs string
	 * @param key
	 * @return <ul>
	 *         <li>if jsonData is null, return null</li>
	 *         <li>if jsonData length is 0, return empty map</li>
	 *         <li>if jsonData {@link JSONObject#JSONObject(String)} exception,
	 *         return null</li>
	 *         <li>return {@link JSONUtils#getMap(JSONObject, String)}</li>
	 *         </ul>
	 */
	public static Map<String, String> getMap(String jsonData, String key) {

		if (jsonData == null) {
			return null;
		}
		if (jsonData.length() == 0) {
			return new HashMap<String, String>();
		}

		try {
			JSONObject jsonObject = new JSONObject(jsonData);
			return getMap(jsonObject, key);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/**
	 * parse key-value pairs to map. ignore empty key, if getValue exception,
	 * put empty value
	 * 
	 * @param sourceObj
	 *            key-value pairs json
	 * @return <ul>
	 *         <li>if sourceObj is null, return null</li>
	 *         <li>else parse entry by
	 *         {@link MapUtils#putMapNotEmptyKey(Map, String, String)} one by
	 *         one</li>
	 *         </ul>
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, String> parseKeyAndValueToMap(JSONObject sourceObj) {
		if (sourceObj == null) {
			return null;
		}

		Map<String, String> keyAndValueMap = new HashMap<String, String>();
		for (Iterator iter = sourceObj.keys(); iter.hasNext();) {
			String key = (String) iter.next();
			MapUtils.putMapNotEmptyKey(keyAndValueMap, key,
					getString(sourceObj, key, ""));

		}
		return keyAndValueMap;
	}

	/**
	 * parse key-value pairs to map. ignore empty key, if getValue exception,
	 * put empty value
	 * 
	 * @param source
	 *            key-value pairs json
	 * @return <ul>
	 *         <li>if source is null or source's length is 0, return empty map</li>
	 *         <li>if source {@link JSONObject#JSONObject(String)} exception,
	 *         return null</li>
	 *         <li>return {@link JSONUtils#parseKeyAndValueToMap(JSONObject)}</li>
	 *         </ul>
	 */
	public static Map<String, String> parseKeyAndValueToMap(String source) {
		if (StringUtils.isEmpty(source)) {
			return null;
		}

		try {
			JSONObject jsonObject = new JSONObject(source);
			return parseKeyAndValueToMap(jsonObject);
		} catch (JSONException e) {
			if (isPrintException) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public enum JSON_TYPE {
		/** JSONObject */
		JSON_TYPE_OBJECT,
		/** JSONArray */
		JSON_TYPE_ARRAY,
		/** 不是JSON格式的字符串 */
		JSON_TYPE_ERROR
	}

	/***
	 * 获取JSON类型 判断规则 判断第一个字母是否为{或[ 如果都不是则不是一个JSON格式的文本
	 * 
	 * @param str
	 * @return
	 */
	public static JSON_TYPE getJSONType(String str) {
		if (TextUtils.isEmpty(str)) {
			return JSON_TYPE.JSON_TYPE_ERROR;
		}
		final char[] strChar = str.substring(0, 1).toCharArray();
		final char firstChar = strChar[0];
		if (firstChar == '{') {
			return JSON_TYPE.JSON_TYPE_OBJECT;
		} else if (firstChar == '[') {
			return JSON_TYPE.JSON_TYPE_ARRAY;
		} else {
			return JSON_TYPE.JSON_TYPE_ERROR;
		}
	}
}
