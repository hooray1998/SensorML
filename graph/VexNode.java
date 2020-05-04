public class VexNode {
    /*顶点结点*/
    String info;//顶点信息
    double lat;
    double lng;
    ArcNode head;//边表头指针，指向邻接边
    public VexNode(String in, double ln, double la){
        info=in;
        lng=ln;
        lat=la;
        head=null;
    }
}

