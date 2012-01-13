package com.datasalt.pangolin.pangool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.io.VLongWritable;

/**
 * Encapsulates one Pangool schame composed of {@link Field} instances.
 * 
 * @author pere
 *
 */
public class Schema {

	public static class PrimitiveTypes {

		public final static String INT = "int";
		public final static String VINT = "vint";
		public final static String LONG = "long";
		public final static String VLONG = "vlong";
		public final static String FLOAT = "float";
		public final static String DOUBLE = "double";
		public final static String STRING = "string";
		public final static String BOOLEAN = "boolean";
	}

	private static final BidiMap strClassMap = new DualHashBidiMap();

	static {
		strClassMap.put(PrimitiveTypes.INT, Integer.class);
		strClassMap.put(PrimitiveTypes.VINT, VIntWritable.class);
		strClassMap.put(PrimitiveTypes.LONG, Long.class);
		strClassMap.put(PrimitiveTypes.VLONG, VLongWritable.class);
		strClassMap.put(PrimitiveTypes.FLOAT, Float.class);
		strClassMap.put(PrimitiveTypes.DOUBLE, Double.class);
		strClassMap.put(PrimitiveTypes.STRING, String.class);
		strClassMap.put(PrimitiveTypes.BOOLEAN, Boolean.class);
	}

	public static class Field {
		
		public final static String SOURCE_ID_FIELD = "#source#";
		public final static Field SOURCE_ID = new Field(SOURCE_ID_FIELD, VIntWritable.class);
		
		private String name;
		private Class<?> type;

		public Field() {
		}

		public Field(String name, Class<?> clazz) {
			this.name = name;
			this.type = clazz;
		}

		public Class<?> getType() {
			return type;
		}

		public String getName() {
			return name;
		}
	}

	private List<Field> fields;

	public static Class<?> strToClass(String str) throws ClassNotFoundException {
		Class<?> clazz = (Class<?>) strClassMap.get(str);
		if(clazz == null) {
			clazz = Class.forName(str);
		}
		return clazz;
	}

	public static String classToStr(Class<?> clazz) {
		String clazzStr = (String) strClassMap.inverseBidiMap().get(clazz);
		if(clazzStr == null) {
			return clazz.getName().toString();
		} else {
			return clazzStr;
		}
	}

	private Map<String, Integer> indexByFieldName = new HashMap<String, Integer>();

	public Schema(List<Field> fields) {
		this.fields = Collections.unmodifiableList(fields);
		int index = 0;
		for(Field field : fields) {
			this.indexByFieldName.put(field.getName(), index);
			index++;
		}
	}

	public List<Field> getFields() {
		return fields;
	}

	public Field getField(String fieldName) {
		int index = indexByFieldName(fieldName);
		return fields.get(index);
	}

	public Field getField(int i) {
		return fields.get(i);
	}

	public String serialize() {
		StringBuilder b = new StringBuilder();
		String fieldName = fields.get(0).name;
		Class<?> fieldType = fields.get(0).type;
		b.append(fieldName).append(":").append(classToStr(fieldType));
		for(int i = 1; i < fields.size(); i++) {
			fieldName = fields.get(i).name;
			fieldType = fields.get(i).type;
			String clazzStr = classToStr(fieldType);
			if(clazzStr == null) {
				clazzStr = fieldType.getName();
			}
			b.append(",").append(fieldName).append(":").append(clazzStr);
		}
		return b.toString();
	}

	public boolean containsFieldName(String fieldName) {
		return indexByFieldName.containsKey(fieldName);
	}

	public int indexByFieldName(String name) {
		return indexByFieldName.get(name);
	}

	@Override
	public String toString() {
		return serialize();
	}

	public static Schema parse(String serialized) throws CoGrouperException {
		try {
			if(serialized == null || serialized.isEmpty()) {
				return null;
			}
			String[] fieldsStr = serialized.split(",");
			List<Field> fields = new ArrayList<Field>(fieldsStr.length);
			for(String field : fieldsStr) {
				String[] nameType = field.split(":");
				if(nameType.length != 2) {
					throw new CoGrouperException("Incorrect fields description " + serialized);
				}
				String name = nameType[0].trim();
				String type = nameType[1].trim();
				fields.add(new Field(name, strToClass(type)));
			}
			return new Schema(fields);
		} catch(ClassNotFoundException e) {
			throw new CoGrouperException(e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Schema) {
			return toString().equals(o.toString());
		}
		return false;
	}
}