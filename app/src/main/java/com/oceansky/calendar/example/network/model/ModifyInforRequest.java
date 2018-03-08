package com.oceansky.calendar.example.network.model;

public class ModifyInforRequest {
    private String last_name;
    private int    sex;
    private String birthday;
    private int    first_year;
    private String graduate;
    private int education;
    private String wechat_id;
    private String email;
    private int    title_check;
    private int    ot_experience;
    private String avatar;

    public ModifyInforRequest(String last_name, int sex, String birthday, int first_year,
                              String graduate, int education, String wechat_id, String email,
                              int title_check, int ot_experience, String avatar) {
        this.last_name = last_name;
        this.sex = sex;
        this.birthday = birthday;
        this.first_year = first_year;
        this.graduate = graduate;
        this.education = education;
        this.wechat_id = wechat_id;
        this.email = email;
        this.title_check = title_check;
        this.ot_experience = ot_experience;
        this.avatar = avatar;
    }
}
