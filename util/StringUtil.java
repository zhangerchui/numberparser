/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: StringUtil
 * Author:   sinosoft
 * Date:     2020/12/31 16:53
 * Description: 字符串判断方法
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */

package numberparser.util;/**
 * Created by sinosoft on 2020/12/31.
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 〈一句话功能简述〉<br> 
 * 〈字符串判断方法〉
 *
 * @author sinosoft
 * @create 2020/12/31
 * @since 1.0.0
 */

public class StringUtil {


    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
       if(isEmpty(cs)){
           return false;
       }else{
           return true;
       }
    }

    public static boolean isContainChinese(String str) {

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
}
