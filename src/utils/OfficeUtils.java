package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * office 工具类，操作文档、表格等
 */
public class OfficeUtils {

	public static void saveCVS(List<?> datas, File file) {
		if (datas == null || datas.size() <= 0) {
			return;
		}

		try {
			StringBuilder sbHeader = new StringBuilder();
			StringBuilder sbContent = new StringBuilder();

			for (int i = 0; i < datas.size(); i++) {
				Object data = datas.get(i);
				Class<?> clazz = data.getClass();
				Field[] fields = clazz.getDeclaredFields();

				for (Field field : fields) {
					field.setAccessible(true);
					// header
					if (i == 0) {
						String name = field.getName();
						if (sbHeader.indexOf(name) == -1) {
							sbHeader.append(name.replace(",", "，") + ",");
						}
					}

					// content
					Object value = field.get(data);
					// 替换英文逗号成中文逗号，防止csv解析时影响
					String valueStr = value == null ? "" : value.toString()
							.replace(",", "，");
					sbContent.append(valueStr + ",");
				}

				sbContent = sbContent.replace(sbContent.length() - 1,
						sbContent.length(), "\n");

			}

			String cvsStr = sbHeader.substring(0, sbHeader.length() - 1) + "\n"
					+ sbContent.toString();

			if (!file.exists()) {
				file.createNewFile();
			}
			FileUtils.writeString2File(cvsStr.trim(), file, "UTF-8");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static <T> List<T> readDatasFromCSV(File file, Class<T> clazz)
			throws Exception {
		List<T> datas = new ArrayList<T>();

		FileReader fr;
		fr = new FileReader(file);
		BufferedReader bufr = new BufferedReader(fr);

		// header
		String header;
		String[] fieldNames = null;
		if ((header = bufr.readLine()) != null) {
			fieldNames = header.split(",");
		}

		if (fieldNames == null || fieldNames.length == 0) {
			bufr.close();
			fr.close();
			return datas;
		}

		// content
		String line;
		while ((line = bufr.readLine()) != null) {
			// 防止末尾,无法split的处理
			String[] fieldValues = (line+"a").split(",");
			String last = fieldValues[fieldValues.length - 1];
			last = last.substring(0, last.length() - 1);
			fieldValues[fieldValues.length - 1] = last;
			
			if(fieldValues.length != fieldNames.length) {
				System.out.println(line + " 的value和header " + header + " 不一致");
				continue;
			}

			T data = clazz.newInstance();
			for (int i = 0; i < fieldNames.length; i++) {
				try {
					Field field = clazz.getDeclaredField(fieldNames[i]);
					field.setAccessible(true);
					if (field != null) {
						field.set(data, fieldValues[i]);
					}
				} catch (Exception e) {
					System.out.println(line + " 中无变量 " + fieldNames[i]);
				}
			}
			datas.add(data);
		}
		bufr.close();
		fr.close();

		return datas;
	}

}
