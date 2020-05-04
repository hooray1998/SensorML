public class ArcNode {
    /*邻接结点*/
    int adjvex;//顶点序号
    double distance;//两点之间的距离
    double direction;//该点指向邻接点的方向
    ArcNode next;//指向下一个邻接点
    public ArcNode(int a, double dis, double dir){
        adjvex=a;
        distance=dis;
        direction=dir;
        next=null;
    }
}
