##Restful 通用返回格式类设计
https://blog.csdn.net/weixin_41896508/article/details/80689826
https://blog.csdn.net/zjerryj/article/details/79847222
https://blog.csdn.net/matengbing/article/details/83188833
https://blog.csdn.net/icarusliu/article/details/80515222

restful 接口返回遵循统一格式, 设计参考类 Optional 
遵循函数式编程规范



--------------------- 
```
{
    data : { // 请求数据，对象或数组均可
        user_id: 123,
        user_name: "tutuge",
        user_avatar_url: "http://tutuge.me/avatar.jpg"
        ...
    },
    code: 1001, // 返回结果编码，成功的话我喜欢设为0000，业务自定义状态码
    msg : "done", // 返回消息，一般放置可追溯的错误消息，调试用
    extra : { // 全局附加数据，字段、内容不定
        type: 1,
        desc: "签到成功！"
    }
}
```
####msg字段 - 请求状态描述，调试用
msg字段是本次请求的业务、状态描述信息，主要用于调试、测试等。
如“done”、“请求缺少参数！”
服务端可以自由发挥，开发人员看得懂就好。

####data字段 - 请求数据
首先是本次请求结果的数据data字段，其值为对象（字典）或数组均可以，根据业务而定。

如请求的是某个用户的个人profile信息，就可以是对象，对象里面是用户profile的键值对数据，如user_id: 123、user_name: "tutuge"等。
如果请求的是列表数据，就可以是数组，如请求用户列表：
```
data: [
    {user_id: 123, user_name: "tutuge"},
    {user_id: 321, user_name: "zekunyan"},
    ...
]
```
数组、对象，相互嵌套，灵活组合即可。
对于iOS来说，解析data字段是对象还是数组也很容易，在接收到JSON数据字典后，如AFNetworking的返回结果，对data判断其类型即可：
```
if ([jsonDict[@"data"] isKindOfClass:[NSDictionary class]]) {
    // JSON对象
} else if ([jsonDict[@"data"] isKindOfClass:[NSArray class]]) {
    // JSON数组
}
```
####code 业务自定义状态码定义示例
```
// 系统相关，授权相关，认证相关、限流相关等
1001: 无权限访问
1002: access_token过期
1003: unique_token无效
...

// 用户相关
2001: 未登录
2002: 用户信息错误
2003: 用户不存在
...

// 业务1
3001: 业务1XXX
3002: 业务1XXX
...

// 业务2
4001: 业务1XXX
4002: 业务1XXX
...
```
####boolean布尔值的处理
由于语言本身的限制、框架的处理方式，不对布尔类型的值做限制总觉得不踏实，像C、C++、Objective-C里面的布尔就是数字0和1，其它语言也都各自不一样，还有从数据库读写导致的布尔值类型不一致等。

所以，如果可以的话，最好一开始就对所有请求参数、结果的布尔值类型做限定，个人觉得统一成数字0和1最好。

    isLogin:"1"
    isLogin:"0"
    
####时间、日期字段
种做法就是强制所有时间参数只能传Unix时间戳，也就是标准GMT/UTC时间戳，然后由各自的客户端根据自己的时区、显示要求做处理后显示。
```
// 从服务器接收的时间数据
loginAt: 1462068610

// 根据时区、显示要求转换，如北京时间
显示：2019年5月1日下午1点、1天前等
```
这样的话，客户端、服务端存储、读取时间都相当于处理纯数字。

####type类型的处理
API数据中免不了各种类型字段，如用户类型user_type、登录类型login_type等，类型的表示也可以分为数字、字符串两种。
- 数字表示类型

这个应该是最直接的方式了，客户端和服务端共同维护某个API下、某个数据类型中的type常量，靠文档约束。

- 字符串表示类型

数字的类型毕竟不利于直观阅读，如果可以的话，用字符串也是不错的，

####完整的URL
API里面的数据也会有URL类型的，一般来说如用户的头像、各种图片、音频等资源，都是以URL链接的形式返回的。

返回的URL一定要“完整”，主要指的是不要忘记URL里面的协议部分，也就是scheme部分。

像tutuge.me/imgs/1.jpg这种URL值，就是不完整的，没有指明网络协议，难道靠猜=。=
应该是http://tutuge.me/imgs/1.jpg。


####java代码实现
```

package model;
 
import java.io.Serializable;
import java.util.Map;
 
/**
 * @author dwei
 * @date 2019/3/24
 *
 */
public class ResponseResult implements Serializable{
 
    private static final long serialVersionUID = -999362211833007653L;
 
    /**
     * 是否成功，成功找data、失败找msg
     */
    protected final boolean success;
 
    /**
     * 返回结果编码，成功的话我喜欢设为0
     */
    protected final int code;
 
    /**
     * 返回消息，一般放置可追溯的错误消息
     */
    protected final String msg;
 
    /**
     * 返回数据
     */
    protected final Object data;
 
    /**
     * 额外参数,不使用Map<String, Object>防止JSON解析时过于复杂
     */
    protected final Map<String, String> extra;
 
    private ResponseResult(boolean success, int code, String msg, Object data, Map<String, String> extra) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data=data;
        this.extra = extra;
    }
    
    public static ResponseResult fail(int code, String msg, Map<String, String> extra){
        return new ResponseResult(false,code,msg,null,extra);
    }
 
    public static ResponseResult success(Object data, Map<String, String> extra){
        return new ResponseResult(true,0,"",data,extra);
    }
```


```
public enum ResponseEnums {
 
     SUCCESS(200,"成功"),
     NO_PERMISSION(211,"权限不足"),
     SERVER_ERROR(10000,"服务器异常"),
     AUTH_ERROR(10001,"认证失败"),
     PARAMS_ERROR(10002,"参数错误"),
     JSON_PARSE_ERROR(10003,"Json解析错误"),
     ILLEAGAL_STRING(15001,"非法字符串"),
     UNKNOW_ERROR(16000,"未知错误");
     
      /* 成功状态码 */
         SUCCESS(0, "成功"),
     
         /* 参数错误：10001-19999 */
         PARAM_IS_INVALID(10001, "参数无效"),
         PARAM_IS_BLANK(10002, "参数为空"),
         PARAM_TYPE_BIND_ERROR(10003, "参数类型错误"),
         PARAM_NOT_COMPLETE(10004, "参数缺失"),
     
         /* 用户错误：20001-29999*/
         USER_NOT_LOGGED_IN(20001, "用户未登录"),
         USER_LOGIN_ERROR(20002, "账号不存在或密码错误"),
         USER_ACCOUNT_FORBIDDEN(20003, "账号已被禁用"),
         USER_NOT_EXIST(20004, "用户不存在"),
         USER_HAS_EXISTED(20005, "用户已存在"),
     
         /* 业务错误：30001-39999 */
         SPECIFIED_QUESTIONED_USER_NOT_EXIST(30001, "某业务出现问题"),
     
         /* 系统错误：40001-49999 */
         SYSTEM_INNER_ERROR(40001, "系统繁忙，请稍后重试"),
     
         /* 数据错误：50001-599999 */
         RESULE_DATA_NONE(50001, "数据未找到"),
         DATA_IS_WRONG(50002, "数据有误"),
         DATA_ALREADY_EXISTED(50003, "数据已存在"),
     
         /* 接口错误：60001-69999 */
         INTERFACE_INNER_INVOKE_ERROR(60001, "内部系统接口调用异常"),
         INTERFACE_OUTTER_INVOKE_ERROR(60002, "外部系统接口调用异常"),
         INTERFACE_FORBID_VISIT(60003, "该接口禁止访问"),
         INTERFACE_ADDRESS_INVALID(60004, "接口地址无效"),
         INTERFACE_REQUEST_TIMEOUT(60005, "接口请求超时"),
         INTERFACE_EXCEED_LOAD(60006, "接口负载过高"),
     
         /* 权限错误：70001-79999 */
         PERMISSION_NO_ACCESS(70001, "无访问权限");

 
	SYSTEM_ERROR("-001","系统异常"),
	BAD_REQUEST("-002","错误的请求参数"),
	NOT_FOUND("-003","找不到请求路径！"),
	CONNECTION_ERROR("-004","网络连接请求失败！"),
	METHOD_NOT_ALLOWED("-005","不合法的请求方式"),
	DATABASE_ERROR("-004","数据库异常"),
	BOUND_STATEMENT_NOT_FOUNT("-006","找不到方法！"),
	REPEAT_REGISTER("001","重复注册"),
	NO_USER_EXIST("002","用户不存在"),
	INVALID_PASSWORD("003","密码错误"),
	NO_PERMISSION("004","非法请求！"),
	SUCCESS_OPTION("005","操作成功！"),
	NOT_MATCH("-007","用户名和密码不匹配"),
	FAIL_GETDATA("-008","获取信息失败"),
	BAD_REQUEST_TYPE("-009","错误的请求类型"),
	INVALID_MOBILE("010","无效的手机号码"),
	INVALID_EMAIL("011","无效的邮箱"),
	INVALID_GENDER("012","无效的性别"),
	REPEAT_MOBILE("014","已存在此手机号"),
	REPEAT_EMAIL("015","已存在此邮箱地址"),
	NO_RECORD("016","没有查到相关记录"),
	LOGIN_SUCCESS("017","登陆成功"),
	LOGOUT_SUCCESS("018","已退出登录"),
	SENDEMAIL_SUCCESS("019","邮件已发送，请注意查收"),
	EDITPWD_SUCCESS("020","修改密码成功"),
	No_FileSELECT("021","未选择文件"),
	FILEUPLOAD_SUCCESS("022","上传成功"),
	NOLOGIN("023","未登陆"),
	ILLEGAL_ARGUMENT("024","参数不合法"),
	ERROR_IDCODE("025","验证码不正确");
 
	private String code;
	private String msg;
	
	private UnicomResponseEnums(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
```

```
public class ResultBuilder {

    //成功，不返回具体数据
    public static <T> AppResult<T> successNoData(ResultCode code){
        AppResult<T> result = new AppResult<T>();
        result.setCode(code.getCode());
        result.setMsg(code.getMsg());
        return result;
    }
    //成功，返回数据
    public static <T> AppResult<T> success(T t,ResultCode code){
        AppResult<T> result = new AppResult<T>();
        result.setCode(code.getCode());
        result.setMsg(code.getMsg());
        result.setData(t);
        return result;
    }

    //失败，返回失败信息
    public static <T> AppResult<T> faile(ResultCode code){
        AppResult<T> result = new AppResult<T>();
        result.setCode(code.getCode());
        result.setMsg(code.getMsg());
        return result;
    }
}
```

------------------------------------
------------------------------------


普通格式
```
{
  "code": "1001",
  "msg": null,
  "data": "waht",
  "extera": null
  "is_redirect": true,
  "redirect_url": "http://baidu.com"
}
```

带页码的格式
```
{
  "code": "0000",
  "msg": "error",
  "data": {
    "list": null,
    "pagebar": {
      "page": 1,
      "total": 2,
      "limit": 10
    }
  },
  "extera": null,
  "is_redirect": true,
  "redirect_url": "http://qq.com"
}
```
样例代码
```
@RequestMapping("/get")
    public Result<String> get(@RequestParam(value = "msg", required = false) String msg) {
        return Result.success(msg).orFail("error")
                .redirect("http://baidu.com").orFailRedirect("http://qq.com");
    }

    @RequestMapping("/page")
    public PageResult<String> page() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        return PageResult.success(list).pageBar(1, 10).total(2)
                .orFail("error").orFailRedirect("http://qq.com");
    }
```
字段解释
```
    /**
     * 错误信息
     */
    private String msg;
    /**
     * 状态码 1成功 其它失败，失败需要返回msg
     */
    private int code;

    /**
     * 是否跳转
     */
    private boolean is_redirect = false;

    /**
     * 跳转地址
     */
    private String redirect_url;

    /**
     * extera
     */
    private String extera;
```
Result 普通返回类设计
```
package org.vergil.pojo;

import java.util.Objects;

/**
 * @author: dwei
 * @Date: 2018/4/20
 * @Description:
 */
public class Result<T> {

    private final static Result<?> EMPTY = new Result<>();

    
    
     /**
      * 状态码 1成功 其它失败，失败需要返回msg
      */
    private int code;

    /**
     * 错误信息
     */
    private String msg;
  
    private T data;

    /**
     * 是否跳转
     */
    private boolean is_redirect = false;

    /**
     * 跳转地址
     */
    private String redirect_url;

    /**
     * extera
     */
    private String extera;

    public T getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }

    public int getState() {
        return code;
    }

    public boolean isIs_redirect() {
        return is_redirect;
    }

    public String getRedirect_url() {
        return redirect_url;
    }

    public String getExtera() {
        return extera;
    }

    private Result() {
        this.data = null;
    }

    private Result(String message, int code) {
        this.msg = message;
        this.code = code;
    }

    private Result(T data, int code) {
        this.data = data;
        this.code = code;
    }

    /**
     *
     * 功能描述: 创建一个空Result类
     * @auther: dwei
     * @date: 2018/4/21
     */
    public static <T> Result<T> empty() {
        @SuppressWarnings("unchecked")
        Result<T> t = (Result<T>) EMPTY;
        return t;
    }

    /**
     *
     * 功能描述: 生成一个成功状态Result类
     * @auther: dwei
     * @param: Data
     * @return: Result<T>
     * @date: 2018/4/21
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(data, 1);
    }

    /**
     *
     * 功能描述: 生成一个成功状态Result类
     * @auther: dwei
     * @param: 返回的错误信息
     * @return: Result<T>
     * @date: 2018/4/21
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(message, 0);
    }

    /**
     *
     * 功能描述: 设置跳转地址
     * @auther: dwei
     * @param: 跳转地址
     * @return: Result<T>
     * @date: 2018/4/21
     */
    public <T> Result<T> redirect(String url) {
        this.is_redirect = true;
        this.redirect_url = url;
        return (Result<T>) this;
    }

    /**
     *
     * 功能描述: 设置失败状态跳转地址
     * @auther: dwei
     * @param: 跳转地址
     * @return: Result<T>
     * @date: 2018/4/21
     */
    public <T> Result<T> orFailRedirect(String url) {
        if (code == 1) {
            return (Result<T>) this;
        }
        this.is_redirect = true;
        this.redirect_url = url;
        return (Result<T>) this;
    }

    /**
     *
     * 功能描述: 判断是否传入值是否为空,非空则返回值，为空则返回失败信息
     * @auther: dwei
     * @param: 返回的错误信息
     * @return: Result<T>
     * @date: 2018/4/21
     */
    public <T> Result<T> orFail(String message) {
        if (null != data) {
            return (Result<T>) this;
        } else {
            this.msg = message;
            this.code = 0;
        }
        return (Result<T>) this;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(data);
    }


    @Override
    public String toString() {
        return data != null
                ? String.format("result[%s]", data)
                : "result.empty";
    }
}
```

带页码类设计
```
package org.vergil.pojo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author: dwei
 * @Date: 2018/4/20
 * @Description:
 */
public class PageResult<T> {

    private final static PageResult<?> EMPTY = new PageResult<>();

    private Data<T> data;

    /**
     * 错误信息
     */
    private String msg;
    /**
     * 状态码 1成功 其它失败，失败需要返回msg
     */
    private int code;

    /**
     * 是否跳转
     */
    private boolean is_redirect = false;

    /**
     * 跳转地址
     */
    private String redirect_url;

    /**
     * extera
     */
    private String extera;

    public Data<T> getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }

    public int getState() {
        return dwei;
    }

    public boolean isIs_redirect() {
        return is_redirect;
    }

    public String getRedirect_url() {
        return redirect_url;
    }

    public String getExtera() {
        return extera;
    }

    private PageResult() {
        this.data = null;
    }

    private PageResult(String message, int dwei) {
        this.msg = message;
        this.code = code;
    }

    private PageResult(List<T> list, int code) {
        Data<T> data = new Data<>(list);
        this.data = data;
        this.code = code;
    }

    /**
     * 功能描述: 创建一个空PageResult类
     *
     * @auther: dwei
     * @date: 2018/4/21
     */
    public static <T> PageResult<T> empty() {
        @SuppressWarnings("unchecked")
        PageResult<T> t = (PageResult<T>) EMPTY;
        return t;
    }

    /**
     * 功能描述: 生成一个成功状态PageResult类
     *
     * @auther: dwei
     * @param: List<T>
     * @return: PageResult<T>
     * @date: 2018/4/21
     */
    public static <T> PageResult<T> success(List<T> data) {
        return new PageResult<>(data, 1);
    }

    /**
     * 功能描述: 生成一个失败状态PageResult类
     *
     * @auther: dwei
     * @param: 错误信息
     * @return: PageResult<T>
     * @date: 2018/4/21
     */
    public static <T> PageResult<T> fail(String message) {
        return new PageResult<>(message, 0);
    }

    /**
     * 功能描述: 设置页码信息
     *
     * @auther: dwei
     * @param: pageNum, pageSize
     * @return: PageResult<T>
     * @date: 2018/4/21
     */
    public <T> PageResult<T> pageBar(int pageNum, int pageSize) {
        if (code == 1) {
            data.pageBar(pageNum, pageSize);
        }
        return (PageResult<T>) this;
    }

    /**
     * 功能描述: 设置总数
     *
     * @auther: dwei
     * @param: total
     * @return: PageResult<T>
     * @date: 2018/4/21
     */
    public <T> PageResult<T> total(int total) {
        if (data.pagebar != null) {
            data.pagebar.total(total);
        }
        return (PageResult<T>) this;
    }

    /**
     * 功能描述: 设置跳转地址
     *
     * @auther: dwei
     * @param: 跳转地址
     * @return: PageResult<T>
     * @date: 2018/4/21
     */
    public <T> PageResult<T> redirect(String url) {
        this.is_redirect = true;
        this.redirect_url = url;
        return (PageResult<T>) this;
    }

    /**
     * 功能描述: 设置失败跳转地址
     *
     * @auther: dwei
     * @param: 跳转地址
     * @return: PageResult<T>
     * @date: 2018/4/21
     */
    public <T> PageResult<T> orFailRedirect(String url) {
        if (code == 1) {
            return (PageResult<T>) this;
        }
        this.is_redirect = true;
        this.redirect_url = url;
        return (PageResult<T>) this;
    }

    /**
     * 功能描述: 判断是否传入值是否为空,非空则返回值，为空则返回失败信息
     *
     * @auther: dwei
     * @param: 返回的错误信息
     * @return: PageResult<T>
     * @date: 2018/4/21
     */
    public <T> PageResult<T> orFail(String message) {
        if (null != data.list) {
            return (PageResult<T>) this;
        } else {
            this.msg = message;
            this.code = 0;
        }
        return (PageResult<T>) this;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(data);
    }


    @Override
    public String toString() {
        return data != null
                ? String.format("result[%s]", data)
                : "result.empty";
    }

    public static class Data<T> {
        /**
         * 接口数据
         */
        private List<T> list;

        /**
         * 分页信息
         */
        private PageBar pagebar;

        private Data(List<T> list) {
            this.list = list;
        }

        public List<T> getList() {
            return list;
        }

        public PageBar getPagebar() {
            return pagebar;
        }

        public void pageBar(int pageNum, int pageSize) {
            PageBar pageBar = new PageBar(pageNum, pageSize);
            this.pagebar = pageBar;
        }

        public static class PageBar {
            private int page;
            private int total;
            private int limit;

            private PageBar(int page, int limit) {
                this.page = page;
                this.limit = limit;
            }

            private void total(int total) {
                this.total = total;
            }

            public int getPage() {
                return page;
            }

            public int getTotal() {
                return total;
            }

            public int getLimit() {
                return limit;
            }
        }
    }
}
```