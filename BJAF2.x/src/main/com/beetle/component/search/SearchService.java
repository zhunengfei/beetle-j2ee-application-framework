package com.beetle.component.search;

import java.util.List;

import com.beetle.component.search.def.Record;
import com.beetle.component.search.def.StoreType;

public interface SearchService {
	/**
	 * ����������
	 * 
	 * @param storeType
	 *            ������Ĵ洢���ͣ��Ƿ��ڴ滹���ļ�
	 * @param uid
	 *            ������Ψһ��ʶ
	 * @param path
	 *            �����Ĵ洢·��
	 * @throws SearchServiceException
	 */
	void createStore(StoreType storeType, String uid, String path)
			throws SearchServiceException;

	/**
	 * ���������¼��������
	 * 
	 * @param uid
	 *            ������Ψһ��ʶ
	 * @param records
	 *            ������¼����ӳɹ���List���ݻᱻ���
	 * @throws SearchServiceException
	 */
	void addRecordsToStore(String uid, List<Record> records)
			throws SearchServiceException;

	/**
	 * ���ݲ�ѯ���ʽɾ��������ļ�¼
	 * 
	 * @param uid
	 *            ������Ψһ��ʶ
	 * @param queryExpression
	 *            ��ѯ���ʽ
	 * @throws SearchServiceException
	 */
	void deleteRecordsFromStore(String uid, String queryExpression)
			throws SearchServiceException;

	/**
	 * ɾ�������⣬ɾ����������½���
	 * 
	 * @param uid
	 *            ������Ψһ��ʶ
	 * @throws SearchServiceException
	 */
	void deleteStore(String uid) throws SearchServiceException;

	/**
	 * ����
	 * 
	 * @param uid
	 *            ������Ψһ��ʶ
	 * @param queryExpression
	 *            ��ѯ���ʽ
	 * @return �����¼�б�
	 * @throws SearchServiceException
	 */
	List<Record> search(String uid, String queryExpression)
			throws SearchServiceException;
}
