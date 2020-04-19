import java.io.*;
public class calRoute {
    private static long startTime=0;

    private static int[] stepArr;
    private static int curStep;
    private static float[] degressArr;
    private static int degreeSize;
    private static final int MaxSize = 216000;//6hx60x60 = 36000x6 = 216000s

    private static float[] oneSecondDegreeArr;
    private static int oneSecondDegreeLen;
    private static long oneSecondDegreeStartTime;

    private static long lastStepTime;
    private static boolean lastStepUp;
    private static final int MinStepTime = 300;

    //直行区间总是>=5s
    private static int waitIndex;
    private static int goLen;//当前秒是否在直行(>=5),以及直行了多少秒了(总是大于等于5),因为5s内不算一段有效的直行
    private static boolean lastIsGo;//上一秒是否在直行区间内(不是前五秒)
    private static boolean recording;
	private static float lastStepValue;

    private static final int straightMinTime = 5;
    private static final int limit = 15; //直行偏差在均值的+-15度以内
    private static final float score = (float) 0.8; //5s中要有80%的点符合limit

    public static void main(String[] args){
        oneSecondDegreeArr = new float[100];
        degressArr = new float[MaxSize];
        stepArr = new int[MaxSize];

        readOrientFile();
        readLinearFile();
    }

    public static void initVariable() {
        lastStepTime = -MinStepTime;
        lastStepUp = false;
        lastStepValue = 10000;
        oneSecondDegreeLen = 0;
        waitIndex = straightMinTime;
        lastIsGo = false;

        curStep = 0;
        degreeSize = 0;
    }

    public static void readOrientFile() {
        String pathname = "./carDegreeData/1584255711153_0_Orientation.txt"; // 绝对路径或相对路径都可以，写入文件时演示相对路径,读取以上路径的input.txt文件
        //防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
        //不关闭文件会导致资源的泄露，读写文件都同理
        //Java7的try-with-resources可以优雅关闭文件，异常时自动关闭文件；详细解读https://stackoverflow.com/a/12665271
        initVariable();
        recording = true;
        try (FileReader reader = new FileReader(pathname);
                BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
            ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                String[] data = line.split(",");
                float[] values = new float[3];
                values[0] = Float.parseFloat(data[1]);
                values[1] = Float.parseFloat(data[2]);
                values[2] = Float.parseFloat(data[3]);

                appendCurSecondDegree(Long.parseLong(data[0]), values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        recording = false;
        updateRoute();
    }

    public static void readLinearFile() {
        String pathname = "./carDegreeData/1584255711153_0_Linear.txt"; // 绝对路径或相对路径都可以，写入文件时演示相对路径,读取以上路径的input.txt文件
        //防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
        //不关闭文件会导致资源的泄露，读写文件都同理
        //Java7的try-with-resources可以优雅关闭文件，异常时自动关闭文件；详细解读https://stackoverflow.com/a/12665271
        try (FileReader reader = new FileReader(pathname);
                BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
            ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                String[] data = line.split(",");
                float[] values = new float[3];
                values[0] = Float.parseFloat(data[1]);
                values[1] = Float.parseFloat(data[2]);
                values[2] = Float.parseFloat(data[3]);

                float g = mang(values);
                updateStep(Long.parseLong(data[0]), g);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateStep(long t, float g) {
        boolean Up = (g > lastStepValue);
        //NOTE: 顶点判定
        if(lastStepUp && (!Up)){
            //NOTE: 时间和赋值判定
            //infoText.append(" G=" + lastStepValue + "\n");
            if((t-lastStepTime)>MinStepTime && lastStepValue>10.5){
                lastStepTime = t;
                curStep += 1;
            }
        }
        lastStepUp = Up;
        lastStepValue = g;
    }

    public static void appendCurSecondDegree(long curTime, float[] oriValues) {
        // fOrient.append(oriValues);
        if(oneSecondDegreeLen==0){
            oneSecondDegreeStartTime = curTime;
        }else if(curTime - oneSecondDegreeStartTime >= 1000){
            stepArr[degreeSize] = curStep;
            degressArr[degreeSize++] = averageOneSecond();
            updateRoute();
            oneSecondDegreeStartTime = curTime;
            oneSecondDegreeLen = 0;
        }
        oneSecondDegreeArr[oneSecondDegreeLen++] = oriValues[0];
    }

    public static float transform(float standard, float needTransform){
        //转换needTransform到与standard的差距在180之内
        if(Math.abs(standard - needTransform) <= 180){
            return needTransform;
        }else if(needTransform > standard){
            return needTransform - 360;
        }else{
            return needTransform + 360;
        }
    }

    public static float diffDegree(float a, float b){
        // 返回 -180 ~ 180
        float diff = a-b;
        while(diff<-180){
            diff += 360;
        }
        while(diff>180){
            diff -= 360;
        }
        return diff;
    }

    public static float averageOneSecond(){
        float sum = 0;
        for(int i=0;i<oneSecondDegreeLen;i++){
            sum += transform(oneSecondDegreeArr[0], oneSecondDegreeArr[i]);
        }
        if(oneSecondDegreeLen == 0) return 0;
        // System.out.println("avg:" + sum/oneSecondDegreeLen);
        return sum/oneSecondDegreeLen;
    }

    /**
     * 计算end索引前面的size个点的平均值，不包括end
     *
     * @param end 结束的点，计算时不包含
     * @param size 点的个数
     * @return 返回平均值
     */
    public static float averageDegreeArr(int end, int size) {
        if(end < size){
            System.out.println("error in averageDegreeArr");
            return 0;
        }
        float sum = 0;
        for(int i=end-size;i<end;i++){
            sum += transform(degressArr[end-size], degressArr[i]);
        }
        return sum/size;
    }

    /**
     * 判断当前秒为终点的五秒的区间是否是直的
     * @return true/false
     */
    public static boolean judgeStraight() {
        float average = averageDegreeArr(waitIndex, straightMinTime);
        int yes = 0;
        for(int i=waitIndex-straightMinTime;i<waitIndex;i++){
            if(Math.abs(diffDegree(degressArr[i], average)) <= limit){
                yes += 1;
            }
        }
        return yes/straightMinTime >= score;
    }

    /**
     * 每有了1s的角度数据后就判断是否在直行区间
     */
    public static void updateRoute() {
        //如果结束了，上一次还是直行的话也要输出
        //
        //思路:
        //1. 等有了五秒数据的时候开始判断
        //2. 判断刚刚五秒是否有4个点在平均分附近,因为刚开始,所以直行记录直行了五秒钟golen
        //3. 继续看第六秒
        //   如果刚刚5s是直行,那么以该点为终点的5s判断是否是直的
        //          如果当前也直,那么len=6
        //          如果当前不直,那么len=5or6.7.8 结束并输出刚刚直行的长度,并且之后的三秒也不需要判断,因为如果算了,那么就跟刚刚的直行连接上了,那也就不算中断了
        //   如果刚刚不直,现在也不直,那就跳过
        //   如果现在5s直了,那就记录len=5
        if(waitIndex != degreeSize && recording){
            return;
        }
        //NOTE:此时waitIndex 等于 degreeSize ，也就是最后一个(当前处理的)点的索引+1
        boolean curIsGo = false;
        if(recording){
            curIsGo = judgeStraight();
        }

        if(curIsGo && lastIsGo){
            goLen += 1;
        }else if(curIsGo){
            goLen = 5;
        //当直行中断后的几秒并不需要判断,直接跳过,所以waitIndex + 4
        }else if(lastIsGo){
            float average = averageDegreeArr(waitIndex-1, goLen);
            int distance =  diffStep(waitIndex-1, goLen) * 2; //一圈为2米
            System.out.println(String.format("直行：%d ~ %dS, 距离: %d 方向:%f",waitIndex-goLen-1, waitIndex-1, distance, average));
            // infoText.append(String.format("直行: %3d ~ %3ds, 距离: %3dm 方向: %3f°\n",waitIndex-goLen-1, waitIndex-1, distance, average));
            waitIndex += 4;
        }

        lastIsGo = curIsGo;
        waitIndex += 1;
    }

    private static int diffStep(int end, int size) {
        System.out.println("stepArr:"+ degreeSize + " " + end + " " + size + " " + stepArr[degreeSize-1]);
        if(end >= degreeSize)
            end = degreeSize - 1;
        if(end < size){
            System.out.println("error in diffStep");
            size = end;
        }
        return stepArr[end] - stepArr[end-size];
    }
    private static float mang(float[] values){
        return (float)Math.sqrt(values[0]*values[0] + values[1]*values[1] + values[2]*values[2]);
    }


}
