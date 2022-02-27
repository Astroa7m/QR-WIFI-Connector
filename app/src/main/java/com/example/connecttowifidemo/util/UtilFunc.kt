package com.example.connecttowifidemo.util

import android.os.Build
import android.util.Log

inline fun<T> is29AndAbove(
    func: () -> T
) :T?{
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        func.invoke()
    else
        null
}