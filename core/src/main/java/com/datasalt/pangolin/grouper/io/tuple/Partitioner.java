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
package com.datasalt.pangolin.grouper.io.tuple;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;

import com.datasalt.pangolin.grouper.io.tuple.ITuple.InvalidFieldException;

/**
 * 
 * @author eric
 *
 */
public class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<ITuple,NullWritable> implements Configurable{

	private static final String CONF_PARTITIONER_FIELDS ="datasalt.grouper.partitioner_fields";
	
	private Configuration conf;
	private String[] groupFields;
	
	@Override
	public int getPartition(ITuple key, NullWritable value, int numPartitions) {
		return key.partialHashCode(groupFields) % numPartitions;
		
	}

	@Override
	public Configuration getConf() {
		return conf;
	}

	@Override
	public void setConf(Configuration conf) {
		if (conf != null){
			this.conf = conf;
			String fieldsGroupStr = conf.get(CONF_PARTITIONER_FIELDS);
			groupFields = fieldsGroupStr.split(",");
		}
	}
	
	public static void setPartitionerFields(Configuration conf,String[] fields){
		conf.setStrings(CONF_PARTITIONER_FIELDS, fields);
	}
	
	public static String[] getPartitionerFields(Configuration conf){
		return conf.getStrings(CONF_PARTITIONER_FIELDS);
	}
	
	
}