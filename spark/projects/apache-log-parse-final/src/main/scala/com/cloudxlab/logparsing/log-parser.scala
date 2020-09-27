package com.cloudxlab.logparsing

import org.apache.spark._
import org.apache.spark.SparkContext

case class LogRecord( host: String, timeStamp: String, url:String,httpCode:Int)

class Utils extends Serializable {
    val PATTERN = """^(\S+) (\S+) (\S+) \[([\w:/]+\s[+\-]\d{4})\] "(\S+) (\S+)(.*)" (\d{3}) (\S+)""".r

    def parseLogLine(log: String): LogRecord = {

        val res = PATTERN.findFirstMatchIn(log)
        if (res.isEmpty)
        {
            println("Rejected Log Line: " + log)
            LogRecord("Empty", "", "",  -1 )
        }
        else
        {
            val m = res.get
            LogRecord(m.group(1), m.group(4),m.group(6), m.group(8).toInt)
        }
    }

}


object EntryPoint {
    val usage = """
        Usage: EntryPoint <file_or_directory_in_hdfs>
        Eample: EntryPoint /data/spark/project/access/access.log.45.gz
    """
    
    def main(args: Array[String]) {
        
        if (args.length != 2) {
            println("Expected:2 , Provided: " + args.length)
            println(usage)
            return;
        }

        var utils = new Utils

        val conf = new SparkConf().setAppName("LogParse").setMaster("local[1]")
        val spark = org.apache.spark.sql.SparkSession
          .builder()
          .appName("LogParser")
          .config(conf)
          .getOrCreate()

        import spark.implicits._

        spark.sparkContext.setLogLevel("WARN")

        // val logFile = sc.textFile("/data/spark/project/NASA_access_log_Aug95.gz")
        /*val logFile = spark.sparkContext.textFile("file:///C:\\Users\\Abhi Studios\\Documents\\GitHub\\bigdata\\spark\\projects\\apache-log-parse-final\\testfile.txt")*/

        val logFile = spark.sparkContext.textFile(args(1))
        val accessLog = logFile.map(utils.parseLogLine)
        val accessDf = accessLog.toDF()
        accessDf.printSchema
        accessDf.createOrReplaceTempView("nasalog")
        val output = spark.sql("select * from nasalog")
        output.createOrReplaceTempView("nasa_log")
        spark.sql("cache TABLE nasa_log")

        spark.sql("select url,count(*) as req_cnt from nasa_log where upper(url) like '%HTML%' group by url order by req_cnt desc LIMIT 10").show

        spark.sql("select host,count(*) as req_cnt from nasa_log group by host order by req_cnt desc LIMIT 5").show

        spark.sql("select substr(timeStamp,1,14) as timeFrame,count(*) as req_cnt from nasa_log group by substr(timeStamp,1,14) order by req_cnt desc LIMIT 5").show

        spark.sql("select substr(timeStamp,1,14) as timeFrame,count(*) as req_cnt from nasa_log group by substr(timeStamp,1,14) order by req_cnt  LIMIT 5").show

        spark.sql("select httpCode,count(*) as req_cnt from nasa_log group by httpCode ").show

    }
}
