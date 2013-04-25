package beetle.generate.genbean;

import java.util.ArrayList;
import java.util.HashMap;

import beetle.generate.conf.Configuration;
import beetle.generate.conf.Type;
import beetle.generate.db.DBOperate;
import beetle.generate.file.FileOperate;
import beetle.generate.util.Common;

import com.beetle.framework.AppProperties;

public class GenAll {
	public GenAll() {
	}

	public void genConfigFile() {
		GenDaoConfigFile ff = new GenDaoConfigFile();
		ff.gen(Configuration.getInstance().getValue("java.outPath"),
				AppProperties.getAppHome());
	}

	public void genVOs() {
		Configuration cfg = Configuration.getInstance();
		String packageName = cfg.getValue("java.package");
		String tabName;
		String fileName;
		String dir = cfg.getValue("java.outPath") + "valueobject\\";
		FileOperate f = new FileOperate();
		DBOperate dbOperate = new DBOperate();
		GenVOBase genVOBase = new GenVOBase();
		f.createFile(genVOBase.getSb(), dir, "VOBase.java");
		ArrayList arrayList = dbOperate.getAllTbFields();
		for (int i = 0; i < arrayList.size(); i++) {
			System.out.println(i + 1);
			HashMap tbFields = (HashMap) arrayList.get(i);
			tabName = tbFields.get("tabName").toString();
			GenVO genVO = new GenVO(packageName, tabName, tbFields);

			// fileName = Common.fisrtCharToUpCase(tabName) + ".java";
			fileName = Common.genTableClassName(tabName) + ".java";
			f.createFile(genVO.getSb(), dir, fileName);
		}
	}

	/* �������ļ���ȡ��Ϣ,������е�DAO�ӿ� */
	public void genDaos() {
		Configuration cfg = Configuration.getInstance();
		String packageName = cfg.getValue("java.package");
		String tabName;
		String fileName;
		String dir = cfg.getValue("java.outPath") + "persistence\\dao\\";
		FileOperate f = new FileOperate();
		DBOperate dbOperate = new DBOperate();

		ArrayList arrayList = dbOperate.getAllTbFields();
		for (int i = 0; i < arrayList.size(); i++) {
			System.out.println(i + 1);
			HashMap tbFields = (HashMap) arrayList.get(i);
			tabName = tbFields.get("tabName").toString();
			GenDao genDao = new GenDao(packageName, tabName, tbFields);

			fileName = "I" + Common.fisrtCharToUpCase(tabName) + "Dao.java";
			f.createFile(genDao.getSb(), dir, fileName);
			II ii = new II();
			ii.setFace(packageName + ".persistence.dao." + "I"
					+ Common.fisrtCharToUpCase(tabName) + "Dao");
			GenDaoConfigFile.IIMAP.put(tabName, ii);
		}
	}

	public void genImps2() {
		Configuration cfg = Configuration.getInstance();
		String packageName = cfg.getValue("java.package");
		String tabName;
		String fileName;
		String dir = cfg.getValue("java.outPath") + "persistence\\imp\\";
		FileOperate f = new FileOperate();
		DBOperate dbOperate = new DBOperate();
		ArrayList arrayList = dbOperate.getAllTbFields();
		for (int i = 0; i < arrayList.size(); i++) {
			System.out.println(i + 1);
			HashMap tbFields = (HashMap) arrayList.get(i);
			tabName = tbFields.get("tabName").toString();
			try {
				GenImp2 genImp = new GenImp2(packageName, tabName, tbFields);
				fileName = "Ps" + Common.fisrtCharToUpCase(tabName) + ".java";
				f.createFile(genImp.getSb(), dir, fileName);
				II ii = GenDaoConfigFile.IIMAP.get(tabName);
				if (ii != null) {
					ii.setImp(packageName + ".persistence.imp." + "Ps"
							+ Common.fisrtCharToUpCase(tabName));
				}
			} catch (Exception e) {
			}

		}
	}

	/* �������ļ���ȡ��Ϣ,������е��� */
	public static void main(String[] args) {
		GenAll genAll = new GenAll();
		Type.getInstance();
		genAll.genVOs();
		genAll.genDaos();
		// genAll.genImps();
		genAll.genImps2();
	}
}
