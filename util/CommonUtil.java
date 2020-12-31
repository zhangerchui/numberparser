package numberparser.util;



/**
 * 
 * packagename:com.hualife.task.inner.numberparser.util
 * @author zhangqian
 * Time:2020年12月8日上午10:38:48
 * describe:数字解析器的公共变量
 */
public class CommonUtil {
  
 public static final String  CHINESE_CHAR_LIST = "['幺','零', '一', '二', '两', '三', '四', '五', '六', '七', '八', '九', '十', '百', '千', '万', '亿']";
 public static final String  CHINESE_SIGN_LIST = "['负','正','-','+']";
 public static final String  CHINESE_CONNECTING_SIGN_LIST = "['.','点','·']";
 public static final String  CHINESE_PER_COUNTING_STRING_LIST = "百分之,千分之,万分之";
 public static final String  CHINESE_PER_COUNTING_SEG = "分之";
 public  static final String CHINESE_PURE_NUMBER_LIST = "幺,一,二,两,三,四,五,六,七,八,九,十,零";
 public  static final String CHINESE_SIGN_DICT = "负:-,正:+,-:-,+:+}";
 public  static final String CHINESE_PER_COUNTING_DICT = "百分之:%,千分之:‰,万分之:‱";
 public  static final String CHINESE_CONNECTING_SIGN_DICT =  ".:.,点:.,·:.";
 public  static final String CHINESE_COUNTING_STRING = "{'十':10,'百':100,'千':1000,'万':10000,'亿':100000000}";
 public  static final String CHINESE_PURE_COUNTING_UNIT_LIST = "十,百,千,万,亿";
 public  static final String TRADITIONAl_CONVERT_DICT = "壹:一,贰:二,叁:三,肆:四,伍:五,陆:六,柒:七,捌:八,玖:九";
 public  static final String SPECIAL_TRADITIONAl_COUNTING_UNIT_CHAR_DICT = "拾:十,佰:百,仟:千,萬:万,億:亿";
 public  static final String SPECIAL_NUMBER_CHAR_DICT = "两:二,俩:二";
 
/**
 * 中文转阿拉伯数字common_used_ch_numerals
 */
 public  static final String  COMMON_USED_CH_NUMERALS= "幺:1,零:0,一:1,二:2,两:2,三:3,四:4,五:5,六:6,七:7,八:8,九:9,十:10,百:100,千:1000,万:10000,亿:100000000";
 /**
  * 阿拉伯数字转中文digits_char_ch_dict
  */
 public  static final String  DIGITS_CHAR_CH_DICT= "0:零,1:一,2:二,3:三,4:四,5:五,6:六,7:七,8:八,9:九,%:百分之,‰:千分之,‱:万分之,.:点";

 public  static final String DIGITS_CHAR_LIST = "['0','1', '2', '3', '4', '5', '6', '7', '8', '9']";
 public  static final String DIGITS_SIGN_LIST = "['-','+']";
 public  static final String DIGITS_CONNECTING_SIGN_LIST = "['.']";
 public  static final String DIGITS_PER_COUNTING_STRING_LIST = "['%','‰','‱']";
 
 public  static final String TAKING_DIGITS_RE_RULE = "(?:(?:\\+|\\-){0,1}\\d+(?:\\.\\d+){0,1}(?:[\\%\\‰\\‱]){0,1}|(?:\\+|\\-){0,1}\\.\\d+(?:[\\%\\‰\\‱]){0,1})";
 

 public  static final String  TAKING_CHINESE_DIGITS_MIX_RE_RULER  = "(?:(?:分之){0,1}(?:\\+|\\-){0,1}[正负]{0,1})"
                                            + "(?:(?:(?:\\d+(?:\\.\\d+){0,1}(?:[\\%\\‰\\‱]){0,1}|\\.\\d+(?:[\\%\\‰\\‱]){0,1}){0,1}"
                                            + "(?:(?:(?:[一二三四五六七八九十千万亿兆幺零百]+(?:点[一二三四五六七八九幺零]+){0,1})|(?:点[一二三四五六七八九幺零]+))))"
                                            + "|(?:(?:\\d+(?:\\.\\d+){0,1}(?:[\\%\\‰\\‱]){0,1}|\\.\\d+(?:[\\%\\‰\\‱]){0,1})"
                                            + "(?:(?:(?:[一二三四五六七八九十千万亿兆幺零百]+(?:点[一二三四五六七八九幺零]+){0,1})|(?:点[一二三四五六七八九幺零]+))){0,1}))"; 

}
