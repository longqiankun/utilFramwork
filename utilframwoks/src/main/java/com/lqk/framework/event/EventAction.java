package com.lqk.framework.event;

import android.content.Intent;


/**
 * @ClassName: EventAction
 * @Description: TODO
 * @author longqiankun
 * @date 2014-9-16 上午11:59:51
 * 
 */

public class EventAction {
public String action;
public Intent intent;
public EventAction() {
	super();
}
public EventAction(String action, Intent intent) {
	super();
	this.action = action;
	this.intent = intent;
}

}
