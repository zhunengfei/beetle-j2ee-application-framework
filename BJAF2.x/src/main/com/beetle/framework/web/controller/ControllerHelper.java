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
package com.beetle.framework.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.beetle.framework.log.AppLogger;
import com.beetle.framework.web.common.CommonUtil;
import com.beetle.framework.web.view.View;
import com.beetle.framework.web.view.ViewFactory;

/**
 * <p>
 * Title: FrameWork
 * </p>
 * <p>
 * Description: MVC框架的主控制器，在需要web.xml文件中配置
 * <p/>
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: 甲壳虫软件
 * <p/>
 * </p>
 * 
 * @author 余浩东
 * @version 1.0
 */

final public class ControllerHelper {

	private static AppLogger logger = AppLogger
			.getInstance(ControllerHelper.class);
	private final static String const_ftl = "ftl";
	private static final Map<String, Method> methodCache = new HashMap<String, Method>();

	public static Method getActionMethod(String ctrlname, String actionName,
			Object o, Class<?> methodParameter) throws ControllerException {
		final String key = ctrlname + actionName;
		Method method = methodCache.get(key);
		if (method == null) {
			synchronized (methodCache) {
				if (methodCache.containsKey(key)) {
					method = methodCache.get(key);
				} else {
					try {
						method = o.getClass().getMethod(actionName,
								new Class[] { methodParameter });
						methodCache.put(key, method);
						logger.debug("cache key:{}", key);
						logger.debug("cache method:{}", method);
					} catch (Exception e) {
						throw new ControllerException(e);
					}
				}
			}
		}
		return method;
	}

	private static void dealModelAndForward(View view,
			HttpServletRequest request, HttpServletResponse response,
			ServletContext app) throws IOException, ServletException {
		String viewName = view.getViewname();
		String url = ViewFactory.getViewUrlByName(app, viewName);
		if (logger.isDebugEnabled()) {
			logger.debug("-->controllerName:"
					+ request.getAttribute(CommonUtil.controllname));
			logger.debug("-->viewName:" + viewName);
			logger.debug("-->viewURL:" + url);
			logger.debug("-->viewData:{}", view.getData());
			logger.debug("-------->Report End<--------");
		}
		// 建立控制器与视图的映射关系
		ControllerFactory.mapCtrlView(request, viewName);
		String ext = CommonUtil.getExt(url);
		if (ext.equalsIgnoreCase(const_ftl)) { // freeMarker
			ViewFactory.dealWithFreeMarkerFtl(app, request, response, url,
					viewName, view.getData());
		} else { // html/jsp
			ViewFactory.transferDataForView(view.getData(), request);
			RequestDispatcher rd = request.getRequestDispatcher(url);
			if (rd == null) {
				throw new ServletException("can't senddirect:" + url);
			}
			rd.forward(request, response);
		}
	}

	public static void doService(HttpServletRequest request,
			HttpServletResponse response, ServletContext app)
			throws ControllerException {
		if (logger.isDebugEnabled()) {
			logger.debug("-------->MainController Execute Report<--------");
			logger.debug("-->servletPath:" + request.getServletPath());
			logger.debug("-->contextPath:" + request.getContextPath());
			logger.debug("-->pathInfo:" + request.getPathInfo());
			logger.debug("-->queryString:" + request.getQueryString());
			logger.debug("-->requestURI:" + request.getRequestURI());
			logger.debug("-->request params:{}", request.getParameterMap());
			logger.debug("-->session:{}", request.getSession(false));
		}
		request.setAttribute(CommonUtil.app_Context, app);
		View view = null;
		try {
			ControllerImp imp = ControllerFactory.findController(app, request);
			view = imp.dealRequest(request, response);
			if (view != null) {
				if (view.getViewname().equals(
						AbnormalViewControlerImp.abnormalViewName)) { // 流视图
					OutputStream out = response.getOutputStream();
					out.flush();
					out.close();
				} else { // 正常视图
					dealModelAndForward(view, request, response, app);
				}
			} else {
				throw new ControllerException(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"View can not be null [please make sure your controller class,the 'View perform(WebInput webInput)' method can't  raise the return null View Object case !]");
			}

		} catch (ControllerException se) {
			throw se;
		} catch (Throwable se) {
			throw new ControllerException(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, se);
		} finally {
			if (view != null) {
				view.clear();
			}
		}
	}
}
