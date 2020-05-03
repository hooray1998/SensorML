public class ArcNode {
    /*邻接结点*/
    int adjvex;//顶点序号
    int distance;//两点之间的距离
    int direction;//该点指向邻接点的方向
    ArcNode next;//指向下一个邻接点
    public ArcNode(){
        adjvex=-1;
        distance=-1;
        direction=-1;
        next=null;
    }
}
