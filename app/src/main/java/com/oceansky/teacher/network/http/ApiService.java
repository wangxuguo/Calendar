package com.oceansky.teacher.network.http;

import com.oceansky.teacher.network.model.BindCidRequest;
import com.oceansky.teacher.network.model.CreateHomeworkRequest;
import com.oceansky.teacher.network.model.FeedBackRequest;
import com.oceansky.teacher.network.model.ForgetPwdRequest;
import com.oceansky.teacher.network.model.HomeworkAssignRequest;
import com.oceansky.teacher.network.model.HomeworkModifyRequest;
import com.oceansky.teacher.network.model.HomeworkSelectRequest;
import com.oceansky.teacher.network.model.LoginRequest;
import com.oceansky.teacher.network.model.ModifyInforRequest;
import com.oceansky.teacher.network.model.RegisterRequest;
import com.oceansky.teacher.network.model.ResetPwdRequest;
import com.oceansky.teacher.network.response.BaseDataEntity;
import com.oceansky.teacher.network.response.ClassEntity;
import com.oceansky.teacher.network.response.ClassListEntity;
import com.oceansky.teacher.network.response.CourseEntity;
import com.oceansky.teacher.network.response.CreateHomeworkEntity;
import com.oceansky.teacher.network.response.HomeWorkRedPointEntity;
import com.oceansky.teacher.network.response.HomeworkClassEntity;
import com.oceansky.teacher.network.response.HomeworkEntity;
import com.oceansky.teacher.network.response.HomeworkListEntity;
import com.oceansky.teacher.network.response.HomeworkPreviewEntity;
import com.oceansky.teacher.network.response.HomeworkReuseEntity;
import com.oceansky.teacher.network.response.HttpResponse;
import com.oceansky.teacher.network.response.KnowledgePointEntity;
import com.oceansky.teacher.network.response.LoginEntity;
import com.oceansky.teacher.network.response.MessageEntity;
import com.oceansky.teacher.network.response.ModifyInforEntity;
import com.oceansky.teacher.network.response.OrdersEntity;
import com.oceansky.teacher.network.response.RedPointEntity;
import com.oceansky.teacher.network.response.RegisterEntity;
import com.oceansky.teacher.network.response.SimpleResponse;
import com.oceansky.teacher.network.response.TeacherCourseEntity;
import com.oceansky.teacher.network.response.TeacherInforEntity;
import com.oceansky.teacher.network.response.UploadTokenEntity;

import java.util.ArrayList;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * User: dengfa
 * Date: 16/6/2
 * Tel:  18500234565
 */
public interface ApiService {

    @POST("reg/checkcode")
    Observable<SimpleResponse> getVerfyCode(@Query("phone_number") String phone_number);

    @POST("reg")
    Observable<HttpResponse<RegisterEntity>> doRegister(@Body RegisterRequest map);//??返回4011
    // Observable<RegisterBean> doRegister(@FieldMap TreeMap<String, String> map);


    @POST("pwdretrieve/checkcode")
    Observable<SimpleResponse> getForgetPwdVerfyCode(@Query("phone_number") String phone_number);

    @POST("auth/get_token")
    Observable<HttpResponse<LoginEntity>> doLogin(@Header("device-id") String deviceId, @Body LoginRequest map);

    @POST("pwdretrieve")
    Observable<SimpleResponse> doForgetPwd(@Body ForgetPwdRequest map);

    //根据状态查询课程
    @GET("courses/teacher")
    Observable<HttpResponse<ArrayList<CourseEntity>>> getCourses(
            @Header("Authorization") String authorization, @Query("size") int size, @Query("before_id") int before_id, @Query("status") int status);

    //获取全部课程
    @GET("courses/teacher")
    Observable<HttpResponse<ArrayList<CourseEntity>>> getCourses(
            @Header("Authorization") String authorization, @Query("size") int size, @Query("before_id") int before_id);

    @GET("me/teacher")
    Observable<HttpResponse<TeacherInforEntity>> getTeacherInfor(@Header("Authorization") String authorization);

    @GET("fileupload/token/{bucket}/{file_md5}")
    Observable<HttpResponse<UploadTokenEntity>> getUploadtoken(@Path("bucket") String bucket, @Path("file_md5") String file_md5);

    @POST("me/teacher")
    Observable<HttpResponse<ModifyInforEntity>> ModifyInfor(@Header("Authorization") String authorization, @Body ModifyInforRequest map);

    @POST("feedback")
    Observable<SimpleResponse> doFeedBack(@Body FeedBackRequest map);

    @POST("pwdreset")
    Observable<SimpleResponse> doResetPassword(@Header("Authorization") String authorization, @Body ResetPwdRequest map);

    @GET("teacher/contacts")
    Observable<HttpResponse<ArrayList<ClassListEntity>>> getClassList(@Header("Authorization") String authorization);

    @GET("teacher/contacts/{course_id}")
    Observable<HttpResponse<ClassEntity>> getClassDetail(@Header("Authorization") String authorization, @Path("course_id") int course_id);

    @GET("orders")
    Observable<HttpResponse<OrdersEntity>> getOrders(
            @Header("Authorization") String authorization, @Query("offset") int offset,
            @Query("limit") int limit, @Query("teacher_id") String teacher_id);

    @GET("msgbox")
    Observable<HttpResponse<ArrayList<MessageEntity>>> getMessageDetail(
            @Header("Authorization") String authorization, @Query("r_type") int r_type,
            @Query("size") int size, @Query("before_id") int before_id, @Query("after_id") int after_id);

    @GET("basedata")
    Observable<HttpResponse<BaseDataEntity>> getBaseData();

    @GET("redpoint")
    Observable<RedPointEntity> getRedPoint(@Header("Authorization") String authorization);

    @POST("msgbox/setreaded/{id}")
    Observable<SimpleResponse> setMessageItmeReaded(@Header("Authorization") String authorization, @Path("id") int id);

    @GET("courses/teacher/period/month/{from}/{to}")
    Observable<HttpResponse<ArrayList<TeacherCourseEntity>>> getTeacherCoursesPeriodMonth(
            @Header("Authorization") String authorization, @Path("from") String start, @Path("to") String end);

    @GET("homework/teacher/{teacher_id}/list")
    Observable<HttpResponse<HomeworkListEntity>> getHomeworkList(
            @Header("Authorization") String authorization, @Path("teacher_id") String teacher_id,
            @Query("assigned") int assigned, @Query("offset") int offset, @Query("size") int size);

    @POST("homework/{homework_id}/delete")
    Observable<SimpleResponse> deleteHomework(
            @Header("Authorization") String authorization, @Path("homework_id") int homework_id);

    @POST("homework")
    Observable<HttpResponse<CreateHomeworkEntity>> createHomework(
            @Header("Authorization") String authorization, @Body CreateHomeworkRequest createHomeworkRequest);

    @GET("knowledges")
    Observable<HttpResponse<KnowledgePointEntity>> getKnowledgeTree(
            @Header("Authorization") String authorization, @Query("grade_id") int grade_id,
            @Query("lesson_id") int lesson_id, @Query("textbook_id") int textbook_id);

    //筛选题目
    @POST("questions")
    Observable<HttpResponse<ArrayList<HomeworkEntity>>> selectHomework(
            @Header("Authorization") String authorization, @Body HomeworkSelectRequest homeworkSelectRequest);

    //修改作业
    @POST("homework/{homework_id}/modify")
    Observable<SimpleResponse> modifyHomework(
            @Header("Authorization") String authorization, @Path("homework_id") int homeworkId,
            @Body HomeworkModifyRequest homeworkModifyRequest);

    //作业预览
    @GET("homework/{homework_id}")
    Observable<HttpResponse<ArrayList<HomeworkPreviewEntity>>> previewHomework(
            @Header("Authorization") String authorization, @Path("homework_id") int homeworkId);

    //布置作业的班级
    @GET("homework/teacher/classes")
    Observable<HttpResponse<HomeworkClassEntity>> getHomeworkClassList(
            @Header("Authorization") String authorization, @Query("homework_id") int homework_id);

    //布置作业
    @POST("homework/{homework_id}/assignment")
    Observable<SimpleResponse> AssignHomework(
            @Header("Authorization") String authorization, @Path("homework_id") int homeworkId,
            @Body HomeworkAssignRequest HomeworkAssignRequest);

    //作业重新使用
    @POST("homework/{homework_id}/reuse")
    Observable<HttpResponse<HomeworkReuseEntity>> ReuseHomework(
            @Header("Authorization") String authorization, @Path("homework_id") int homeworkId);

    @GET("homeworkredpoint")
    Observable<HttpResponse<HomeWorkRedPointEntity>> getHomeWorkRedPoint(@Header("Authorization") String authorization);

    //绑定个推cid
    @POST("push/client_id/bind")
    Observable<SimpleResponse> bindCid(@Header("Authorization") String authorization, @Body BindCidRequest bindCidRequest);
}
