import java.util.*;
public class GraphList {

    /*创建图的邻接表存储*/
    public void CreateGraph(VGraph graph,int weight[][],int n){
        /*n为顶点总数*/
        int i,j;
        for(i=0;i<n;i++){
            for(j=n-1;j>=0;j--){
                if(weight[i][j]!=0){
                    /*邻接矩阵元素不为0，则生成一个边表结点*/
                    ArcNode edge=new ArcNode();
                    edge.adjvex=j;
                    edge.next=graph.list[i].head;//从链表头部插入新的结点
                    graph.list[i].head=edge;
                    graph.edges++;
                }
            }
        }
    }
    // public void CreateArc(VGraph graph, ) {
    // }

    /*显示图的邻接表存储*/
    public void ShowGraph(VGraph graph){
        int i;
        for(i=0;i<graph.vexs;i++){
            System.out.print(graph.list[i].data+": ");
            ArcNode edge=new ArcNode();
            edge=graph.list[i].head;
            while(edge!=null){
                System.out.print(edge.adjvex+" ");
                edge=edge.next;
            }
            System.out.println();
        }
    }

    /*获取第一个邻接点*/
    public int GetFirst(VGraph graph,int k){
        if(k<0||k>graph.vexs-1){
            System.out.println("参数k超出范围");
            return -1;
        }
        if(graph.list[k].head==null){
            return -1;
        }
        else {
            //System.out.println(graph.list[k].head.adjvex);
            return graph.list[k].head.adjvex;
        }
    }

    /*获取下一个邻接点*/
    public int GetNext(VGraph graph,int k,int t){
        if(k<0||k>graph.vexs-1||t<0||t>graph.vexs-1){
            System.out.println("参数 k or t 超出范围");
            return -1;
        }
        if(graph.list[k].head==null){
            return -1;
        }
        else {
            ArcNode edge=new ArcNode();
            edge=graph.list[k].head;
            while(edge!=null && edge.adjvex!=t){
                edge=edge.next;
            }
            //  System.out.println(edge.next.adjvex);
            return edge.next.adjvex;
        }
    }


    /*广度优先遍历邻接表*/
    public void BFSGraph(VGraph graph,int k,int n){
        //k为第一个要访问的顶点，n为图中顶点数
        if(k<0||k>graph.vexs-1){
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
        ArcNode v=new ArcNode();   //v用于存储顶点u的邻接顶点
        while(!queue.isEmpty()){
            u=queue.remove();
            System.out.print(graph.list[u].data);
            v=graph.list[u].head;
            while(v!=null){
                if(visited[v.adjvex]!=1){
                    queue.add(v.adjvex);
                    visited[v.adjvex]=1;
                }
                v=v.next;
            }
        }
    }


    public static void main(String args[]){
        char []data=new char[]{'A','B','C','D','E'};
        int n=data.length;
        int [][]weight=new int[][]{
            {0,1,0,0,1},
            {1,0,1,1,0},
            {0,1,0,0,0},
            {0,1,0,0,1},
            {1,0,0,1,0}
        };
        VexNode []list=new VexNode[n];
        for(int i=0;i<n;i++){
            /*初始化结点链表*/
            list[i]=new VexNode(data[i]);
        }

        VGraph graph=new VGraph(n,0,list); //初始化邻接表
        GraphList gp=new GraphList();
        gp.CreateGraph(graph,weight, n);  //创建邻接表
        gp.ShowGraph(graph);  //显示邻接表
        gp.GetNext(graph, 0,1);
        gp.BFSGraph(graph, 0, n);  //广度优先遍历邻接表
    }

}
