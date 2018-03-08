package com.oceansky.example.network.http;

import com.oceansky.example.constant.Constants;
import com.oceansky.example.network.model.BindCidRequest;
import com.oceansky.example.network.model.CreateHomeworkRequest;
import com.oceansky.example.network.model.FeedBackRequest;
import com.oceansky.example.network.model.ForgetPwdRequest;
import com.oceansky.example.network.model.HomeworkAssignRequest;
import com.oceansky.example.network.model.HomeworkModifyRequest;
import com.oceansky.example.network.model.HomeworkSelectRequest;
import com.oceansky.example.network.model.LoginRequest;
import com.oceansky.example.network.model.ModifyInforRequest;
import com.oceansky.example.network.model.RegisterRequest;
import com.oceansky.example.network.model.ResetPwdRequest;
import com.oceansky.example.network.response.BaseDataEntity;
import com.oceansky.example.network.response.ClassEntity;
import com.oceansky.example.network.response.ClassListEntity;
import com.oceansky.example.network.response.CourseEntity;
import com.oceansky.example.network.response.CreateHomeworkEntity;
import com.oceansky.example.network.response.HomeworkClassEntity;
import com.oceansky.example.network.response.HomeworkEntity;
import com.oceansky.example.network.response.HomeWorkRedPointEntity;
import com.oceansky.example.network.response.HomeworkListEntity;
import com.oceansky.example.network.response.HomeworkPreviewEntity;
import com.oceansky.example.network.response.HomeworkReuseEntity;
import com.oceansky.example.network.response.HttpResponse;
import com.oceansky.example.network.response.KnowledgePointEntity;
import com.oceansky.example.network.response.LoginEntity;
import com.oceansky.example.network.response.MessageEntity;
import com.oceansky.example.network.response.ModifyInforEntity;
import com.oceansky.example.network.response.OrdersEntity;
import com.oceansky.example.network.response.RedPointEntity;
import com.oceansky.example.network.response.RegisterEntity;
import com.oceansky.example.network.response.SimpleResponse;
import com.oceansky.example.network.response.TeacherCourseEntity;
import com.oceansky.example.network.response.TeacherInforEntity;
import com.oceansky.example.network.response.UploadTokenEntity;
import com.oceansky.example.network.transformer.DefaultSchedulerTransformer;
import com.oceansky.example.network.transformer.DefaultTransformer;
import com.oceansky.example.network.transformer.ErrorCheckTransformer;

import java.util.ArrayList;

import rx.Observable;

/**
 * User: dengfa
 * Date: 16/6/5
 * Tel:  18500234565
 */
public class HttpManager {

    public static Observable<SimpleResponse> getVerifyCode(String phone) {
        return RestClient.getService(ApiService.class)
                .getVerfyCode(phone).compose(new DefaultSchedulerTransformer<>());
    }

    public static Observable<SimpleResponse> getForgetPwdVerifyCode(String phone) {
        return RestClient.getService(ApiService.class)
                .getForgetPwdVerfyCode(phone)
                .compose(new DefaultSchedulerTransformer<>());
    }

    public static Observable<SimpleResponse> forgetPwd(String phone, String pwd, String checkcode) {
        return RestClient.getService(ApiService.class)
                .doForgetPwd(new ForgetPwdRequest(phone, pwd, checkcode))
                .compose(new DefaultSchedulerTransformer<>());
    }

    public static Observable<LoginEntity> login(String deviceId, String phone, String pwd) {
        return RestClient.getService(ApiService.class)
                .doLogin(deviceId, new LoginRequest(Constants.GRANT_TYPE, phone, pwd)).compose(new DefaultTransformer<>());
    }

    public static Observable<RegisterEntity> register(String username, String password, String checkcode) {
        return RestClient.getService(ApiService.class)
                .doRegister(new RegisterRequest(Constants.GRANT_TYPE, username, password, checkcode))
                .compose(new DefaultTransformer<>());
    }

    /**
     * 根据课程的状态获取课程数据
     *
     * @param authorization
     * @param size
     * @param before_id
     * @param status
     */
    public static Observable<ArrayList<CourseEntity>> getCourse(String authorization, int size, int before_id, int status) {
        Observable<HttpResponse<ArrayList<CourseEntity>>> coursesObservable = RestClient.getService(ApiService.class)
                .getCourses(authorization, size, before_id, status);
        return coursesObservable.compose(new DefaultTransformer<>());
    }

    /**
     * 不分状态获取课程数据
     *
     * @param authorization
     * @param size
     * @param before_id
     */
    public static Observable<ArrayList<CourseEntity>> getCourse(String authorization, int size, int before_id) {
        Observable<HttpResponse<ArrayList<CourseEntity>>> coursesObservable = RestClient.getService(ApiService.class)
                .getCourses(authorization, size, before_id);
        return coursesObservable.compose(new DefaultTransformer<>());
    }

    public static Observable<TeacherInforEntity> getTeacherInfor(String authorization) {
        return RestClient.getService(ApiService.class)
                .getTeacherInfor(authorization)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<UploadTokenEntity> getUploadToken(String bucket, String file_md5) {
        return RestClient.getService(ApiService.class)
                .getUploadtoken(bucket, file_md5)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<ModifyInforEntity> modifyInfor(
            String authorization, String last_name, int sex, String birthday, int first_year,
            String graduate, int education, String wechat_id, String email, int title_check,
            int ot_experience, String avatar) {
        ModifyInforRequest modifyInforRequest = new ModifyInforRequest(last_name, sex, birthday, first_year,
                graduate, education, wechat_id, email, title_check, ot_experience, avatar);
        return RestClient.getService(ApiService.class)
                .ModifyInfor(authorization, modifyInforRequest)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<SimpleResponse> feedback(String phone, String name, String messeage) {
        return RestClient.getService(ApiService.class)
                .doFeedBack(new FeedBackRequest(phone, name, messeage))
                .compose(new DefaultSchedulerTransformer<>());
    }

    public static Observable<SimpleResponse> resetPwd(String authorization, String oldPwd, String newPwd, String gt_cid) {
        return RestClient.getService(ApiService.class)
                .doResetPassword(authorization, new ResetPwdRequest(oldPwd, newPwd, gt_cid))
                .compose(new DefaultSchedulerTransformer<>());
    }

    public static Observable<ArrayList<ClassListEntity>> getClassList(String authorization) {
        return RestClient.getService(ApiService.class)
                .getClassList(authorization)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<ClassEntity> getClassDetail(String authorization, int course_id) {
        return RestClient.getService(ApiService.class)
                .getClassDetail(authorization, course_id)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<OrdersEntity> getOrders(
            String authorization, int offset, int limit, String teacher_id) {
        return RestClient.getService(ApiService.class)
                .getOrders(authorization, offset, limit, teacher_id)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<ArrayList<MessageEntity>> getMessageList(
            String authorization, int r_type, int size, int before_id, int after_id) {
        return RestClient.getService(ApiService.class)
                .getMessageDetail(authorization, r_type, size, before_id, after_id)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<BaseDataEntity> getBaseData() {
        return RestClient.getService(ApiService.class)
                .getBaseData()
                .compose(new ErrorCheckTransformer<>());
    }

    public static Observable<RedPointEntity> getRedPoint(String authorization) {
        return RestClient.getService(ApiService.class)
                .getRedPoint(authorization)
                .compose(new DefaultSchedulerTransformer<>());
    }

    public static Observable<SimpleResponse> setMessageItemReaded(String authorization, int id) {
        return RestClient.getService(ApiService.class)
                .setMessageItmeReaded(authorization, id)
                .compose(new DefaultSchedulerTransformer<>());
    }

    public static Observable<ArrayList<TeacherCourseEntity>> getTeacherCoursesPeriodMonth(
            String authorization, String start, String end) {
        return RestClient.getService(ApiService.class)
                .getTeacherCoursesPeriodMonth(authorization, start, end)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<HomeworkListEntity> getHomeworkList(
            String authorization, String teacher_id, int assigned, int offset, int size) {
        return RestClient.getService(ApiService.class)
                .getHomeworkList(authorization, teacher_id, assigned, offset, size)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<SimpleResponse> deleteHomework(String authorization, int homeworkId) {
        return RestClient.getService(ApiService.class)
                .deleteHomework(authorization, homeworkId)
                .compose(new DefaultSchedulerTransformer<>());
    }

    public static Observable<CreateHomeworkEntity> createHomework(
            String authorization, CreateHomeworkRequest createHomeworkRequest) {
        return RestClient.getService(ApiService.class)
                .createHomework(authorization, createHomeworkRequest)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<KnowledgePointEntity> getknowledgeTree(
            String authorization, int gradeId, int lessonId, int textbookId) {
        return RestClient.getService(ApiService.class)
                .getKnowledgeTree(authorization, gradeId, lessonId, textbookId)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<ArrayList<HomeworkEntity>> selectHomework(
            String authorization, int lesson, String detail_ids, String knowledge_points, int question_type, int difficulty, String offset, int size) {
        return RestClient.getService(ApiService.class)
                .selectHomework(authorization, new HomeworkSelectRequest(lesson, detail_ids, knowledge_points, question_type, difficulty, offset, size))
                .compose(new DefaultTransformer<>());
    }

    public static Observable<SimpleResponse> modifyHomework(
            String authorization, int homeworkId, String textbook_ids, String knowledge_chapter_ids,
            String knowledge_section_ids, String knowledge_detail_ids, String question_ids) {
        return RestClient.getService(ApiService.class)
                .modifyHomework(authorization, homeworkId, new HomeworkModifyRequest(textbook_ids, knowledge_chapter_ids,
                        knowledge_section_ids, knowledge_detail_ids, question_ids))
                .compose(new DefaultSchedulerTransformer<>());
    }

    public static Observable<SimpleResponse> modifyHomework(String authorization, int homeworkId, String title) {
        return RestClient.getService(ApiService.class)
                .modifyHomework(authorization, homeworkId, new HomeworkModifyRequest(title))
                .compose(new DefaultSchedulerTransformer<>());
    }

    public static Observable<ArrayList<HomeworkPreviewEntity>> previewHomework(String authorization, int homeworkId) {
        return RestClient.getService(ApiService.class)
                .previewHomework(authorization, homeworkId)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<HomeworkClassEntity> getHomeworkClassList(String authorization, int homeworkId) {
        return RestClient.getService(ApiService.class)
                .getHomeworkClassList(authorization, homeworkId)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<SimpleResponse> assignHomework(
            String authorization, int homeworkId, int courseId, String publishTime, String deadline, int publishType) {
        return RestClient.getService(ApiService.class)
                .AssignHomework(authorization, homeworkId, new HomeworkAssignRequest(courseId, publishTime, deadline, publishType))
                .compose(new DefaultSchedulerTransformer<>());
    }

    public static Observable<HomeworkReuseEntity> reuseHomework(String authorization, int homeworkId) {
        return RestClient.getService(ApiService.class)
                .ReuseHomework(authorization, homeworkId)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<HomeWorkRedPointEntity> getHomeWorkRedPoint(String authorization) {
        return RestClient.getService(ApiService.class)
                .getHomeWorkRedPoint(authorization)
                .compose(new DefaultTransformer<>());
    }

    public static Observable<SimpleResponse> bindGetuiCid(String authorization, String cid) {
        return RestClient.getService(ApiService.class)
                .bindCid(authorization, new BindCidRequest(cid))
                .compose(new DefaultSchedulerTransformer<>());
    }
}
