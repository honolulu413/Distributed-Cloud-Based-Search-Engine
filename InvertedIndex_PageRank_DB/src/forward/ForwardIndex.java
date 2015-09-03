package forward;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
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

public class ForwardIndex {
	public static class Map extends Mapper<LongWritable, Text, Text, Text> {
		private Text keyInfo = new Text();
		private Text valueInfo = new Text();
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String str = value.toString();
			if (str.startsWith("Link")) {
				String left = str.split("\t", 2)[0].substring(4);
				String outlink = str.split("\t", 2)[1];
				String url = left.split(" ")[0];
				String num = left.split(" ")[1];
				keyInfo.set("Link\t" + url + "\t" + num);
				valueInfo.set(outlink);
				context.write(keyInfo, valueInfo);
			} else if (str.startsWith("Url")) {
				String url = str.split("\t")[0].substring(3);
				String output = str.split("\t")[1];
				keyInfo.set("Url\t" + url);
				valueInfo.set(output);
				context.write(keyInfo, valueInfo);
			}

		}
	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
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
			String url = key.toString().split("\t", 2)[1];
			if (key.toString().startsWith("Link")) {
				StringBuilder sb = new StringBuilder();
				HashMap<String, Integer> hm = new HashMap<String, Integer>();
				for (Text value : values) {
					String str = value.toString();
					if (hm.containsKey(str)) {
						hm.put(str, hm.get(str) + 1);
					} else {
						hm.put(str, 1);
					}
				}

				for (String str : hm.keySet()) {
					sb.append(str + "," + hm.get(str) + "||");
				}
				keyInfo.set(url + ",1.0");
				valueInfo.set(sb.toString());
				mos.write("links", keyInfo, valueInfo);
			}

			if (key.toString().startsWith("Url")) {
				HashMap<String, CombinedOccurence> hm = new HashMap<String, CombinedOccurence>();
				for (Text value : values) {
					String[] entry = value.toString().split(",");
					String word = entry[0];
					double importance = Double.parseDouble(entry[1]);
					int position = Integer.parseInt(entry[2]);
					if (hm.containsKey(word)) {
						CombinedOccurence temp = hm.get(word);
						temp.tf += importance;
						temp.addPosition(position);
					} else {
						CombinedOccurence temp = new CombinedOccurence(url);
						temp.tf += importance;
						temp.addPosition(position);
						hm.put(word, temp);
					}
				}

				double max = 0;
				for (String word : hm.keySet()) {
					max = Math.max(max, hm.get(word).tf);
				}
				for (String word : hm.keySet()) {
					CombinedOccurence temp = hm.get(word);
					temp.tf = 0.5 + 0.5 * temp.tf / max;
					keyInfo.set(word);
					valueInfo.set(temp.toString());

					context.write(keyInfo, valueInfo);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Job job = new Job(conf, "ForwardIndex");

		job.setJarByClass(ForwardIndex.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		
		job.setNumReduceTasks(3);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		MultipleOutputs.addNamedOutput(job, "links", TextOutputFormat.class,
				Text.class, Text.class);

		boolean ret = job.waitForCompletion(true);
		if (!ret) {
			throw new Exception("Job Failed");
		}

	}
}
