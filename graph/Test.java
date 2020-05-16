import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
public class Test {
    public static void print(double[][] l){
        for (double[] ds : l) {
            System.out.print(ds[0] + "," + ds[1]);
            System.out.println();
        }
    }

    public static void MatchDistance(String label, double[][] a, double[][] b){
        System.out.println("=======================");
        double distance = new Dtw().getDistance(a, b);
        try{
            File writename = new File("../routes/" + label + "#" + distance); // 相对路径，如果没有则要建立一个新的output。txt文件
            writename.createNewFile(); // 创建新文件
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            for (double[] ds : b) {
                out.write(ds[0] + "," + ds[1]+"\n");
            }
            out.flush(); // 把缓存区内容压入文件
            out.close(); // 最后记得关闭文件

        } catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("=======================");
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }
    public static void main(String[] args) {
        CleanFile cf = new CleanFile();
        cf.init();
        System.out.println("start");

        System.out.println(cf.predictRoute("./data/1588508030284_predict_Orientation.txt"));
        double x = 0;
        double y = 0;
        double d, a, X, Y;
        cf.reverse();

        System.out.println("print data");

        for (int i = 0; i < cf.finalSize; i++) {
            d = cf.finalDistance[i];
            a = cf.finalDegree[i];
            System.out.println( d + "," + a);
        }
        for (int i = 0; i < cf.finalSize; i++) {
            d = cf.finalDistance[i];
            a = cf.finalDegree[i];
            X = d * Math.sin(rad(a));
            Y = d * Math.cos(rad(a));
            x += X;
            y += Y;
            System.out.println("xxx="+x+" yyy="+y);
        }


        VGraph gp=new VGraph("map.txt"); //初始化邻接表
        gp.ShowGraph();  //显示邻接表
        gp.BFSGraph(0, 18);  //广度优先遍历邻接表

        // 计算 距离和角度偏移
        double real_direct = (Math.atan(x/y) * 180 / Math.PI);
        double real_distance = Math.sqrt(x*x + y*y);

        double expect_direct = gp.getAngle(0, 17);
        double expect_distance  = gp.getDistance(0, 17);

        double revise_direct = expect_direct - real_direct;
        double revise_distance = expect_distance / real_distance;
        System.out.println(real_direct + " , " + real_distance);
        System.out.println(expect_direct + " , " + expect_distance);
        System.out.println(revise_direct + " , " + revise_distance);

        // 纠正后的坐标存入xy_real
        x = 0; y = 0;
        double[][] xy_real = new double[cf.finalSize+1][2];
        xy_real[0][0] = 0;
        xy_real[0][1] = 0;
        for (int i = 0; i < cf.finalSize; i++) {
            d = cf.finalDistance[i] * revise_distance;
            a = cf.finalDegree[i] + revise_direct;
            X = d * Math.sin(rad(a));
            Y = d * Math.cos(rad(a));
            x += X;
            y += Y;
            xy_real[i+1][0] = x;
            xy_real[i+1][1] = y;
        }

        MatchDistance("k", xy_real, xy_real);
        MatchDistance("c", xy_real, gp.getXYList(new int[]{0, 1, 2, 3, 4, 9, 13, 17}));
        MatchDistance("r", xy_real, gp.getXYList(new int[]{0, 1, 2, 3, 8, 9, 13, 17}));
        MatchDistance("b", xy_real, gp.getXYList(new int[]{0, 1, 2, 3, 8, 12, 13, 17}));
        MatchDistance("g", xy_real, gp.getXYList(new int[]{0, 1, 2, 7, 8, 9, 13, 17}));
        MatchDistance("y", xy_real, gp.getXYList(new int[]{0, 1, 6, 7, 8, 9, 13, 17}));
        MatchDistance("m", xy_real, gp.getXYList(new int[]{0, 1, 6, 11, 12, 13, 17}));
    }
}
