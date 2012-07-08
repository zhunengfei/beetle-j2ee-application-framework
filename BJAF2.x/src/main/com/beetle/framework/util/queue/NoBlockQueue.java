/*
 * BJAF - Beetle J2EE Application Framework
 * 甲壳虫J2EE企业应用开发框架
 * 版权所有2003-2015 余浩东 (www.beetlesoft.net)
 * 
 * 这是一个免费开源的软件，您必须在
 *<http://www.apache.org/licenses/LICENSE-2.0>
 *协议下合法使用、修改或重新发布。
 *
 * 感谢您使用、推广本框架，若有建议或问题，欢迎您和我联系。
 * 邮件： <yuhaodong@gmail.com/>.
 */
package com.beetle.framework.util.queue;

import java.util.Vector;

public class NoBlockQueue implements IQueue {
	private Vector<Object> vctData;

	public NoBlockQueue() {
		this.vctData = new Vector<Object>();
	}

	public NoBlockQueue(int initialCapacity) {
		this.vctData = new Vector<Object>(initialCapacity);
	}

	public boolean isEmpty() {
		return vctData.isEmpty();
	}

	public Object pop() {
		if (vctData.isEmpty()) {
			return null;
		} else {
			// Object o = vctData.elementAt(0);
			// vctData.removeElementAt(0);
			return vctData.remove(0);
		}
	}

	public void push(Object obj) {
		vctData.addElement(obj);
	}

	public void clear() {
		vctData.removeAllElements();

	}

	public int size() {
		return vctData.size();
	}
}
