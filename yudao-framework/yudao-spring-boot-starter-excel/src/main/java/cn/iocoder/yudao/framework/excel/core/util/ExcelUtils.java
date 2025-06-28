package cn.iocoder.yudao.framework.excel.core.util;

import cn.iocoder.yudao.framework.excel.core.handler.SelectSheetWriteHandler;
import cn.iocoder.yudao.framework.excel.core.convert.BigDecimalConvert;
import cn.iocoder.yudao.framework.excel.core.convert.DoubleConvert;
import cn.iocoder.yudao.framework.excel.core.convert.IntegerConvert;
import cn.iocoder.yudao.framework.excel.core.convert.LocalDateTimeConvert;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.converters.bigdecimal.BigDecimalStringConverter;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Excel 工具类
 *
 * @author 芋道源码
 */
public class ExcelUtils {

    private static final Logger log = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * 将列表以 Excel 响应给前端
     *
     * @param response  响应
     * @param filename  文件名
     * @param sheetName Excel sheet 名
     * @param head      Excel head 头
     * @param data      数据列表哦
     * @param <T>       泛型，保证 head 和 data 类型的一致性
     * @throws IOException 写入失败的情况
     */
    public static <T> void write(HttpServletResponse response, String filename, String sheetName,
                                 Class<T> head, List<T> data) throws IOException {
        // 输出 Excel
        EasyExcel.write(response.getOutputStream(), head)
                .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 基于 column 长度，自动适配。最大 255 宽度
                .registerWriteHandler(new SelectSheetWriteHandler(head)) // 基于固定 sheet 实现下拉框
                .registerConverter(new LongStringConverter()) // 避免 Long 类型丢失精度
                .registerConverter(new BigDecimalStringConverter())
                .sheet(sheetName).doWrite(data);
        // 设置 header 和 contentType。写在最后的原因是，避免报错时，响应 contentType 已经被修改了
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8.name()));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
    }

    public static <T> List<T> read(MultipartFile file, Class<T> head) throws IOException {
        return EasyExcel.read(file.getInputStream(), head, null)
                .autoCloseStream(false)  // 不要自动关闭，交给 Servlet 自己处理
                .registerConverter(new IntegerConvert()) // 注册自定义转换器
                .registerConverter(new BigDecimalConvert())
                .registerConverter(new DoubleConvert())
                .registerConverter(new LocalDateTimeConvert())
                .doReadAllSync();
    }

    // 新增InputStream参数的read方法
    public static <T> List<T> read(InputStream inputStream, Class<T> head) throws IOException {
        return EasyExcel.read(inputStream, head, null)
                .autoCloseStream(false)
                .registerConverter(new IntegerConvert()) // 注册自定义转换器
                .registerConverter(new BigDecimalConvert())
                .registerConverter(new DoubleConvert())
                .registerConverter(new LocalDateTimeConvert())
                .doReadAllSync();
    }

    /**
     * 带监听器的Excel读取方法
     *
     * @param inputStream 输入流
     * @param head        数据类型
     * @param listener    读取监听器
     * @param <T>         泛型
     * @return 读取的数据列表
     * @throws IOException 读取异常
     */
    public static <T> List<T> read(InputStream inputStream, Class<T> head, ReadListener<T> listener) throws IOException {
        log.info("[ExcelUtils] 开始读取Excel文件，类: {}", head.getSimpleName());
        
        // 创建转换器实例并添加调试日志
        IntegerConvert integerConvert = new IntegerConvert();
        BigDecimalConvert bigDecimalConvert = new BigDecimalConvert();
        DoubleConvert doubleConvert = new DoubleConvert();
        LocalDateTimeConvert localDateTimeConvert = new LocalDateTimeConvert();
        
        log.info("[ExcelUtils] 转换器实例创建完成");
        
        return EasyExcel.read(inputStream, head, listener)
                .autoCloseStream(false)  // 不要自动关闭，交给调用方处理
                .registerConverter(new LongStringConverter()) // 避免 Long 类型丢失精度
                .registerConverter(new BigDecimalStringConverter())
                .registerConverter(integerConvert) // 注册自定义转换器
                .registerConverter(bigDecimalConvert)
                .registerConverter(doubleConvert)
                .registerConverter(localDateTimeConvert)
                .doReadAllSync();
    }

    /**
     * 带监听器的Excel读取方法（MultipartFile版本）
     *
     * @param file     文件
     * @param head     数据类型
     * @param listener 读取监听器
     * @param <T>      泛型
     * @return 读取的数据列表
     * @throws IOException 读取异常
     */
    public static <T> List<T> read(MultipartFile file, Class<T> head, ReadListener<T> listener) throws IOException {
        return EasyExcel.read(file.getInputStream(), head, listener)
                .autoCloseStream(false)
                .registerConverter(new IntegerConvert()) // 注册自定义转换器
                .registerConverter(new BigDecimalConvert())
                .registerConverter(new DoubleConvert())
                .registerConverter(new LocalDateTimeConvert())
                .doReadAllSync();
    }

}
