/**
 * Test
 */
public class Test {
    public static void print(double[][] l){
        for (double[] ds : l) {
            for (double var : ds) {
                System.out.print(var + " ");
            }
            System.out.println();
        }
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }
    public static void main(String[] args) {
        CleanFile cf = new CleanFile();
        cf.init();

        System.out.println(cf.predictRoute("./data/1588508030284_predict_Orientation.txt"));
        double x = 0;
        double y = 0;
        double d, a, X, Y;
        double temp;

        // 反转数据,从左下往右上进行
        for (int i = 0; i < cf.finalSize; i++) {
            // d = cf.finalDistance[i];
            temp = cf.finalDegree[i] - 180;
            if(temp < 0 ) temp += 360;
            if(temp >= 360 ) temp -= 360;
            System.out.println(cf.finalDegree[i] + " => " + temp);
            cf.finalDegree[i] = temp;
        }
        for (int i = 0; i < cf.finalSize/2; i++) {
            temp = cf.finalDistance[i];
            cf.finalDistance[i] = cf.finalDistance[cf.finalSize - i - 1];
            cf.finalDistance[cf.finalSize - i - 1] = temp;
            temp = cf.finalDegree[i];
            cf.finalDegree[i] = cf.finalDegree[cf.finalSize - i - 1];
            cf.finalDegree[cf.finalSize - i - 1] = temp;
        }


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
            System.out.println("X="+X+" Y="+Y);
            x += X;
            y += Y;
        }
        System.out.println("xxx="+x+" yyy="+y);


        VGraph gp=new VGraph("map.txt"); //初始化邻接表
        gp.ShowGraph();  //显示邻接表
        gp.BFSGraph(0, 18);  //广度优先遍历邻接表
        double real_direct = (Math.atan(x/y) * 180 / Math.PI);
        double real_distance = Math.sqrt(x*x + y*y);

        double expect_direct = gp.getAngle(gp.list[0].lng, gp.list[0].lat, gp.list[17].lng, gp.list[17].lat);
        double expect_distance  = gp.getDistance(gp.list[0].lng, gp.list[0].lat, gp.list[17].lng, gp.list[17].lat);

        double revise_direct = expect_direct - real_direct;
        double revise_distance = expect_distance / real_distance;
        System.out.println(real_direct + " , " + real_distance);
        System.out.println(expect_direct + " , " + expect_distance);
        System.out.println(revise_direct + " , " + revise_distance);

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


        int[] l1 = {0, 1, 2, 3, 8, 9, 13, 17};
        double[][] xy_mat1 = gp.getXYList(l1);
        int[] l2 = {0, 1, 6, 7, 8, 9, 13, 17};
        double[][] xy_mat2 = gp.getXYList(l2);
        int[] l3 = {0, 1, 6, 7, 8, 9, 13, 12};
        double[][] xy_mat3 = gp.getXYList(l3);
        int[] l4 = {0, 1, 2, 7, 8, 9, 13, 17};
        double[][] xy_mat4 = gp.getXYList(l4);
        int[] l5 = {0, 1, 2, 3, 4, 9, 13, 17};
        double[][] xy_mat5 = gp.getXYList(l5);
        int[] l6 = {0, 1, 2, 3, 8, 12, 13, 17};
        double[][] xy_mat6 = gp.getXYList(l6);
        System.out.println(xy_mat1);
        print(xy_mat1);
        print(xy_mat2);
        print(xy_real);

        Dtw dtw = new Dtw();
        double[] _x = { 3, 5, 6, 7, 7, 1 };
        double[] _y = { 3, 6, 6, 7, 8, 1, 1 };
        double[] _z = { 2, 5, 7, 7, 7, 7, 2 };
        System.out.println(dtw.getDistance(_x, _y));
        System.out.println(dtw.getDistance(_x, _z));

        System.out.println(dtw.getDistance(xy_real, xy_mat1));
        System.out.println(dtw.getDistance(xy_real, xy_mat2));
        System.out.println(dtw.getDistance(xy_real, xy_mat3));
        System.out.println(dtw.getDistance(xy_real, xy_mat4));
        System.out.println(dtw.getDistance(xy_real, xy_mat5));
        System.out.println(dtw.getDistance(xy_real, xy_mat6));
    }
}
