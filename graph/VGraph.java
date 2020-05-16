import java.util.*;
import java.io.*;
public class VGraph {
    /*邻接表数据结构*/
    VexNode list[];//表
    int edges,vexs;//边数，顶点数
    // Stack<Integer>s;
    // Stack<Double>l;
    // boolean visited_dfs[];

    public VGraph(String pathname){
        vexs=0;
        edges=0;
        // visited_dfs=new boolean[20];//设置结点被访问标记
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

    public double getDistance(int dot1, int dot2) {
        return getDistance(list[dot1].lng, list[dot1].lat, list[dot2].lng, list[dot2].lat);
    }

    public double getAngle(int dot1, int dot2) {
        return getAngle(list[dot1].lng, list[dot1].lat, list[dot2].lng, list[dot2].lat);
    }
    //得到x的邻接点为y的后一个邻接点位置,为-1说明没有找到
    public int getNextNode(int x,int y){
        int next_node=-1;
        ArcNode edge=list[x].head;
        if(null!=edge&&y==-1){
            int n=edge.adjvex;
            //元素还不在stack中
            if(!states.get(n))
                return n;
            return -1;
        }

        while(null!=edge){
            //节点未访问
            if(edge.adjvex==y){
                if(null!=edge.next){
                    next_node=edge.next.adjvex;
                    if(!states.get(next_node))
                        return next_node;
                }
                else
                    return -1;
            }
            edge=edge.next;
        }
        return -1;
    }

    //代表某节点是否在stack中,避免产生回路
    public Map<Integer,Boolean> states=new HashMap();

    //存放放入stack中的节点
    public Stack<Integer> stack=new Stack();

    //输出2个节点之间的输出路径
    public void visit(int x,int y){
        //初始化所有节点在stack中的情况
        for(int i=0;i<vexs;i++){
            states.put(i,false);
        }
        //stack top元素
        int top_node;
        //存放当前top元素已经访问过的邻接点,若不存在则置-1,此时代表访问该top元素的第一个邻接点
        int adjvex_node=-1;
        int next_node;
        stack.add(x);
        states.put(x,true);
        while(!stack.isEmpty()){
            top_node=stack.peek();
            //找到需要访问的节点
            if(top_node==y){
                //打印该路径
                printPath();
                adjvex_node=stack.pop();
                states.put(adjvex_node,false);
            }
            else{
                //访问top_node的第advex_node个邻接点
                next_node=getNextNode(top_node,adjvex_node);
                if(next_node!=-1){
                    stack.push(next_node);
                    //置当前节点访问状态为已在stack中
                    states.put(next_node,true);
                    //临接点重置
                    adjvex_node=-1;
                }
                //不存在临接点，将stack top元素退出 
                else{
                    //当前已经访问过了top_node的第adjvex_node邻接点
                    adjvex_node=stack.pop();
                    //不在stack中
                    states.put(adjvex_node,false);
                }
            }
        }
    }

    //打印stack中信息,即路径信息
    public void printPath(){
        StringBuilder sb=new StringBuilder();
        for(Integer i :stack){
            sb.append(i+"->");
        }
        sb.delete(sb.length()-2,sb.length());
        System.out.println(sb.toString());

    }
}

