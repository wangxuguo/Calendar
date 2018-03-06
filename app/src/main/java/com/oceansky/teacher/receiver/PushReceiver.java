package com.oceansky.teacher.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.oceansky.teacher.R;
import com.oceansky.teacher.activities.TabMainActivity;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.SecurePreferences;
import com.oceansky.teacher.utils.SharePreferenceUtils;
import com.oceansky.teacher.utils.SystemUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: dengfa
 * Date: 16/6/2
 * Tel:  18500234565
 * Des:  接收个推推送的消息
 */
public class PushReceiver extends BroadcastReceiver {

    private static final String TAG = PushReceiver.class.getSimpleName();
    private String push_data;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA:
                // 获取透传数据
                // String appid = bundle.getString("appid");
                byte[] payload = bundle.getByteArray("payload");
                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");
                // smartPush第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
                boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
                if (payload != null) {
                    String payloadstr = new String(payload);
                    LogHelper.d(TAG, "payloadstr: " + payloadstr);
                    JSONObject payloadObj = null;
                    try {
                        payloadObj = new JSONObject(payloadstr);
                        String event = payloadObj.getString("event");
                        String message = payloadObj.getString("msg");
                        JSONObject data = payloadObj.getJSONObject("data");
                        push_data = data.toString();
                        int id = 0;
                        if (data != null) {
                            id = data.getInt("id");
                        }
                        String title = context.getResources().getString(R.string.app_name);
                        Intent pushIntent = new Intent(Constants.ACTION_RECEIVE_PUSH);
                        pushIntent.putExtra(Constants.PUSH_EVENT, event);
                        boolean isLogined = !TextUtils.isEmpty(SecurePreferences.getInstance(context, true)
                                .getString(Constants.KEY_ACCESS_TOKEN));
                        switch (event) {
                            case Constants.EVENT_PWD_CHANGE:
                                if (!isLogined) {
                                    //如果未登陆，对push不操作
                                    return;
                                }
                                //另一端修改密码，token失效，提示另一端重新登录
                                //解绑消息推送的UID
                                String uid = SecurePreferences.getInstance(context, true).getString(Constants.KEY_USER_ID);
                                LogHelper.d(TAG, "uid: " + uid);
                                if (uid != null) {
                                    PushManager pushManager = PushManager.getInstance();
                                    pushManager.unBindAlias(context, uid, true);
                                    //isSelf：是否只对当前 cid 有效，如果是 true，只对当前cid做解绑；
                                    // 如果是 false，对所有绑定该别名的cid列表做解绑
                                }
                                NotificationManager mNotificationManager =
                                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                mNotificationManager.cancelAll();
                                if (SystemUtils.isRunningBack(context)) {
                                    return;
                                }
                                context.sendBroadcast(new Intent(Constants.ACTION_PASSWORD_CHANGED));
//                                Intent TokenInvalidIntent = new Intent(context, PasswordModifiedDialogActivity.class);
//                                TokenInvalidIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                                context.startActivity(TokenInvalidIntent);
                                break;
                            case Constants.EVENT_ORDER:
                                if (!isLogined) {
                                    //如果未登陆，对push不操作
                                    return;
                                }
                                context.sendBroadcast(pushIntent);
                                addNotificaction(context, id, event, title, message);
                                break;
                            case Constants.EVENT_COURSE:
                                if (!isLogined) {
                                    //如果未登陆，对push不操作
                                    return;
                                }
                                context.sendBroadcast(pushIntent);
                                addNotificaction(context, id, event, title, message);
                                break;
                            case Constants.EVENT_MSG_NOTIFY_BEGIN:
                            case Constants.EVENT_MSG_COURSE_DELAY:
                            case Constants.EVENT_MSG_COURSE_END:
                            case Constants.EVENT_MSG_SALARY:
                            case Constants.EVENT_MSG_EVALUATE:
                                if (!isLogined) {
                                    //如果未登陆，对push不操作
                                    return;
                                }
                                context.sendBroadcast(pushIntent);
                                addNotificaction(context, id, event, title, message);
                                break;
                            case Constants.EVENT_MSG_OPERATION:
                                if (!isLogined) {
                                    //如果未登陆，对push不操作
                                    return;
                                }
                            case Constants.EVENT_MSG_BLESSING:
                                SharePreferenceUtils.setBooleanPref(context, Constants.HAVE_COMMON_MSG, true);
                                context.sendBroadcast(pushIntent);
                                addNotificaction(context, id, event, title, message);
                                break;
                            case Constants.EVENT_MSG_HWREPORT:
                                if (!isLogined) {
                                    //如果未登陆，对push不操作
                                    return;
                                }
                                context.sendBroadcast(pushIntent);
                                addNotificaction(context, id, event, title, message);
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case PushConsts.GET_CLIENTID:
                // 获取ClientID(CID)
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                String cid = bundle.getString("clientid");
                LogHelper.d(TAG, "cid: " + cid);
                String cacheCid = SharePreferenceUtils.getStringPref(context, Constants.GT_CLIENT_ID, "");
                if (!TextUtils.isEmpty(cid) && !TextUtils.equals(cid, cacheCid)) {
                    SharePreferenceUtils.setStringPref(context, Constants.GT_CLIENT_ID, cid);
                    context.sendBroadcast(new Intent(Constants.ACTION_RECEIVE_GETUI_CID));
                }
                break;

            case PushConsts.THIRDPART_FEEDBACK:
                break;

            default:
                break;
        }

    }

    /**
     * 接收到推送，弹notification
     *
     * @param context
     * @param event
     * @param title
     */

    private void addNotificaction(Context context, int id, String event, String title, String contentText) {
        if (!SystemUtils.isRunningBack(context)) {
            return;
        }
        // 创建一个Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentTitle(title)
                .setContentText(contentText);

        // Creates an explicit intent for an Activity in your app
        Intent tabmainIntent = new Intent(context, TabMainActivity.class);
        tabmainIntent.putExtra(Constants.PUSH_EVENT, event);
        if (push_data != null) {
            tabmainIntent.putExtra(Constants.PUSH_DATA, push_data);
        }
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(TabMainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(tabmainIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        id,
                        PendingIntent.FLAG_ONE_SHOT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(id, builder.build());
    }
}
