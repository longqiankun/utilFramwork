package com.lqk.framework.adapter;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.lqk.framework.image.ImageLoader;

/**
 * 自定义适配器基类
 * @author longqiankun
 *2014-06-29
 */
public abstract class CustomizedBaseAdapter<T> extends BaseAdapter {
	protected Context context;
	protected List<T> appAsks;
	protected LayoutInflater lf;
	protected Resources res;
	protected ImageLoader imageLoader;
	protected int index = -1;
	protected ClipboardManager manager;
/**
	 * 创建一个新的实例 SelfBaseAdapter.
	 * @param context
	 * @param appAsks
 */
	public CustomizedBaseAdapter(Context context, List<T> appAsks) {
		super();
		this.context = context;
		this.appAsks = appAsks;
		lf = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		res = context.getResources();
		imageLoader = ImageLoader.getInstance(context);
		manager = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
	}
/**
 * 
* @Title: updateData
* @Description: 更新列表
* @param @param appAsks 列表数据
* @return void
* @throws
 */
	public void updateData(List<T> appAsks) {
		this.appAsks = appAsks;
		this.notifyDataSetChanged();
	}
/**
 * 
* @Title: updateFrame
* @Description: 更新当前选中的位置
* @param @param position 当前位置
* @return void
* @throws
 */
	public void updateFrame(int position) {
		this.index = position;
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return appAsks.size();
	}



	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return appAsks.get(position);
	}



	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
}
