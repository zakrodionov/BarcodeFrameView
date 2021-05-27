package com.zakrodionov.barcodeframeview

import android.content.res.Resources
import android.graphics.Rect
import android.view.View

val Int.pxToDp get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.dpToPx get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.pxToDpF get() = (this / Resources.getSystem().displayMetrics.density)

val Int.dpToPxF get() = (this * Resources.getSystem().displayMetrics.density)

val View.rect: Rect
    get() = Rect(0, 0, width, height)