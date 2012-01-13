package com.datasalt.pangool;

import java.util.ArrayList;
import java.util.List;

import com.datasalt.pangolin.grouper.io.tuple.ITuple.InvalidFieldException;
import com.datasalt.pangool.Schema.Field;

/**
 * Builds one inmutable {@link Schema} instance.
 * 
 * @author pere
 *
 */
public class SchemaBuilder {

	private List<Field> fields = new ArrayList<Field>();

	public SchemaBuilder add(String fieldName, Class<?> type) throws InvalidFieldException {
		if(fieldAlreadyExists(fieldName)) {
			throw new InvalidFieldException("Field '" + fieldName + "' already exists");
		}

		if(type == null) {
			throw new InvalidFieldException("Type for field '" + fieldName + "' can't be null");
		}

		if(fieldName.equals(Schema.Field.SOURCE_ID_FIELD_NAME)) {
			throw new InvalidFieldException("Can't define a field with reserved name: " + Schema.Field.SOURCE_ID_FIELD_NAME);
		}
		
		innerAdd(fieldName, type);
		return this;
	}
	
	void innerAdd(String fieldName, Class<?> type) {
		fields.add(new Field(fieldName, type));
	}

	private boolean fieldAlreadyExists(String fieldName) {
		for(Field field : fields) {
			if(field.getName().equals(fieldName)) {
				return true;
			}
		}
		return false;
	}

	public Schema createSchema() {
		return new Schema(fields);
	}
}