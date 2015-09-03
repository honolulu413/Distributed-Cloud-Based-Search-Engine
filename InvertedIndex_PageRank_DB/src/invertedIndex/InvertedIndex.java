package invertedIndex;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class InvertedIndex {
	static int numberOfNodes = 3;
	static BigInteger total = new BigInteger(
			"ffffffffffffffffffffffffffffffffffffffff", 16);
	static BigInteger unit = total.divide(BigInteger.valueOf(numberOfNodes));
	
	
	public static class InvertedMap extends
			Mapper<LongWritable, Text, Text, Text> {
		private Text keyInfo = new Text();
		private Text valueInfo = new Text();
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String myValue = value.toString();
			String word = myValue.split("\\s+", 2)[0];
			String content = myValue.split("\\s+", 2)[1];
			keyInfo.set(word);
			valueInfo.set(content);
			context.write(keyInfo, valueInfo);
		}
	}



	
	public static class InvertedReduce extends Reducer<Text, Text, Text, Text> {
		private MultipleOutputs<Text, Text> mos;
		private Text keyInfo = new Text();
		private Text valueInfo = new Text();

		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			mos = new MultipleOutputs<Text, Text>(context);
			super.setup(context);
		}

		@Override
		protected void cleanup(Context context) throws IOException,
				InterruptedException {
			mos.close();
			super.cleanup(context);
		}

		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			String s = "";
			String realKey = key.toString();
			try {
				MessageDigest crypt = MessageDigest.getInstance("SHA-1");
				crypt.reset();
				crypt.update(realKey.getBytes("UTF-8"));
				s = byteArrayToHexString(crypt.digest());
			} catch (Exception e) {
				e.printStackTrace();
			}

			BigInteger keyValue = new BigInteger(s, 16);
			int n = (keyValue.divide(unit)).intValue();
			
			StringBuilder sb = new StringBuilder();
			for (Text value: values) {
				String content = value.toString();
				sb.append(content + "||");
			}
			valueInfo.set(sb.toString());
			mos.write("output" + n, realKey, valueInfo);
		}
		
		private static String byteArrayToHexString(byte[] b) {
			String result = "";
			for (int i = 0; i < b.length; i++) {
				result += Integer.toString((b[i] & 0xff) + 0x100, 16)
						.substring(1);
			}
			return result;
		}
	}
	
	

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Job job = new Job(conf, "InvertedIndex");

		job.setJarByClass(InvertedIndex.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setMapperClass(InvertedMap.class);
		job.setReducerClass(InvertedReduce.class);
		
		job.setNumReduceTasks(3);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		for (int i = 0; i < numberOfNodes; i++) {
			MultipleOutputs.addNamedOutput(job, "output" + i,
					TextOutputFormat.class, Text.class, Text.class);
		}

		boolean ret = job.waitForCompletion(true);
		if (!ret) {
			throw new Exception("Job Failed");
		}

	}
}

