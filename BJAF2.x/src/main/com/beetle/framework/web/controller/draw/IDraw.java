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
package com.beetle.framework.web.controller.draw;

import com.beetle.framework.web.controller.ControllerException;
import com.beetle.framework.web.controller.WebInput;

/**
 * <p>Title: BeetleSoft Framework</p>
 *
 * <p>Description: 画图控制器接口</p>
 *
 * <p>Copyright: Copyright (c) 2003</p>
 *
 * <p>Company: 甲壳虫软件</p>
 *
 * @author not attributable
 * @version 1.0
 */

public interface IDraw {
  /**
   * 执行，画图
   *
   * @param webInput 页面参数输入对象
   * @return DrawInfo－－返回画图属性信息对象
   */
  DrawInfo draw(WebInput webInput)throws ControllerException;
}
