package com.example.springproject.web;

import com.example.springproject.domain.Repo;
import com.example.springproject.service.RepoService;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
//import jdk.internal.util.xml.impl.Input;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.lang.reflect.Array;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


@RestController
@RequestMapping("/repo")
public class RepoController {
    private ArrayList<Commit> commits1=new ArrayList<>();//NullAway
    private ArrayList<Issue> issues1=new ArrayList<>();
    private ArrayList<Developer> developers1=new ArrayList<>();
    private ArrayList<Release> releases1=new ArrayList<>();

    private static ArrayList<Commit> commits=new ArrayList<>();//qulice
    private static ArrayList<Issue> OpenIssues=new ArrayList<>();
    private static ArrayList<Issue> ClosedIssues=new ArrayList<>();
    private static ArrayList<Developer> developers=new ArrayList<>();
    private static ArrayList<Release> releases=new ArrayList<>();

    private static boolean quilce_store=false;
    /**
     * 读取json文件，返回json串
     * @param fileName
     * @return
     */
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }

            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Autowired
    private RepoService repoService;

    @GetMapping("/getInfo1")
    public Repo getInfo_1(){
        return repoService.findInfo();
    }

    @GetMapping("/getInfo2")
    public ArrayList<String> getInfo_2() throws IOException {

        ArrayList<String> arrayList = new ArrayList<>();

        String json = "";

        try {
            BufferedReader in = new BufferedReader(new FileReader("src/temp.json"));
            String str;
            while ((str = in.readLine()) != null) {
                System.out.println(str);
                json = json.concat(str);
            }
//            System.out.println(str);
        } catch (IOException e) {
            System.out.println(e);
        }

        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);

        arrayList.add(""+JsonPath.read(document, "$.repo"));
        arrayList.add(""+JsonPath.read(document, "$.developers"));
        arrayList.add(""+JsonPath.read(document, "$.most_active_developer.login"));
        arrayList.add(""+JsonPath.read(document, "$.open_issues"));
        arrayList.add(""+JsonPath.read(document, "$.close_issues"));

        return arrayList;
    }

    //开发者总数
    @GetMapping("/developersNum")
    public int getInfo3() throws Exception {
        if(!quilce_store)
            StoreDatas();
        return developers.size();
    }

    //commit 数量前几位的 developers 信息
    @GetMapping("/developersTop")
    public ArrayList<Developer> getInfo4() throws Exception {
        if(!quilce_store)
            StoreDatas();
        ArrayList<Developer> tops=new ArrayList<>();
        int k=0;
        for (Developer d:developers
             ) {

            if(k==5)
                break;
            k++;
            tops.add(d);
        }
        return tops;
    }

    //open 的 issue 数量
    @GetMapping("/issue/open")
    public int getInfo5() throws Exception {
        if(!quilce_store)
            StoreDatas();
        return OpenIssues.size();
    }

    //close 的 issue 数量
    @GetMapping("/issue/close")
    public int getInfo6() throws Exception {
        if(!quilce_store)
            StoreDatas();
        return ClosedIssues.size();
    }

    //了对 issue 解决时间的典型处理，如平均值、极值差、方差等
    @GetMapping("/issue/solveTime/avg")
    public double getInfo7() throws Exception {
        TimeAnalysis t=calculate();
        return t.avg/60/60/24;
    }

    @GetMapping("/issue/solveTime/max")
    public double getInfo7_1() throws Exception {
        TimeAnalysis t=calculate();
        return t.max/60/60/24;
    }
    @GetMapping("/issue/solveTime/min")
    public double getInfo7_2() throws Exception {
        TimeAnalysis t=calculate();
        return t.min/60/60/24;
    }

    @GetMapping("/issue/solveTime/E")
    public double getInfo7_3() throws Exception {
        TimeAnalysis t=calculate();
        return t.Extrem_value/60/60/24;
    }

    public TimeAnalysis calculate() throws Exception {
        if(!quilce_store)
            StoreDatas();
        ArrayList<Long> times=new ArrayList<>();
        for (Issue i:ClosedIssues
        ) {
            String diff=i.diff.replace("PT","");
            System.out.println("before split: "+diff);
            int hour=-1;
            int mins=-1;
            int ss=-1;
            String rest1=null;

            if(diff.contains("H")){
                String h=diff.split("H")[0];
                hour=Integer.parseInt(h);
                rest1=diff.split("H")[1];
            }
            else{
                hour=0;
                rest1=diff;
            }
            if(rest1.contains("M")){
                String min=rest1.split("M")[0];
                mins=Integer.parseInt(min);
                if(rest1.contains("S")) {
                    String s = rest1.split("M")[1];
                    ss = Integer.parseInt(s.substring(0,s.length()-1));
                }else{
                    ss=0;
                }
            }else{
                mins=0;
                if(rest1.contains("S")) {
                    String s = rest1;
                    ss = Integer.parseInt(s.substring(0,s.length()-1));
                }else{
                    ss=0;
                }
            }
//            String min=diff.split("H")[1].split("M")[0];
//            String s=diff.split("H")[1].split("M")[1].split("S")[0];
            System.out.println(hour+" hours "+mins+" minutes "+ss+" s ");
            times.add((long)(hour*60*60+mins*60+ss));
        }
        long max=Long.MIN_VALUE;
        long min=Long.MAX_VALUE;
        for (long num:times
        ) {
            if(max<num)
                max=num;
            if(min>num)
                min=num;
        }
        double mean = 0;
        for (long num : times) {
            mean += num;
        }
        mean /= times.size();

        double variance = 0;
        for (long num : times) {
            variance += (num - mean) * (num - mean);
        }
        variance /= times.size();
        TimeAnalysis timeAnalysis=new TimeAnalysis(mean,max,min,variance);

        return timeAnalysis;
//        ArrayList<String> re=new ArrayList<String>();
//        re.add((String)mean);
    }
    class TimeAnalysis{
        double avg;
        long max;
        long min;
        double Extrem_value;

        public TimeAnalysis(double avg, long max, long min, double extrem_value) {
            this.avg = avg;
            this.max = max;
            this.min = min;
            Extrem_value = extrem_value;
        }

        @Override
        public String toString() {
            return "TimeAnalysis{" +
                    "avg=" + avg +
                    ", max=" + max +
                    ", min=" + min +
                    ", Extrem_value=" + Extrem_value +
                    '}';
        }
    }



    //release的总数
    @GetMapping("/releaseNum")
    public int getInfo8() throws Exception {
        if(!quilce_store)
            StoreDatas();
        return releases.size();
    }

    // release 间的 commit 数量
    @GetMapping("/commitNum/duringRelease")
    public ArrayList<Integer> getInfo9() throws Exception {
        if(!quilce_store)
            StoreDatas();
        return null;
    }

    //  commit 的时间分布
    @GetMapping("/commitTime")
    public int getInfo10() throws Exception {
        if(!quilce_store)
            StoreDatas();
        return 0;
    }

    public void StoreDatas() throws Exception {
        quilce_store=true;
        StoreCommit();
        System.out.println("---------------");
        System.out.println(commits);
        System.out.println(commits.size());

        StoreDeveloper();
        System.out.println("---------------");
        System.out.println(developers);
        System.out.println(developers.size());

        StoreRelease();
        System.out.println("---------------");
        System.out.println(releases);
        System.out.println(releases.size());

        StoreIssue();
        System.out.println("---------------");
        System.out.println(OpenIssues);
        System.out.println(OpenIssues.size());
        System.out.println("---------------");
        System.out.println(ClosedIssues);
        System.out.println(ClosedIssues.size());
    }

    public void StoreCommit() throws Exception{
        String prefix="yegor256_qulice_";
        String path=System.getProperty("user.dir")+"\\datas\\"+prefix+"commits.json";
        FileInputStream fileInputStream=new FileInputStream(path);
        InputStreamReader reader=new InputStreamReader(fileInputStream,"UTF-8");
        BufferedReader br=new BufferedReader(reader);
        String line="";
        String time=null;
        String committer=null;
        line=br.readLine();//跳过第一行
        int count=0;
        while((line=br.readLine())!=null){//42行为一个单位
            String[] s=line.split(":");
            if(s.length>1) {
                if (s[0].contains("date")) {
                    count++;
                    if(count%2==0) {
                        if(s[0].contains("published_at")){
                            s[1]=line.substring(16,line.length()-1);
                            //System.out.println("origin: "+s[1]);
                            s[1] = s[1].trim();
                            s[1]=s[1].replace("\"","");
                            time=s[1].substring(3,s[1].length()-1);
                            Commit commit=new Commit(time);
                            commits.add(commit);
                            time=null;
                        }
                    }
                }
            }
        }
    }
    public void StoreDeveloper() throws Exception{
        String prefix="yegor256_qulice_";
        String path=System.getProperty("user.dir")+"\\datas\\"+prefix+"contributors.json";
        FileInputStream fileInputStream=new FileInputStream(path);
        InputStreamReader reader=new InputStreamReader(fileInputStream,"UTF-8");
        BufferedReader br=new BufferedReader(reader);
        String line="";
        String time=null;
        String committer=null;
        line=br.readLine();//跳过第一行
        int count=0;
        String name=null;
        String head=null;
        int con=0;
        while((line=br.readLine())!=null){//42行为一个单位
            String[] s=line.split(":");
            if(s.length>1) {
                if(s[0].contains("login")){
                    s[1]=s[1].trim();
                    s[1]=s[1].substring(0,s[1].length()-2);
                    name=s[1].replace("\"","");
                }
                if(s[0].contains("contributions")){
                    s[1]=s[1].trim();
                    con=Integer.parseInt(s[1].replace("\"",""));

                }
                if(s[0].contains("avatar_url")){
                    s[1]=s[1].trim();
                    s[1]=s[1].substring(0,s[1].length()-2);
                    head=s[1].replace("\"","");
                    Developer developer=new Developer(name,con,head);
                    name=null;
                    con=0;
                    head=null;
                    developers.add(developer);
                }
            }
        }
    }

    public void StoreIssue() throws Exception{
        String prefix="yegor256_qulice_";
        //String path="D:\\sustech\\Java2\\A3_pro_demo\\CS209-project-22fall-master\\datas\\"+prefix+"issues.json";
        String path=System.getProperty("user.dir")+"\\datas\\"+prefix+"issues_all.json";
        FileInputStream fileInputStream=new FileInputStream(path);
        InputStreamReader reader=new InputStreamReader(fileInputStream,"UTF-8");
        BufferedReader br=new BufferedReader(reader);
        String line="";
        String time=null;
        String committer=null;
        line=br.readLine();//跳过第一行
        String creat=null;
        String close=null;
        String dif=null;

        boolean closed=false;
        while((line=br.readLine())!=null){//42行为一个单位
            String[] s=line.split(":");
            if(s.length>1) {
                if(s[0].contains("created_at")){
                    s[1]=line.substring(17,line.length()-1);
                    s[1]=s[1].trim();
                    s[1]=s[1].replace("\"","");
                    creat=s[1].substring(0,s[1].length()-1);;
                }
                if(s[0].contains("closed_at")){
                    if(s[1].contains("null")){
                        closed=false;
                        Issue issue=new Issue(creat,null,null,true);
                        OpenIssues.add(issue);

                    }
                    else {
                        s[1]=line.substring(16,line.length()-1);
                        //System.out.println("origin: "+s[1]);
                        s[1] = s[1].trim();
                        s[1]=s[1].replace("\"","");
                        close=s[1].substring(0,s[1].length()-1);
                        // 定义日期格式
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        // 将日期字符串解析成 Instant 对象
//                        System.out.println(creat);
//                        System.out.println(close);
                        LocalDateTime start = LocalDateTime.parse(creat, formatter);
                        LocalDateTime end = LocalDateTime.parse(close, formatter);
                        Duration duration = Duration.between(start,end);

                        // 输出时间差
//                        System.out.println("Duration: " + duration.toString());
                        // 定义日期字符串
                        closed=true;
                        Issue issue=new Issue(creat,close,duration.toString(),false);
                        ClosedIssues.add(issue);
                    }
                }

            }
        }
    }

    public void StoreRelease() throws Exception{
        String prefix="yegor256_qulice_";
        String path=System.getProperty("user.dir")+"\\datas\\"+prefix+"releases.json";
        FileInputStream fileInputStream=new FileInputStream(path);
        InputStreamReader reader=new InputStreamReader(fileInputStream,"UTF-8");
        BufferedReader br=new BufferedReader(reader);
        String line="";
        String time=null;
        String tag=null;
        line=br.readLine();//跳过第一行
        int count=0;
        while((line=br.readLine())!=null){//42行为一个单位
            String[] s=line.split(":");
            if(s.length>1) {
                if (s[0].contains("tag_name")) {

                    s[1]=s[1].trim();
                    s[1] = s[1].replace("\"", "");
                    tag=s[1].substring(0,s[1].length()-1);

                }
                else if(s[0].contains("published_at")){
                    s[1]=line.substring(16,line.length()-1);
                    //System.out.println("origin: "+s[1]);
                    s[1] = s[1].trim();
                    s[1]=s[1].replace("\"","");
                    time=s[1].substring(3,s[1].length()-1);
                    Release release=new Release(tag,time);
                    releases.add(release);
                }
            }
        }
        Release last=null;
        for (Release r:releases
             ) {
            if(last==null){
                last=r;
                continue;
            }
            else{
                last.end_time=r.publish_time;//如果publish time=null说明是最新的发布
            }
        }
    }

    class Developer{
        String name;
        int contribution;
        String head_url;

        public Developer(String name, int contribution) {
            this.name = name;
            this.contribution = contribution;
        }
        public Developer(String name, int contribution, String head) {
            this.name = name;
            this.contribution = contribution;
            head_url=head;
        }

        @Override
        public String toString() {
            return "Developer{" +
                    "name='" + name + '\'' +
                    ", contribution=" + contribution +
                    ", head url=" + head_url +
                    '}';
        }
    }
    class Commit{
        String time;
        //String committer;//name

        public Commit(String time, String committer) {
            this.time = time;
            //this.committer = committer;
        }
        public Commit(String time) {
            this.time = time;
            //this.committer = committer;
        }

        @Override
        public String toString() {
            return "Commit{" +
                    "time='" + time + '\'' +
                    '}';
        }
    }
    class Release{
        String tag_name;
        String publish_time;
        String end_time;

        public Release(String tag_name, String publish_time) {
            this.tag_name = tag_name;
            this.publish_time = publish_time;
        }

        @Override
        public String toString() {
            return "Release{" +
                    "tag_name='" + tag_name + '\'' +
                    ", publish_time='" + publish_time + '\'' +
                    ", end_time='" + end_time + '\'' +
                    '}';
        }
    }
    class Issue{
        String start_time;
        String end_time;
        String diff;
        boolean open;


        public Issue( String start_time, String end_time, String diff, boolean open) {

            this.start_time = start_time;
            this.end_time = end_time;
            this.open = open;
            this.diff=diff;
        }

        @Override
        public String toString() {
            return "Issue{" +
                    "start_time='" + start_time + '\'' +
                    ", end_time='" + end_time + '\'' +
                    ", diff='" + diff + '\'' +
                    ", open=" + open +
                    '}';
        }
    }
}
