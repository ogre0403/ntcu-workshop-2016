package pagerank;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class PageRankJob extends Configured implements Tool {

    private static NumberFormat nf = new DecimalFormat("00");

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new Configuration(), new PageRankJob(), args));
    }

    @Override
    public int run(String[] args) throws Exception {


        boolean isCompleted = false;
        String lastResultPath = null;

        //Run the second MapReduce Job, calculating new pageranks from existing values
        //Run this job several times, with each iteration the pagerank value will become more accurate

        String inPath = "data/pagerank.txt";
        for (int runs = 0; runs < 8; runs++) {

            lastResultPath = "output/HadoopPageRank/ranking/iter" + nf.format(runs + 1);

            isCompleted = runRankCalculator(inPath, lastResultPath);
            inPath = "output/HadoopPageRank/ranking/iter" + nf.format(runs + 1);

            if (!isCompleted) return 1;
        }


        if (!isCompleted) return 1;
        return 0;
    }


    //Calculation MapReduce Job 2
    private boolean runRankCalculator(String inputPath, String outputPath) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();

        Job rankCalculator = Job.getInstance(conf, "rankCalculator");
        rankCalculator.setJarByClass(PageRankJob.class);
        rankCalculator.setNumReduceTasks(1);
        // Input -> Mapper -> Map
        rankCalculator.setOutputKeyClass(Text.class);
        rankCalculator.setOutputValueClass(Text.class);
        rankCalculator.setMapperClass(RankCalculateMapper.class);

        // Map -> Reducer -> Output
        FileInputFormat.setInputPaths(rankCalculator, new Path(inputPath));
        FileOutputFormat.setOutputPath(rankCalculator, new Path(outputPath));
        rankCalculator.setReducerClass(RankCalculateReducer.class);

        return rankCalculator.waitForCompletion(true);
    }


}