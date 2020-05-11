package com.example.cameratest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener{

    private ImageView picture;//(视频帧数据预览)
    private TextureView textureView;//摄像头预览
    private Camera mCamera;//控制打开那个相机
    private Button GetVideoStreamBack,GetVideoStreamFront;
    private static String[] PERMISSIONS_STORAGE = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//写权限
            Manifest.permission.CAMERA//照相权限
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //定义后置摄像头的按钮
        GetVideoStreamBack=(Button) findViewById(R.id.take_photo_BackCamera);
        GetVideoStreamFront=(Button) findViewById(R.id.take_photo_FrontCamera);
        //定义照片实例用于显示图片(视频帧数据预览)
        picture =(ImageView) findViewById(R.id.iv_pic_back);
        //摄像头预览
        textureView=(TextureView) findViewById(R.id.texture_view_back);
        textureView.setRotation(90); // // 设置预览角度，并不改变获取到的原始数据方向(与Camera.setDisplayOrientation(0)

        //用于判断SDK版本是否大于23
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            //检查权限
            int i = ContextCompat.checkSelfPermission(this,PERMISSIONS_STORAGE[0]);
            //如果权限申请失败，则重新申请权限
            if(i!= PackageManager.PERMISSION_GRANTED){
                //重新申请权限函数
                startRequestPermission();
                Log.e("这里","权限请求成功");
            }
        }

        //查看摄像头的个数
        initData();

        //定义按钮点击事件
        GetVideoStreamBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                // 打开相机 0后置 1前置
//                mCamera = Camera.open(0);
                //定义捕获相机的视频流
                addCallBack();
            }
        });
        GetVideoStreamFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 打开相机 0后置 1前置
                mCamera = Camera.open(1);
            }
        });

        textureView.setSurfaceTextureListener(this);


    }


    //////****************定义一系列函数***************///////////
    private void initData() {
        int numberOfCameras = Camera.getNumberOfCameras();// 获取摄像头个数
        if(numberOfCameras<1){
            Toast.makeText(this, "没有相机", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    //调用后置摄像头函数
    private void addCallBack() {
        if (mCamera!=null){
            mCamera.setPreviewCallback(new Camera.PreviewCallback(){
                //预览
                //通过Android Camera拍摄预览中设置setPreviewCallback实现onPreviewFrame接口，实时截取每一帧视频流数据
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size=camera.getParameters().getPreviewSize();
                    try{
                        YuvImage image=new YuvImage(data, ImageFormat.NV21,size.width,size.height,null);
                        if(image!=null){
                            ByteArrayOutputStream stream =new ByteArrayOutputStream();
                            //将摄像头预览回调的每一帧Nv21数据通过jpeg压缩
                            image.compressToJpeg(new Rect(0,0,size.width,size.height),80,stream);
                            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());//解码
                            picture.setImageBitmap(bmp);
                            stream.close();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    //用TextureView预览Camera
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // 打开相机 0后置 1前置
        mCamera = Camera.open(0);
        if (mCamera != null) {
            // 设置相机预览宽高，此处设置为TextureView宽高
            Camera.Parameters params = mCamera.getParameters();
//            params.setPreviewSize(width, height);
            // 设置自动对焦模式
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera.setParameters(params);
            }
            try {
//                mCamera.setDisplayOrientation(0);// 设置预览角度，并不改变获取到的原始数据方向
                // 绑定相机和预览的View
                mCamera.setPreviewTexture(surface);
                // 开始预览
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startRequestPermission(){
        //321为请求码
        ActivityCompat.requestPermissions(this,PERMISSIONS_STORAGE,321);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return false;
    }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}


}
