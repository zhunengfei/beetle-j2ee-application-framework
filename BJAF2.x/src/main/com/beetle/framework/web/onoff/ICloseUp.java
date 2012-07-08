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
package com.beetle.framework.web.onoff;

/**
 * web application全局关键（停止）接口
 *依赖于Globaldispatchservlet总指派servlet
 * @author 余浩东(hdyu@beetlesoft.net)
 * @version 1.0
 */
public interface ICloseUp {
  /**
   * closeUp
   */
  void closeUp();
}
