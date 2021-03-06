package ru.emdev.security.auth.util;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.User;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;

/**
 * @author Alexey Melnikov
 */
public class ExpandoUtil {

	private static final Log _log = LogFactoryUtil.getLog(ExpandoUtil.class);


	public static long getExpandoTableId(long companyId, String className) throws PortalException, SystemException {
		ExpandoTable exandoTable = null;
		long tableId = 0l;
	
		try {
			exandoTable = ExpandoTableLocalServiceUtil.getTable(companyId,
					className, ExpandoTableConstants.DEFAULT_TABLE_NAME);
		} catch (Exception ex) { }
	
		if (exandoTable == null) {
			exandoTable = ExpandoTableLocalServiceUtil.addTable(companyId,
					className, ExpandoTableConstants.DEFAULT_TABLE_NAME);
		}
	
		tableId = exandoTable.getTableId();
		return tableId;
	}

	public static ExpandoColumn addUserExpandoColumn(long companyId, String name, int type)
			throws PortalException, SystemException {

		long tableId = ExpandoUtil.getExpandoTableId(companyId, User.class.getName());

		ExpandoColumn column = ExpandoColumnLocalServiceUtil.getColumn(tableId, name);

		if (column == null)
			column = ExpandoColumnLocalServiceUtil.addColumn(tableId, name, type);

		return column;

	}

	public static boolean isAccessByDateEnabled(long companyId, long userId) {
		boolean result = false;
		try {
			result = ExpandoValueLocalServiceUtil.getData(companyId, User.class.getName(),
					ExpandoTableConstants.DEFAULT_TABLE_NAME,
					ExpandoTableConstants.COLUMN_ACCESS_BY_DATE, userId, false);
		} catch (Exception e) {
			_log.error("Can't access to user[" + userId + "] attributes", e);
		}

		return result;
	}

	public static Date getDateFrom(long companyId, long userId) {
		Date result = null;
		try {
			result = ExpandoValueLocalServiceUtil.getData(companyId, User.class.getName(),
					ExpandoTableConstants.DEFAULT_TABLE_NAME,
					ExpandoTableConstants.COLUMN_ACCESS_DATE_FROM, userId, result);
		} catch (Exception e) {
			_log.error("Can't access to user[" + userId + "] attributes", e);
		}

		return result;
	}

	public static Date getDateTo(long companyId, long userId) {
		Date result = null;
		try {
			result = ExpandoValueLocalServiceUtil.getData(companyId, User.class.getName(),
					ExpandoTableConstants.DEFAULT_TABLE_NAME,
					ExpandoTableConstants.COLUMN_ACCESS_DATE_TO, userId, result);
		} catch (Exception e) {
			_log.error("Can't access to user[" + userId + "] attributes", e);
		}

		return result;
	}

	public static int getSessionCount(long companyId, long userId) {
		int result = 0;
		try {
			result = ExpandoValueLocalServiceUtil.getData(companyId, User.class.getName(),
					ExpandoTableConstants.DEFAULT_TABLE_NAME,
					ExpandoTableConstants.COLUMN_SESSION_COUNT, userId, result);
		} catch (Exception e) {
			_log.error("Can't access to user[" + userId + "] attributes", e);
		}

		return result;
	}

	/**
	 * @param companyId
	 * @param userId
	 * @return empty list, or list with values
	 */
	public static List<String[]> getAllowedUserIP(long companyId, long userId) {
		String[] dataArray = null;
		try {
			dataArray = ExpandoValueLocalServiceUtil.getData(companyId,
					User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME,
					ExpandoTableConstants.COLUMN_ALLOWED_IPS, userId, dataArray);

			dataArray = dataArray != null && dataArray.length > 0 ? StringUtil.split(dataArray[0],
					CharPool.NEW_LINE) : new String[0];

		} catch (Exception e) {
			_log.error("Can't access to user[" + userId + "] attributes", e);
		}

		List<String[]> result = new LinkedList<String[]>();
		for (String ip : dataArray) {
			if (StringUtils.isBlank(ip))
				continue;
			if (ip.contains("-")) {
				String[] range = ip.split("-");
				if (StringUtils.isNotBlank(range[0]) && StringUtils.isNotBlank(range[1])) {
					range[0] = range[0].trim();
					range[1] = range[1].trim();
					result.add(range);
				}
			}
			result.add(new String[] { ip.trim() });
		}
		return result;
	}
}