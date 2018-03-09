package com.oceansky.calendar.example.listeners;

import android.animation.Animator;

/**
 *
 * Created by 王旭国 on 16/6/13 10:55
 */
public class SimpleAnimatorListener implements Animator.AnimatorListener {
    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        onAnimationCancelOrEnd(animation);
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        onAnimationCancelOrEnd(animation);
    }

    public void onAnimationCancelOrEnd(Animator animator){

    }
    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
