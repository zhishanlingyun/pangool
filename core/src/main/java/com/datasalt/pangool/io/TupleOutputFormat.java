package com.datasalt.pangool.io;

import java.io.IOException;

import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.mapred.AvroOutputFormat;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.datasalt.pangool.CoGrouperException;
import com.datasalt.pangool.Schema;
import com.datasalt.pangool.io.tuple.ITuple;
import com.datasalt.pangool.io.tuple.ITuple.InvalidFieldException;

/**
 * An Avro-based output format for {@link ITuple}s
 * 
 * @author pere
 * 
 */
public class TupleOutputFormat extends FileOutputFormat<ITuple, NullWritable> {

	public final static String CONF_TUPLE_OUTPUT_SCHEMA = TupleOutputFormat.class.getName() + ".output.schema";
	public final static String FILE_PREFIX = "tuple";

	public static final String DEFLATE_CODEC = "deflate";
	public static final String SNAPPY_CODEC = "snappy";

	private static final int SYNC_SIZE = 16;
	private static final int DEFAULT_SYNC_INTERVAL = 1000 * SYNC_SIZE;

	public static class TupleRecordWriter extends RecordWriter<ITuple, NullWritable> {

		Record record;
		DataFileWriter<Record> writer;
		Schema pangoolSchema;

		public TupleRecordWriter(org.apache.avro.Schema schema, Schema pangoolSchema, DataFileWriter<Record> writer) {
			record = new Record(schema);
			this.writer = writer;
			this.pangoolSchema = pangoolSchema;
		}

		@Override
		public void close(TaskAttemptContext arg0) throws IOException, InterruptedException {
			writer.close();
		}

		@Override
		public void write(ITuple tuple, NullWritable ignore) throws IOException, InterruptedException {
			// Convert Tuple to Record
			for(int i = 0; i < pangoolSchema.getFields().size(); i++) {
				Object obj = tuple.get(i);
				if(obj instanceof byte[]) {
					obj = new Utf8((byte[])obj).toString();
				}
				record.put(pangoolSchema.getField(i).name(), obj);
			}
			writer.append(record);
		}
	}

	@Override
	public RecordWriter<ITuple, NullWritable> getRecordWriter(TaskAttemptContext context) throws IOException,
	    InterruptedException {

		Schema pangoolOutputSchema;
		try {
			pangoolOutputSchema = Schema.parse(context.getConfiguration().get(CONF_TUPLE_OUTPUT_SCHEMA));
		} catch(InvalidFieldException e) {
			throw new RuntimeException(e);
		} catch(CoGrouperException e) {
			throw new RuntimeException(e);
		}

		org.apache.avro.Schema avroSchema = AvroUtils.toAvroSchema(pangoolOutputSchema);
		DataFileWriter<Record> writer = new DataFileWriter<Record>(new ReflectDatumWriter<Record>());

		// Compression etc - use Avro codecs

		Configuration conf = context.getConfiguration();
		if(conf.getBoolean("mapred.output.compress", false)) {
			String codec = conf.get("mapred.output.compression");
			int level = conf.getInt(AvroOutputFormat.DEFLATE_LEVEL_KEY, AvroOutputFormat.DEFAULT_DEFLATE_LEVEL);
			CodecFactory factory = codec.equals(DEFLATE_CODEC) ? CodecFactory.deflateCodec(level) : CodecFactory
			    .fromString(codec);
			writer.setCodec(factory);
		}
		writer.setSyncInterval(conf.getInt(AvroOutputFormat.SYNC_INTERVAL_KEY, DEFAULT_SYNC_INTERVAL));

		Path file = getDefaultWorkFile(context, "");
		writer.create(avroSchema, file.getFileSystem(context.getConfiguration()).create(file));

		return new TupleRecordWriter(avroSchema, pangoolOutputSchema, writer);
	}
}