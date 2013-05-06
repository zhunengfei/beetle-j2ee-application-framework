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
package com.beetle.framework.resource.dic.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.beetle.framework.resource.dic.DIContainer;
import com.beetle.framework.resource.dic.ReleBinder;
import com.beetle.framework.resource.dic.ReleBinder.BeanVO;
import com.beetle.framework.resource.dic.def.ServiceTransaction;

/**
 * AOP方法拦截器 实现拦截方法前后执行功能，<br>
 * 如果before/after不能满足要求，可以重载invoke方法来进行更为灵活的拦截操作。
 */
public abstract class AopInterceptor {
	public static class InnerHandler implements InvocationHandler {
		private final Class<?> targetImpFace;
		private static final Map<Method, AopInterceptor> CACHE = new ConcurrentHashMap<Method, AopInterceptor>();

		public InnerHandler(Class<?> targetImpFace) {
			super();
			this.targetImpFace = targetImpFace;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			AopInterceptor interceptor = getInterceptor(method);
			if (interceptor != null) {
				if (interceptor.interrupt()) {
					return interceptor.interruptResult(proxy, method, args);
				}
				interceptor.before(method, args);
			}
			//
			Object targetImp = DIContainer.Inner
					.getBeanFromDIBeanCache(targetImpFace.getName());
			Object rs = null;
			if (BeanVO.existInTrans(method)) {
				ServiceTransaction.Manner manner = BeanVO.getFromTrans(method);
				if (manner.equals(ServiceTransaction.Manner.REQUIRED)) {
					rs = com.beetle.framework.business.common.tst.aop.ServiceTransactionInterceptor
							.invoke(targetImp, method, args);
				} else if (manner
						.equals(ServiceTransaction.Manner.REQUIRES_NEW)) {
					//
				}
			} else {
				rs = method.invoke(targetImp, args);
			}
			//
			if (interceptor != null) {
				interceptor.after(rs, method, args);
			}
			return rs;
		}

		private AopInterceptor getInterceptor(Method method) {
			AopInterceptor interceptor = CACHE.get(method);
			if (interceptor == null) {
				synchronized (CACHE) {
					if (interceptor == null) {
						ReleBinder binder = DIContainer.Inner.getReleBinder();
						List<BeanVO> tmpList = binder.getBeanVoList();
						for (BeanVO bvo : tmpList) {
							Method m = bvo.getAopMethod();
							if (m != null && m.equals(method)) {
								interceptor = bvo.getInterceptor();
								CACHE.put(method, interceptor);
								break;
							}
						}
					}
				}
			}
			return interceptor;
		}

	}

	/**
	 * 重载此方法返回为true可终止原方法的执行
	 * 
	 * @return
	 */
	protected boolean interrupt() {
		return false;
	}

	/**
	 * 在重载interrupt方式后，可继续重载此方法返回结果。 如果不重载，则返回为null
	 * 
	 * @param proxy
	 * @param method
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	protected Object interruptResult(Object proxy, Method method, Object[] args)
			throws Throwable {
		return null;
	}

	/**
	 * 在被拦截方法执行之前，执行此方法（事件）
	 * 
	 * @param method
	 *            --被拦截方法
	 * @param args
	 *            --被拦截方法的输入参数
	 * @throws Throwable
	 */
	protected abstract void before(Method method, Object[] args)
			throws Throwable;

	/**
	 * 被拦截方法执行完毕后，执行此方法（事件）
	 * 
	 * @param returnValue
	 *            --被拦截方法执行后返回的结果
	 * @param method
	 *            --被拦截方法
	 * @param args
	 *            --被拦截方法的输入参数
	 * @throws Throwable
	 */
	protected abstract void after(Object returnValue, Method method,
			Object[] args) throws Throwable;

}
