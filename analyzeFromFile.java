import java.io.*;
import java.text.SimpleDateFormat;
//太容易出现断点了
//如果连续两个点不满足条件就会断掉
//重新再生成一段
//可以在此基础上继续优化,不修改当前的算法
//
//TODO:
//stop的可以直接连接
//  多远的距离连接还需要看这两个区间的大小
//      两个区间越大,连接允许的距离越大,设置一个阈值
//      比如100s允许向两边各延迟5s,然后看彼此的连接度
//直行的话需要看方向是否一致,一致的话再连接两个区间到一起
//

/*
 * 首先读取方向文件的数据,把每秒的平均方向存储起来
 * 然后读取加速度数据,
 */
public class analyzeFromFile {
    private static long startTime;

    // 先读取方向传感器,更新到
    private static float[] oneSecondDegreeArr;
    private static int oneSecondDegreeLen;
    private static long oneSecondDegreeEndTime;

    private static float[] degreeArr;
    private static int degreeArrSize;

    // 再读取加速度数据,得到每秒累计的步数,以及每秒最大的加速度值,用来判断是否停止了
    private static double[] topArr;
    private static double topMaxValue; //每秒最大的g值
    private static int[] stepArr;
    private static int stepArrSize;
    private static int curStep;
    private static float lastStepValue;
    private static long lastStepTime; // 上一次记录步数的时间,用来判断两步之间的时间间隔要大于0.3s
    private static boolean lastStepUp;

    // 用来判断停止的时间区间
    private static int waitStopIndex;
    private static boolean lastIsStop;//上一秒是否在直行区间内(不是前五秒)
    private static int stopLen;//当前秒是否在直行(>=5),以及直行了多少秒了(总是大于等于5),因为5s内不算一段有效的直行
    private static int stopCount;
    private static int[] stopArr;
    private static int stopArrSize;

    //直行区间总是>=5s
    private static int waitIndex; // 监听的时刻,如果当前秒不监听,则跳过
    private static boolean lastIsGo;//上一秒是否在直行区间内(不是前五秒)
    private static int goLen;//当前秒是否在直行(>=5),以及直行了多少秒了(总是大于等于5)
    private static int goCount;
    private static int goDistance;

    // 方向判断和步数判断的一些常量
    private static final int MinStopTime       = 3    ; //最短的停止有效时间,单位秒
    private static final double stopScore        = 0.6  ; //MinStopTime 秒的时间内要有超过 3x0.6s的秒数停止=>这三秒算作有效的停止区间
    private static final int MinStepTime     = 300  ; //每一圈之间的最短间隔,单位毫秒ms
    private static final double MinStepValue = 10.5 ; //最小的峰值
    private static final int MinGoTime       = 5    ; //最短的直行有效时间,单位秒s
    private static final int limit           = 25   ; //直行偏差在均值的+-15度以内
    private static final double score        = 0.8  ; //5s中要有80%的点符合limit
    private static final int MaxSize = 216000;//6hx60x60 = 36000x6 = 216000s  数组的最大长度

    public static void main(String[] args){
        oneSecondDegreeArr = new float[100];
        degreeArr = new float[MaxSize];
        stepArr = new int[MaxSize];
        topArr = new double[MaxSize];
        stopArr = new int[MaxSize];

        predictRoute("./niceData/1588506829888_predict_Orientation.txt");
    }

    public static void initVariable() {
        topMaxValue = 0;
        lastStepTime = -MinStepTime;
        lastStepUp = false;
        lastStepValue = 10000;
        oneSecondDegreeLen = 0;
        waitIndex = MinGoTime;
        lastIsGo = false;

        waitStopIndex = MinStopTime;
        lastIsStop = false;
        stopLen = 0;
        stopCount = 0;

        curStep = 0;
        degreeArrSize = 0;
        stepArrSize = 0;
        goLen = 0;
        goCount = 0;
        goDistance = 0;

        stopArrSize = 0;
    }

    public static void predictRoute(String pathname){
        System.out.println("Start:" + pathname + "    ==========");
        initVariable();
        readOrientFile(pathname);
        readLinearFile(pathname);
        //让数据对齐,两种数组的大小一致
        if(stepArrSize > degreeArrSize) stepArrSize = degreeArrSize;
        else degreeArrSize = stepArrSize;//防止出现错误

        //统计停顿的时间
        // for(int i = 0;i < degreeArrSize; i++){
        //     cleanData(i);
        // }
        // unionStopArr();

        // updateArray();

        // 去除前
        for(int i = 0;i < degreeArrSize; i++){
            updateRoute(i);
        }
        // 去除后

        System.out.println(String.format("%s-%s(%.1f分钟)" , new SimpleDateFormat("[MM-dd]HH:mm:ss").format(startTime), new SimpleDateFormat("HH:mm:ss").format(oneSecondDegreeEndTime),(float)(oneSecondDegreeEndTime-startTime)/60000));
        System.out.println(String.format("Go%d %d 米", goCount, goDistance));
        System.out.println("End: =====================================");

    }

    public static void readOrientFile(String pathname) {
        pathname = pathname.replace("Linear.txt", "Orientation.txt");
        try (FileReader reader = new FileReader(pathname);
                BufferedReader br = new BufferedReader(reader)
            ) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                float[] values = new float[3];
                values[0] = Float.parseFloat(data[1]);
                values[1] = Float.parseFloat(data[2]);
                values[2] = Float.parseFloat(data[3]);
                appendCurSecondDegree(Long.parseLong(data[0]), values);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void readLinearFile(String pathname) {
        pathname = pathname.replace("Orientation.txt", "Linear.txt");
        try (FileReader reader = new FileReader(pathname);
                BufferedReader br = new BufferedReader(reader)
            ) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                float[] values = new float[3];
                values[0] = Float.parseFloat(data[1]);
                values[1] = Float.parseFloat(data[2]);
                values[2] = Float.parseFloat(data[3]);

                float g = mang(values);
                updateStep(Long.parseLong(data[0]), g);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void updateStep(long t, float g) {
        boolean Up = (g > lastStepValue);
        // 初始化当前秒的结束时间
        if(stepArrSize==0) oneSecondDegreeEndTime = startTime + 1000;
        //NOTE: 极值点判定
        if(lastStepUp && (!Up)){
            //NOTE: 时间和赋值判定
            if((t-lastStepTime)>MinStepTime && lastStepValue>MinStepValue){
                lastStepTime = t;
                curStep += 1;
            }
        }

        // 当前这秒时间结束,将上一秒的数据更新
        if(t >= oneSecondDegreeEndTime){
            // 追加top数组和step数组, top用来判断停止的时间段
            topArr[stepArrSize] = topMaxValue;
            stepArr[stepArrSize++] = curStep;
            oneSecondDegreeEndTime += 1000;
            topMaxValue = 0;
        }

        // 更新当前秒的最大值
        if(g>topMaxValue) topMaxValue = g;
        lastStepUp = Up;
        lastStepValue = g;
    }


    public static void appendCurSecondDegree(long curTime, float[] oriValues) {
        if(degreeArrSize==0 && oneSecondDegreeLen==0){
            startTime = curTime;
            oneSecondDegreeEndTime = curTime + 1000;
        }else if(curTime >= oneSecondDegreeEndTime){
            degreeArr[degreeArrSize++] = averageOneSecond();
            oneSecondDegreeEndTime += 1000;
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
            System.out.println("error in averageDegreeArr " + end + "," + size);
            return 0;
        }
        float sum = 0;
        for(int i=end-size;i<end;i++){
            sum += transform(degreeArr[end-size], degreeArr[i]);
        }
        return sum/size;
    }

    public static boolean judgeStop() {
        int yes = 0;
        for(int i=waitStopIndex-MinStopTime;i<waitStopIndex;i++){
            if(topArr[i] < MinStepValue){
                yes += 1;
            }
        }
        return yes/MinStopTime >= stopScore;
    }

    /**
     * 判断当前秒为终点的五秒的区间是否是直的
     * @return true/false
     */
    public static boolean judgeStraight() {
        float average = averageDegreeArr(waitIndex, MinGoTime);
        int yes = 0;
        for(int i=waitIndex-MinGoTime;i<waitIndex;i++){
            if(Math.abs(diffDegree(degreeArr[i], average)) <= limit){
                yes += 1;
            }
        }
        return yes/MinGoTime >= score;
    }

    //TODO: 使用独立的变量,防止下面的步骤乱掉
    public static void cleanData(int size) {
        if(waitStopIndex != size){
            return;
        }
        //NOTE:此时waitStopIndex 等于 size ，也就是最后一个(当前处理的)点的索引+1
        boolean curIsStop = false;
        curIsStop = judgeStop();

        if(curIsStop && lastIsStop){
            stopLen += 1;
        }else if(curIsStop){
            stopLen = MinStopTime;
        //当直行中断后的几秒并不需要判断,直接跳过,所以waitStopIndex + 4
        }else if(lastIsStop){
            // System.out.println(String.format("%3d~%3ds | %3d",waitStopIndex-stopLen-1, waitStopIndex-1, stopLen));
            stopArr[stopArrSize] = waitStopIndex-stopLen-1;
            stopArr[stopArrSize+1] = waitStopIndex-1;
            stopArrSize += 2;
            stopCount += 1;
            waitStopIndex += MinStopTime-1;
        }

        lastIsStop = curIsStop;
        waitStopIndex += 1;
    }

    /**
     * 每有了1s的角度数据后就判断是否在直行区间
     */
    public static void updateRoute(int size) {
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
        if(waitIndex != size){
            return;
        }
        //NOTE:此时waitIndex 等于 size ，也就是最后一个(当前处理的)点的索引+1
        boolean curIsGo = false;
        curIsGo = judgeStraight();

        if(curIsGo && lastIsGo){
            goLen += 1;
        }else if(curIsGo){
            goLen = MinGoTime;
        //当直行中断后的几秒并不需要判断,直接跳过,所以waitIndex + 4
        }else if(lastIsGo){
            float average = averageDegreeArr(waitIndex-1, goLen);
            int distance =  diffStep(waitIndex-1, goLen) * 2; //一圈为2米
            System.out.println(String.format("%3d~%3ds | %3dm | %5.1f° | %3d | %2.1f m/s",waitIndex-goLen-1, waitIndex-1, distance, average, goLen, (float)distance/goLen));
            goCount += 1;
            goDistance += distance;
            waitIndex += MinGoTime-1;
        }

        lastIsGo = curIsGo;
        waitIndex += 1;
    }

    private static int diffStep(int end, int size) {
        // System.out.println("stepArr:"+ degreeArrSize + " " + end + " " + size + " " + stepArr[degreeArrSize-1]);
        if(end >= degreeArrSize)
            end = degreeArrSize - 1;
        if(end < size){
            System.out.println("error in diffStep " + end + "," + size);
            size = end;
        }
        return stepArr[end] - stepArr[end-size];
    }
    private static float mang(float[] values){
        return (float)Math.sqrt(values[0]*values[0] + values[1]*values[1] + values[2]*values[2]);
    }

    /**
     * 合并较相近的停顿片段
     * 比如1-20s是停顿,22-50s也是停顿,会通过向两边增长20%的判断相连的方式将他们合并
     * stopArr刚开始存储的是clean算法得出的停顿区间
     * 合并之后的区间也放到了stopArr中了
     */
    private static void unionStopArr(){
        int start = 0;
        int end = 0;
        double len = 0;
        double last = -100; //上一段区间延伸之后达到的距离

        int newStopSize = -2;
        for (int i = 0; i < stopArrSize; i+=2) {
            start = stopArr[i];
            end = stopArr[i+1];
            len = (end - start);
            if ((start - len*0.2) <= last) {
                stopArr[i] = stopArr[i-2];
                len = stopArr[i+1] - stopArr[i];
                stopArr[newStopSize + 1] = end;
            }
            else{
                newStopSize += 2;
                stopArr[newStopSize] = start;
                stopArr[newStopSize + 1] = end;
            }
            last = stopArr[i+1] + len * 0.2;
        }
        stopArrSize = newStopSize + 2;

        for (int i = 0; i < stopArrSize; i+=2) {
            start = stopArr[i];
            end = stopArr[i+1];
            System.out.println(String.format("%d~%ds | %d", start,end , end - start));
        }
    }

    private static void updateArray(){
        for (int i = 0; i < stepArrSize; i++) {
            // System.out.println("pre"+stepArr[i]);
        }
        // System.out.println("prelen"+stepArrSize);
        // step数组删除的方法
        for (int i = stopArrSize - 2; i >= 0; i-=2) {

            int start = stopArr[i];
            int end = stopArr[i+1] + 1;
            int diff = end - start;

            int stepDiff = stepArr[end] - stepArr[start];
            stepArrSize -= diff;
            // 删除step
            for (int j = start; j < stepArrSize; j++) {
                stepArr[j] = stepArr[j+diff] - stepDiff;
            }

            //删除degree
            degreeArrSize -= diff;
            for (int j = start; j < degreeArrSize; j++) {
                degreeArr[j] = degreeArr[j+diff];
            }

        }
        for (int i = 0; i < stepArrSize; i++) {
            // System.out.println("old"+stepArr[i]);
        }
        // System.out.println("oldlen"+stepArrSize);
    }
}
