/* Generated by Together */

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
package com.beetle.framework.persistence.access.base;

import com.beetle.framework.persistence.access.operator.SqlParameter;

import java.sql.*;
import java.util.List;

 class MannerImp implements IAccessManner {
	private List<SqlParameter> declaredParameters; // = new LinkedList();

	private String sql;

	public MannerImp(List<SqlParameter> declaredParameters, String sql) {
		this.declaredParameters = declaredParameters;
		this.sql = sql;
	}

	public PreparedStatement accessByPreStatement(Connection conn)
			throws SQLException {
		if (this.declaredParameters == null) {
			return newPsWithoutParameters(conn);
		}
		return newPSWithParameters(conn);
	}

	private PreparedStatement newPsWithoutParameters(Connection conn)
			throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql);
		return ps;
	}

	private PreparedStatement newPSWithParameters(Connection conn)
			throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql);
		for (int i = 0; i < declaredParameters.size(); i++) {
			SqlParameter sqlParam = declaredParameters.get(i);
			if (sqlParam.getValue() == null) {
				// System.out.println("-->");
				// System.out.println(i + 1);
				// System.out.println(sqlParam.getType());
				ps.setNull(i + 1, sqlParam.getType());
				// ps.setObject(i+1,null);
			} else {
				if (sqlParam.getType() == -1001) {
					ps.setObject(i + 1, sqlParam.getValue());
				} else {
					switch (sqlParam.getType()) {
					case Types.VARCHAR:
						ps.setString(i + 1, (String) sqlParam.getValue());
						break;
					case Types.TIMESTAMP:
						ps.setTimestamp(i + 1, (Timestamp) sqlParam.getValue());
						break;
					default:
						ps.setObject(i + 1, sqlParam.getValue(),
								sqlParam.getType());
						break;
					}
				}
			}
		}
		if (!declaredParameters.isEmpty()) {
			declaredParameters.clear();
		}
		return ps;
	}

	public CallableStatement accessByCallableStatement(Connection conn)
			throws SQLException {
		return conn.prepareCall(sql);
	}
}
