package numberparser.nlp;

import numberparser.util.CommonUtil;
import numberparser.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * 
 * packagename:com.hualife.task.inner.numberparser.nlp
 * @author zhangqian
 * Time:2020年12月9日下午1:52:37
 * describe:数字处理方法
 */

public class NumberHandlingModule {
  
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月9日下午1:58:02
   * describe:
   * @param numberString
   * @param : convert percent simple. Default is True.  3% will be 0.03 in the result
   * @param : Switch to convert the take pure digits number
   * @param : if true, will return words that raised exception and catch the error
   * @return
   */
  public  List<String>  takeNumberFromString(String numberString){
    /*
      percentConvert: 转换简单百分比。默认值为True  3% will be 0.03 in the result
      traditionalConvert: 将繁体字转换为简体中文
      digitsNumberSwitch:转换为纯数字的开关
      verbose: 如果为true，将返回引发异常的单词并捕获错误
    */
    boolean traditionalConvert = true;
    boolean digitsNumberSwitch = false;
    boolean verbose = false;
    boolean percentConvert = true;
    List<String>  finalResult = takeChineseNumberFromString(numberString,percentConvert,traditionalConvert,digitsNumberSwitch,verbose);   
    return finalResult;
  }
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月9日下午1:57:57
   * describe:
   * @param numberString
   * @param verbose   
   * @param digitsNumberSwitch 
   * @param traditionalConvert 
   * @param percentConvert 
   * @return
   */
  public List<String> takeChineseNumberFromString(String numberString, boolean percentConvert, boolean traditionalConvert, boolean digitsNumberSwitch, boolean verbose) {
    /**
     * 是否只提取数字
     */
    if (digitsNumberSwitch ==  true){
      return takeDigitsNumberFromString(numberString,percentConvert);
    }
    /**
     * 简体转换开关
     */
     String convertedCHString = traditionalTextConvertFunc(numberString,traditionalConvert);
    /**
     *  字符串 汉字数字字符串切割提取 正则表达式方法
     */
    
     Pattern   takingChineseDigitsMixRERules = Pattern.compile(CommonUtil.TAKING_CHINESE_DIGITS_MIX_RE_RULER);
     Matcher matcher = takingChineseDigitsMixRERules.matcher(convertedCHString);
     List<String> chNumberStr =  new ArrayList<>();
     while(matcher.find()){
       chNumberStr.add(matcher.group(0).toString()); 
     }
     String[] chNumberStringListTemp = new String[chNumberStr.size()] ;
     for(int e=0;e<chNumberStr.size();e++){
       chNumberStringListTemp[e] = chNumberStr.get(e);
     }
     //检查是不是  分之 切割不完整问题
     chNumberStringListTemp = checkNumberSeg(chNumberStringListTemp,convertedCHString);
     //检查末位是不是正负号
     chNumberStringListTemp = checkSignSeg(chNumberStringListTemp);
     // #备份一个原始的提取，后期处结果的时候显示用
     String[] originCHNumberTake = chNumberStringListTemp;
     
     //将阿拉伯数字变成汉字  不然合理性检查 以及后期 如果不是300万这种乘法  而是 四分之345  这种 就出错了
     chNumberStringListTemp = digitsToCHChars(chNumberStringListTemp);
     
     //#检查合理性 是否是单纯的单位 等
     String[] chNumberStringList = new String[chNumberStringListTemp.length] ;
     String[] originCHNumberForOutput = new String[chNumberStringListTemp.length];
     if(chNumberStringListTemp.length>0){
       for(int s = 0 ;s<chNumberStringListTemp.length;s++){
         String tempText = chNumberStringListTemp[s] ;
         boolean textFlag = checkChineseNumberReasonable(tempText);
         if (textFlag){
           //如果合理  则添加进被转换列表
           chNumberStringList[s] = tempText;
           originCHNumberForOutput[s] = originCHNumberTake[s];
         }
       }
     }
     chNumberStringList = removeArrayEmptyTextBackNewArray(chNumberStringList);
     originCHNumberForOutput = removeArrayEmptyTextBackNewArray(originCHNumberForOutput);
     //进行标准汉字字符串转换 例如 二千二  转换成二千零二
     //String[] 转换成list
     List<String> chNumberStringLists = new ArrayList<>();
     for(int m=0;m<chNumberStringList.length;m++){
       if(StringUtil.isNotEmpty(chNumberStringList[m])){
         String straderReplace = standardChNumberConvert(chNumberStringList[m]);
         chNumberStringLists.add(straderReplace);
       }
     }
     List<String> chNumberStringListTemps = chNumberStringLists.stream().map(String::valueOf).collect(Collectors.toList());
     /**
      * 将中文转换为数字
      */
     String[] digitsStringList = new String[chNumberStringListTemps.size()];
     List<String> digitList = new  ArrayList<>();
     String   replacedText = convertedCHString;
     if(chNumberStringListTemps.size()>0){
       // 用标准清洗后的字符串进行转换
       for(int u =0 ;u<chNumberStringListTemps.size();u++){
          String highTolerancce =  chineseToDigitsHighTolerance( chNumberStringListTemps.get(u),percentConvert,verbose,u);
          digitsStringList[u] = highTolerancce;
          digitList.add(highTolerancce);
       }
     }
     List<String> originChNumberList = new ArrayList<>();
     List<String> originChNumberLethonList = new ArrayList<>();
     for(int p=0;p<originCHNumberForOutput.length;p++){
       originChNumberList.add(originCHNumberForOutput[p]);
       originChNumberLethonList.add(String.valueOf(originCHNumberForOutput[p].length()));
     }
     
     List<List<String>> tupleToReplace =  Zip.zip(originChNumberList,digitList,originChNumberLethonList);
     /**
      * 按照提取出的中文数字字符串长短排序，然后替换。防止百分之二十八 ，二十八，这样的先把短的替换完了的情况
      */
     tupleToReplace= tupleToReplace.stream().sorted((o1, o2) -> {
       for (int m = 0; m < Math.min(o1.size(), o2.size()); m++) {
         int c = o1.get(2).compareTo(o2.get(2));
         if (c != 0) {
             return c;
         }
       }
       return Integer.compare(o1.size(), o2.size());
     }).collect(Collectors.toList());
     
     for(int n =0 ;n<tupleToReplace.size();n++){
       replacedText = replacedText.replace(tupleToReplace.get(n).get(0),tupleToReplace.get(n).get(1));
     }
     return digitList;
  }

  
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月9日下午2:19:04
   * describe:
   * @param numberString
   * @param
   * @return
   */
  public static List<String> takeDigitsNumberFromString(String numberString, boolean percentConvert) {
   
    Pattern   takingDigitsRERule = Pattern.compile(CommonUtil.TAKING_DIGITS_RE_RULE);
    Matcher matcher = takingDigitsRERule.matcher(numberString);
    List<String> digitsNumberStringList = new ArrayList<>();
    while(matcher.find()){
      digitsNumberStringList.add(matcher.group(0).toString());
    }
    /**
     * 最后检查有没有百分号
     */
    List<String> digitsStringList = new ArrayList<>();
    String replacedText = numberString;
    if(digitsNumberStringList.size()>0){  
     List<String> listStr = new ArrayList<>();
     int  digitsNumber = digitsNumberStringList.size(); 
     while(digitsNumber -- > 0){
       listStr.add(String.valueOf(digitsNumberStringList.get(digitsNumber).length()));
     }
     List<List<String>> tupleToReplace = Zip.zip(digitsNumberStringList,digitsStringList,listStr);
     /**
      * 按照提取出的中文数字字符串长短排序，然后替换。防止百分之二十八 ，二十八，这样的先把短的替换完了的情况
      */
     tupleToReplace= tupleToReplace.stream().sorted((o1, o2) -> {
       for (int i = 0; i < Math.min(o1.size(), o2.size()); i++) {
         //根据第三个维度进行排序
         if(o1.size()>2 && o1.size()>2){
           int c = o1.get(2).compareTo(o2.get(2));
           if (c != 0) {
               return c;
           }
         }else{
           break;
         }
       }
       return Integer.compare(o1.size(), o2.size());
     }).collect(Collectors.toList());
     if(tupleToReplace.size()>0){
       if(tupleToReplace.get(0).size()>2){
         for(int i =0 ;i<tupleToReplace.size();i++){
           replacedText = replacedText.replace(tupleToReplace.get(i).get(0),tupleToReplace.get(i).get(1));
         }
       }
     }
       
    }
    return digitsNumberStringList;
  }
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月15日下午1:53:57
   * describe:简体转换方法
   * @param numberString
   * @param traditionalConvert
   * @return
   */
  public String traditionalTextConvertFunc(String numberString, boolean traditionalConvert) {
    char[] numberStr = numberString.toCharArray();
    List<String> numberStrResult = new ArrayList<>();
    StringBuffer  strResult = new StringBuffer();
    if(traditionalConvert){
      for(int j=0;j<numberStr.length;j++){
        //繁体中文数字转简体中文数字
        String reqStr =  String.valueOf(numberStr[j]);
        String realice=this.replaceString(CommonUtil.TRADITIONAl_CONVERT_DICT,reqStr); 
        if(StringUtil.isNotEmpty(realice)){
          numberStrResult.add(realice);
        }else{
          numberStrResult.add(reqStr);
        }
      }
    }
    //检查繁体单体转换
    for(int k = 0;k<numberStrResult.size();k++ ){
      //如果 前后有 pure 汉字数字 则转换单位为简体
      String reqStrNum = String.valueOf(numberStrResult.get(k)); 
      String pureString = "";
      if(StringUtil.isNotEmpty(String.valueOf(this.replaceString(CommonUtil.SPECIAL_TRADITIONAl_COUNTING_UNIT_CHAR_DICT,reqStrNum)))){
        // 如果前后有单纯的数字 则进行单位转换
        if(k == 0){
          if(CommonUtil.CHINESE_PURE_NUMBER_LIST.indexOf(numberStrResult.get(k+1))!=-1){
            pureString = this.replaceString(CommonUtil.SPECIAL_TRADITIONAl_COUNTING_UNIT_CHAR_DICT,reqStrNum);
            if(StringUtil.isNotEmpty(pureString)){
              Collections.replaceAll(numberStrResult, numberStrResult.get(k), pureString);
            }
          }
        }else if(k == numberStr.length-1){
          if(CommonUtil.CHINESE_PURE_NUMBER_LIST.indexOf(numberStrResult.get(k-1))!=-1){
            pureString = this.replaceString(CommonUtil.SPECIAL_TRADITIONAl_COUNTING_UNIT_CHAR_DICT,reqStrNum);
            if(StringUtil.isNotEmpty(pureString)){
              Collections.replaceAll(numberStrResult, numberStrResult.get(k), pureString);
            }
          }
        }else{
          if(CommonUtil.CHINESE_PURE_COUNTING_UNIT_LIST.indexOf(numberStrResult.get(k-1))!=-1 || 
              CommonUtil.CHINESE_PURE_COUNTING_UNIT_LIST.indexOf(numberStrResult.get(k+1))!=-1){
            pureString = this.replaceString(CommonUtil.SPECIAL_TRADITIONAl_COUNTING_UNIT_CHAR_DICT,reqStrNum);
            if(StringUtil.isNotEmpty(pureString)){
              Collections.replaceAll(numberStrResult, numberStrResult.get(k), pureString);
            }
          }
        }
      }
     //特殊变换 俩变二
      if(StringUtil.isNotEmpty(String.valueOf(this.replaceString(CommonUtil.SPECIAL_NUMBER_CHAR_DICT,reqStrNum)))){
        //如果前后有单位 则进行转换
        if(k == 0){
          if(CommonUtil.CHINESE_PURE_COUNTING_UNIT_LIST.indexOf(numberStrResult.get(k+1))!=-1){
            String chinesePureStr = this.replaceString(CommonUtil.SPECIAL_NUMBER_CHAR_DICT,reqStrNum);
            if(StringUtil.isNotEmpty(chinesePureStr)){
              Collections.replaceAll(numberStrResult, numberStrResult.get(k), chinesePureStr);
            }
          }
        }else if(k == numberStr.length-1){
          if(CommonUtil.CHINESE_PURE_COUNTING_UNIT_LIST.indexOf(numberStrResult.get(k-1)) != -1){
            String chinStr = this.replaceString(CommonUtil.SPECIAL_NUMBER_CHAR_DICT,reqStrNum);
            if(StringUtil.isNotEmpty(chinStr)){
              Collections.replaceAll(numberStrResult, numberStrResult.get(k), chinStr);
            }
          }
        }else{
          if(CommonUtil.CHINESE_PURE_COUNTING_UNIT_LIST.indexOf(numberStrResult.get(k-1))!=-1 || 
              CommonUtil.CHINESE_PURE_COUNTING_UNIT_LIST.indexOf(numberStrResult.get(k+1))!=-1){
            String spstr =replaceString(CommonUtil.SPECIAL_NUMBER_CHAR_DICT,reqStrNum);
            if(StringUtil.isNotEmpty(spstr)){
              Collections.replaceAll(numberStrResult, numberStrResult.get(k), spstr);
            }
          }
        }
      }
     
    }
    if(numberStrResult.size()>0){
      for(int n=0;n<numberStrResult.size();n++){
        strResult.append(numberStrResult.get(n));
      }
    }
    
    return strResult.toString();
  }
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月15日下午3:40:18
   * describe:替换文字的值
   * @param traditionalConvertDict
   * @param
   * @return
   */
  public   String replaceString(String traditionalConvertDict, String  requString) {
    String result = "";
    String[] tradition = traditionalConvertDict.split(",");
    for(int d=0;d<tradition.length;d++){
      String[] traString = tradition[d].split(":");
      if(requString.equals(traString[0])){
        result = traString[1];
      }
    }
    return result;
  }
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月16日上午10:05:15
   * describe:检查是不是  分之 切割不完整问题
   * @param
   * @param
   * @return
   */
  public String[] checkNumberSeg(String[] chineseNumberList,
      String originText) {
    List<String>  newChineseNumberList = new  ArrayList<>();
    //用来控制是否前一个已经合并过  防止多重合并
    String tempPreText = "";
    String tempMixedString = "";
    
    if (chineseNumberList.length >0){
      //加入唯一的一个 或者第一个
      if(chineseNumberList[0].length()>2){
        if(CommonUtil.CHINESE_PER_COUNTING_SEG.indexOf(chineseNumberList[0].substring(0, 2)) != -1){
          //如果以分之开头 记录本次 防止后面要用 是否出现连续的 分之
          tempPreText = chineseNumberList[0];
          newChineseNumberList.add(chineseNumberList[0].substring(0, 2));
        }else{
          newChineseNumberList.add(chineseNumberList[0]);
        }
      }else{
        newChineseNumberList.add(chineseNumberList[0]);
      }
      
      if(chineseNumberList.length >1){
        for(int t = 1 ;t<chineseNumberList.length;t++){
          if(chineseNumberList[t].length()>1 && chineseNumberList[t].length() != 2){
            if(CommonUtil.CHINESE_PER_COUNTING_SEG.indexOf(chineseNumberList[t].substring(0, 2)) != -1){
              //如果是以 分之 开头 那么检查他和他见面的汉子数字是不是连续的 即 是否在原始字符串出现
              tempMixedString = chineseNumberList[t-1] + chineseNumberList[t];
              if(originText.indexOf(tempMixedString) != -1){
                //如果连续的上一个字段是以分之开头的  本字段又以分之开头  
                if (tempPreText != ""){
                  //检查上一个字段的末尾是不是 以 百 十 万 的单位结尾
                  if(CommonUtil.CHINESE_PURE_COUNTING_UNIT_LIST.indexOf(tempPreText.substring(tempPreText.length()-1, tempPreText.length()) ) != -1){
                    //先把上一个记录进去的最后一位去掉
                    String  repliceString = newChineseNumberList.get(newChineseNumberList.size()-1);
                    Collections.replaceAll(newChineseNumberList,newChineseNumberList.get(newChineseNumberList.size()-1),
                        repliceString.substring(0, repliceString.length()-1));
                    newChineseNumberList.add(tempPreText.substring(tempPreText.length()-1, tempPreText.length()) + chineseNumberList[t]); 
                  }else{
                    //如果上一个字段不是以单位结尾  同时他又是以分之开头，那么 本次把分之去掉
                    newChineseNumberList.add(chineseNumberList[t].substring(2, tempPreText.length()));
                  }
                }else{
                  //上一个字段不以分之开头，那么把两个字段合并记录
                  if (newChineseNumberList.size()>0){
                    Collections.replaceAll(newChineseNumberList,newChineseNumberList.get(newChineseNumberList.size()-1),tempMixedString);
                  }else{
                    newChineseNumberList.add(tempMixedString) ;
                  }
                }
              }else{
                //#说明前一个数字 和本数字不是连续的
                //本数字去掉分之二字
                newChineseNumberList.add(chineseNumberList[t].substring(2, chineseNumberList[t].length()))  ;
              }
              //#记录以 分之 开头的字段  用以下一个汉字字段判别
              tempPreText = chineseNumberList[t];
            }else{
              //#不是  分之 开头 那么把本数字加入序列
              newChineseNumberList.add(chineseNumberList[t]);
              //#记录把不是 分之 开头的字段  临时变量记为空
              tempPreText = "";
            }
          }else{
            //#不是  分之 开头 那么把本数字加入序列
            newChineseNumberList.add(chineseNumberList[t]);
            //#记录把不是 分之 开头的字段  临时变量记为空
            tempPreText = "";
          }
        }
          
      }
    }
    String[] newChineseNumberString = new String[newChineseNumberList.size()] ;
    if(newChineseNumberList.size()>0){
      for(int s = 0;s<newChineseNumberList.size();s++){
        newChineseNumberString[s] = newChineseNumberList.get(s);
      }
    }
    return newChineseNumberString;
  }
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月16日下午5:18:29
   * describe:检查末位是不是正负号
   * @param
   * @return
   */
  private String[] checkSignSeg(String[] chineseNumberList) {
    String[] newChineseNumberList = new String[chineseNumberList.length];
    String tempSign = "";
    if(chineseNumberList.length>0){
      for(int i = 0;i<chineseNumberList.length;i++){
        //新字符串 需要加上上一个字符串 最后1位的判断结果
       String tempString =  tempSign + chineseNumberList[i];
       String lastString = tempString.substring(tempString.length()-1, tempString.length());
       //如果最后1位是正负号 那么本字符去掉最后1位  下一个数字加上最后3位
       if(CommonUtil.CHINESE_SIGN_LIST.indexOf(lastString) != -1){
         tempSign = lastString;
         //如果最后1位 是  那么截掉最后1位
         tempString = tempString.substring(0, tempString.length()-1);
       }else{
         tempSign = "";
       }
       newChineseNumberList[i] = tempString;
      }
    }
    return newChineseNumberList;
  }
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月16日下午5:52:42
   * describe:将阿拉伯数字变成汉字  不然合理性检查 以及后期 如果不是300万这种乘法  而是 四分之345 
   * @param
   * @return
   */
  public String[] digitsToCHChars(String[] mixedStringList) {
    String[] resultList = new  String[mixedStringList.length];
    if(mixedStringList.length>0){
      for(int t=0 ;t<mixedStringList.length;t++){
        String mixedString = mixedStringList[t];
        if(mixedString.startsWith(".")){
          mixedString= '0'+mixedString;
        }
        for(int k=0;k<mixedString.length();k++){
          //应当记录下来有转换，然后再操作  在核心函数里 通过小数点判断是否应该强制 
          String repliceMixed = "";
          if(!StringUtil.isContainChinese(String.valueOf(mixedString.charAt(k)))){
             repliceMixed = this.replaceString(CommonUtil.DIGITS_CHAR_CH_DICT, 
                String.valueOf(mixedString.charAt(k)));
          }else{
            continue;
          }
          if(StringUtil.isNotEmpty(repliceMixed)){
            mixedString = mixedString.replace(String.valueOf(mixedString.charAt(k)),String.valueOf(repliceMixed));
          }
          //#应当是只要有百分号 就挪到前面 阿拉伯数字没有四百分之的说法
          //#防止这种 3%万 这种问题
          String repliceMixedEmpty = this.analysisString(CommonUtil.CHINESE_PER_COUNTING_STRING_LIST, 
              repliceMixed);
          if(StringUtil.isNotEmpty(repliceMixedEmpty)){
            String temp = repliceMixedEmpty + mixedString.replace(repliceMixedEmpty,"");
            mixedString = temp;
          }
          
        }
        resultList[t]  = mixedString;
      }
    }
    
    return resultList;
  }
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月17日下午5:40:05
   * describe:解析字符串逗号分隔
   * @return
   */
  public  String  analysisString(String type,String analysed){
    String[] analyString = type.split(",");
    String result = "";
    if(analyString.length>0){
      for(int n=0;n<analyString.length;n++){
        if(analyString[n].equals(analysed)){
          result = analyString[n];
        }
      }
    }
    return result;
  }
  /**
   * 
   * location:检查合理性 是否是单纯的单位  等
   * @author zhangqian
   * Time:2020年12月17日下午6:27:34
   * describe:
   * @param tempText
   * @return
   */
  public boolean checkChineseNumberReasonable(String tempText) {
    if(tempText.length()>0){
      //由于在上个检查点 已经把阿拉伯数字转为中文 因此不用检查阿拉伯数字部分
      // 如果汉字长度大于0 则判断是不是 万  千  单字这种
      String[] chinesePure = CommonUtil.CHINESE_PURE_NUMBER_LIST.split(",");
      int  number = chinesePure.length;
      while(number -- > 0){
        if(tempText.indexOf(chinesePure[number]) != -1){
          return true;
        }
      }
      
    }
    return false;
  }
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月18日下午2:57:38
   * describe:
   * @param
   * @param percentConvert
   * @param
   * @param
   * @param
   * @return
   */
  public String chineseToDigitsHighTolerance(String chineseDigitsMixString, boolean percentConvert,
      boolean skipError,int number) {
    String total ="";
    if (skipError){
      try {
        total = chineseToDigits(chineseDigitsMixString,percentConvert);
      } catch (Exception e) {
        // #返回类型不能是none 是空字符串
        total = "";

      }  
    }else{
      total = chineseToDigits(chineseDigitsMixString,percentConvert);
    } 
    
    
   return total;
    
  }
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月18日下午3:07:25
   * describe:中文解析成数字
   * @param chineseDigitsMixString
   * @param percentConvert
   * @return
   */
  public String chineseToDigits(String chineseDigitsMixString, boolean percentConvert) {
    /**
     * #之前已经做过罗马数字变汉字 所以不存在罗马数字部分问题了
     * 分之  分号切割  要注意
     */
    String[] chineseCharsListByDiv = chineseDigitsMixString.split("分之");
    List<String> convertResultList = new ArrayList<>();
    String finalTotal = "";
    if(chineseCharsListByDiv.length>0){
      for(int g=0;g<chineseCharsListByDiv.length;g++){
       String  tempChineseChars = chineseCharsListByDiv[g];
       String chineseChars  = tempChineseChars;
       String[] chineseCharsDotSplitList = null;
       String convertResult = "";
       
       //看有没有符号
       String sign = "";
       for(int t=0;t<tempChineseChars.length();t++){
         String tempChar = String.valueOf(tempChineseChars.charAt(t));
         String  tempRepliace = String.valueOf(replaceString(CommonUtil.CHINESE_SIGN_DICT,tempChar));
         if(StringUtil.isNotEmpty(tempRepliace)){
           sign = tempRepliace;
           tempChineseChars = tempChineseChars.replace(tempChar, "");
         }
       }
       //防止没有循环完成就替换 报错
       chineseChars = tempChineseChars;
       //小数点切割，看看是不是有小数点
       if(StringUtil.isNotEmpty(chineseChars)){
         for(int n=0;n<chineseChars.length();n++){
           String replaceChars = String.valueOf(replaceString(CommonUtil.CHINESE_CONNECTING_SIGN_DICT,
               String.valueOf(chineseChars.charAt(n))));
           if(StringUtil.isNotEmpty(replaceChars) && !replaceChars.equals(String.valueOf(chineseChars.charAt(n)))){
             chineseCharsDotSplitList = chineseChars.split(String.valueOf(chineseChars.charAt(n)));
           }
         }
       }
      
       if (null == chineseCharsDotSplitList || chineseCharsDotSplitList.length == 0){
         convertResult = coreCHToDigits(chineseChars);
       }else{
         //#如果小数点右侧有 单位 比如 2.55万  4.3百万 的处理方式
         //#先把小数点右侧单位去掉
        String tempCountString = "";
        String lastSplitList = chineseCharsDotSplitList[chineseCharsDotSplitList.length-1];
        for(int s=lastSplitList.length()-1;s>=0;s--){
          if(StringUtil.isNotEmpty(analysisString("千,百,万",
              String.valueOf(lastSplitList.charAt(s))))){
            tempCountString = String.valueOf(lastSplitList.charAt(s)) + tempCountString;
          }else{
            lastSplitList = lastSplitList.substring(0, s+1);
            break;
          }
        }
        float tempCountNum  = 0f;
        if(tempCountString != ""){
           tempCountNum = Float.valueOf((coreCHToDigits(tempCountString)));
        }else{
          tempCountNum = 1.0f;
        }
        if (chineseCharsDotSplitList[0] == ""){
          //.01234 这种开头  用0 补位
          convertResult = "0."+ coreCHToDigits(chineseCharsDotSplitList[1]);
        }else{
          convertResult = coreCHToDigits(chineseCharsDotSplitList[0]) + '.' + coreCHToDigits(chineseCharsDotSplitList[1]);
        }
        convertResult = String.valueOf(Float.valueOf(convertResult)*tempCountNum);
       }
       //如果 convertResult 是空字符串， 表示可能整体字符串是 负百分之10 这种  或者 -百分之10
       if (convertResult ==""){
         convertResult = "1";
       }
       if(StringUtil.isNotEmpty(sign)){
         convertResult = sign + convertResult;
       }
       //#最后在双向转换一下 防止出现 0.3000 或者 00.300的情况
       BigDecimal bigDecimal = new BigDecimal(convertResult);
       convertResult = bigDecimal.toString();
       if (convertResult.endsWith(".0")){
         convertResult=convertResult.substring(0, convertResult.length()-2);
       }
       convertResultList.add(convertResult) ;
      }
      
      if (convertResultList.size()>1){
        //#是否转换分号及百分比
        if (percentConvert){
          BigDecimal bdResultOne = new BigDecimal(convertResultList.get(1));
          BigDecimal bdResultZore = new BigDecimal(convertResultList.get(0));
          try {
            BigDecimal divide = bdResultOne.divide(bdResultZore);
            finalTotal = divide.toString();
          } catch (Exception e) {
            //除法除不尽
            BigDecimal divide = bdResultOne.divide(bdResultZore,20,BigDecimal.ROUND_HALF_UP);
            finalTotal = divide.toString();
          }
          
         
          if (finalTotal.endsWith(".0")){
            finalTotal=finalTotal.substring(0, finalTotal.length()-2);
          }
        }else{
          if (convertResultList.get(0) == "100"){
            finalTotal = convertResultList.get(1) + "%";
          }
          else if (convertResultList.get(0) == "1000"){
            finalTotal = convertResultList.get(1) + "‰";
          }else if( convertResultList.get(0) == "10000"){          
            finalTotal = convertResultList.get(1) + "‱";
          }else{
            finalTotal = convertResultList.get(1)+'/' + convertResultList.get(0);
          }
        }
      }else{
        finalTotal = convertResultList.get(0);
      }
      
    }
    return finalTotal;
  }
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月18日下午5:13:00
   * describe:
   * @param chineseChars
   * @return
   */
  public String coreCHToDigits(String chineseChars) {
    int total = 0;
    String totalResult = "";
    //#用以记录临时是否建议数字拼接的字符串 例如 三零万 的三零
    String tempVal = "";
    //#表示单位：个十百千,用以计算单位相乘 例如八百万 百万是相乘的方法，但是如果万前面有 了一千八百万 这种，千和百不能相乘，要相加...
    int countingUnit = 1;    
    //#原始字符串提取的单位应该是一个list  在计算的时候，新的单位应该是本次取得的数字乘以已经发现的最大单位，例如 4千三百五十万， 等于 4000万+300万+50万
    List<Integer> countingUnitFromString = new ArrayList<>();
    countingUnitFromString.add(1);
    for(int i=chineseChars.length()-1;i>=0;i--){
      int val = replaceStringInteger(CommonUtil.COMMON_USED_CH_NUMERALS,
          String.valueOf(chineseChars.charAt(i)));
      //#应对 十三 十四 十*之类，说明为十以上的数字，看是不是十三这种
      if(val >= 10 && i == 0){
        //#说明循环到了第一位 也就是最后一个循环 看看是不是单位开头
        //#取最近一次的单位
        //#如果val大于 contingUnit 说明 是以一个更大的单位开头 例如 十三 千二这种
        if (val > countingUnit){
          //#赋值新的计数单位
          countingUnit = val;
          //#总值等于  全部值加上新的单位 类似于13 这种
          total = total + val;
          countingUnitFromString.add(val);
        }else{
          countingUnitFromString.add(val);
          //# 计算用的单位是最新的单位乘以字符串中最大的原始单位  为了计算四百万这种
          //# countingUnit = countingUnit * val
          countingUnit = getMax(countingUnitFromString) * val;
        }
      }else if(val >= 10){
        if (val > countingUnit){
          countingUnit = val;
          countingUnitFromString.add(val);
        }else{
          countingUnitFromString.add(val);
          //# 计算用的单位是最新的单位乘以字符串中最大的原始单位 为了计算四百万这种
          //# countingUnit = countingUnit * val
          countingUnit = getMax(countingUnitFromString) * val;
        }
      }else{
        if (i > 0){
          //#如果下一个不是单位 则本次也是拼接
          int countval = replaceStringInteger(CommonUtil.COMMON_USED_CH_NUMERALS,
              String.valueOf(chineseChars.charAt(i-1)));
          if(countval<10){
            tempVal = String.valueOf(val) + tempVal;
          }else{
            //#说明已经有大于10的单位插入 要数学计算了
            //#先拼接再计算
            //#如果取值不大于10 说明是0-9 则继续取值 直到取到最近一个大于10 的单位   应对这种30万20千 这样子
            total = total + countingUnit * Integer.valueOf(String.valueOf(val) + tempVal) ; 
            tempVal = "";
          }
        }else{
          //#那就是无论如何要收尾了
          total = total + countingUnit * Integer.valueOf(String.valueOf(val) + tempVal);
        }
      }
      
    }
    //#如果 total 为0  但是 countingUnit 不为0  说明结果是 十万这种  最终直接取结果 十万
    //#如果countingUnit 大于10 说明他是就是 汉字零
    if (total == 0 && countingUnit>10){
      totalResult = String.valueOf(countingUnit);
    }else{
      totalResult = String.valueOf(total);
    } 
    if (totalResult.endsWith(".0")){
      totalResult = totalResult.substring(0, totalResult.length()-2);
    }
      
    return totalResult;
  }
  
  /***
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月18日下午5:23:47
   * describe:转换成为数字
   * @param traditionalConvertDict
   * @param requString
   * @return
   */
  public int replaceStringInteger(String traditionalConvertDict, String  requString) {
    String result = "";
    int resultNumber ;
    String[] tradition = traditionalConvertDict.split(",");
    for(int d=0;d<tradition.length;d++){
      String[] traString = tradition[d].split(":");
      if(requString.equals(traString[0])){
        result = traString[1];
      }
    }
    resultNumber = Integer.valueOf(String.valueOf(result));
    return resultNumber;
  }
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月18日下午5:44:26
   * describe:获取最大值
   * @param countingUnitFromString
   * @return
   */
  public  int getMax(List<Integer> countingUnitFromString) {
    // 定义一个参照物
    int max = countingUnitFromString.get(0);
    //遍历数组
    for (int i = 0; i < countingUnitFromString.size(); i++) {
    //判断大小
      if (countingUnitFromString.get(i) > max) {
         max = countingUnitFromString.get(i);
        }
    }
    return max;
  }
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月24日上午11:25:03
   * describe:进行标准汉字字符串转换 例如 二千二  转换成二千零二
   * @param
   * @return
   */
  public String standardChNumberConvert(String requestion) {
    String result ="";
    //#大于2的长度字符串才有检测和补位的必要
    StringBuffer  strbuf = new StringBuffer(requestion);
    if(requestion.length()>2){
      //#十位补一
      String countIndex = "";
      for(int t=0;t<requestion.length();t++){
        if(String.valueOf(requestion.charAt(t)).equals("十")){
          countIndex = String.valueOf(t);
        }
      }
      if(StringUtil.isNotEmpty(countIndex)){
        int tenNumberIndex = Integer.valueOf(countIndex);
        if(tenNumberIndex == 0){
          strbuf.insert(tenNumberIndex, "一");
        }else{
          // # 如果没有左边计数数字 插入1
          if(CommonUtil.CHINESE_PURE_NUMBER_LIST.indexOf
              (strbuf.charAt(tenNumberIndex - 1))==-1){
            strbuf.insert(tenNumberIndex, "一");
          }
        }
      }
      //#差位补零
      //#逻辑 如果最后一个单位 不是十结尾 而是百以上 则数字后面补一个比最后一个出现的单位小一级的单位
      //#从倒数第二位开始看,且必须是倒数第二位就是单位的才符合条件
      String[]  pureStr = CommonUtil.CHINESE_PURE_COUNTING_UNIT_LIST.split(",");
      int lastCountingUnit = 0;
      for(int p = 0;p<pureStr.length;p++){
        if(pureStr[p].equals(String.valueOf(strbuf.charAt(strbuf.length() - 2)))){
          lastCountingUnit = p;
        }
      }
      //# 如果最末位的是百开头
      if(lastCountingUnit>=1){
        //# 则字符串最后拼接一个比最后一个单位小一位的单位 例如四万三 变成四万三千
        //# 如果最后一位结束的是亿 则补千万
        if (lastCountingUnit == 4){
          strbuf.append("千万");
        }else{
          strbuf.append(pureStr[lastCountingUnit - 1]);
        }
      }
    }
    //#检查是否是 万三  千四点五这种表述 百三百四
    result = strbuf.toString();
    int  perCountSwitch = 0;
    if(result.length()>1){
      if("千,万,百".indexOf(String.valueOf(result.charAt(0))) != -1){
        for(int i=1;i<result.length();i++){
          //#其余位数都是纯数字 才能执行
          if (CommonUtil.CHINESE_PURE_NUMBER_LIST.indexOf
              (String.valueOf(result.charAt(i))) != -1){
              perCountSwitch = 1;
          }else{
              perCountSwitch = 0;
              //#y有一个不是数字 直接退出循环
              break;
          }
        }
      }
    }
    if( perCountSwitch == 1 ){
      result = result.substring(0, 1) +"分之" + result.substring(1, result.length());
    }
    return result;
  }
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月25日上午9:57:27
   * describe:数组的null值去掉
   * @param strArray
   * @return
   */
  public static String[] removeArrayEmptyTextBackNewArray(String[] strArray) {
    List<String> strList= Arrays.asList(strArray);
    List<String> strListNew=new ArrayList<>();
    for (int i = 0; i <strList.size(); i++) {
        if (strList.get(i)!=null&&!strList.get(i).equals("")){
            strListNew.add(strList.get(i));
        }
    }
    String[] strNewArray = strListNew.toArray(new String[strListNew.size()]);
    return   strNewArray ;
  }
  public static void main(String[] args) {
    
     NumberHandlingModule numberHand =  new  NumberHandlingModule();
     System.out.println(numberHand.takeNumberFromString("12.55万"));
     System.out.println(numberHand.takeNumberFromString("三零万二零千拉阿拉啦啦30万20千嚯嚯或百四嚯嚯嚯四百三十二分之2345啦啦啦啦"));
     System.out.println(numberHand.takeNumberFromString("百分之5负千分之15"));
     System.out.println(numberHand.takeNumberFromString("啊啦啦啦300十万你好我20万.3%万你好啊300咯咯咯-.34%啦啦啦300万"));
     System.out.println(numberHand.takeNumberFromString("aaaa.3%万"));
     System.out.println(numberHand.takeNumberFromString("十分之一"));
     System.out.println(numberHand.takeNumberFromString("四分之三啦啦五百分之二"));
     System.out.println(numberHand.takeNumberFromString("4分之3负五分之6咿呀呀 四百分之16ooo千千万万"));
     System.out.println(numberHand.takeNumberFromString("百分之五1234%"));
     System.out.println(numberHand.takeNumberFromString("五百分之一"));
     System.out.println(numberHand.takeNumberFromString("百分之四百三十二万分之四三千分之五"));
     System.out.println(numberHand.takeNumberFromString("四千三"));
     System.out.println(numberHand.takeNumberFromString("伍亿柒仟万拾柒今天天气不错百分之三亿二百万五啦啦啦啦负百分之点二八你好啊三万二"));
     System.out.println(numberHand.takeNumberFromString("llalala万三威风威风千四五"));
     System.out.println(numberHand.takeNumberFromString("哥两好"));
     System.out.println(numberHand.takeNumberFromString("伍亿柒仟万拾柒百分之"));
     System.out.println(numberHand.takeNumberFromString("负百分之点二八你好啊百分之三五是不是点五零百分之负六十五点二八"));
  }

}
