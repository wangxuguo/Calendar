package com.oceansky.teacher.network.response;

import java.io.Serializable;

/**
 * User: 王旭国
 * Date: 16/6/16 21:09
 * Email:wangxuguo@jhyx.com.cn
 */
public class MessageEntity implements Serializable {

    /**
     * data : [{"id":36,"code":102,"title":"这是公共消息3","text":"这是公共借消息内容15","is_public":1,"is_read":0,"time":1465672935},{"id":35,"code":102,"title":"这是公共消息3","text":"这是公共借消息内容14","is_public":1,"is_read":0,"time":1465672935},{"id":34,"code":102,"title":"这是公共消息3","text":"这是公共借消息内容13","is_public":1,"is_read":1,"time":1465672935},{"id":33,"code":102,"title":"这是公共消息3","text":"这是公共借消息内容12","is_public":1,"is_read":0,"time":1465672935},{"id":32,"code":102,"title":"这是公共消息3","text":"这是公共借消息内容11","is_public":1,"is_read":0,"time":1465672935},{"id":31,"code":102,"title":"这是公共消息3","text":"这是公共借消息内容10","is_public":1,"is_read":0,"time":1465672935},{"id":30,"code":102,"title":"这是公共消息3","text":"这是公共借消息内容9","is_public":1,"is_read":0,"time":1465672935},{"id":29,"code":102,"title":"这是公共消息3","text":"这是公共借消息内容8","is_public":1,"is_read":0,"time":1465672935},{"id":28,"code":102,"title":"这是公共消息3","text":"这是公共借消息内容7","is_public":1,"is_read":0,"time":1465672935},{"id":27,"code":102,"title":"这是公共消息3","text":"这是公共借消息内容6","is_public":1,"is_read":0,"time":1465672935},{"id":26,"code":102,"title":"这是公共消息3","text":"这是公共借消息内容5","is_public":1,"is_read":1,"time":1465672935},{"id":25,"code":102,"title":"这是公共消息3","text":"这是公共借消息内容4","is_public":1,"is_read":1,"time":1465672935},{"id":24,"code":105,"title":"标题22","text":"内容22","is_public":0,"is_read":0,"time":1466027952},{"id":23,"code":104,"title":"标题21","text":"内容21","is_public":0,"is_read":0,"time":1466027926},{"id":22,"code":103,"title":"标题20","text":"内容20","is_public":0,"is_read":0,"time":1466027868},{"id":21,"code":102,"title":"标题19","text":"这是内容19","is_public":0,"is_read":0,"time":1466027824},{"id":20,"code":102,"title":"这是公共消息3","text":"这是公共借消息内容3","is_public":1,"is_read":1,"time":1465672935},{"id":19,"code":101,"title":"标题18","text":"这是内容18","is_public":0,"is_read":0,"time":1465672849},{"id":18,"code":101,"title":"标题17","text":"这是内容17","is_public":0,"is_read":0,"time":1465672812},{"id":16,"code":105,"title":"标题15","text":"内容15","is_public":0,"is_read":0,"time":1466027952}]
     */
    private int    id;
    private int    code;
    private String title;
    private String text;
    private int    is_public;
    private int    is_read;
    private int    time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getIs_public() {
        return is_public;
    }

    public void setIs_public(int is_public) {
        this.is_public = is_public;
    }

    public int getIs_read() {
        return is_read;
    }

    public void setIs_read(int is_read) {
        this.is_read = is_read;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", code=" + code +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", is_public=" + is_public +
                ", is_read=" + is_read +
                ", time=" + time +
                '}';
    }
}
