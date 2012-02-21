package com.datasalt.pangool.processor;

import java.io.Serializable;

import org.apache.hadoop.mapreduce.Mapper;

/**
 * Class to be extended by handlers that interact with {@link Processor} API.
 *
 */
public abstract class ProcessorHandler<I1, I2, O1, O2> extends Mapper<I1, I2, O1, O2> implements Serializable {

	protected void map(I1 key, I2 value, org.apache.hadoop.mapreduce.Mapper<I1,I2,O1,O2>.Context context) throws java.io.IOException ,InterruptedException {
		
	};
	
	protected void setup(org.apache.hadoop.mapreduce.Mapper<I1,I2,O1,O2>.Context context) throws java.io.IOException ,InterruptedException {
		
	};
	
	protected void cleanup(org.apache.hadoop.mapreduce.Mapper<I1,I2,O1,O2>.Context context) throws java.io.IOException ,InterruptedException {
		
	};
}
