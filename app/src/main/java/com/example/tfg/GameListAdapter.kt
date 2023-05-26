package com.example.tfg

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GameListAdapter(private val context: Context, private val gameList: List<Juegos>) : RecyclerView.Adapter<GameListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.game_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val juego = gameList[position]
        holder.bind(juego)
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.nombre1)
        private val genreTextView: TextView = itemView.findViewById(R.id.genre1)
        private val sizeTextView: TextView = itemView.findViewById(R.id.size1)

        fun bind(juego: Juegos) {
            titleTextView.text = juego.nombre
            genreTextView.text = juego.genero
            sizeTextView.text = juego.size
        }
    }
}
