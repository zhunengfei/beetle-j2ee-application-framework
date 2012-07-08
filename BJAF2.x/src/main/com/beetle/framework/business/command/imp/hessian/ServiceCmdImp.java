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
package com.beetle.framework.business.command.imp.hessian;

import com.beetle.framework.business.command.CommandImp;
import com.beetle.framework.business.command.imp.PojoCommandTarget;

public class ServiceCmdImp implements ICmdService {
	public ServiceCmdImp() {
	}

	/**
	 * perform
	 * 
	 * @param paramObj
	 *            Object
	 * @param executeFlag
	 *            int
	 * @return Object
	 * @throws HessianCmdServiceException
	 * @todo Implement this com.beetle.framework.business.rpcserver.IService
	 *       method
	 */
	public Object perform(Object paramObj, int executeFlag)
			throws HessianCmdServiceException {
		CommandImp cmd = (CommandImp) paramObj;
		if (executeFlag == ICmdService.EXECUTE_WITH_TRANSACTION) {
			return PojoCommandTarget.getInstance()
					.executeCommandWithTransation(cmd);

		} else if (executeFlag == ICmdService.EXECUTE_WITHOUT_TRANSACTION) {
			return PojoCommandTarget.getInstance().executeCommand(cmd);
		}
		throw new HessianCmdServiceException("can't match the executeFlag:"
				+ executeFlag);
	}

}
