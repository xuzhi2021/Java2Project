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
    public ArrayList<String> getInfo4() throws Exception {
        if(!quilce_store)
            StoreDatas();
        return null;
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
    @GetMapping("/issue/solveTime")
    public ArrayList<String> getInfo7() throws Exception {
        if(!quilce_store)
            StoreDatas();
        return null;
    }

    //release的总数
    @GetMapping("/releaseNum")
    public int getInfo8() throws Exception {
        if(!quilce_store)
            StoreDatas();
        return 0;
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
                        s[1]=s[1].trim();
                        time = s[1].replace("\"", "");

                        Commit commit = new Commit(time);
                        commits.add(commit);
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
                    Developer developer=new Developer(name,con);
                    name=null;
                    con=0;
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
                        System.out.println("origin: "+s[1]);
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

    class Developer{
        String name;
        int contribution;

        public Developer(String name, int contribution) {
            this.name = name;
            this.contribution = contribution;
        }

        @Override
        public String toString() {
            return "Developer{" +
                    "name='" + name + '\'' +
                    ", contribution=" + contribution +
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

        public Release(String tag_name, String publish_time) {
            this.tag_name = tag_name;
            this.publish_time = publish_time;
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
