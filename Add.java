package com.example.note;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.String;
import java.security.Permissions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Add extends AppCompatActivity implements OnClickListener{
    String Title,Content,simpleDate,photo;
    Button ButtonAddCancel,ButtonAddSave;
    EditText EditTextAddTitle,EditTextAddContent,EditTextAddAuthor;
    String Author;

    public static final int TAKE_PHOTO = 1;
    private ImageView picture;
    private Uri imageUri;


    public static String imagePath =null;//定义一个全局变量，把图片路径变为string保存到数据库中
    public static final int CHOOSE_PHOTO = 2;

    public void qu(Bitmap bitmap){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        ButtonAddCancel = (Button)findViewById(R.id.ButtonAddCancel);
        ButtonAddSave = (Button)findViewById(R.id.ButtonAddSave);

        EditTextAddContent = findViewById(R.id.EditTextAddContent);
        EditTextAddTitle = findViewById(R.id.EditTextAddTitle);
        EditTextAddAuthor = findViewById(R.id.EditTextAddAuthor);

        ButtonAddCancel.setOnClickListener(this);
        ButtonAddSave.setOnClickListener(this);

        Button takePhoto = (Button) findViewById(R.id.take_photo);
        Button chooseFromAlbum = (Button) findViewById(R.id.choose_from_album);
        picture = (ImageView) findViewById(R.id.picture);

        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
        EditTextAddAuthor.setText(pref.getString("username",""));

//        通过拍照
        takePhoto.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                // 相机权限检查
                if (ContextCompat.checkSelfPermission(Add.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                    File outputImage = new File(getExternalCacheDir(), "output_image.jpg");//创建File对象，用于存储拍照后的照片
                    try {
                        if (outputImage.exists()) {
                            outputImage.delete();
                        }
                        outputImage.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (Build.VERSION.SDK_INT >= 24) {
                        imageUri = FileProvider.getUriForFile(Add.this, "com.example.note", outputImage);
                    } else {
                        //如果Android版本低于7.0，需要调用Uri的formFile（）方法将File对象转换为Uri对象
                        imageUri = Uri.fromFile(outputImage);
                    }
                    imagePath = imageUri.getPath();
                    Log.d("PHOTO", imagePath);
                    //启动相机
                      Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                  /// Intent intent = new Intent();
                    //intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//指定图片的输出地址
                    startActivityForResult(intent, TAKE_PHOTO);//打开相机
                }

            }
        });
        //通过相册
        chooseFromAlbum.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(Add.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Add.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    openAlbum();
                }
            }
        });

        //对照相功能的响应
//        takePhoto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 在新的Intent里面打开，并且传递TAKE_PHOTO选项
//                Intent intent = new Intent();
//                intent.setClass(Add.this, Image_album_showActivity.class);//也可以这样写intent.setClass(MainActivity.this, OtherActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putInt("id", TAKE_PHOTO);//使用显式Intent传递参数，用以区分功能
//                intent.putExtras(bundle);
//
//                Add.this.startActivity(intent);//启动新的Intent
//            }
//        });
//
//        //设置相册选择的响应
//        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 在新的Intent里面打开，并且传递CHOOSE_PHOTO选项
//                Intent intent = new Intent();
//                intent.setClass(Add.this, Image_album_showActivity.class);//也可以这样写intent.setClass(MainActivity.this, OtherActivity.class);
//
//                Bundle bundle = new Bundle();
//                bundle.putInt("id", CHOOSE_PHOTO);
//                intent.putExtras(bundle);
//
//                Add.this.startActivity(intent);
//            }
//        });
//


    }
    //把拍照照片存到相册
        @RequiresApi(api = Build.VERSION_CODES.N)
        public String saveToSystemGallery(Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(getDataDir(), "Mycamera");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
//        try {
//            MediaStore.Images.Media.insertImage(getContentResolver(),
//                    file.getAbsolutePath(), fileName, null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        imagePath = uri.getPath();//把uri格式转换为string类型
        Log.d("PHOTO",file.getPath());

        intent.setData(uri);
        sendBroadcast(intent);// 发送广播，通知图库更新
        return file.getPath();
    }


    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);//打开相册
    }

    @Override
    public void onRequestPermissionsResult(int requstCode,String[] permissions,int[] grantResults) {
        switch (requstCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                }else {
                    Toast.makeText(this,"you denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    //拍照完以后返回到这儿
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {//如果拍照成功
                try {
                    //将拍摄照片显示出来
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));//把这张照片转换为Bitmap对象
                    picture.setImageBitmap(bitmap);//显示出来
                    imagePath = imageUri.getPath();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == CHOOSE_PHOTO) {
            if (Build.VERSION.SDK_INT >= 19) {
                handleImageOnKitKat(data);
            } else {
                handleImageBeforeKitKat(data);
            }
        }

    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        // String imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);

        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }
            else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath); // 根据图片路径显示图片
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);


            picture.setImageBitmap(bitmap);
        }
        else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api=Build.VERSION_CODES.N)

    @Override
    public void onClick(View v){
        MyDataBaseHelper dbHelper = new MyDataBaseHelper(this,"Note.db",null,1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (v.getId()) {
            case R.id.ButtonAddSave:

                String path="";
                if (picture.getDrawable()!=null) {
                    BitmapDrawable bmpDrawable = (BitmapDrawable) picture.getDrawable();
                    Bitmap bitmap = bmpDrawable.getBitmap();
                    path = saveToSystemGallery(bitmap);//将图片保存到本地
                    Toast.makeText(getApplicationContext(), "图片保存成功！", Toast.LENGTH_SHORT).show();
                }

                Date date = new Date();
                DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");        //配置时间格式
                simpleDate = simpleDateFormat.format(date);
                ContentValues values = new ContentValues();
                Title = String.valueOf(EditTextAddTitle.getText());         //获取需要储存的值
                Content = String.valueOf(EditTextAddContent.getText());
                Author=String.valueOf(EditTextAddAuthor.getText());
                if(Title.length()==0){               //标题为空给出提示
                    Toast.makeText(this, "请输入一个标题", Toast.LENGTH_LONG).show();
                }else {
                    values.put("title", Title);
                    values.put("author", Author);
                    values.put("content", Content);
                    values.put("date", simpleDate);
                    Log.d("PHOTO_Write",path);

                    values.put("picture", path);//把图片存到数据库中
                    db.insert("Note", null, values);                 //将值传入数据库中
                    Add.this.setResult(RESULT_OK, getIntent());
                    Add.this.finish();
                }


//                Author = String.valueOf(EditTextAddAuthor.getText());
//                SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
//                editor.putString("author",Author);      //使用sharedperferences设置默认作者
//                editor.apply();
                break;

            case R.id.ButtonAddCancel:
                Add.this.setResult(RESULT_OK,getIntent());
                Add.this.finish();

                break;
        }


    }

}