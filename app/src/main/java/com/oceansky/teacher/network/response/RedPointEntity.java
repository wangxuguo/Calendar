package com.oceansky.teacher.network.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

/**
 * User: dengfa  王旭国
 * Date: 16/6/26    16/9/9
 * Tel:  18500234565  15210637996
 */

public class RedPointEntity  implements Serializable {

    /**
     * code : 200
     * message : OK
     * data : {"msgbox":{"total":1,"pri":1,"pub":0},"private_events":{"4001":0,"4005":1}}
     */

    @SerializedName("code")
    private int code;
    @SerializedName("message")
    private String message;
    /**
     * msgbox : {"total":1,"pri":1,"pub":0}
     * private_events : {"4001":0,"4005":1}
     */

    @SerializedName("data")
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RedPointEntity{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    public static class DataBean {
        /**
         * total : 1
         * pri : 1
         * pub : 0
         */

        @SerializedName("msgbox")
        private MsgBox              msgbox;
        /**
         * 4001 : 0
         * 4005 : 1
         */

        @SerializedName("private_events")
        private HashMap<String,Integer> privateEvents;

        public MsgBox getMsgBox() {
            return msgbox;
        }

        public void setMsgBox(MsgBox msgbox) {
            this.msgbox = msgbox;
        }

        public HashMap<String, Integer> getPrivateEvents() {
            return privateEvents;
        }

        public void setPrivateEvents(HashMap<String, Integer> privateEvents) {
            this.privateEvents = privateEvents;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "msgbox=" + msgbox +
                    ", privateEvents=" + privateEvents +
                    '}';
        }

        public static class MsgBox {
            @SerializedName("total")
            private int total;
            @SerializedName("pri")
            private int pri;
            @SerializedName("pub")
            private int pub;

            public int getTotal() {
                return total;
            }

            public void setTotal(int total) {
                this.total = total;
            }

            public int getPri() {
                return pri;
            }

            public void setPri(int pri) {
                this.pri = pri;
            }

            public int getPub() {
                return pub;
            }

            public void setPub(int pub) {
                this.pub = pub;
            }

            @Override
            public String toString() {
                return "MsgBox{" +
                        "total=" + total +
                        ", pri=" + pri +
                        ", pub=" + pub +
                        '}';
            }
        }

    }
}