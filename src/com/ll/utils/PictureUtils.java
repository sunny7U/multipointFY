package com.ll.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONStringer;

import com.ll.map.LocCollectingActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class PictureUtils {
	private String TAG=PictureUtils.class.getName();
	
	/**
	 * 将指定路径的图片上传
	 */
    public void toUploadImage(String imagePath){
        Log.d(TAG,"toUploadImage");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] appicon = baos.toByteArray();
        String imageStr = Base64.encodeToString(appicon, Base64.DEFAULT);
        try{
            JSONStringer json = new JSONStringer();
            json.object();
            json.key("image");
            json.value(imageStr);
            json.endObject();
            Log.d("aaa","image:"+json.toString());
            String result = HttpUtils.post(json.toString());
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    /**
     * 发起照相intent并将图片存储在指定文件中
     */
    private void dispatchTakePictureIntent(Context context){
        //intent本身，传递执行目的
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //resolveActivity()会返回能处理该intent的第一个Activity，ensure that there's a camera activity to handle the intent
        if(takePictureIntent.resolveActivity(context.getPackageManager()) != null){
            // Create the File where the photo should go
            File photoFile = null;
            try{
                photoFile = createImageFileInPublic();
            }catch(IOException ex){
                //Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if(photoFile != null){
                //将图片uri存放在key为output的Extra中
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, Constants.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * 在外存储中创建用于存储照片的File（会保存全尺寸照片到给定路径）
     * @throws IOException
     */
    private File createImageFileInPublic() throws IOException{
        //Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //getExternalStoragePublicDirectory()方法提供的目录可被所有应用所共享，需在manifest中声明写权限
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);   //DIRECTORY_PICTURES参数可以使函数返回适用于存储公共图片的目录
        File image = File.createTempFile(
                imageFileName,    /* prefix */
                ".jpg",           /* suffix */
                storageDir        /* directory */
        );
//        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * 从文件中加载图片。通过缩放图片到目标视图尺寸后再载入到内存，来降低内存的使用
     * @param imagePath 待显示的图片路径
     * @param view  
     * @return  待显示的图片的bitmap对象
     */
    public Bitmap loadImageFromFile(String imagePath, ImageView view){
    	view.setVisibility(View.VISIBLE);
        //Get the dimensions of the View
        int targetW = view.getWidth();
        int targetH = view.getHeight();

        //Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        //Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        //Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        return bitmap;
    }
}
