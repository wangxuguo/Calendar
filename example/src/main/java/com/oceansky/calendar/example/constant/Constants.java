package com.oceansky.calendar.example.constant;

import android.os.Environment;

public class Constants {
    public static final String CLIENT_ID      = "D7B96A95D245";
    public static final String CLIENT_SECRET  = "823294D35085D03721337153F9C92FE3";
    public static final String CLIENT_VERSION = "Android/V2.0";
    public static final String CLINET_ACCEPT  = "application/vnd.ywl.v1+json";
    public static final String GRANT_TYPE     = "password";

    public static final String USR_AGREEMENT_URL = FeatureConfig.BASE_URL + "/help/terms_teacher";
    public static final String TEACHER_BASE_URL  = FeatureConfig.API_HOST_NAME + "me/teacher";
    public static final String TEACHER_URL       = FeatureConfig.BASE_URL + "/teachers";//教师详情web

    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_TOKEN_TYPE   = "token_type";
    public static final String KEY_EXPIRES_IN   = "expires_in";
    public static final String KEY_USER_ID      = "user_id";
    public static final String GT_CLIENT_ID     = "gt_clientID";
    public static final String KEY_TEACHER_ID   = "teacher_id";

    public static final String LOGIN_SUCCESS_BROADCAST     = "login_success";
    public static final String LOGOUT_SUCCESS_BROADCAST    = "logout_success";
    public static final String LOGIN_SUCCESS_MSG_BROADCAST = "login_success_msg";

    public static final String CLASS_ID      = "classId";
    public static final String CLASS_TITL    = "classTitl";
    public static final String COURSE_STATUS = "course_status";

    //Acache
    public static final String CLASS_LIST                = "classList";
    public static final String ORDER_LIST                = "orderlist";
    public static final String COURSE_LIST               = "courseList";
    public static final String HOMEWORK_LIST             = "homeworkList";
    public static final String CLASS_DATA                = "classdata";
    public static final String TEACHERCOURSE_DATA        = "coursedata";
    public static final int    CACHE_MAX_SIZE            = 1024 * 1024 * 50;//50M
    //基础数据：学历名称列表
    public static final String BASE_DATA_EDUCATIONS_NAME = "base_data_educations_name";
    //基础数据：学历ID列表
    public static final String BASE_DATA_EDUCATIONS_ID   = "base_data_educations_id";
    //基础数据：科目名称列表
    public static final String BASE_DATA_LESSONS_NAME    = "base_data_lessons_name";
    //基础数据：科目ID列表
    public static final String BASE_DATA_LESSONS_ID      = "base_data_lessons_id";
    //基础数据：年级名称列表
    public static final String BASE_DATA_GRADES_NAME     = "base_data_grades_name";
    //基础数据：年级ID列表
    public static final String BASE_DATA_GRADES_ID       = "base_data_grades_id";
    //基础数据：教材名称列表
    public static final String BASE_DATA_TEXTBOOK_NAME   = "base_data_book_name";
    //基础数据：教材ID列表
    public static final String BASE_DATA_TEXTBOOK_ID     = "base_data_book_id";

    public static final String MSG_COMMON_LIST = "classCommonList";
    public static final String MSG_PERSON_LIST = "classPersonList";

    //SharePreference
    public static final String TEAHER_NAME           = "teacherName";
    public static final String TEACHER_PHOTO         = "teacherPhoto";
    public static final String TEACHER_SEX           = "teacherSex";
    public static final String TEACHER_BIRTHDAY      = "teacherBir";
    public static final String TEACHER_FIRST_TEACH   = "teacherFirstYear";
    public static final String TEACHER_GRADUATE      = "teacherGraduate";
    public static final String TEACHER_EDUCATION     = "teacherEdu";
    public static final String TEACHER_WECHAT        = "teacherwechat";
    public static final String TEACHER_EMAIL         = "teacheremail";
    public static final String TEACHER_QUALIFICATION = "teacherqualification";
    public static final String TEACHER_EXPERIENCEN   = "teacherexp";
    public static final String TEACHER_ID            = "teacherID";
    public static final String QI_NIU_KEY            = "qiniukey";
    public static final String QI_NIU_TOKEN          = "qiniutoken";
    public static final String KEY_GUID_SHOW         = "keyguidshow";
    public static final String TEACHER_STATUS        = "teacherStatus";
    public static final String HAVE_COMMON_MSG       = "havecommonmsg";
    public static final String COMMON_MSG_SUM        = "commonMsgSum";
    public static final String PRI_MSG_SUM           = "priMsgSum";

    //used to webview
    public static final String WEBVIEW_URL       = "url";
    public static final String WEBVIEW_TITLE     = "title";
    public static final String WEBVIEW_BUTTON    = "button";
    public static final String COURSE_ID         = "course_id";
    //course state
    public static final int    COURSE_STATE_ING  = 2;
    public static final int    COURSE_STATE_WAIT = 1;
    public static final int    COURSE_STATE_END  = 3;
    public static final int    COURSE_STATE_ALL  = 0;

    //loading state
    public static final int LOADING_STATE_SUCCESS                  = 0;
    public static final int LOADING_STATE_FAIL                     = 1;
    public static final int LOADING_STATE_NO_NET                   = 2;
    public static final int LOADING_STATE_UNLOGIN                  = 3;
    public static final int LOADING_STATE_TIME_OUT                 = 4;
    public static final int LOADING_STATE_HOMEWORK_EMPTY           = 5;
    public static final int LOADING_STATE_HOMEWORK_TYPE_NOT_ONLINE = 6;
    public static final int LOADING_STATE_CAN_NOT_VISIT            = 7;
    public static final int LOADING_STATE_HOMEWORK_NO_MORE         = 8;

    //push
    public static final String PUSH_EVENT             = "pushevent";
    public static final String PUSH_DATA              = "pushdata";
    public static final String EVENT_ORDER            = "3001";
    public static final String EVENT_COURSE           = "3002";
    public static final String EVENT_MSG_NOTIFY_BEGIN = "3003";
    public static final String EVENT_MSG_COURSE_DELAY = "3004";
    public static final String EVENT_MSG_COURSE_END   = "3005";
    public static final String EVENT_MSG_SALARY       = "3006";
    public static final String EVENT_MSG_EVALUATE     = "3007";
    public static final String EVENT_MSG_OPERATION    = "3008";
    public static final String EVENT_MSG_HWREPORT     = "3009";  //作业报告生成推送
    public static final String EVENT_MSG_BLESSING     = "9001";
    public static final String EVENT_PWD_CHANGE       = "UTC";

    public static final String ACTION_DATA_LOAD_SUCCEED = "com.ocensky.teacher.action.courseDataLoadSucceed";
    public static final String ACTION_DATA_LOAD_FAILURE = "com.ocensky.teacher.action.courseDataLoadFailure";
    public static final String ACTION_RECEIVE_PUSH      = "com.ocensky.teacher.action.receivePush";
    public static final String ACTION_PASSWORD_CHANGED  = "com.ocensky.teacher.action.pwdChanged";
    public static final String ACTION_RECEIVE_GETUI_CID = "com.ocensky.teacher.action.receiveCid";
    public static final String LOADING_STATE            = "loadingstate";

    //teacher state
    public static final int TEACHER_STATE_PASS = 2;//已上架

    public static final int TIME_OUT       = 10000;
    public static final int TIMER_INTERVAL = 1000;

    //消息中心广播
    public static final String BROAD_MSGCENTER_HAVEREADALL = "com.oceansky.activity.MSGCENTERACTIVITY.HAVEREADALL";
    public static final String BROAD_PRI_MSG_READED        = "com.oceansky.activity.action.pri_msg_readed";
    public static final String BROAD_PUB_MSG_READED        = "com.oceansky.activity.action.pub_msg_readed";

    //代替startActivityForResult
    public static final String REQUEST_CODE = "request_code";
    public static final String REQUEST_MSG  = "msg";

    public static final String IS_COMMONMSG_READEDALL = "is_commonmsg_readedall";
    public static final String IS_PERSONMSG_READEDALL = "is_personmsg_readedall";

    //homework select
    public static final String HOMEWORK_DIFFICULTY            = "homework_difficulty";
    public static final int    HOMEWORK_DIFFICULTY_EASY       = 3;
    public static final int    HOMEWORK_DIFFICULTY_MEDIUM     = 4;
    public static final int    HOMEWORK_DIFFICULTY_HARD       = 5;
    public static final String HOMEWORK_TYPE                  = "homework_type";
    public static final int    HOMEWORK_TYPE_CHOICE           = 1;
    public static final int    HOMEWORK_TYPE_COMPLETION       = 2;
    public static final int    HOMEWORK_TYPE_CHECKING         = 3;
    public static final String HOMEWORK_STATE                 = "homework_state";
    public static final int    HOMEWORK_STATE_PENDING         = 0;//待布置
    public static final int    HOMEWORK_STATE_DONE            = 1;//已布置
    public static final String GRADE_ID                       = "grade_id";//默认值
    public static final String LESSON_ID                      = "lesson_id";//默认值
    public static final String TEXTBOOK_ID                    = "textbook_id";
    public static final String CHAPTER_ID                     = "chapter_id";
    public static final String SECTION_ID                     = "section_id";
    public static final String KNOWLEDGE_DETAIL_ID            = "knowledge_point_id";
    public static final String KNOWLEDGE_POINT                = "knowledge_point";//知识点
    public static final String HOMEWORK_ID                    = "homework_id";
    public static final String HOMEWORK_TITLE                 = "homework_title";
    public static final String HOMEWORK_CACHE_TEXTBOOK_ID_SET = "homework_cache_textbookid";
    public static final String HOMEWORK_CACHE_CHAPTER_ID_SET  = "homework_cache_chapterid";
    public static final String HOMEWORK_CACHE_SECTION_ID_SET  = "homework_cache_sectionid";
    public static final String HOMEWORK_CACHE_DETAIL_ID_SET   = "homework_cache_detailId";
    public static final String HOMEWORK_CACHE_QUESTION_ID_SET = "homework_cache_questionid";
    public static final String HOMEWORK_CACHE_HTML_MAP        = "homework_cache_html_map";
    public static final String IS_CHANGE_KNOWLEDGE_POINT      = "changeKnowledgePoint";
    public static final String IS_ADD_HOMEWORK                = "addHomework";
    public static final String IS_REUSE_HOMEWORK              = "is_reuse_homework";
    public static final String TEXTBOOK_ID_SET                = "textbook_id_set";
    public static final String CHAPTER_ID_SET                 = "chapter_id_set";
    public static final String SECTION_ID_SET                 = "section_id_set";
    public static final String DETAIL_ID_SET                  = "detail_id_set";
    public static final String QUESTION_ID_SET                = "question_id_set";
    public static final String HOMEWORK_HTML_MAP              = "homework_html_map";
    public static final int    PUBLISH_TYPE_AFTER_ASSIGN      = 1;//学生提交后公布
    public static final int    PUBLISH_TYPE_NEVER             = 2;//永不公布
    public static final int    PUBLISH_TYPE_PUBLISH_TIME      = 3;//到公布时间公布
    public static final String ACTION_HOMEWORK_MSG            = "com.oceansky.teacher.action.homework_message";
    public static final String CLASS_NAME                     = "className";

    // v2.0 作业部分   统计事件
    public static final String MINE_HOMEWORK                = "mine_my_homework";    //我的作业  --->jhyx_tap_mine_homework
    public static final String HOMEWORK_PENDING_TAB_TOUCHED = "homework_tab_standby";     //待布置作业
    public static final String HOMEWORK_DONE_TAB_TOUCHED    = "homework_tab_published";     //已布置作业

    public static final String LEFT_SWIP_HOMEWORK_ITEM                     = "homework_slide_left";     //布置作业左滑
    public static final String LEFTSWIP_DELETE_HOMEWORK_TOUCHED            = "homework_slide_left_reuse_tap";    //布置作业左滑后点击(布置作业点击)
    public static final String LEFTSWIP_REUSE_HOMEWORK_TOUCHED             = "homework_slide_left_reuse_tap";    //布置作业左滑后点击(布置作业点击)
    public static final String CREATE_NEW_HOMEWORK_TOUCHED                 = "homework_create_button_touched";     //新建作业
    public static final String BACK_CREATE_HOMEWORK_TOUCHED                = "homework_create_back";     //新建作业的back
    public static final String SELECT_GRADE_TOUCHED                        = "homework_create_grade";     //请选择年级
    public static final String SELECT_SUBJECT_TOUCHED                      = "homework_create_subject";     //请选择科目
    public static final String SELECT_TEXTBOOK_TOUCHED                     = "homework_create_textbook";     //请选择教材
    public static final String SELECT_CHAPTER_TOUCHED                      = "homework_create_chapter";     //请选择章节
    public static final String SELECT_KNOWLEDGE_POINT_SURE_TOUCHED         = "homework_create_confirm_ok";     //提示窗的确认
    public static final String SELECT_KNOWLEDGE_POINT_CANCEL_TOUCHED       = "homework_create_confirm_cancel";     //提示窗的取消
    public static final String SELECT_QUESTION_TYPE_TOUCHED                = "subject_choice_category";     //题型的按钮
    public static final String SELECT_QUESTION_LEVEL_TOUCHED               = "subject_choice_difficulty";     //难度的选择
    public static final String GENERATE_HOMEWORK_TOUCHED                   = "homework_create_generate";     //生成作业
    public static final String SAVE_HOMEWORK_TOUCHED                       = "homework_preview_save";     //保存作业
    public static final String PUBLISH_HOMEWORK_TOUCHED                    = "homework_preview_publish";     //布置作业
    public static final String SELECT_CLASS_TOUCHED                        = "homework_publish_grade";     //选择班级
    public static final String SELECT_DEADLINE_TIME_TOUCHED                = "homework_publish_time_end";     //选择截止时间
    public static final String SELECT_PUBLISH_HOMEWORK_ANSWER_TIME_TOUCHED = "homework_publish_answer_announce";     //选择公布答案时间
    public static final String HOMEWORK_REPORT_STUDENT_NAME_TOUCHED        = "homework_report_student_name";     //作业报告页学生姓名
    public static final String HOMEWORK_WRONG_QUESTION_TOUCHED             = "homework_report_error_question";     //错题的点击
    // 消息类型
    public static final String MSG_TYPE                                    = "msg_type";
    public static final String MSG_COMON                                   = "msg_common";
    public static final String MSG_PERSONAL                                = "msg_personal";
    public static final String ACTION_PUBMSG_COUNT                         = "com.oceansky.action.pubmsg_count";
    public static final String ACTION_PRIMSG_COUNT                         = "com.oceansky.action.primsg_count";
    public static final String IS_HAVE_UNREADMSG                           = "is_have_unreadmsg";
    public static final String ACTION_MSG_COUNTDOWN                        = "com.oceansky.action.msg_countdown";
    public static final String ACTION_REDPOINT_STATE                       = "com.oceansky.action.redpoint_state";

    public static final String APP_PATH           = Environment.getExternalStorageDirectory().getPath() + "/JHYX/";
    public static final String EXTERNAL_PATH_LOGS = APP_PATH + "logs/";
}