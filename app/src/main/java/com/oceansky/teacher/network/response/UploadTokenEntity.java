package com.oceansky.teacher.network.response;

import com.google.gson.annotations.SerializedName;

/**
 * User: dengfa
 * Date: 16/8/1
 * Tel:  18500234565
 */
public class UploadTokenEntity {
    @SerializedName("upload_token")
    String upload_token;
    @SerializedName("file_name")
    String file_name;

    public String getUpload_token() {
        return upload_token;
    }

    public void setUpload_token(String upload_token) {
        this.upload_token = upload_token;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    @Override
    public String toString() {
        return "UploadData{" +
                "upload_token='" + upload_token + '\'' +
                ", file_name='" + file_name + '\'' +
                '}';
    }
}
