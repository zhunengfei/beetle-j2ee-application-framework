package com.beetle.framework.business.common.tst.aop;

import java.lang.reflect.Method;

import com.beetle.framework.business.command.CommandException;
import com.beetle.framework.business.command.CommandExecutor;
import com.beetle.framework.business.command.CommandImp;
import com.beetle.framework.resource.dic.aop.AopInterceptor;

public class ServiceTransactionAopInterceptor extends AopInterceptor {
	private static class Cmd extends CommandImp {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Cmd() {
			super();
		}

		//private transient MethodInvocation mi;
		private Object result;

		public Object getResult() {
			return result;
		}

	

		@Override
		public void process() throws CommandException {
			try {
				//result = mi.proceed();
			} catch (Throwable e) {
				throw new CommandException(e);
			}
		}

	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Cmd cmd = new Cmd();
		//cmd.setMi(mi);
		//TODO
		cmd = (Cmd) CommandExecutor.executeWithTransaction(cmd,
				CommandExecutor.COMMON_EXECUTE);
		return cmd.getResult();
	}

	@Override
	protected void before(String methodName, Object[] args) throws Throwable {
	}

	@Override
	protected void after(Object returnValue, String methodName, Object[] args)
			throws Throwable {

	}

}
