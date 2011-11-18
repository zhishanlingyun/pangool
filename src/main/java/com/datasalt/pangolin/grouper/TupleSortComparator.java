/**
 * Copyright [2011] [Datasalt Systems S.L.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datasalt.pangolin.grouper;

import java.io.IOException;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.io.VLongWritable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

import com.datasalt.pangolin.grouper.Schema.Field;
import com.datasalt.pangolin.grouper.SortCriteria.Sort;
/**
 * 
 * @author epalace
 *
 */
public class TupleSortComparator extends WritableComparator implements Configurable {

	private Configuration conf;
	private Schema schema;
	private SortCriteria sortCriteria;

	public TupleSortComparator() {
		super(Tuple.class);
	}

	public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
		int maxDepth = sortCriteria.getFieldNames().length;
		return compare(maxDepth, b1, s1, l1, b2, s2, l2);
	}

	public int compare(int maxFieldsCompared, byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {

		try {
			int offset1 = s1;
			int offset2 = s2;
			for(int depth = 0; depth < maxFieldsCompared; depth++) {
				Field field = schema.getFields()[depth];
				Class type = field.getType();
				Sort sort = sortCriteria.getSortByFieldName(field.getName());
				if(type == Integer.class) {
					int value1 = readInt(b1, offset1);
					int value2 = readInt(b2, offset2);
					if(value1 > value2) {
						return (sort == Sort.ASC) ? 1 : -1;
					} else if(value1 < value2) {
						return (sort == Sort.ASC) ? -1 : 1;
					}
					offset1 += Integer.SIZE / 8;
					offset2 += Integer.SIZE / 8;
				} else if(type == Long.class) {
					long value1 = readLong(b1, offset1);
					long value2 = readLong(b2, offset2);
					if(value1 > value2) {
						return (sort == Sort.ASC) ? 1 : -1;
					} else if(value1 < value2) {
						return (sort == Sort.ASC) ? -1 : 1;
					}
					offset1 += Long.SIZE / 8;
					offset2 += Long.SIZE / 8;
				} else if(type == VIntWritable.class) {
					int value1 = readVInt(b1, offset1);
					int value2 = readVInt(b2, offset2);
					if(value1 > value2) {
						return (sort == Sort.ASC) ? 1 : -1;
					} else if(value1 < value2) {
						return (sort == Sort.ASC) ? -1 : 1;
					}
					offset1 += WritableUtils.decodeVIntSize(b1[offset1]);
					offset2 += WritableUtils.decodeVIntSize(b2[offset2]);

				} else if(type == VLongWritable.class) {
					long value1 = readVLong(b1, offset1);
					long value2 = readVLong(b2, offset2);
					if(value1 > value2) {
						return (sort == Sort.ASC) ? 1 : -1;
					} else if(value1 < value2) {
						return (sort == Sort.ASC) ? -1 : 1;
					}
					offset1 += WritableUtils.decodeVIntSize(b1[offset1]);
					offset2 += WritableUtils.decodeVIntSize(b2[offset2]);
				} else if(type == Float.class) {
					float value1 = readFloat(b1, offset1);
					float value2 = readFloat(b2, offset2);
					if(value1 > value2) {
						return (sort == Sort.ASC) ? 1 : -1;
					} else if(value1 < value2) {
						return (sort == Sort.ASC) ? -1 : 1;
					}
					offset1 += Float.SIZE / 8;
					offset2 += Float.SIZE / 8;

				} else if(type == String.class) {
					int strLength1 = readVInt(b1, offset1);
					int strLength2 = readVInt(b2, offset2);
					int comparison = compareBytes(b1, offset1, strLength1, b2, offset2, strLength2);
					if(comparison != 0) {
						return (sort == Sort.ASC) ? comparison : (-comparison);
					}
					offset1 += WritableUtils.decodeVIntSize(b1[offset1]) + strLength1;
					offset2 += WritableUtils.decodeVIntSize(b2[offset2]) + strLength2;
				}
			}
			return 0; // equals
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Configuration getConf() {
		return conf;
	}

	@Override
	public void setConf(Configuration conf) {
		try {
			if(conf != null) {
				this.conf = conf;
				this.schema = Grouper.getSchema(conf);
				try {
					this.sortCriteria = SortCriteria.parse(this.conf.get(Grouper.CONF_SORT_CRITERIA));
				} catch(GrouperException e) {
					throw new RuntimeException(e);
				}
			}
		} catch(GrouperException e) {
			throw new RuntimeException(e);
		}
	}

	static {
		WritableComparator.define(Tuple.class, new TupleSortComparator());
	}

}