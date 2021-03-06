package com.example.jinphy.mylooklook.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.example.jinphy.mylooklook.exception.ConditionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jinphy on 2017/7/26.
 */

public class FileUtils {

    public static final String FILE_SEPARATOR_DOT = ".";
    public static final String PREFIX_IMAGE = "IMG";
    public static final String PREFIX_AUDIO = "AUD";
    public static final String PREFIX_VIDEO = "VID";

    private FileUtils() {
    }

    /**
     * 生成一个文件，更多信息请参考
     *
     * @param absolutePath    文件的绝对路径，包括目录和文件名
     * @param overrideIfExist 标志是否覆盖已存在的文件
     * @return 生成的文件
     * @see FileUtils#createFile(String, String, boolean)
     */
    public static File createFile(@NonNull String absolutePath, boolean overrideIfExist) {
        if (overrideIfExist) {
            return createFile(absolutePath);
        } else {
            if (TextUtils.isEmpty(absolutePath) && !absolutePath.contains(File.separator)) {
                return null;
            } else {
                int index = absolutePath.lastIndexOf(File.separator);
                String parent = absolutePath.substring(0, index + 1);
                String file = absolutePath.substring(index + 1);
                return createFile(parent, file, overrideIfExist);
            }
        }
    }


    /**
     * 该函数生成一个文件，更多信息请参见
     *
     * @param parent          文件所在的目录
     * @param file            文件名，包括前后缀
     * @param overrideIfExist 标志是否覆盖已存在的文件
     * @return 生成的文件
     * @see FileUtils#createFile(String, String, String, boolean)
     */
    public static File createFile(
            @NonNull String parent,
            @NonNull String file,
            boolean overrideIfExist) {

        String prefix = getPrefix(file);
        String suffix = getSuffix(file);


        return createFile(parent, prefix, suffix, overrideIfExist);

    }

    /**
     * 该方法生成一个文件，如果成功则返回生成的文件，否则返回null
     * 参数overrideIfExist标志是否覆盖已存在的文件，如果为true
     * 则新文件将覆盖已存在的同名文件，否则按照重名次数迭代出不同名的文件
     *
     * @param parent          文件所在目录
     * @param prefix          文件名前缀
     * @param suffix          文件名后缀
     * @param overrideIfExist 标志是否覆盖已存在的文件
     * @return 生成的文件
     */
    public static File createFile(
            @NonNull String parent,
            @NonNull String prefix,
            @NonNull String suffix, boolean overrideIfExist) {
        try {
            checkNonNull(parent);
            checkNonNull(prefix);
            checkNonNull(suffix);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        parent = handleFileNameParent(parent);
        prefix = handleFileNamePrefix(prefix);
        suffix = handleFileNameSuffix(suffix);

        String path = parent + prefix + suffix;

        if (!overrideIfExist) {
            // 不覆盖已经存在的文件，所以按次序迭代
            path = generateUnRepeatFileName(parent, prefix, suffix);
        }
        return createFile(path);
    }

    /**
     * 生成一个独一无二的文件名，在文件名中，前缀后面加上创建文件是的时间戳，
     * 例如,/storage/emulate/0/temp/IMG_15654564161312.jpg
     *
     * @param parent 图片文件所在的目录
     * @param prefix 图片文件前椎名，例如 IMG
     * @param suffix 图片文件后缀名，例如 .jpg
     * @return 返回生成的图片文件
     */
    public static File createImageFile(
            @NonNull String parent,
            String prefix,
            @NonNull String suffix) {

        prefix = wrapPrefix(prefix, PREFIX_IMAGE);

        return createFile(parent, prefix, suffix, true);
    }


    /**
     * 生成一个独一无二的文件名，在文件名中，前缀后面加上创建文件是的时间戳，
     * 例如,/storage/emulate/0/temp/AUD_15654564161312.mp3
     *
     * @param parent 音频文件所在的目录
     * @param prefix 音频文件前椎名，例如 AUD
     * @param suffix 音频文件后缀名，例如 .mp3
     * @return 返回生成的音频文件
     */
    public static File createAudioFile(
            @NonNull String parent,
            String prefix,
            @NonNull String suffix) {

        prefix = wrapPrefix(prefix, PREFIX_AUDIO);

        return createFile(parent, prefix, suffix, true);
    }


    /**
     * 生成一个独一无二的文件名，在文件名中，前缀后面加上创建文件是的时间戳，
     * 例如,/storage/emulate/0/temp/VID_15654564161312.mp4
     *
     * @param parent 视频文件所在的目录
     * @param prefix 视频文件前椎名，例如 VID
     * @param suffix 视频文件后缀名，例如 .mp4
     * @return 返回生成的视频文件
     */
    public static File createVideoFile(
            @NonNull String parent,
            String prefix,
            @NonNull String suffix) {

        prefix = wrapPrefix(prefix, PREFIX_VIDEO);

        return createFile(parent, prefix, suffix, true);
    }

    /**
     *
     * @param context 上下文
     * @param subDir 缓存目录下的子目录
     * @return 返回生成的缓存目录文件
     * */
    public static File getCacheDir(Context context ,String subDir){
        File cache = context.getCacheDir();
        File out = new File(cache, subDir);
        out.mkdirs();
        return out;
    }


    //--------------------图片相关----------------------------------------------------


    /**
     * 将ImageV中的图片保存到指定的文件中
     * @param context 上下文
     * @param bitmap 要保存的图片位图
     * @param target 目标文件
     * @param addToGallery 标志是否需要将保存的图片添加到相册中
     * @return 图片保存成功则返回true，否则返回false
     * */
    public static boolean saveImage(
            @NonNull Context context,
            @NonNull Bitmap bitmap,
            @NonNull File target,
            boolean addToGallery) {
        try {
            // 获取文件流
            FileOutputStream out = new FileOutputStream(target);

            // 首先把图片写入文件
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            // 检测是否需要将保存的文件添加到相册
            check(addToGallery);

            // 其次把文件插入到系统图库
            MediaStore.Images.Media.insertImage(
                    context.getContentResolver(), target.getAbsolutePath(), "", "");

            // 最后通知图库更新
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
                    .fromFile(target));
            context.getApplicationContext().sendBroadcast(intent);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }catch (ConditionException e){
            return true;
        }
    }




    /**
     * 将ImageV中的图片保存到指定的文件中
     * @param context 上下文
     * @param bitmap 要保存的图片位图
     * @param file 目标文件路径
     * @param addToGallery 标志是否需要将保存的图片添加到相册中
     * @return 图片保存成功则返回true，否则返回false
     * */
    public static boolean saveImage(
            @NonNull Context context,
            @NonNull Bitmap bitmap,
            @NonNull String file,
            boolean addToGallery){

        // 获取要图片要存放的目标文件文件
        File target = FileUtils.createFile(file);

        // 保存图片
        return saveImage(context, bitmap, target, addToGallery);
    }

    /**
     * 将图片保存到指定的文件中
     * @param context 上下文
     * @param view 存放要保存的图片的ImageView
     * @param target 目标文件
     * @param addToGallery 标志是否需要将保存的图片添加到相册中
     * @return 图片保存成功则返回true，否则返回false
     * */
    public static boolean saveImage(
    @NonNull Context context,
    @NonNull ImageView view,
    @NonNull File target,
    boolean addToGallery){
        // 获取ImageView中的的Bitmap
        Bitmap bitmap = getBitmapFromImageView(view);

        // 保存bitmap
        return saveImage(context,bitmap,target,addToGallery);
    }


    /**
     * 将图片保存到指定的文件中
     * @param context 上下文
     * @param view 存放要保存的图片的ImageView
     * @param file 目标文件
     * @param addToGallery 标志是否需要将保存的图片添加到相册中
     * @return 图片保存成功则返回true，否则返回false
     * */
    public static boolean saveImage(
            @NonNull Context context,
            @NonNull ImageView view,
            @NonNull String file,
            boolean addToGallery){
        // 获取ImageView中的的Bitmap
        Bitmap bitmap = getBitmapFromImageView(view);

        // 创建图片所存放的文件
        File target = FileUtils.createFile(file);

        // 保存bitmap
        return saveImage(context,bitmap,target,addToGallery);
    }




    public static Bitmap getBitmapFromImageView(@NonNull ImageView view){
        try {
            checkNull(view);

            view.setDrawingCacheEnabled(true);
            return view.getDrawingCache();

        }catch (NullPointerException e){
            return null;
        }
    }


    //=============================================================================
    //-----------------------------------------------------------------------------
    // 根据文件的绝对路径生成一个文件，如果成功则返回生成的文件，否则返回null
    private static File createFile(@NonNull String absolutePath) {

        if (TextUtils.isEmpty(absolutePath)) {
            return null;
        }

        File file = new File(absolutePath);
        if (file.getParentFile().mkdirs() || file.getParentFile().exists()) {

            // 判断是否能够生成文件所在的目录
            try {
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();

                return file;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }


    private static void check(boolean condition)throws ConditionException{
        if (condition) {
            return;
        }
        throw new ConditionException();
    }

    private static void checkNull(Object object)throws  NullPointerException{
        if (object==null){
            throw new NullPointerException();
        }
    }


    //生成不重名的文件名
    private static String generateUnRepeatFileName(String parent, String prefix, String suffix) {

        int repeats = 1;
        File file = new File(parent + prefix + suffix);

        while (file.exists()) {
            file = new File(parent + prefix + "(" + (repeats++) + ")" + suffix);
        }
        return file.getAbsolutePath();
    }

    // 获取文件名前缀
    private static String getPrefix(String fileName) {
        if (fileName == null) {
            return null;
        }
        fileName = fileName.trim();
        int index = fileName.lastIndexOf(FILE_SEPARATOR_DOT);
        if (index > 0 && index < fileName.length() - 1) {
            return fileName.substring(0, index);
        }
        return fileName;
    }


    // 获取文件名后缀
    private static String getSuffix(String fileName) {
        if (fileName == null) {
            return null;
        }
        fileName = fileName.trim();
        int index = fileName.lastIndexOf(FILE_SEPARATOR_DOT);
        if (index > 0 && index < fileName.length() - 1) {
            return fileName.substring(index);
        }
        return "";
    }

    // 测试不为空（包括null和空串）
    private static void checkNonNull(String str) throws Exception {
        if (TextUtils.isEmpty(str)) {
            throw new Exception("the string is null or empty");
        }
    }

    // 处理文件的父目录
    private static String handleFileNameParent(String parent) {
        parent = parent.trim();
        return parent.endsWith(File.separator) ? parent : parent + File.separator;
    }

    // 处理文件名前缀
    private static String handleFileNamePrefix(String prefix) {
        prefix = prefix.trim();
        while (prefix.endsWith(FileUtils.FILE_SEPARATOR_DOT)) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }

    // 处理文件名后缀
    private static String handleFileNameSuffix(String suffix) {
        suffix = suffix.trim();
        int index = suffix.lastIndexOf(FILE_SEPARATOR_DOT);
        if (index == suffix.length() - 1) {
            suffix = FILE_SEPARATOR_DOT + suffix.substring(0, suffix.length() - 1);
        } else if (index < 0) {
            suffix = FILE_SEPARATOR_DOT + suffix;
        } else {
            suffix = suffix.substring(index, suffix.length());
        }
        return suffix;

    }

    private static String wrapPrefix(String prefix, String type) {
        if (TextUtils.isEmpty(prefix)) {
            prefix = type + "_";
        } else if (!prefix.endsWith("_")) {
            prefix += "_";
        }
        prefix += System.nanoTime();

        return prefix;
    }

}
