package com.example.mybookapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment


class PageFragment: Fragment() {

    private var onFragmentReadyListener: OnFragmentReadyListener? = null
    private val ARG_TAB_POSITON = "tab_position"

    companion object {
        fun newInstance(tabPosition: Int) = PageFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_TAB_POSITON, tabPosition)
            }
        }
    }
    interface OnFragmentReadyListener {
        fun onFragmentReady(position: Int): View?
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onFragmentReadyListener = context as OnFragmentReadyListener
    }

    override fun onDestroy() {
        super.onDestroy()
        onFragmentReadyListener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_display, container, false)
        val mainLayout = rootView.findViewById<RelativeLayout>(R.id.fragment_layout)
        val view: View? = onFragmentReadyListener?.onFragmentReady(arguments!!.getInt(ARG_TAB_POSITON))
        if (view != null) {
            mainLayout.addView(view)
        }
        return rootView
    }
}