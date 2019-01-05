package com.anwesh.uiprojects.linkedshootingballsstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.shootingballstepview.ShootingBallStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShootingBallStepView.create(this)
    }
}
