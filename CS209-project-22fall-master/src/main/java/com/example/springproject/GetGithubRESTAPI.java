import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetGithubRESTAPI {
    public static String name;
    public static String weight;
    public static String height;
    public static String ability;

    public static void main(String[] args) throws IOException {

        crawl("yegor256/qulice", "contributors", 1);//43+32
        crawl("yegor256/qulice", "issues", 8);//210+579
        crawl("yegor256/qulice", "releases", 2);//68
        crawl("yegor256/qulice", "commits", 19);//1808
//
//        crawl("uber/NullAway","contributors",1);//30+19
//        crawl("uber/NullAway","issues",1);//75
//        crawl("uber/NullAway","releases",1);//71 tags 0release
//        crawl("uber/NullAway","commits",6);//571
    }

    public static void crawl(String repository, String type, int page_num) {
        StringBuilder sb = new StringBuilder();
        if (type.equals("issues")) {
            for (int i = 1; i <= page_num; i++) {
                String url = "https://api.github.com/repos/" + repository + "/" + type + "?per_page=100&page=" + i + "&state=all";
                String re = run(url);
                sb.append(re + "\n");

                //printResult(re);
//            System.out.println("结果: "+re);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String s1 = repository.replaceAll("/", "_");
            String result1 = sb.toString();//reduceInfo(sb.toString());
            saveJson("D:\\sustech\\Java2\\A3_pro\\withURL", s1 + "_" + type + "_all", result1);
            System.out.println("closed issue done");
        } else {
            for (int i = 1; i <= page_num; i++) {
                String url = "https://api.github.com/repos/" + repository + "/" + type + "?per_page=100&page=" + i;
                String re = run(url);
                sb.append(re + "\n");

                //printResult(re);
//            System.out.println("结果: "+re);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String s = repository.replaceAll("/", "_");
            String result = sb.toString();//reduceInfo(sb.toString());
            saveJson("D:\\sustech\\Java2\\A3_pro\\withURL", s + "_" + type, result);
            System.out.println("yegor256/qulice's contributors success");
        }
    }

    public static String run(String theURL) {
        boolean hasNext = true;
        int pageNum = 0;
        String data = "";
        while (hasNext) {
            pageNum++;
            try {
//                URL url = new URL("https://tmtbi.tcl.com/api/bi/v1/ewdetail?startDay="+datetime+"&endDay="+datetime+"&pageSize=1000&pageNum="+pageNum);
                //URL url = new URL("https://tmtbi.tcl.com/api/bi/v1/ewdetail?startDay="+datetime+"&endDay="+datetime+"&pageSize=1000&pageNum="+pageNum);
                URL url = new URL(theURL);
                System.out.println("请求地址：" + url);
                //开发访问此连接
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                //设置连接时间为5秒
                urlConn.setConnectTimeout(5 * 10000);
                //设置读取时间为5秒
                urlConn.setReadTimeout(5 * 10000);
                // 设置是否向httpUrlConnection输出，因为这个是get请求，参数要放在
                // http正文内，因此需要设为true, 默认情况下是false;
                urlConn.setDoOutput(true);
                // 设置是否从httpUrlConnection读入，默认情况下是true;
                urlConn.setDoInput(true);
                // Get 请求不能使用缓存
                urlConn.setUseCaches(false);
                // 设定传送的内容类型是可序列化的java对象,setRequestProperty的信息都是设置在head头里面
                // (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
                urlConn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.215 Safari/535.1");
                urlConn.setRequestProperty("token", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrdGJpIiwicm9sZSI6IlJPTEVfVVNFUiIsImlzcyI6InRjbGVyIiwiZXhwIjoxNTk5ODA3Mzc5LCJpYXQiOjE1OTkyMDI1Nzl9.IciVkGcsYfD9hDacV4-lKkGFft-Z-LnEkmYtcDlPjYeN2styo3IA6dbE0JP08bmy8uS8sy3TL_65_fTbEoilow");
                urlConn.setRequestProperty("Content-type", "application/x-java-serialized-object");
                int code = urlConn.getResponseCode();//获得相应码
                System.out.println("请求响应码:" + code);
                // 设置所有的http连接是否自动处理重定向；设置成true，系统自动处理重定向
                urlConn.setInstanceFollowRedirects(true);
                // 存储返回的字符串

                //得到数据流（输入流）
                InputStream is = urlConn.getInputStream();
                byte[] buffer = new byte[1024];
                int length = 0;

                while ((length = is.read(buffer)) != -1) {
                    String str = new String(buffer, 0, length);

                    data += str;
                }
                System.out.println("请求页面：" + pageNum);

                return data;

            } catch (Exception e) {
                e.printStackTrace();
                hasNext = false;
            }
        }
        //System.out.println(data);
        return data;
    }
    ///////////////////////////////////////////////////////////

    public static String reduceInfo(String data) {
        String[] datas = data.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String s : datas
        ) {
            String[] strs = s.split(":");
            if (strs[0].contains("url"))
                continue;
            sb.append(s + "\n");
        }
        return sb.toString();
    }

    public static void saveJson(String filePath, String fileName, String content) {
        BufferedWriter writer = null;
        File file = new File(filePath + "\\" + fileName + ".json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"));
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("文件写入成功！");
    }


    ///////////////////////////////////////////////////////////
    public static boolean createNewFile(String filePath) {
        boolean isSuccess = true;
        // 如有则将”\\”转为”/”,没有则不产生任何变化
        String filePathTurn = filePath.replaceAll("\\\\", "/");
        // 先过滤掉文件名
        int index = filePathTurn.lastIndexOf("/");
        String dir = filePathTurn.substring(0, index);
        // 再创建文件夹
        File fileDir = new File(dir);
        isSuccess = fileDir.mkdirs();
        // 创建文件
        File file = new File(filePathTurn);
        try {
            isSuccess = file.createNewFile();
        } catch (IOException e) {
            isSuccess = false;
            e.printStackTrace();
        }
        return isSuccess;
    }


}
