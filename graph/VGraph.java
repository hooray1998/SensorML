import java.util.*;
import java.io.*;
public class VGraph {
    /*邻接表数据结构*/
    VexNode list[];//表
    int edges,vexs;//边数，顶点数

    public VGraph(String pathname){
        vexs=0;
        edges=0;
        list=new VexNode[100];
        CreateGraph(pathname);
    }
    public void CreateGraph(String pathname){
        try (FileReader reader = new FileReader(pathname);
                BufferedReader br = new BufferedReader(reader)
            ) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if(data.length==3){
                    String info = data[0];
                    double lng = Double.parseDouble(data[1]);
                    double lat = Double.parseDouble(data[2]);
                    list[vexs]=new VexNode(info, lng, lat);
                    this.vexs += 1;
                }
                else if(data.length==2){
                    int a = Integer.parseInt(data[0]);
                    int b = Integer.parseInt(data[1]);
                    CreateArc(a,b);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    /**
     * 建立a与b连接,增加两个邻接点
     * 方向也要算两个相反的方向
     *
     * @param a 第一个点
     * @param b 第二个点
     */
    public void CreateArc(int a, int b) {
        double distance  = getDistance(list[a].lng, list[a].lat, list[b].lng, list[b].lat);
        double direction = getAngle(list[a].lng, list[a].lat, list[b].lng, list[b].lat);

        // a临接表添加b
        ArcNode edge_b=new ArcNode(b, distance, direction);
        edge_b.next=this.list[a].head;//从链表头部插入新的结点
        this.list[a].head=edge_b;
        // b临接表添加a
        ArcNode edge_a=new ArcNode(a, distance, (360-direction));
        edge_a.next=this.list[b].head;//从链表头部插入新的结点
        this.list[b].head=edge_a;
        this.edges++;
    }

    /*显示图的邻接表存储*/
    public void ShowGraph(){
        int i;
        for(i=0;i<this.vexs;i++){
            double diff_lng = list[i].lng - list[0].lng;
            double diff_lat = list[i].lat - list[0].lat;
            diff_lng *= 111194.926 * Math.cos(32);
            diff_lat *= 111194.926;
            System.out.print(this.list[i].info+"("+(int)diff_lng+","+(int)diff_lat+"): ");
            ArcNode edge=new ArcNode(0,0,0);
            edge=this.list[i].head;
            while(edge!=null){
                System.out.print(edge.adjvex+"["+(int)edge.distance+"m,"+(int)edge.direction+"°] ");
                edge=edge.next;
            }
            System.out.println();
        }
    }
    public double[] getXY(int index) {
        double[] diff = new double[2];
        diff[0] = list[index].lng - list[0].lng;
        diff[1] = list[index].lat - list[0].lat;
        diff[0] *= 111194.926 * Math.cos(32);
        diff[1] *= 111194.926;
        return diff;
    }

    public double[][] getXYList(int[] indexs) {

        double[][] xy = new double[indexs.length][2];
        for (int i = 0; i < indexs.length; i++) {
            xy[i] = getXY(indexs[i]);
        }
        return xy;
    }


    /*广度优先遍历邻接表*/
    public void BFSGraph(int k,int n){
        //k为第一个要访问的顶点，n为图中顶点数
        if(k<0||k>this.vexs-1){
            System.out.println("参数 k 超出范围");
            return ;
        }

        int visited[]=new int[n];//设置结点被访问标记
        for(int i=0;i<n;i++){//初始化，设置没有被访问过
            visited[i]=0;
        }

        //用队列存储图中的顶点，先进先出
        Queue <Integer>queue=new LinkedList<Integer>();
        queue.add(k);
        visited[k]=1;
        int u;     //u用于存储队顶元素
        ArcNode v=new ArcNode(0,0,0);   //v用于存储顶点u的邻接顶点
        while(!queue.isEmpty()){
            u=queue.remove();
            System.out.print(this.list[u].info+" ");
            v=this.list[u].head;
            while(v!=null){
                if(visited[v.adjvex]!=1){
                    queue.add(v.adjvex);
                    visited[v.adjvex]=1;
                }
                v=v.next;
            }
        }
    }
    private double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 通过经纬度获取距离(单位：米)
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public double getDistance(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                    + Math.cos(radLat1) * Math.cos(radLat2)
                    * Math.pow(Math.sin(b / 2), 2)));
        s = s * 6378.137;
        s = Math.round(s * 10000d) / 10000d;
        s = s*1000;
        return s;
    }

    public double getAngle(double lng1, double lat1, double lng2, double lat2) {
 
        double y = Math.sin(lng2-lng1) * Math.cos(lat2);
        double x = Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(lng2-lng1);
        double brng = Math.atan2(y, x);
 
        brng = Math.toDegrees(brng);
        if(brng < 0)
            brng = brng +360;
        return brng;
 
    }
}

