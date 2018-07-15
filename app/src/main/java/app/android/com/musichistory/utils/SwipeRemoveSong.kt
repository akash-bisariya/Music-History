package app.android.com.musichistory.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import app.android.com.musichistory.R

/**
 * Created by akash bisariya on 28-06-2018.
 */
abstract class SwipeRemoveSong(val context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_sweep_white_24dp)
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        return false
    }


    override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val itemView = viewHolder!!.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            c!!.drawRect(itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), clearPaint)
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }
        val view = ColorDrawable()
        view.color = (context.resources.getColor(R.color.color_red))
        // Calculate position of delete icon
        val deleteIconTop = itemView.top + (itemHeight - deleteIcon!!.intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - deleteIcon.intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - deleteIcon.intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + deleteIcon.intrinsicHeight
        view.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        view.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

    }
}