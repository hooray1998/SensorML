public class Test {

    public static void main(String[] args) {
        LocationUtils lu = new LocationUtils();
// 118.802397,31.943844,118.811902,31.944318
// 118.807375,31.944063


        System.out.println("dis:"+lu.getAngle1( 118.815958,31.952821,118.81613,31.9445)); //上下 180 南北
        System.out.println("dis:"+lu.getDistance( 118.815958,31.952821,118.81613,31.9445)); //上下 180
        System.out.println("dis:"+lu.getAngle1(118.811688,31.952784,118.815958,31.952821)); //左右 90
        System.out.println("dis:"+lu.getDistance(118.811688,31.952784,118.815958,31.952821)); //左右 90

    }

}
